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
		JenkinsResponse message = new JenkinsResponse.JenkinsMessage().build();

		assertFalse(message.getError());
		assertFalse(message.getPrompt());
		assertEquals("", message.getMessageText());
		Map<String, Object> expected = new HashMap<>();
		expected.put("error", false);
		expected.put("prompt", false);
		expected.put("messageText", "");
		assertEquals(expected, message.getMessage());
	}

	@Test
	public void testCreateResponse() {
		JenkinsResponse message = new JenkinsResponse.JenkinsMessage().error(true).prompt(true)
				.messageText("message").build();

		assertTrue(message.getError());
		assertTrue(message.getPrompt());
		assertEquals("message", message.getMessageText());
	}
}
