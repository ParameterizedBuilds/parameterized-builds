package com.kylenicholls.stash.parameterizedbuilds.conditions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.plugin.web.Condition;
import com.kylenicholls.stash.parameterizedbuilds.helper.SettingsService;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseCondition implements Condition {
    protected static final String REPOSITORY = "repository";
    private static final String REQUEST = "request";
    private static final Pattern REPOREGEX = Pattern.compile(".*?/projects/(.*?)/repos/(.*?)/.*");

    protected SettingsService settingsService;
    private RepositoryService repositoryService;

    public BaseCondition(RepositoryService repositoryService, SettingsService settingsService) {
        this.settingsService = settingsService;
        this.repositoryService = repositoryService;
    }

    @Override
    public void init(Map<String, String> params) {
        // Nothing to do here
    }

    private Repository getRepository(HttpServletRequest request){
        String path = request.getRequestURI();
        Matcher matcher = REPOREGEX.matcher(path);
        if(matcher.matches()){
            String projectKey = matcher.group(1);
            String repoSlug = matcher.group(2);
            return repositoryService.getBySlug(projectKey, repoSlug);
        }
        return null;
    }

    protected Repository getRepository(Map<String, Object> context) {
        final Object obj = context.get(REPOSITORY);
        if (!(obj instanceof Repository)) {
            Object request = context.get(REQUEST);
            if (!(request instanceof HttpServletRequest)) {
                return null;
            }
            return getRepository((HttpServletRequest) request);
        }
        return (Repository) obj;
    }
}
