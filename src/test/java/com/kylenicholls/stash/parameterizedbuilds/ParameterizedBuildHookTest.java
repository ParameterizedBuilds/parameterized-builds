package com.kylenicholls.stash.parameterizedbuilds;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class ParameterizedBuildHookTest {
	private RepositoryHookContext context;
	private Settings settings;
	private RefChange refChange;
	private MinimalRef minimalRef;
	private ParameterizedBuildHook buildHook;
	private SettingsService settingsService;
	private CommitService commitService;
	private Jenkins jenkins;
	private String pathRegex;
	private String branchRegex;
	private String branch;
	private Repository repository;
	private Project project;
	public static final String COND_BASEURL_PREFIX = "cond-baseurl-";
	public static final String COND_CI_PREFIX = "cond-ciserver-";
	public static final String COND_JOB_PREFIX = "cond-jobname-";
	public static final String COND_BRANCH_PREFIX = "cond-branch-";
	public static final String COND_PATH_PREFIX = "cond-path-";
	public static final String COND_PARAM_PREFIX = "cond-param-";
	public static final String COND_TRIGGER_PREFIX = "cond-trigger-";
	public static final String COND_USERNAME_PREFIX = "cond-username-";
	public static final String COND_PASSWORD_PREFIX = "cond-password-";
	private Collection<String> fileNames = new ArrayList<String>();
	private AuthenticationContext authenticationContext;
	
	
	@Before
	public void setup() throws Exception {
		context = mock(RepositoryHookContext.class);
		settings = mock(Settings.class);
		refChange = mock(RefChange.class);
		minimalRef = mock(MinimalRef.class);
		settingsService = mock(SettingsService.class);
		commitService = mock(CommitService.class);
		jenkins = mock(Jenkins.class);
		authenticationContext = mock(AuthenticationContext.class);
		buildHook = new ParameterizedBuildHook(settingsService, commitService, jenkins,authenticationContext);
		
		fileNames.add("path/to/file");
		fileNames.add("foo/bar/file");
		fileNames.add("test3/test4");
		branch = "anewbranch";
		repository = mock(Repository.class);
		project = mock(Project.class);
	}
	
	// Test buildBranchCheck function
	@Test
	public void testBranchUpdatedAndTriggerIsAlways() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.PUSH, Trigger.MANUAL, Trigger.PULLREQUEST);
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue(results);
	}
	
	@Test
	public void testBranchUpdatedAndTriggerIsPostreceive() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.PUSH);
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue(results);
	}

	@Test
	public void testBranchUpdatedAndTriggerIsPullrequests() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.PULLREQUEST);
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertFalse(results);
	}
	
	@Test
	public void testBranchAddedAndTriggerIsPostreceive() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.PUSH);
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertFalse(results);
	}
	
	@Test
	public void testBranchUpdatedAndTriggerIsManual() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.MANUAL);
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertFalse(results);
	}
	
	@Test
	public void testBranchUpdatedAndNoRestrictionsOnBuilding() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.PUSH, Trigger.MANUAL, Trigger.PULLREQUEST);
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue("Branch was not built when it should have been", results);
	}
	
	@Test
	public void testBranchAddedAndNoRestrictionsOnBuilding() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.ADD, Trigger.MANUAL, Trigger.PULLREQUEST);
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue("Branch was not built when it should have been", results);
	}
	
	@Test
	public void testBranchUpdatedAndRestrictionOnBranch() {
		pathRegex = "";
		branchRegex = "anewbr.*|foobar";
		List<Trigger> triggers = Arrays.asList(Trigger.PUSH);
		when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue("Branch regex matching failed", results);
		
		branchRegex = "anewbranch|foobar";
		results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue("Branch strict matching filed", results);
		
		branchRegex = "foobar|barfoo";
		results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertFalse("Branch was matched when it shouldn't have been", results);
	}

	@Test
	public void testBranchAddedAndRestrictionOnBranch() {
		pathRegex = "";
		branchRegex = "anewbr.*|foobar";
		List<Trigger> triggers = Arrays.asList(Trigger.ADD);
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue("Branch regex matching failed", results);
		
		branchRegex = "anewbranch|foobar";
		results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue("Branch strict matching filed", results);
		
		branchRegex = "foobar";
		results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertFalse("Branch was matched when it shouldn't have been", results);
	}
	
	@Test
	public void testBranchDeletedDoNotMatch() {
		pathRegex = "test3.*|foobar.*";
		branchRegex = "anewbranch|foobar";
		List<Trigger> triggers = Arrays.asList(Trigger.NULL);
		when(refChange.getType()).thenReturn(RefChangeType.DELETE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertFalse(results);

		pathRegex = "foobar|barfoo";
		branchRegex = "foobar|barfoo.*";
		results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertFalse(results);
	}
	
	@Test
	public void testBranchDeletedMatch() {
		pathRegex = "";
		branchRegex = "";
		List<Trigger> triggers = Arrays.asList(Trigger.DELETE);
		when(refChange.getType()).thenReturn(RefChangeType.DELETE);
		boolean results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue(results);
		
		pathRegex = "";
		branchRegex = "anewbranch|foobar";
		results = buildHook.buildBranchCheck(repository, refChange, branch, branchRegex, pathRegex, triggers);
		assertTrue(results);
	}
	
	@Test
	public void testJobTriggeredWhenTagAndTriggerTag() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(context.getRepository()).thenReturn(repository);
		when(refChange.getRef()).thenReturn(minimalRef);
		when(refChange.getToHash()).thenReturn("hash");
		when(minimalRef.getId()).thenReturn("refs/tags/tagname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		when(context.getSettings()).thenReturn(settings);
		when(repository.getSlug()).thenReturn("repoSlug");
		when(repository.getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("project_key");
		List<Job> jobs = new ArrayList<Job>();
		Job job = new Job
				.JobBuilder(1)
				.jobName("name")
				.isTag(true)
				.triggers(new String[]{"add", "manual"})
				.buildParameters("param1=value1\r\nparam2=value2")
				.branchRegex("")
				.pathRegex("")
				.createJob();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);
		verify(jenkins, times(1)).triggerJob(job, "param1=value1&param2=value2", null);
	}
	
	@Test
	public void testJobNotTriggeredWhenTagAndNotTriggerTag() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(context.getRepository()).thenReturn(repository);
		when(refChange.getRef()).thenReturn(minimalRef);
		when(refChange.getToHash()).thenReturn("hash");
		when(minimalRef.getId()).thenReturn("refs/tags/tagname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		when(context.getSettings()).thenReturn(settings);
		when(repository.getSlug()).thenReturn("repoSlug");
		when(repository.getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("project_key");
		List<Job> jobs = new ArrayList<Job>();
		Job job = new Job
				.JobBuilder(1)
				.jobName("name")
				.isTag(false)
				.triggers(new String[]{"add", "manual"})
				.buildParameters("param1=value1\r\nparam2=value2")
				.branchRegex("")
				.pathRegex("")
				.createJob();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);
		verify(jenkins, times(0)).triggerJob(job, "param1=value1&param2=value2", null);
	}
	
	@Test
	public void testJobTriggeredWhenBranchAndTriggerBranch() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(context.getRepository()).thenReturn(repository);
		when(refChange.getRef()).thenReturn(minimalRef);
		when(refChange.getToHash()).thenReturn("hash");
		when(minimalRef.getId()).thenReturn("refs/heads/branchname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		when(context.getSettings()).thenReturn(settings);
		when(repository.getSlug()).thenReturn("repoSlug");
		when(repository.getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("project_key");
		List<Job> jobs = new ArrayList<Job>();
		Job job = new Job
				.JobBuilder(1)
				.jobName("name")
				.isTag(false)
				.triggers(new String[]{"add", "manual"})
				.buildParameters("param1=value1\r\nparam2=value2")
				.branchRegex("")
				.pathRegex("")
				.createJob();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);
		verify(jenkins, times(1)).triggerJob(job, "param1=value1&param2=value2", null);
	}
	
	@Test
	public void testJobNotTriggeredWhenBranchAndNotTriggerBranch() {
		List<RefChange> refChanges = new ArrayList<RefChange>();
		refChanges.add(refChange);
		when(context.getRepository()).thenReturn(repository);
		when(refChange.getRef()).thenReturn(minimalRef);
		when(refChange.getToHash()).thenReturn("hash");
		when(minimalRef.getId()).thenReturn("refs/heads/branchname");
		when(refChange.getType()).thenReturn(RefChangeType.ADD);
		when(context.getSettings()).thenReturn(settings);
		when(repository.getSlug()).thenReturn("repoSlug");
		when(repository.getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("project_key");
		List<Job> jobs = new ArrayList<Job>();
		Job job = new Job
				.JobBuilder(1)
				.jobName("name")
				.isTag(true)
				.triggers(new String[]{"add", "manual"})
				.buildParameters("param1=value1\r\nparam2=value2")
				.branchRegex("")
				.pathRegex("")
				.createJob();
		jobs.add(job);
		when(settingsService.getJobs(any())).thenReturn(jobs);
		buildHook.postReceive(context, refChanges);
		verify(jenkins, times(0)).triggerJob(job, "param1=value1&param2=value2", null);
	}
}
