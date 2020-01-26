package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PRMergedHandlerTest extends PRTestBase {

    @Test
    public void testPRMergedAndTriggerIsPRMERGED() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PRMERGED" }).build();
        jobs.add(job);
        PullRequestMergedEvent mergedEvent = eventFactory.getMockedMergeEvent(repository);
        PRMergedHandler handler = new PRMergedHandler(settingsService, pullRequestService, jenkins,
                mergedEvent, PR_URL);
        PRMergedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

    @Test
    public void testMergeCommitAdded() throws IOException {
        Job job = jobBuilder.triggers(new String[] { "PRMERGED" }).build();
        jobs.add(job);
        PullRequestMergedEvent mergedEvent = eventFactory.getMockedMergeEvent(repository);
        PRMergedHandler handler = new PRMergedHandler(settingsService, pullRequestService, jenkins,
                mergedEvent, PR_URL);
        PRMergedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }

}