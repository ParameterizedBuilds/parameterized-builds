package com.kylenicholls.stash.parameterizedbuilds.ciserver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.kylenicholls.stash.parameterizedbuilds.item.BitbucketVariables;
import com.kylenicholls.stash.parameterizedbuilds.item.JenkinsResponse;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import com.kylenicholls.stash.parameterizedbuilds.item.Server;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class JenkinsConnectionTest {
    private static final String PLUGIN_KEY = "com.kylenicholls.stash.parameterized-builds";
    private static final String USER_SLUG = "slug";
    private static final String PROJECT_KEY = "projkey";
    private Jenkins jenkins;
    private JenkinsConnection jenkinsConnection;
    private PluginSettings pluginSettings;
    private ApplicationUser user;
    private Project project;

    @Before
    public void setup() throws IOException {
        PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
        pluginSettings = mock(PluginSettings.class);
        when(factory.createSettingsForKey(PLUGIN_KEY)).thenReturn(pluginSettings);
        jenkins = new Jenkins(factory);
        jenkinsConnection = new JenkinsConnection(jenkins);

        user = mock(ApplicationUser.class);
        when(user.getSlug()).thenReturn(USER_SLUG);
        project = mock(Project.class);
        when(project.getKey()).thenReturn(PROJECT_KEY);
    }

    @Test
    public void testTriggerJobUseJobServer(){
        String userToken = USER_SLUG + ":token";
        String userCSRF = null;
        Server expected = new Server("http://globalurl", null, user.getDisplayName(), "token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY))
                .thenReturn("token");

        Job job = new Job.JobBuilder(1).jobName("testJob").buildParameters("").branchRegex("")
                .jenkinsServer(PROJECT_KEY).pathRegex("").prDestRegex("").build();
        BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder()
                .add("$TRIGGER", () -> Job.Trigger.ADD.toString())
                .build();
        JenkinsConnection jenkinsSpy = spy(jenkinsConnection);
        jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

        verify(jenkinsSpy, times(1)).sanitizeTrigger("http://globalurl/job/testJob/build", userToken, userCSRF, false);
    }

    @Test
    public void testTriggerJobWithServerContext(){
        String userToken = USER_SLUG + ":token";
        String userCSRF = null;
        Server expected = new Server("http://globalurl/jenkins", null, user.getDisplayName(), "token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY))
                .thenReturn("token");

        Job job = new Job.JobBuilder(1).jobName("testJob").buildParameters("").branchRegex("")
                .jenkinsServer(PROJECT_KEY).pathRegex("").prDestRegex("").build();
        BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder()
                .add("$TRIGGER", () -> Job.Trigger.ADD.toString())
                .build();
        JenkinsConnection jenkinsSpy = spy(jenkinsConnection);
        jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

        verify(jenkinsSpy, times(1)).sanitizeTrigger("http://globalurl/jenkins/job/testJob/build", userToken, userCSRF, false);
    }

    @Test
    public void testTriggerJobUseJobServerGlobal(){
        String userToken = USER_SLUG + ":token";
        String userCSRF = null;
        Server expected = new Server("http://globalurl", null, user.getDisplayName(), "token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY))
                .thenReturn("token");

        Job job = new Job.JobBuilder(1).jobName("testJob").buildParameters("").branchRegex("")
                .jenkinsServer(PROJECT_KEY).pathRegex("").prDestRegex("").build();
        BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder()
                .add("$TRIGGER", () -> Job.Trigger.ADD.toString())
                .build();
        JenkinsConnection jenkinsSpy = spy(jenkinsConnection);
        jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

        verify(jenkinsSpy, times(1)).sanitizeTrigger("http://globalurl/job/testJob/build", userToken, userCSRF, false);
    }

    @Test
    public void testTriggerJobUseProjectServerAndUserToken(){
        String userToken = USER_SLUG + ":token";
        String userCSRF = null;
        Server expected = new Server("http://globalurl", null, user.getDisplayName(), "token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG + "." + PROJECT_KEY))
                .thenReturn("token");

        Job job = new Job.JobBuilder(1).jobName("testJob").buildParameters("").branchRegex("")
                .pathRegex("").prDestRegex("").build();
        BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder()
                .add("$TRIGGER", () -> Job.Trigger.ADD.toString())
                .build();
        JenkinsConnection jenkinsSpy = spy(jenkinsConnection);
        jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

        verify(jenkinsSpy, times(1)).sanitizeTrigger("http://globalurl/job/testJob/build", userToken, userCSRF, false);
    }

    @Test
    public void testTriggerJobUseGlobalJenkinsAndUserToken(){
        String userToken = USER_SLUG + ":token";
        String userCSRF = null;
        Server expected = new Server("http://globalurl", null, user.getDisplayName(), "token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(null);
        when(pluginSettings.get(".jenkinsSettings")).thenReturn(expected.asMap());
        when(pluginSettings.get(".jenkinsUser." + USER_SLUG)).thenReturn("token");

        Job job = new Job.JobBuilder(1).jobName("testJob").buildParameters("").branchRegex("")
                .pathRegex("").prDestRegex("").build();
        BitbucketVariables bitbucketVariables =  new BitbucketVariables.Builder()
                .add("$TRIGGER", () -> Job.Trigger.ADD.toString())
                .build();;
        JenkinsConnection jenkinsSpy = spy(jenkinsConnection);
        jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

        verify(jenkinsSpy, times(1)).sanitizeTrigger("http://globalurl/job/testJob/build", userToken, userCSRF, false);
    }

    @Test
    public void testTriggerJobNoProjectUserSet(){
        String userToken = USER_SLUG + ":token";
        String userCSRF = null;
        when(user.getDisplayName()).thenReturn(USER_SLUG);
        Server expected = new Server("http://globalurl", null, user.getDisplayName(), "token", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());

        Job job = new Job.JobBuilder(1).jobName("testJob").buildParameters("").branchRegex("")
                .pathRegex("").prDestRegex("").build();
        BitbucketVariables bitbucketVariables =  new BitbucketVariables.Builder()
                .add("$TRIGGER", () -> Job.Trigger.ADD.toString())
                .build();;
        JenkinsConnection jenkinsSpy = spy(jenkinsConnection);
        jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

        verify(jenkinsSpy, times(1)).sanitizeTrigger("http://globalurl/job/testJob/build", userToken, userCSRF, true);
    }

    @Test
    public void testTriggerJobNoDefaultUserSet(){
        when(user.getDisplayName()).thenReturn("user");
        Server expected = new Server("http://globalurl", "", "", "", false, false);
        when(pluginSettings.get(".jenkinsSettings." + PROJECT_KEY)).thenReturn(expected.asMap());

        Job job = new Job.JobBuilder(1).jobName("testJob").buildParameters("").branchRegex("")
                .pathRegex("").prDestRegex("").build();
        BitbucketVariables bitbucketVariables = new BitbucketVariables.Builder()
                .add("$TRIGGER", () -> Job.Trigger.ADD.toString())
                .build();
        JenkinsConnection jenkinsSpy = spy(jenkinsConnection);
        jenkinsSpy.triggerJob(PROJECT_KEY, user, job, bitbucketVariables);

        verify(jenkinsSpy, times(1)).sanitizeTrigger("http://globalurl/job/testJob/build", null, null, true);
    }

    @Test
    public void testTriggerJobNoBuildUrl() {
        JenkinsResponse actual = jenkinsConnection.sanitizeTrigger(null, null, null, false);

        assertEquals(true, actual.getError());
        assertEquals(false, actual.getPrompt());
        assertEquals("Jenkins settings are not setup", actual.getMessageText());
    }
}
