package shinzo.cineffi.Utils;

public class CinEffiUtils {
    public static Float averageScore(Float sum, Integer count) {
        return 0 < count ? Math.round((sum / count) * 10.0f) / 10.0f : null;
    }
}
