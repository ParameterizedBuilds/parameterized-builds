package com.kylenicholls.stash.parameterizedbuilds.helper;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.scope.*;

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
