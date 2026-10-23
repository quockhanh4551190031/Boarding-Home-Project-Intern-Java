package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.exception.TooManyRequestsException;
import tqkhanh.project.boardinghomeproject.repository.RoomImageRepository;
import tqkhanh.project.boardinghomeproject.repository.RoomRepository;
import tqkhanh.project.boardinghomeproject.repository.ViewingRequestRepository;
import tqkhanh.project.boardinghomeproject.util.DisplayNames;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ViewingService {

    private static final Set<ViewingStatus> ACTIVE = EnumSet.of(ViewingStatus.PENDING, ViewingStatus.ACCEPTED);
    private static final String[] WEEKDAYS = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật"};
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern PHONE = Pattern.compile("^0\\d{9}$");
    private static final int MAX_PAGE_SIZE = 50;
    private static final String DUPLICATE = "Bạn đã có lịch hẹn đang chờ hoặc đã được xác nhận cho phòng này";

    private final ViewingRequestRepository viewingRepository;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final ChatService chatService;

    @Value("${app.timezone:Asia/Ho_Chi_Minh}")
    private String timezone;

    @Value("${viewing.min-lead-minutes:60}")
    private int minLeadMinutes;

    @Value("${viewing.max-days-ahead:30}")
    private int maxDaysAhead;

    @Value("${viewing.max-pending-per-tenant:5}")
    private int maxPending;

    @Value("${viewing.max-per-day-per-tenant:10}")
    private int maxPerDay;

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of(timezone));
    }

    /* ---------- Tạo yêu cầu ---------- */

    @Transactional
    public ViewingResponse create(User tenant, CreateViewingRequest request) {
        LocalDateTime now = now();

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ"));
        String blocked = roomBlockReason(tenant, room);
        if (blocked != null) throw new IllegalArgumentException(blocked);

        LocalDateTime when = validateSlot(request.scheduledAt(), now);
        String phone = normalizePhone(request.contactPhone());
        String note = clean(request.message());

        // Lịch cũ đã quá giờ thì không còn chặn việc đặt lại
        viewingRepository.releaseStaleKeys(tenant.getId(), now);

        String key = tenant.getId() + ":" + room.getId();
        if (viewingRepository.findByActiveKey(key).isPresent()) throw new IllegalArgumentException(DUPLICATE);

        if (maxPending > 0 && viewingRepository.countByTenantIdAndStatusAndScheduledAtGreaterThanEqual(
                tenant.getId(), ViewingStatus.PENDING, now) >= maxPending) {
            throw new TooManyRequestsException("Bạn đang có " + maxPending
                    + " yêu cầu chờ xác nhận. Hãy đợi chủ trọ phản hồi hoặc hủy bớt rồi thử lại");
        }
        if (maxPerDay > 0 && viewingRepository.countByTenantIdAndCreatedAtAfter(
                tenant.getId(), now.minusHours(24)) >= maxPerDay) {
            throw new TooManyRequestsException("Bạn đã gửi quá nhiều yêu cầu trong 24 giờ qua (tối đa "
                    + maxPerDay + "). Vui lòng thử lại sau");
        }

        User landlord = room.getHouse().getLandlord();
        ViewingRequest v = ViewingRequest.builder()
                .room(room).tenant(tenant).landlord(landlord)
                .scheduledAt(when).message(note).contactPhone(phone)
                .status(ViewingStatus.PENDING).activeKey(key).createdAt(now)
                .build();
        try {
            v = viewingRepository.saveAndFlush(v);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(DUPLICATE);
        }

        chatService.postSystemMessage(tenant, landlord, room,
                "[Lịch xem phòng] Yêu cầu xem phòng " + roomText(v) + " lúc " + slotText(when)
                        + " đang chờ chủ trọ xác nhận." + (note == null ? "" : "\nGhi chú: " + note));

        return single(v, now);
    }

    /* ---------- Xử lý yêu cầu ---------- */

    @Transactional
    public ViewingResponse accept(User landlord, Long id) {
        ViewingRequest v = loadForUpdate(id);
        requireParty(v, landlord, false);
        LocalDateTime now = now();

        if (v.getStatus() != ViewingStatus.PENDING)
            throw new IllegalArgumentException("Yêu cầu này đã được xử lý trước đó");
        if (v.getScheduledAt().isBefore(now))
            throw new IllegalArgumentException("Lịch hẹn này đã quá giờ nên không thể xác nhận");

        v.setStatus(ViewingStatus.ACCEPTED);
        v.setRespondedAt(now);

        chatService.postSystemMessage(landlord, v.getTenant(), v.getRoom(),
                "[Lịch xem phòng] Chủ trọ đã xác nhận lịch xem phòng " + roomText(v) + " lúc " + slotText(v.getScheduledAt()) + ".");
        return single(v, now);
    }

    @Transactional
    public ViewingResponse reject(User landlord, Long id, String rawNote) {
        ViewingRequest v = loadForUpdate(id);
        requireParty(v, landlord, false);
        LocalDateTime now = now();

        if (v.getStatus() != ViewingStatus.PENDING)
            throw new IllegalArgumentException("Yêu cầu này đã được xử lý trước đó");
        if (v.getScheduledAt().isBefore(now)) throw new IllegalArgumentException("Lịch hẹn này đã quá giờ");

        String note = clean(rawNote);
        v.setStatus(ViewingStatus.REJECTED);
        v.setResponseNote(note);
        v.setRespondedAt(now);
        v.setActiveKey(null);

        chatService.postSystemMessage(landlord, v.getTenant(), v.getRoom(),
                "[Lịch xem phòng] Chủ trọ đã từ chối lịch xem phòng " + roomText(v) + " lúc "
                        + slotText(v.getScheduledAt()) + "." + reasonText(note));
        return single(v, now);
    }

    @Transactional
    public ViewingResponse cancel(User user, Long id, String rawNote) {
        ViewingRequest v = loadForUpdate(id);
        boolean isTenant = v.getTenant().getId().equals(user.getId());
        boolean isLandlord = v.getLandlord().getId().equals(user.getId());
        if (!isTenant && !isLandlord) throw notFound();

        LocalDateTime now = now();
        if (!ACTIVE.contains(v.getStatus()))
            throw new IllegalArgumentException("Lịch hẹn này đã kết thúc hoặc đã được xử lý trước đó");
        if (v.getScheduledAt().isBefore(now))
            throw new IllegalArgumentException("Lịch hẹn này đã quá giờ nên không thể hủy");
        if (isLandlord && v.getStatus() == ViewingStatus.PENDING) {
            throw new IllegalArgumentException("Yêu cầu này đang chờ xác nhận. Hãy dùng chức năng Từ chối");
        }

        String note = clean(rawNote);
        User other = isTenant ? v.getLandlord() : v.getTenant();
        v.setStatus(isTenant ? ViewingStatus.CANCELLED_BY_TENANT : ViewingStatus.CANCELLED_BY_LANDLORD);
        v.setResponseNote(note);
        v.setRespondedAt(now);
        v.setActiveKey(null);

        chatService.postSystemMessage(user, other, v.getRoom(),
                "[Lịch xem phòng] " + (isTenant ? "Người thuê" : "Chủ trọ") + " đã hủy lịch xem phòng "
                        + roomText(v) + " lúc " + slotText(v.getScheduledAt()) + "." + reasonText(note));
        return single(v, now);
    }

    /* ---------- Truy vấn ---------- */

    @Transactional(readOnly = true)
    public ViewingListResponse mine(User user, String scope, int page, int size) {
        LocalDateTime now = now();
        boolean history = switch (normScope(scope, "upcoming")) {
            case "upcoming" -> false;
            case "history" -> true;
            default -> throw new IllegalArgumentException("Bộ lọc không hợp lệ");
        };
        Pageable pageable = pageable(page, size, history);
        Page<ViewingRequest> result = history
                ? viewingRepository.tenantHistory(user.getId(), ACTIVE, now, pageable)
                : viewingRepository.tenantActive(user.getId(), ACTIVE, now, pageable);
        return toList(result, now, Set.of());
    }

    @Transactional(readOnly = true)
    public ViewingListResponse incoming(User landlord, String scope, int page, int size) {
        LocalDateTime now = now();
        Long id = landlord.getId();
        Page<ViewingRequest> result = switch (normScope(scope, "pending")) {
            case "pending" ->
                    viewingRepository.landlordActive(id, EnumSet.of(ViewingStatus.PENDING), now, pageable(page, size, false));
            case "upcoming" ->
                    viewingRepository.landlordActive(id, EnumSet.of(ViewingStatus.ACCEPTED), now, pageable(page, size, false));
            case "history" -> viewingRepository.landlordHistory(id, ACTIVE, now, pageable(page, size, true));
            default -> throw new IllegalArgumentException("Bộ lọc không hợp lệ");
        };
        // Giờ đã nhận lịch khác: chỉ để cảnh báo trùng giờ, không chặn chủ trọ
        Set<LocalDateTime> busy = new HashSet<>(viewingRepository.acceptedTimes(id, ViewingStatus.ACCEPTED, now));
        return toList(result, now, busy);
    }

    @Transactional(readOnly = true)
    public long pendingCount(User landlord) {
        return viewingRepository.countByLandlordIdAndStatusAndScheduledAtGreaterThanEqual(
                landlord.getId(), ViewingStatus.PENDING, now());
    }

    @Transactional(readOnly = true)
    public ViewingRoomState state(User user, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ"));
        LocalDateTime now = now();

        ViewingRequest active = viewingRepository.findByActiveKey(user.getId() + ":" + room.getId())
                .filter(v -> !v.getScheduledAt().isBefore(now))
                .orElse(null);
        if (active != null) return new ViewingRoomState(false, null, single(active, now));

        String reason = roomBlockReason(user, room);
        if (reason == null && maxPending > 0 && viewingRepository.countByTenantIdAndStatusAndScheduledAtGreaterThanEqual(
                user.getId(), ViewingStatus.PENDING, now) >= maxPending) {
            reason = "Bạn đang có " + maxPending + " yêu cầu chờ xác nhận. Hãy đợi chủ trọ phản hồi hoặc hủy bớt.";
        }
        if (reason == null && maxPerDay > 0 && viewingRepository.countByTenantIdAndCreatedAtAfter(
                user.getId(), now.minusHours(24)) >= maxPerDay) {
            reason = "Bạn đã gửi tối đa " + maxPerDay + " yêu cầu trong 24 giờ qua. Vui lòng thử lại sau.";
        }
        return new ViewingRoomState(reason == null, reason, null);
    }

    /* ---------- Kiểm tra ---------- */

    private String roomBlockReason(User user, Room room) {
        if (user.getRole() == Role.ADMIN) return "Tài khoản quản trị không thể đặt lịch xem phòng";
        if (room.getHouse().getLandlord().getId().equals(user.getId()))
            return "Bạn không thể đặt lịch xem phòng của chính mình";
        if (room.getStatus() != RoomStatus.AVAILABLE || room.getHouse().getStatus() != HouseStatus.ACTIVE) {
            return "Phòng này hiện không nhận lịch xem";
        }
        return null;
    }

    private LocalDateTime validateSlot(LocalDateTime when, LocalDateTime now) {
        if (when == null) throw new IllegalArgumentException("Vui lòng chọn ngày giờ xem phòng");
        if (when.getSecond() != 0 || when.getNano() != 0 || (when.getMinute() != 0 && when.getMinute() != 30)) {
            throw new IllegalArgumentException("Giờ hẹn phải theo khung 30 phút (ví dụ 14:00 hoặc 14:30)");
        }
        if (when.getHour() < 7 || when.getHour() > 20) {
            throw new IllegalArgumentException("Chỉ nhận lịch xem phòng từ 07:00 đến 20:30");
        }
        if (when.isBefore(now.plusMinutes(minLeadMinutes))) {
            String lead = minLeadMinutes >= 60 && minLeadMinutes % 60 == 0 ? minLeadMinutes / 60 + " giờ" : minLeadMinutes + " phút";
            throw new IllegalArgumentException("Vui lòng đặt lịch trước ít nhất " + lead);
        }
        if (when.toLocalDate().isAfter(now.toLocalDate().plusDays(maxDaysAhead))) {
            throw new IllegalArgumentException("Chỉ nhận lịch hẹn trong vòng " + maxDaysAhead + " ngày tới");
        }
        return when;
    }

    private String normalizePhone(String raw) {
        if (raw == null) return null;
        String phone = raw.replaceAll("[\\s.\\-]", "");
        if (phone.isEmpty()) return null;
        if (!PHONE.matcher(phone).matches())
            throw new IllegalArgumentException("Số điện thoại liên hệ không hợp lệ (10 số, bắt đầu bằng 0)");
        return phone;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normScope(String scope, String fallback) {
        return scope == null || scope.isBlank() ? fallback : scope.trim().toLowerCase(Locale.ROOT);
    }

    private static Pageable pageable(int page, int size, boolean newestFirst) {
        Sort sort = Sort.by(newestFirst ? Sort.Direction.DESC : Sort.Direction.ASC, "scheduledAt", "id");
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sort);
    }

    private ViewingRequest loadForUpdate(Long id) {
        return viewingRepository.findByIdForUpdate(id).orElseThrow(ViewingService::notFound);
    }

    private static ResourceNotFoundException notFound() {
        return new ResourceNotFoundException("Không tìm thấy yêu cầu xem phòng");
    }

    private static void requireParty(ViewingRequest v, User user, boolean tenantSide) {
        Long ownerId = tenantSide ? v.getTenant().getId() : v.getLandlord().getId();
        if (!ownerId.equals(user.getId())) throw notFound();
    }

    /* ---------- Chuyển đổi ---------- */

    private static String slotText(LocalDateTime t) {
        return t.format(TIME) + ", " + WEEKDAYS[t.getDayOfWeek().getValue() - 1] + " " + t.format(DATE);
    }

    private static String roomText(ViewingRequest v) {
        return "\"" + v.getRoom().getTitle() + "\" (" + v.getRoom().getHouse().getName() + ")";
    }

    private static String reasonText(String note) {
        return note == null ? "" : "\nLý do: " + note;
    }

    private Map<Long, String> loadCovers(Collection<Long> roomIds) {
        if (roomIds.isEmpty()) return Map.of();
        Map<Long, String> covers = new HashMap<>();
        for (Object[] row : roomImageRepository.findCoverImagesByRoom(roomIds)) {
            covers.putIfAbsent(((Number) row[0]).longValue(), (String) row[1]);
        }
        return covers;
    }

    private ViewingResponse single(ViewingRequest v, LocalDateTime now) {
        return toResponse(v, now, loadCovers(List.of(v.getRoom().getId())), Set.of());
    }

    private ViewingListResponse toList(Page<ViewingRequest> page, LocalDateTime now, Set<LocalDateTime> busy) {
        Map<Long, String> covers = loadCovers(page.getContent().stream().map(v -> v.getRoom().getId()).distinct().toList());
        List<ViewingResponse> items = page.getContent().stream().map(v -> toResponse(v, now, covers, busy)).toList();
        return new ViewingListResponse(items, page.getNumber(), page.getTotalPages(), page.getTotalElements(), page.hasNext());
    }

    private ViewingResponse toResponse(ViewingRequest v, LocalDateTime now, Map<Long, String> covers, Set<LocalDateTime> busy) {
        Room room = v.getRoom();
        BoardingHouse house = room.getHouse();
        boolean visible = room.getStatus() != RoomStatus.HIDDEN && house.getStatus() == HouseStatus.ACTIVE;
        String address = Stream.of(house.getAddress(), house.getWard(), house.getCity())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));

        return new ViewingResponse(
                v.getId(), v.getStatus(),
                v.getScheduledAt().isBefore(now),
                v.getStatus() == ViewingStatus.PENDING && busy.contains(v.getScheduledAt()),
                v.getScheduledAt(), v.getMessage(), v.getContactPhone(), v.getResponseNote(),
                v.getRespondedAt(), v.getCreatedAt(),
                room.getId(), room.getTitle(), covers.get(room.getId()), visible,
                house.getId(), house.getName(), address,
                v.getTenant().getId(), DisplayNames.of(v.getTenant()),
                v.getLandlord().getId(), DisplayNames.of(v.getLandlord()));
    }
}