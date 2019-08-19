package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;
import com.atlassian.bitbucket.repository.Branch;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PRAutoMergedHandlerTest extends PRTestBase {

    @Test
    public void testPRAutoMergedAndTriggerIsPRAUTOMERGED() throws IOException{
        Job job = jobBuilder.triggers(new String[] { "PRAUTOMERGED" }).build();
        jobs.add(job);
        AutomaticMergeEvent automaticMergeEvent = eventFactory.getMockedAutoMergeEvent(repository);
        Branch branch = mock(Branch.class);
        PRAutoMergedHandler handler = new PRAutoMergedHandler(settingsService, jenkins, automaticMergeEvent, PR_URL, branch);
        PRAutoMergedHandler spyHandler = spy(handler);
        spyHandler.run();

        verify(spyHandler, times(1)).triggerJenkins(eq(job), any());
    }
}