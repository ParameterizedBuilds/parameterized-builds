package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.event.pull.PullRequestDeletedEvent;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PRDeletedHandlerTest extends PRTestBase {

    @Test
    public void testPRDeletedAndTriggerIsPRDELETED() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PRDELETED" }).build();
        jobs.add(job);
        PullRequestDeletedEvent deletedEvent = eventFactory.getMockedDeletedEvent(repository);
        PRDeletedHandler handler = new PRDeletedHandler(settingsService, pullRequestService, jenkins, deletedEvent, PR_URL);
        PRDeletedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }
}