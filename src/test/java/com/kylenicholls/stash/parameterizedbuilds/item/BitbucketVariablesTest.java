package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

import org.junit.Before;
import org.junit.Test;

public class BitbucketVariablesTest {

	private PullRequest pullRequest;
	private RefChange refChange;
	private Branch branch;
	private Repository repository;

	private final String projectKey = "projectkey";
	private final String forkProjectKey = "forkprojectkey";
	private final Trigger trigger = Trigger.NULL;
	private final String url = "http://uri";
	private final String PR_TITLE = "prtitle";
	private final String REPO_SLUG = "reposlug";
	private final String FORK_REPO_SLUG = "forkreposlug";
	private final Long PR_ID = 15L;
	private final String PR_DESCRIPTION = "Description of this PR";
	private final String PR_AUTHOR = "this guy";
	private final String PR_EMAIL = "example@domain.com";
	private final String SOURCE_BRANCH = "sourcebranch";
	private final String DEST_BRANCH = "destbranch";
	private final String COMMIT = "commithash";

	@Before
	public void setup() {
		pullRequest = mock(PullRequest.class);
		refChange = mock(RefChange.class);
		repository = mock(Repository.class);
		Repository forkRepository = mock(Repository.class);
		Project forkProject = mock(Project.class);
		branch = mock(Branch.class);

		PullRequestParticipant author = mock(PullRequestParticipant.class);
		PullRequestRef prFromRef = mock(PullRequestRef.class);
		PullRequestRef prToRef = mock(PullRequestRef.class);
		ApplicationUser user = mock(ApplicationUser.class);

		when(pullRequest.getFromRef()).thenReturn(prFromRef);
		when(pullRequest.getToRef()).thenReturn(prToRef);
		when(pullRequest.getAuthor()).thenReturn(author);
		when(pullRequest.getDescription()).thenReturn(null);
		when(pullRequest.getTitle()).thenReturn(PR_TITLE);
		when(pullRequest.getId()).thenReturn(PR_ID);
		when(pullRequest.getDescription()).thenReturn(PR_DESCRIPTION);
		when(refChange.getToHash()).thenReturn(COMMIT);
		when(branch.getDisplayId()).thenReturn(SOURCE_BRANCH);
		when(branch.getLatestCommit()).thenReturn(COMMIT);
		when(author.getUser()).thenReturn(user);
		when(user.getDisplayName()).thenReturn(PR_AUTHOR);
		when(user.getEmailAddress()).thenReturn(PR_EMAIL);
		when(prFromRef.getRepository()).thenReturn(forkRepository);
		when(prFromRef.getDisplayId()).thenReturn(SOURCE_BRANCH);
		when(prFromRef.getLatestCommit()).thenReturn(COMMIT);
		when(prToRef.getDisplayId()).thenReturn(DEST_BRANCH);
		when(repository.getSlug()).thenReturn(REPO_SLUG);
		when(forkRepository.getSlug()).thenReturn(FORK_REPO_SLUG);
		when(forkRepository.getProject()).thenReturn(forkProject);
		when(forkProject.getKey()).thenReturn(forkProjectKey);
	}

	@Test
	public void testAddBranch() {
		String branch = "branch";
		BitbucketVariables actual = new BitbucketVariables.Builder().add("$BRANCH", () -> branch).build();

		assertEquals(1, actual.getVariables().size());
		assertEquals(branch, actual.fetch("$BRANCH"));
	}

	@Test
	public void testPopulateFromPRSetsBranch() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(SOURCE_BRANCH, actual.fetch("$BRANCH"));
	}

	@Test
	public void testPopulateFromPRSetsCommit() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(COMMIT, actual.fetch("$COMMIT"));
	}

	@Test
	public void testPopulateFromPRSetsUrl() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(url, actual.fetch("$URL"));
	}

	@Test
	public void testPopulateFromPRSetsRepository() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(REPO_SLUG, actual.fetch("$REPOSITORY"));
	}

	@Test
	public void testPopulateFromPRSetsProject() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(projectKey, actual.fetch("$PROJECT"));
	}

	@Test
	public void testPopulateFromPRSetsPRID() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(PR_ID.toString(), actual.fetch("$PRID"));
	}

	@Test
	public void testPopulateFromPRSetsPRAuthor() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(PR_AUTHOR, actual.fetch("$PRAUTHOR"));
	}

	@Test
	public void testPopulateFromPRSetsPRAuthorEmail() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(PR_EMAIL, actual.fetch("$$PREMAIL"));
	}

	@Test
	public void testPopulateFromPRSetsPRTitle() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(PR_TITLE, actual.fetch("$PRTITLE"));
	}

	@Test
	public void testPopulateFromPRSetsPRDescription() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(PR_DESCRIPTION, actual.fetch("$PRDESCRIPTION"));
	}

	@Test
	public void testPopulateFromPRSetsPRDestinaion() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(DEST_BRANCH, actual.fetch("$PRDESTINATION"));
	}

	@Test
	public void testPopulateFromPRSetsPRUrl() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		String expected = url + "/projects/" + projectKey + "/repos/" + REPO_SLUG + "/pull-requests/" + PR_ID;
		assertEquals(expected, actual.fetch("$PRURL"));
	}

	@Test
	public void testPopulateFromPRSetsTrigger() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(trigger.toString(), actual.fetch("$TRIGGER"));
	}

	@Test
	public void testPopulateFromPRSetsSourceProject() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(forkProjectKey, actual.fetch("$PRSOURCEPROJECT"));
	}

	@Test
	public void testPopulateFromPRSetsSourceRepository() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromPR(pullRequest, repository, projectKey, trigger, url).build();

		assertEquals(FORK_REPO_SLUG, actual.fetch("$PRSOURCEREPOSITORY"));
	}

	@Test
	public void testPopulateFromRefSetsBranch() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromRef(SOURCE_BRANCH, refChange, repository, projectKey, trigger, url).build();

		assertEquals(SOURCE_BRANCH, actual.fetch("$BRANCH"));
	}

	@Test
	public void testPopulateFromRefSetsCommit() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromRef(SOURCE_BRANCH, refChange, repository, projectKey, trigger, url).build();

		assertEquals(COMMIT, actual.fetch("$COMMIT"));
	}

	@Test
	public void testPopulateFromRefSetsUrl() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromRef(SOURCE_BRANCH, refChange, repository, projectKey, trigger, url).build();

		assertEquals(url, actual.fetch("$URL"));
	}

	@Test
	public void testPopulateFromRefSetsRepository() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromRef(SOURCE_BRANCH, refChange, repository, projectKey, trigger, url).build();

		assertEquals(REPO_SLUG, actual.fetch("$REPOSITORY"));
	}

	@Test
	public void testPopulateFromRefSetsProject() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromRef(SOURCE_BRANCH, refChange, repository, projectKey, trigger, url).build();

		assertEquals(projectKey, actual.fetch("$PROJECT"));
	}

	@Test
	public void testPopulateFromBranchSetsBranch() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromBranch(branch, repository, projectKey, trigger, url).build();

		assertEquals(SOURCE_BRANCH, actual.fetch("$BRANCH"));
	}

	@Test
	public void testPopulateFromBranchSetsCommit() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromBranch(branch, repository, projectKey, trigger, url).build();

		assertEquals(COMMIT, actual.fetch("$COMMIT"));
	}

	@Test
	public void testPopulateFromBranchSetsUrl() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromBranch(branch, repository, projectKey, trigger, url).build();

		assertEquals(url, actual.fetch("$URL"));
	}

	@Test
	public void testPopulateFromBranchSetsRepository() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromBranch(branch, repository, projectKey, trigger, url).build();

		assertEquals(REPO_SLUG, actual.fetch("$REPOSITORY"));
	}

	@Test
	public void testPopulateFromBranchSetsProject() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromBranch(branch, repository, projectKey, trigger, url).build();

		assertEquals(projectKey, actual.fetch("$PROJECT"));
	}

	@Test
	public void testPopulateFromStringsSetsBranch() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromStrings(SOURCE_BRANCH, COMMIT, repository, projectKey, trigger, url).build();

		assertEquals(SOURCE_BRANCH, actual.fetch("$BRANCH"));
	}

	@Test
	public void testPopulateFromStringsSetsCommit() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromStrings(SOURCE_BRANCH, COMMIT, repository, projectKey, trigger, url).build();

		assertEquals(COMMIT, actual.fetch("$COMMIT"));
	}

	@Test
	public void testPopulateFromStringsSetsUrl() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromStrings(SOURCE_BRANCH, COMMIT, repository, projectKey, trigger, url).build();

		assertEquals(url, actual.fetch("$URL"));
	}

	@Test
	public void testPopulateFromStringsSetsRepository() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromStrings(SOURCE_BRANCH, COMMIT, repository, projectKey, trigger, url).build();

		assertEquals(REPO_SLUG, actual.fetch("$REPOSITORY"));
	}

	@Test
	public void testPopulateFromStringsSetsProject() {
		BitbucketVariables actual = new BitbucketVariables.Builder()
				.populateFromStrings(SOURCE_BRANCH, COMMIT, repository, projectKey, trigger, url).build();

		assertEquals(projectKey, actual.fetch("$PROJECT"));
	}
}
