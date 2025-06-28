package unq.dapp.grupoj.soccergenius.util;

public class InputSanitizer {
    private InputSanitizer() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static String sanitizeInput(String input) {
        if (input == null) return "";

        String sanitized = input.toLowerCase();
        sanitized = sanitized.replaceAll("[^a-z]", "");
        sanitized = sanitized
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");

        return sanitized;
    }
}
