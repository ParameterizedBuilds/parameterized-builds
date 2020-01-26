package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestReopenedEvent;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PRReopenedHandlerTest extends PRTestBase {

    @Test
    public void testPRReOpenedAndTriggerIsPULLREQUEST() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PRREOPENED" }).build();
        jobs.add(job);
        PullRequestReopenedEvent reopenedEvent = eventFactory.getMockedReopenedEvent(repository);
        PRReopenedHandler handler = new PRReopenedHandler(settingsService, pullRequestService,
                jenkins, reopenedEvent, PR_URL);
        handler.run();
        PRReopenedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }
}