package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ServerTest {
	@Test
	public void testCreateNewServer() {
		String baseUrl = "url";
		String user = "user";
		String token = "token";
		boolean altUrl = false;
		Server server = new Server(baseUrl, user, token, altUrl);

		assertEquals(baseUrl, server.getBaseUrl());
		assertEquals(user, server.getUser());
		assertEquals(token, server.getToken());
		assertEquals(altUrl, server.getAltUrl());
	}

	@Test
	public void testCreateNewServerWithSlash() {
		String baseUrl = "url";
		String user = "user";
		String token = "token";
		boolean altUrl = false;
		Server server = new Server(baseUrl + "/", user, token, altUrl);

		assertEquals(baseUrl, server.getBaseUrl());
		assertEquals(user, server.getUser());
		assertEquals(token, server.getToken());
		assertEquals(altUrl, server.getAltUrl());
	}
}
