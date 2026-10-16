package tqkhanh.project.boardinghomeproject.util;

import tqkhanh.project.boardinghomeproject.entity.Profile;
import tqkhanh.project.boardinghomeproject.entity.User;

public final class DisplayNames {

    private DisplayNames() {
    }

    /**
     * Họ tên nếu có, nếu không thì email đã che (ab***@gmail.com).
     */
    public static String of(User user) {
        Profile profile = user.getProfile();
        if (profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()) {
            return profile.getFullName().trim();
        }
        return maskEmail(user.getEmail());
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) return "Người dùng";
        int at = email.indexOf('@');
        String local = at > 0 ? email.substring(0, at) : email;
        String domain = at > 0 ? email.substring(at) : "";
        return local.substring(0, Math.min(2, local.length())) + "***" + domain;
    }

    public static String maskPhone(String phone) {
        String p = phone.trim();
        if (p.length() <= 3) return "***";
        return p.substring(0, 3) + "*".repeat(p.length() - 3);
    }
}