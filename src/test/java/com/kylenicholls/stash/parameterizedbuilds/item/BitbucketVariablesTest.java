package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

public class BitbucketVariablesTest {
	@Test
	public void testAddBranch() {
		String branch = "branch";
		BitbucketVariables actual = new BitbucketVariables.Builder().branch(branch).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$BRANCH", branch));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddCommit() {
		String commit = "commit";
		BitbucketVariables actual = new BitbucketVariables.Builder().commit(commit).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$COMMIT", commit));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddPRDestination() {
		String prDestination = "prdest";
		BitbucketVariables actual = new BitbucketVariables.Builder().prDestination(prDestination).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$PRDESTINATION", prDestination));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddPRId() {
		int prId = 5;
		BitbucketVariables actual = new BitbucketVariables.Builder().prId(prId).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$PRID", Integer.toString(prId)));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddPRAuthor() {
		String prAuthor = "prauthor";
		BitbucketVariables actual = new BitbucketVariables.Builder().prAuthor(prAuthor).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$PRAUTHOR", prAuthor));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddPRTitle() {
		String prTitle = "prtitle";
		BitbucketVariables actual = new BitbucketVariables.Builder().prTitle(prTitle).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$PRTITLE", prTitle));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddPRDescription() {
		String prDescription = "prdesc";
		BitbucketVariables actual = new BitbucketVariables.Builder().prDescription(prDescription).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$PRDESCRIPTION", prDescription));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddPRUrl() {
		String prUrl = "prurl";
		BitbucketVariables actual = new BitbucketVariables.Builder().prUrl(prUrl).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$PRURL", prUrl));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddRepo() {
		String repo = "repo";
		BitbucketVariables actual = new BitbucketVariables.Builder().repoName(repo).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$REPOSITORY", repo));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}

	@Test
	public void testAddProject() {
		String project = "project";
		BitbucketVariables actual = new BitbucketVariables.Builder().projectName(project).build();

		List<Entry<String, String>> expected = new ArrayList<>();
		expected.add(new SimpleEntry<>("$PROJECT", project));
		assertEquals(1, actual.getVariables().size());
		assertEquals(expected, actual.getVariables());
	}
}
