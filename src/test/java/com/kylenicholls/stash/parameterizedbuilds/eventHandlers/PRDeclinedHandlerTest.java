package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
public class PRDeclinedHandlerTest extends PRTestBase {

    @Test
    public void testPRDeclinedAndTriggerIsPRDECLINED() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PRDECLINED" }).build();
        jobs.add(job);
        PullRequestDeclinedEvent declinedEvent = eventFactory.getMockedDeclinedEvent(repository);
        PRDeclinedHandler handler = new PRDeclinedHandler(settingsService, pullRequestService,
                jenkins, declinedEvent, PR_URL);
        PRDeclinedHandler spyHandler = spy(handler);
        doNothing().when(spyHandler).triggerJenkins(any(), any());
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }
}