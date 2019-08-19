package com.kylenicholls.stash.parameterizedbuilds.eventHandlers;

import com.atlassian.bitbucket.branch.automerge.AutomaticMergeEvent;
import com.atlassian.bitbucket.commit.MinimalCommit;
import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestDeletedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantApprovedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestReopenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestRescopedEvent;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.pull.PullRequestRef;
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
    private PullRequestParticipantApprovedEvent approvedEvent;

    public void setup(Repository repository){
        PullRequest pullRequest = mock(PullRequest.class);
        PullRequestRef prFromRef = mock(PullRequestRef.class);
        PullRequestRef prToRef = mock(PullRequestRef.class);
        PullRequestParticipant author = mock(PullRequestParticipant.class);
        MinimalCommit mergeCommit = mock(MinimalCommit.class);

        openedEvent = mock(PullRequestOpenedEvent.class);
        reopenedEvent = mock(PullRequestReopenedEvent.class);
        rescopedEvent = mock(PullRequestRescopedEvent.class);
        mergedEvent = mock(PullRequestMergedEvent.class);
        autoMergeEvent = mock(AutomaticMergeEvent.class);
        declinedEvent = mock(PullRequestDeclinedEvent.class);
        deletedEvent = mock(PullRequestDeletedEvent.class);
        approvedEvent = mock(PullRequestParticipantApprovedEvent.class);

        String SOURCE_BRANCH = "sourcebranch";
        String DEST_BRANCH = "destbranch";
        String COMMIT = "commithash";
        String newCommit = "newcommithash";
        String PR_TITLE = "prtitle";
        Long PR_ID = 15L;

        when(openedEvent.getPullRequest()).thenReturn(pullRequest);
        when(reopenedEvent.getPullRequest()).thenReturn(pullRequest);
        when(rescopedEvent.getPullRequest()).thenReturn(pullRequest);
        when(mergedEvent.getPullRequest()).thenReturn(pullRequest);
        when(mergedEvent.getCommit()).thenReturn(mergeCommit);
        when(autoMergeEvent.getRepository()).thenReturn(repository);
        when(declinedEvent.getPullRequest()).thenReturn(pullRequest);
        when(deletedEvent.getPullRequest()).thenReturn(pullRequest);
        when(approvedEvent.getPullRequest()).thenReturn(pullRequest);
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
        when(prToRef.getRepository()).thenReturn(repository);
        when(mergeCommit.getId()).thenReturn(newCommit);
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

    public PullRequestParticipantApprovedEvent getMockedApprovedEvent(Repository repository){
        setup(repository);
        return approvedEvent;
    }

    public AutomaticMergeEvent getMockedAutoMergeEvent(Repository repository){
        setup(repository);
        return autoMergeEvent;
    }
}
