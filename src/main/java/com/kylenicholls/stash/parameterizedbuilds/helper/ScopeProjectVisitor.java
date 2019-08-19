package com.kylenicholls.stash.parameterizedbuilds.helper;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.scope.GlobalScope;
import com.atlassian.bitbucket.scope.ProjectScope;
import com.atlassian.bitbucket.scope.RepositoryScope;
import com.atlassian.bitbucket.scope.ScopeVisitor;

public class ScopeProjectVisitor implements ScopeVisitor<Project> {

    public Project visit(RepositoryScope scope){
        return scope.getProject();
    }

    public Project visit(ProjectScope scope){
        return scope.getProject();
    }

    public Project visit(GlobalScope scope){
        return null;
    }
}
