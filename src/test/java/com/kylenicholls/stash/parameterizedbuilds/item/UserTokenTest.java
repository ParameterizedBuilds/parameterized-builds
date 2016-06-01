package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserTokenTest {
	@Test
	public void testCreateNewUserToken() {
		String baseUrl = "url";
		String projectKey = "key";
		String projectName = "name";
		String userSlug = "user";
		String token = "token";
		UserToken actual = new UserToken(baseUrl, projectKey, projectName, userSlug, token);

		assertEquals(baseUrl, actual.getBaseUrl());
		assertEquals(projectKey, actual.getProjectKey());
		assertEquals(projectName, actual.getProjectName());
		assertEquals(userSlug, actual.getUserSlug());
		assertEquals(token, actual.getToken());
	}
}
