package com.kylenicholls.stash.parameterizedbuilds;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.kylenicholls.stash.parameterizedbuilds.ciserver.Jenkins;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class ConditionalBuildHookTest {
	private RefChange refChange;
	private ParameterizedBuildHook buildHook;
	private SettingsService settingsService;
	private CommitService commitService;
	private Jenkins jenkins;
	private String pathRegex;
	private String branchRegex;
	private String branch;
	private Repository repository;
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
	
	@Before
	public void setup() throws Exception {
		refChange = mock(RefChange.class);
		settingsService = mock(SettingsService.class);
		commitService = mock(CommitService.class);
		jenkins = mock(Jenkins.class);
		buildHook = new ParameterizedBuildHook(settingsService, commitService, jenkins);
		
		fileNames.add("path/to/file");
		fileNames.add("foo/bar/file");
		fileNames.add("test3/test4");
		branch = "anewbranch";
		repository = mock(Repository.class);
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
}
