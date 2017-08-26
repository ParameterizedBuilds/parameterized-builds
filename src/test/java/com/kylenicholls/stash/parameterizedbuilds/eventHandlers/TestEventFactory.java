package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;
import com.atlassian.bitbucket.event.pull.*;
import com.atlassian.bitbucket.pull.*;
import com.atlassian.bitbucket.repository.Repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestEventFactory {

    private PullRequestOpenedEvent openedEvent;
    private PullRequestReopenedEvent reopenedEvent;
    private PullRequestRescopedEvent rescopedEvent;
    private PullRequestMergedEvent mergedEvent;
    private PullRequestDeclinedEvent declinedEvent;
    private PullRequestDeletedEvent deletedEvent;
    private AutomaticMergeEvent autoMergeEvent;

    public void setup(Repository repository){
        PullRequest pullRequest = mock(PullRequest.class);
        PullRequestRef prFromRef = mock(PullRequestRef.class);
        PullRequestRef prToRef = mock(PullRequestRef.class);
        PullRequestParticipant author = mock(PullRequestParticipant.class);
        openedEvent = mock(PullRequestOpenedEvent.class);
        reopenedEvent = mock(PullRequestReopenedEvent.class);
        rescopedEvent = mock(PullRequestRescopedEvent.class);
        mergedEvent = mock(PullRequestMergedEvent.class);
        autoMergeEvent = mock(AutomaticMergeEvent.class);
        declinedEvent = mock(PullRequestDeclinedEvent.class);
        deletedEvent = mock(PullRequestDeletedEvent.class);

        String SOURCE_BRANCH = "sourcebranch";
        String DEST_BRANCH = "destbranch";
        String COMMIT = "commithash";
        String PR_TITLE = "prtitle";
        Long PR_ID = 15L;

        when(openedEvent.getPullRequest()).thenReturn(pullRequest);
        when(reopenedEvent.getPullRequest()).thenReturn(pullRequest);
        when(rescopedEvent.getPullRequest()).thenReturn(pullRequest);
        when(mergedEvent.getPullRequest()).thenReturn(pullRequest);
        when(autoMergeEvent.getRepository()).thenReturn(repository);
        when(declinedEvent.getPullRequest()).thenReturn(pullRequest);
        when(deletedEvent.getPullRequest()).thenReturn(pullRequest);
        when(pullRequest.getFromRef()).thenReturn(prFromRef);
        when(pullRequest.getToRef()).thenReturn(prToRef);
        when(pullRequest.getAuthor()).thenReturn(author);
        when(pullRequest.getDescription()).thenReturn(null);
        when(pullRequest.getTitle()).thenReturn(PR_TITLE);
        when(pullRequest.getId()).thenReturn(PR_ID);
        when(prFromRef.getRepository()).thenReturn(repository);
        when(prFromRef.getDisplayId()).thenReturn(SOURCE_BRANCH);
        when(prFromRef.getLatestCommit()).thenReturn(COMMIT);
        when(prToRef.getDisplayId()).thenReturn(DEST_BRANCH);
    }

    public PullRequestMergedEvent getMockedMergeEvent(Repository repository){
        setup(repository);
        return mergedEvent;
    }

    public PullRequestOpenedEvent getMockedOpenedEvent(Repository repository){
        setup(repository);
        return openedEvent;
    }

    public PullRequestReopenedEvent getMockedReopenedEvent(Repository repository){
        setup(repository);
        return reopenedEvent;
    }

    public PullRequestRescopedEvent getMockedRescopedEvent(Repository repository){
        setup(repository);
        return rescopedEvent;
    }

    public PullRequestDeclinedEvent getMockedDeclinedEvent(Repository repository){
        setup(repository);
        return declinedEvent;
    }

    public PullRequestDeletedEvent getMockedDeletedEvent(Repository repository){
        setup(repository);
        return deletedEvent;
    }

    public AutomaticMergeEvent getMockedAutoMergeEvent(Repository repository){
        setup(repository);
        return autoMergeEvent;
    }
}
