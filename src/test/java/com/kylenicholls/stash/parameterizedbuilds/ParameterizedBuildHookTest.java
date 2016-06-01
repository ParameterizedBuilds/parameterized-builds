package com.kylenicholls.stash.parameterizedbuilds;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;

public class ParameterizedBuildHookTest {
	private RepositoryHookContext context;
	private Settings settings;
	private RefChange refChange;
	private MinimalRef minimalRef;
	private ParameterizedBuildHook buildHook;
	private SettingsService settingsService;
	private Jenkins jenkins;
	private Repository repository;
	private SettingsValidationErrors validationErrors;
	private Project project;

	@Before
	public void setup() {
		settingsService = mock(SettingsService.class);
		CommitService commitService = mock(CommitService.class);
		jenkins = mock(Jenkins.class);
		AuthenticationContext authContext = mock(AuthenticationContext.class);
		buildHook = new ParameterizedBuildHook(settingsService, commitService, jenkins,
				authContext);

		context = mock(RepositoryHookContext.class);
		settings = mock(Settings.class);
		refChange = mock(RefChange.class);
		minimalRef = mock(MinimalRef.class);
		repository = mock(Repository.class);
		validationErrors = mock(SettingsValidationErrors.class);
		project = mock(Project.class);

		when(context.getRepository()).thenReturn(repository);
		when(refChange.getRef()).thenReturn(minimalRef);
		when(refChange.getToHash()).thenReturn("commithash");
		when(context.getSettings()).thenReturn(settings);
		when(repository.getSlug()).thenReturn("repoSlug");
		when(repository.getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("project_key");
		Server server = new Server("baseurl", null, null, false);
		when(jenkins.getJenkinsServer()).thenReturn(server);
		when(jenkins.getJenkinsServer(project.getKey())).thenReturn(server);
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
	}

	@Test
	public void testBranchRegexDoesNotMatch() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "push" })
				.buildParameters("").branchRegex("foobar").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(0)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchRegexEmpty() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "push" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchRegexMatches() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "push" })
				.buildParameters("").branchRegex("bran.*").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchUpdatedAndTriggerIsPush() {
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "push" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchUpdatedAndTriggerIsNotPush() {
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "add" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(0)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchUpdatedAndPathRegexEmtpy() {
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "push" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchAddedAndTriggerIsAdd() {
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "add" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchAddedAndTriggerIsNotAdd() {
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "push" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(0)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchDeletedAndTriggerIsDelete() {
		when(refChange.getType()).thenReturn(RefChangeType.DELETE);
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "delete" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testBranchDeletedAndTriggerIsNotDelete() {
		when(refChange.getType()).thenReturn(RefChangeType.DELETE);
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branch");
		Job job = new Job.JobBuilder(1).jobName("").triggers(new String[] { "add" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(0)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testJobTriggeredWhenTagAddedAndTriggerIsTagAdded() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/tags/tagname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = new Job.JobBuilder(1).jobName("name").isTag(true).triggers(new String[] { "add" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testJobNotTriggeredWhenTagAddedAndTriggerIsBranchAdded() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/tags/tagname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = new Job.JobBuilder(1).jobName("name").isTag(false)
				.triggers(new String[] { "add" }).buildParameters("").branchRegex("").pathRegex("")
				.createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(0)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testJobTriggeredWhenBranchAddedAndTriggerIsBranchAdded() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branchname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = new Job.JobBuilder(1).jobName("name").isTag(false)
				.triggers(new String[] { "add" }).buildParameters("").branchRegex("").pathRegex("")
				.createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(1)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testJobNotTriggeredWhenBranchAddedAndTriggerIsTagAdded() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branchname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = new Job.JobBuilder(1).jobName("name").isTag(true).triggers(new String[] { "add" })
				.buildParameters("").branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(0)).triggerJob(job, "", null, project.getKey());
	}

	@Test
	public void testJobNotTriggeredWhenBranchAddedAndTriggerIsBranchPushed() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(minimalRef.getId()).thenReturn("refs/heads/branchname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		Job job = new Job.JobBuilder(1).jobName("name").isTag(false)
				.triggers(new String[] { "push" }).buildParameters("").branchRegex("").pathRegex("")
				.createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);

		verify(jenkins, times(0)).triggerJob(job, "", null, project.getKey());
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
				.branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.validate(settings, validationErrors, repository);
		
		verify(validationErrors, times(1))
				.addFieldError(SettingsService.JOB_PREFIX + "0", "Field is required");
	}

	@Test
	public void testShowErrorIfTriggersEmpty() {
		Job job = new Job.JobBuilder(1).jobName("name").triggers("".split(";")).buildParameters("")
				.branchRegex("").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1)).addFieldError(SettingsService.TRIGGER_PREFIX
				+ "0", "You must choose at least one trigger");
	}

	@Test
	public void testShowErrorIfBranchRegexInvalid() {
		Job job = new Job.JobBuilder(1).jobName("name").triggers("add".split(";"))
				.buildParameters("").branchRegex("(").pathRegex("").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError(SettingsService.BRANCH_PREFIX + "0", "Unclosed group");
	}

	@Test
	public void testShowErrorIfPathRegexInvalid() {
		Job job = new Job.JobBuilder(1).jobName("name").triggers("add".split(";"))
				.buildParameters("").branchRegex("").pathRegex("(").createJob();
		List<Job> jobs = new ArrayList<Job>();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.validate(settings, validationErrors, repository);

		verify(validationErrors, times(1))
				.addFieldError(SettingsService.PATH_PREFIX + "0", "Unclosed group");
	}
}
