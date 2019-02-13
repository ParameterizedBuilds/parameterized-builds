package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ServerTest {
	@Test
	public void testCreateNewServer() {
		String baseUrl = "url";
		String alias = "alias";
		String user = "user";
		String token = "token";
		boolean altUrl = false;
		boolean csrfEnabled = false;
		Server actual = new Server(baseUrl, alias, user, token, altUrl, csrfEnabled);

		assertEquals(baseUrl, actual.getBaseUrl());
		assertEquals(alias, actual.getAlias());
		assertEquals(user, actual.getUser());
		assertEquals(token, actual.getToken());
		assertEquals(altUrl, actual.getAltUrl());
		assertEquals(csrfEnabled, actual.getCsrfEnabled());
		assertEquals(user + ":" + token, actual.getJoinedToken());
	}

	@Test
	public void testCreateNewServerWithSlash() {
		String baseUrl = "url";
		Server actual = new Server(baseUrl + "/", null, "", "", false, false);

		assertEquals(baseUrl, actual.getBaseUrl());
	}

	@Test
	public void testCreateNewServerFromMap() {
		Map<String, Object> expected = new HashMap<>();
		expected.put("baseUrl", "url");
		expected.put("alias", "alias");
		expected.put("user", "user");
		expected.put("token", "token");
		expected.put("altUrl", false);
		expected.put("csrfEnabled", false);
		Map<String, Object> actual = new Server(expected).asMap();

		assertEquals(expected, actual);
	}
}
