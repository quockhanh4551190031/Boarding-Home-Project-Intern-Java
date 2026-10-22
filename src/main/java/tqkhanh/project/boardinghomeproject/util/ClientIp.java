package tqkhanh.project.boardinghomeproject.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public final class ClientIp {

    private static final Pattern IP_LITERAL = Pattern.compile("^[0-9a-fA-F:.]+$");

    private ClientIp() {
    }

    /**
     * Chỉ dùng getRemoteAddr(), tuyệt đối không tự đọc header X-Forwarded-For (client có thể giả).
     * Khi chạy sau proxy, Tomcat tự thay bằng IP thật nhờ server.forward-headers-strategy=native.
     */
    public static String of(HttpServletRequest request) {
        String addr = request.getRemoteAddr();
        if (addr == null || addr.isBlank()) return "unknown";

        if (addr.indexOf(':') >= 0 && IP_LITERAL.matcher(addr).matches()) {
            try {
                InetAddress inet = InetAddress.getByName(addr); // chuỗi IP thuần nên không tra DNS
                byte[] bytes = inet.getAddress();
                if (bytes.length == 16) {
                    // Gộp IPv6 theo nhóm /64 để không né giới hạn bằng cách đổi địa chỉ trong nhóm
                    StringBuilder sb = new StringBuilder("v6:");
                    for (int i = 0; i < 8; i++) sb.append(String.format("%02x", bytes[i]));
                    return sb.toString();
                }
                return inet.getHostAddress(); // IPv4 viết dạng ::ffff:1.2.3.4
            } catch (UnknownHostException e) {
                return addr;
            }
        }
        return addr;
    }
}