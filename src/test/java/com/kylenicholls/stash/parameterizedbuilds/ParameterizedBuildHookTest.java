package com.kylenicholls.stash.parameterizedbuilds;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.bitbucket.hook.repository.PostRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookRequest;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.JobBuilder;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import java.net.URI;
import java.net.URISyntaxException;


public class ParameterizedBuildHookTest {
	private final String BRANCH_REF = "refs/heads/branch";
	private final String PROJECT_KEY = "projectkey";
	private final String COMMIT = "commithash";
	private final String REPO_SLUG = "reposlug";
	private final String URI = "http://uri";
	private final Server globalServer = new Server("globalurl", "globaluser", "globaltoken", false);
	private final Server projectServer = new Server("projecturl", "projectuser", "projecttoken",
			false);
	private PostRepositoryHookContext context;
	private RepositoryHookRequest request;
	private Settings settings;
	private RefChange refChange;
	private MinimalRef minimalRef;
	private ParameterizedBuildHook buildHook;
	private SettingsService settingsService;
	private Jenkins jenkins;
	private ApplicationPropertiesService propertiesService;
	private Repository repository;
	private SettingsValidationErrors validationErrors;
	private Project project;
	private ApplicationUser user;
	private List<RefChange> refChanges;
	private JobBuilder jobBuilder;
	List<Job> jobs;

	@Before
	public void setup() throws URISyntaxException {
		settingsService = mock(SettingsService.class);
		CommitService commitService = mock(CommitService.class);
		jenkins = mock(Jenkins.class);
		propertiesService = mock(ApplicationPropertiesService.class);
		AuthenticationContext authContext = mock(AuthenticationContext.class);
		buildHook = new ParameterizedBuildHook(settingsService, commitService, jenkins,
				propertiesService, authContext);

		context = mock(PostRepositoryHookContext.class);
		request = mock(RepositoryHookRequest.class);
		settings = mock(Settings.class);
		refChange = mock(RefChange.class);
		minimalRef = mock(MinimalRef.class);
		repository = mock(Repository.class);
		validationErrors = mock(SettingsValidationErrors.class);
		project = mock(Project.class);
		user = mock(ApplicationUser.class);

		when(authContext.getCurrentUser()).thenReturn(user);
		when(request.getRepository()).thenReturn(repository);
		when(refChange.getRef()).thenReturn(minimalRef);
		when(refChange.getToHash()).thenReturn(COMMIT);
		when(settingsService.getSettings(any())).thenReturn(settings);
		when(repository.getSlug()).thenReturn(REPO_SLUG);
		when(repository.getProject()).thenReturn(project);
		when(project.getKey()).thenReturn(PROJECT_KEY);
		when(jenkins.getJenkinsServer()).thenReturn(globalServer);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(projectServer);
		when(propertiesService.getBaseUrl()).thenReturn(new URI(URI));
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);

		refChanges = new ArrayList<>();
		refChanges.add(refChange);
		when(request.getRefChanges()).thenReturn(refChanges);
		when(minimalRef.getId()).thenReturn(BRANCH_REF);
		jobBuilder = new Job.JobBuilder(1).jobName("").buildParameters("").branchRegex("")
				.pathRegex("");
		jobs = new ArrayList<>();
		when(settingsService.getJobs(any())).thenReturn(jobs);
	}

	@Test
	public void testBranchRegexDoesNotMatch() {
		Job job = jobBuilder.triggers(new String[] { "push" }).branchRegex("foobar").build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testBranchRegexEmpty() {
		Job job = jobBuilder.triggers(new String[] { "push" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testBranchRegexMatches() {
		Job job = jobBuilder.triggers(new String[] { "push" }).branchRegex("bran.*").build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testBranchUpdatedAndTriggerIsPush() {
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		Job job = jobBuilder.triggers(new String[] { "push" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testBranchUpdatedAndTriggerIsNotPush() {
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		Job job = jobBuilder.triggers(new String[] { "add" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testBranchUpdatedAndPathRegexEmtpy() {
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		Job job = jobBuilder.triggers(new String[] { "push" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testBranchAddedAndTriggerIsAdd() {
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = jobBuilder.triggers(new String[] { "add" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testBranchAddedAndTriggerIsNotAdd() {
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = jobBuilder.triggers(new String[] { "push" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testBranchDeletedAndTriggerIsDelete() {
		when(refChange.getType()).thenReturn(RefChangeType.DELETE);
		Job job = jobBuilder.triggers(new String[] { "delete" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testBranchDeletedAndTriggerIsNotDelete() {
		when(refChange.getType()).thenReturn(RefChangeType.DELETE);
		Job job = jobBuilder.triggers(new String[] { "add" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testJobTriggeredWhenTagAddedAndTriggerIsTagAdded() {
		when(minimalRef.getId()).thenReturn("refs/tags/tagname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = jobBuilder.isTag(true).triggers(new String[] { "add" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testJobNotTriggeredWhenTagAddedAndTriggerIsBranchAdded() {
		when(minimalRef.getId()).thenReturn("refs/tags/tagname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = jobBuilder.isTag(false).triggers(new String[] { "add" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testJobTriggeredWhenBranchAddedAndTriggerIsBranchAdded() {
		when(minimalRef.getId()).thenReturn("refs/heads/branchname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = jobBuilder.isTag(false).triggers(new String[] { "add" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1))
				.triggerJob("projecturl/job/build", projectServer.getJoinedToken(), true);
	}

	@Test
	public void testJobNotTriggeredWhenBranchAddedAndTriggerIsTagAdded() {
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = jobBuilder.isTag(true).triggers(new String[] { "add" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testJobNotTriggeredWhenBranchAddedAndTriggerIsBranchPushed() {
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = jobBuilder.isTag(false).triggers(new String[] { "push" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(0)).triggerJob(any(), any(), anyBoolean());
	}

	@Test
	public void testUseProjectJenkinsAndUserToken() {
		String userToken = "user:token";
		when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(projectServer);
		when(jenkins.getJoinedUserToken(user, PROJECT_KEY)).thenReturn(userToken);
		Job job = jobBuilder.triggers(new String[] { "push" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1)).triggerJob("projecturl/job/build", userToken, false);
	}

	@Test
	public void testUseGlobalJenkinsAndUserToken() {
		String userToken = "user:token";
		when(jenkins.getJenkinsServer(PROJECT_KEY)).thenReturn(null);
		when(jenkins.getJoinedUserToken(user)).thenReturn(userToken);
		Job job = jobBuilder.triggers(new String[] { "push" }).build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1)).triggerJob("globalurl/job/build", userToken, false);
	}

	@Test
	public void testBranchVariable() {
		Job job = jobBuilder.triggers(new String[] { "push" }).buildParameters("param=$BRANCH")
				.build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1)).triggerJob("projecturl/job/buildWithParameters?param="
				+ BRANCH_REF.replace("refs/heads/", ""), projectServer.getJoinedToken(), true);
	}

	@Test
	public void testCommitVariable() {
		Job job = jobBuilder.triggers(new String[] { "push" }).buildParameters("param=$COMMIT")
				.build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1)).triggerJob("projecturl/job/buildWithParameters?param="
				+ COMMIT, projectServer.getJoinedToken(), true);
	}

	@Test
	public void testRepoNameVariable() {
		Job job = jobBuilder.triggers(new String[] { "push" }).buildParameters("param=$REPOSITORY")
				.build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1)).triggerJob("projecturl/job/buildWithParameters?param="
				+ REPO_SLUG, projectServer.getJoinedToken(), true);
	}

	@Test
	public void testProjectNameVariable() {
		Job job = jobBuilder.triggers(new String[] { "push" }).buildParameters("param=$PROJECT")
				.build();
		jobs.add(job);
		buildHook.postUpdate(context, request);

		verify(jenkins, times(1)).triggerJob("projecturl/job/buildWithParameters?param="
				+ PROJECT_KEY, projectServer.getJoinedToken(), true);
	}

	@Test
	public void testShowErrorIfJenkinsSettingsNull() {
		when(jenkins.getJenkinsServer()).thenReturn(null);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(null);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError("jenkins-admin-error", "Jenkins is not setup in Bitbucket Server");
	}

	@Test
	public void testShowErrorIfBaseUrlEmpty() {
		Server server = new Server("", null, null, false);
		when(jenkins.getJenkinsServer()).thenReturn(server);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(server);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError("jenkins-admin-error", "Jenkins is not setup in Bitbucket Server");
	}

	@Test
	public void testShowErrorIfJenkinsSettingsUrlEmpty() {
		Server server = new Server("", null, null, false);
		when(jenkins.getJenkinsServer()).thenReturn(server);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(null);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError("jenkins-admin-error", "Jenkins is not setup in Bitbucket Server");
	}

	@Test
	public void testShowErrorIfProjectSettingsUrlEmpty() {
		when(jenkins.getJenkinsServer()).thenReturn(null);
		Server server = new Server("", null, null, false);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(server);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError("jenkins-admin-error", "Jenkins is not setup in Bitbucket Server");
	}

	@Test
	public void testNoErrorIfOnlyJenkinsSettingsNull() {
		when(jenkins.getJenkinsServer()).thenReturn(null);
		Server server = new Server("baseurl", null, null, false);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(server);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(0)).addFieldError(any(), any());
	}

	@Test
	public void testNoErrorIfOnlyProjectSettingsNull() {
		Server server = new Server("baseurl", null, null, false);
		when(jenkins.getJenkinsServer()).thenReturn(server);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(null);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(0)).addFieldError(any(), any());
	}

	@Test
	public void testShowErrorIfJobNameEmpty() {
		Job job = new Job.JobBuilder(1).jobName("").triggers("add".split(";")).buildParameters("")
				.branchRegex("").pathRegex("").build();
		jobs.add(job);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError(SettingsService.JOB_PREFIX + "0", "Field is required");
	}

	@Test
	public void testShowErrorIfTriggersEmpty() {
		Job job = new Job.JobBuilder(1).jobName("name").triggers("".split(";")).buildParameters("")
				.branchRegex("").pathRegex("").build();
		jobs.add(job);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1)).addFieldError(SettingsService.TRIGGER_PREFIX
				+ "0", "You must choose at least one trigger");
	}

	@Test
	public void testShowErrorIfBranchRegexInvalid() {
		Job job = new Job.JobBuilder(1).jobName("name").triggers("add".split(";"))
				.buildParameters("").branchRegex("(").pathRegex("").build();
		jobs.add(job);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError(SettingsService.BRANCH_PREFIX + "0", "Unclosed group");
	}

	@Test
	public void testShowErrorIfPathRegexInvalid() {
		Job job = new Job.JobBuilder(1).jobName("name").triggers("add".split(";"))
				.buildParameters("").branchRegex("").pathRegex("(").build();
		jobs.add(job);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError(SettingsService.PATH_PREFIX + "0", "Unclosed group");
	}
}
