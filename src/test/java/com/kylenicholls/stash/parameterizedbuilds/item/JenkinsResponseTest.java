package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JenkinsResponseTest {
    @Test
    public void testCreateDefaultResponse() {
        JenkinsResponse actual = new JenkinsResponse.JenkinsMessage().build();

        assertFalse(actual.getError());
        assertFalse(actual.getPrompt());
        assertEquals("", actual.getMessageText());
        Map<String, Object> expected = new HashMap<>();
        expected.put("error", false);
        expected.put("prompt", false);
        expected.put("messageText", "");
        assertEquals(expected, actual.getMessage());
    }

    @Test
    public void testCreateResponse() {
        JenkinsResponse actual = new JenkinsResponse.JenkinsMessage().error(true).prompt(true)
                .messageText("message").build();

        assertTrue(actual.getError());
        assertTrue(actual.getPrompt());
        assertEquals("message", actual.getMessageText());
    }
}
