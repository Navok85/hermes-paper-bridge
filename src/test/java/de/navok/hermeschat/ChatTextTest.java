package de.navok.hermeschat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChatTextTest {

    @Test
    void splitsOnNewlinesAndDropsBlankLines() {
        assertEquals(List.of("a", "b"), ChatText.split("a\n\n b \n", 1500));
    }

    @Test
    void capsLongSingleLine() {
        List<String> lines = ChatText.split("x".repeat(2000), 100);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).endsWith(ChatText.TRUNCATED_MARKER));
        assertTrue(lines.get(0).length() < 130);
    }

    @Test
    void blankInputYieldsNoLines() {
        assertTrue(ChatText.split("   ", 1500).isEmpty());
        assertTrue(ChatText.split(null, 1500).isEmpty());
    }
}
