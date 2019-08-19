package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PROpenedHandlerTest extends PRTestBase {

    @Test
    public void testPROpenedAndTriggerIsPULLREQUEST() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PROPENED" }).build();
        jobs.add(job);
        PullRequestOpenedEvent openedEvent = eventFactory.getMockedOpenedEvent(repository);
        PROpenedHandler handler = new PROpenedHandler(settingsService, pullRequestService, jenkins, openedEvent, PR_URL);
        handler.run();
        PROpenedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }
}