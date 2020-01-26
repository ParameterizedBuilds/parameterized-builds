package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestRescopedEvent;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PRDestRescopedHandlerTest extends PRTestBase {

    @Test
    public void testPRTargetRescopedAndTriggerIsPULLREQUEST() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PRDESTRESCOPED" }).build();
        jobs.add(job);
        PullRequestRescopedEvent rescopedEvent = eventFactory.getMockedRescopedEvent(repository);
        PRDestRescopedHandler handler = new PRDestRescopedHandler(settingsService,
                pullRequestService, jenkins, rescopedEvent, PR_URL);
        PRDestRescopedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }
}