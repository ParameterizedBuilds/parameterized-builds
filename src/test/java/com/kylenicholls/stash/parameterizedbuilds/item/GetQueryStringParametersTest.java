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
		GetQueryStringParameters parameters = new GetQueryStringParameters.Builder().branch(branch).commit(commit)
				.prDestination(prDest).projectName(projectName).repoName(repoName).build();

		assertEquals(branch, parameters.getBranch());
		assertEquals(commit, parameters.getCommit());
		assertEquals(prDest, parameters.getPrDestination());
		assertEquals(projectName, parameters.getProjectName());
		assertEquals(repoName, parameters.getRepoName());
	}
}
