package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ServerTest {
	@Test
	public void testCreateNewServer() {
		String baseUrl = "url";
		String user = "user";
		String token = "token";
		boolean altUrl = false;
		Server actual = new Server(baseUrl, user, token, altUrl);

		assertEquals(baseUrl, actual.getBaseUrl());
		assertEquals(user, actual.getUser());
		assertEquals(token, actual.getToken());
		assertEquals(altUrl, actual.getAltUrl());
		assertEquals(user + ":" + token, actual.getJoinedToken());
	}

	@Test
	public void testCreateNewServerWithSlash() {
		String baseUrl = "url";
		String user = "user";
		String token = "token";
		boolean altUrl = false;
		Server actual = new Server(baseUrl + "/", user, token, altUrl);

		assertEquals(baseUrl, actual.getBaseUrl());
		assertEquals(user, actual.getUser());
		assertEquals(token, actual.getToken());
		assertEquals(altUrl, actual.getAltUrl());
	}

	@Test
	public void testCreateNewServerFromMap() {
		Map<String, Object> expected = new HashMap<>();
		expected.put("baseUrl", "url");
		expected.put("user", "user");
		expected.put("token", "token");
		expected.put("altUrl", false);
		Map<String, Object> actual = new Server(expected).asMap();

		assertEquals(expected, actual);
	}
}
