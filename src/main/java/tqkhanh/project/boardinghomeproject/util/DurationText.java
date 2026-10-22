package tqkhanh.project.boardinghomeproject.util;

public final class DurationText {

    private DurationText() {
    }

    public static String of(long seconds) {
        if (seconds < 60) return Math.max(seconds, 1) + " giây";
        long minutes = (seconds + 59) / 60;
        if (minutes < 60) return minutes + " phút";
        long hours = minutes / 60;
        long rest = minutes % 60;
        return rest == 0 ? hours + " giờ" : hours + " giờ " + rest + " phút";
    }
}