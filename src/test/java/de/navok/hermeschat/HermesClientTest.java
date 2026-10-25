package de.navok.hermeschat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HermesClientTest {

    @Test
    void readsFirstChoiceContent() {
        assertEquals("hi", HermesClient.extractAnswer("{\"choices\":[{\"message\":{\"content\":\"hi\"}}]}"));
    }

    @Test
    void unexpectedShapesYieldEmptyString() {
        assertEquals("", HermesClient.extractAnswer("{\"choices\":[]}"));
        assertEquals("", HermesClient.extractAnswer("{\"choices\":[{}]}"));
        assertEquals("", HermesClient.extractAnswer("{\"choices\":[{\"message\":{\"content\":null}}]}"));
        assertEquals("", HermesClient.extractAnswer("not json"));
        assertEquals("", HermesClient.extractAnswer("[]"));
    }
}
