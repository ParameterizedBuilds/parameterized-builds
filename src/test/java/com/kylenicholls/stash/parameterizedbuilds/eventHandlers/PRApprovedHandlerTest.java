package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestParticipantApprovedEvent;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PRApprovedHandlerTest extends PRTestBase {

    @Test
    public void testPRDeclinedAndTriggerIsPRDECLINED() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PRAPPROVED" }).build();
        jobs.add(job);
        PullRequestParticipantApprovedEvent approvedEvent =
                eventFactory.getMockedApprovedEvent(repository);
        PRApprovedHandler handler = new PRApprovedHandler(settingsService, pullRequestService,
                jenkins, approvedEvent, PR_URL);
        PRApprovedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }
}