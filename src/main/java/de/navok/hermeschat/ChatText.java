package de.navok.hermeschat;

import java.util.ArrayList;
import java.util.List;

/** Turns an agent answer into chat lines. Pure, no server state. */
public final class ChatText {

    public static final String TRUNCATED_MARKER = " … [gekürzt]";

    private ChatText() {
    }

    public static List<String> split(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text == null) {
            return lines;
        }
        String body = text.strip();
        if (body.isEmpty()) {
            return lines;
        }
        if (maxChars > 0 && body.length() > maxChars) {
            body = body.substring(0, maxChars).stripTrailing() + TRUNCATED_MARKER;
        }
        for (String line : body.split("\r?\n")) {
            String stripped = line.strip();
            if (!stripped.isEmpty()) {
                lines.add(stripped);
            }
        }
        return lines;
    }
}
