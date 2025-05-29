package Server.Utils;

import java.util.ArrayList;
import java.util.List;

public class RequestParser {
    public static List<String> parseRequest(String request) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;

        for (char c : request.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart = new StringBuilder();
                }
            } else {
                currentPart.append(c);
            }
        }

        if (currentPart.length() > 0) {
            parts.add(currentPart.toString());
        }

        return parts;
    }
}
