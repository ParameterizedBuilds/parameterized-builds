package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GetQueryStringParametersTest {

	@Test
	public void testCreateNewQueryParams() {
		String branch = "branch";
		String commit = "commit";
		String prDest = "prDest";
		String projectName = "projectName";
		String repoName = "repoName";
		GetQueryStringParameters actual = new GetQueryStringParameters.Builder().branch(branch)
				.commit(commit).prDestination(prDest).projectName(projectName).repoName(repoName)
				.build();

		assertEquals(branch, actual.getBranch());
		assertEquals(commit, actual.getCommit());
		assertEquals(prDest, actual.getPrDestination());
		assertEquals(projectName, actual.getProjectName());
		assertEquals(repoName, actual.getRepoName());
	}
}
