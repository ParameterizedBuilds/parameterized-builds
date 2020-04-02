package com.kylenicholls.stash.parameterizedbuilds.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.atlassian.bitbucket.ServiceException;
import com.atlassian.bitbucket.hook.repository.GetRepositoryHookSettingsRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.hook.repository.RepositoryHookSettings;
import com.atlassian.bitbucket.hook.repository.SetRepositoryHookSettingsRequest;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scope.RepositoryScope;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsBuilder;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Operation;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);
    private static final String KEY = "com.kylenicholls.stash.parameterized-builds:" + 
                                      "parameterized-build-hook";
    public static final String JOB_PREFIX = "jobName-";
    public static final String SERVER_PREFIX = "jenkinsServer-";
    public static final String ISTAG_PREFIX = "isTag-";
    public static final String TRIGGER_PREFIX = "triggers-";
    public static final String TOKEN_PREFIX = "token-";
    public static final String PARAM_PREFIX = "buildParameters-";
    public static final String BRANCH_PREFIX = "branchRegex-";
    public static final String PATH_PREFIX = "pathRegex-";
    public static final String PERMISSIONS_PREFIX = "requirePermission-";
    public static final String PRDEST_PREFIX = "prDestinationRegex-";
    public static final String ISPIPELINE_PREFIX = "isPipeline-";
    public static final String IGNORE_COMMIT_MSG_PREFIX = "ignoreCommitMsg-";
    public static final String IGNORE_COMMITTERS_PREFIX = "ignoreComitters-";

    private RepositoryHookService hookService;
    private SecurityService securityService;

    public SettingsService(RepositoryHookService hookService, SecurityService securityService) {
        this.hookService = hookService;
        this.securityService = securityService;
    }

    public Settings getSettings(final Repository repository) {
        Settings settings = null;
        try {
            settings = securityService
                    .withPermission(Permission.REPO_ADMIN, "Get respository settings")
                    .call(new Operation<Settings, Exception>() {
                        @Override
                        public Settings perform() throws Exception {
                            RepositoryScope scope = new RepositoryScope(repository);
                            GetRepositoryHookSettingsRequest settingsRequest =
                                    new GetRepositoryHookSettingsRequest.Builder(scope, KEY)
                                            .build();
                            return Optional.ofNullable(hookService.getSettings(settingsRequest))
                                    .map(RepositoryHookSettings::getSettings)
                                    .orElse(null);
                        }
                    });
        } catch (Exception e) {
            logger.error("Exception in SettingsService.getSettings: " + e.getMessage(), e);
            return null;
        }

        return settings;
    }

    public void putSettings(Repository repository, Settings settings)
            throws ServiceException {
        securityService
                .withPermission(Permission.REPO_ADMIN, "Get respository settings")
                .call(new Operation<Void, ServiceException>() {
                    @Override
                    public Void perform() throws ServiceException {
                        RepositoryScope scope = new RepositoryScope(repository);
                        SetRepositoryHookSettingsRequest settingsRequest =
                                new SetRepositoryHookSettingsRequest.Builder(scope, KEY)
                                        .settings(settings)
                                        .build();
                        hookService.setSettings(settingsRequest);
                        return null;
                    }
                });
    }

    public SettingsBuilder newSettings(Repository repository){
        return hookService.createSettingsBuilder();
    }

    public RepositoryHook getHook(final Repository repository) {
        RepositoryHook hook = null;
        try {
            hook = securityService.withPermission(Permission.REPO_ADMIN, "Get respository settings")
                    .call(new Operation<RepositoryHook, Exception>() {
                        @Override
                        public RepositoryHook perform() throws Exception {
                            RepositoryScope scope = new RepositoryScope(repository);
                            return hookService.getByKey(scope, KEY);
                        }
                    });
        } catch (Exception e1) {
            logger.error("Exception in SettingsService.getHook: " + e1.getMessage(), e1);
            return null;
        }
        return hook;
    }

    public List<Job> getJobs(final Map<String, Object> parameterMap) {
        if (parameterMap.keySet().isEmpty()) {
            return Collections.emptyList();
        }
        List<Job> jobsList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            if (entry.getKey().startsWith(JOB_PREFIX)) {
                Job job = new Job.JobBuilder(jobsList.size()).jobName(entry.getValue().toString())
                        .jenkinsServer(fetchValue(entry.getKey().replace(JOB_PREFIX, SERVER_PREFIX),
                                parameterMap, ""))
                        .isTag(fetchValue(entry.getKey().replace(JOB_PREFIX, ISTAG_PREFIX),
                                parameterMap, false))
                        .triggers(parameterMap
                                .get(entry.getKey().replace(JOB_PREFIX, TRIGGER_PREFIX)).toString()
                                .replace("pullrequest;", "propened;prreopened;prsourcerescoped;")
                                .split(";"))
                        .buildParameters(parameterMap
                                .get(entry.getKey().replace(JOB_PREFIX, PARAM_PREFIX)).toString())
                        .token(parameterMap.get(entry.getKey().replace(JOB_PREFIX, TOKEN_PREFIX))
                                .toString())
                        .branchRegex(parameterMap
                                .get(entry.getKey().replace(JOB_PREFIX, BRANCH_PREFIX)).toString())
                        .pathRegex(parameterMap.get(entry.getKey().replace(JOB_PREFIX, PATH_PREFIX))
                                .toString())
                        .permissions(fetchValue(entry.getKey()
                                        .replace(JOB_PREFIX, PERMISSIONS_PREFIX),
                                parameterMap, "REPO_READ"))
                        .prDestRegex(fetchValue(entry.getKey().replace(JOB_PREFIX, PRDEST_PREFIX),
                                parameterMap, ""))
                        .isPipeline(fetchValue(entry.getKey()
                                        .replace(JOB_PREFIX, ISPIPELINE_PREFIX),
                                parameterMap, false))
                        .ignoreComitters(fetchValue(entry.getKey()
                                        .replace(JOB_PREFIX, IGNORE_COMMITTERS_PREFIX),
                                parameterMap, ""))
                        .ignoreCommitMsg(fetchValue(entry.getKey()
                                        .replace(JOB_PREFIX, IGNORE_COMMIT_MSG_PREFIX),
                                parameterMap, ""))
                        .build();

                jobsList.add(job);
            }
        }
        return jobsList;
    }

    public List<Job> getJobs(Settings settings){
        return settings.asMap().entrySet().stream()
            .filter(pair -> pair.getKey().startsWith(JOB_PREFIX))
            .map(pair -> pair.getKey().replace(JOB_PREFIX, ""))
            .map(Integer::parseInt)
            .map(jobId -> settingsToJob(jobId, settings))
            .collect(Collectors.toList());
    }

    public List<Job> getJobs(Repository repository){
        return Optional.ofNullable(getSettings(repository))
                .map(this::getJobs)
                .orElse(Collections.emptyList());
    }

    public Job settingsToJob(int jobId, Settings settings){
        try {
            settings.getInt(JOB_PREFIX + jobId);
        } catch (NumberFormatException e){
            return null;
        }

        return new Job.JobBuilder(jobId)
            .jobName(settings.getString(JOB_PREFIX + jobId))
            .jenkinsServer(settings.getString(SERVER_PREFIX + jobId, ""))
            .isTag(settings.getBoolean(ISTAG_PREFIX + jobId, false))
            .triggers(settings.getString(TRIGGER_PREFIX + jobId, "")
                    .replace("pullrequest;", "propened;prreopened;prsourcerescoped;")
                    .split(";")
            )
            .buildParameters(settings.getString(PARAM_PREFIX + jobId, ""))
            .token(settings.getString(TOKEN_PREFIX + jobId, ""))
            .branchRegex(settings.getString(BRANCH_PREFIX + jobId, ""))
            .pathRegex(settings.getString(PATH_PREFIX + jobId, ""))
            .permissions(settings.getString(PERMISSIONS_PREFIX + jobId, "REPO_READ"))
            .prDestRegex(settings.getString(PRDEST_PREFIX + jobId, ""))
            .isPipeline(settings.getBoolean(ISPIPELINE_PREFIX + jobId, false))
            .ignoreComitters(settings.getString(IGNORE_COMMITTERS_PREFIX + jobId, ""))
            .ignoreCommitMsg(settings.getString(IGNORE_COMMIT_MSG_PREFIX + jobId, ""))
            .build();
    }

    public SettingsBuilder addJobToSettings(SettingsBuilder settings, Job job) {
        int jobId = job.getJobId();
        return settings
            .add(JOB_PREFIX + jobId, job.getJobName())
            .add(SERVER_PREFIX + jobId, job.getJenkinsServer())
            .add(ISTAG_PREFIX + jobId, job.getIsTag())
            .add(TRIGGER_PREFIX + jobId, job.getTriggers().stream()
                    .map(Job.Trigger::toString)
                    .collect(Collectors.joining(";"))
            )
            .add(PARAM_PREFIX + jobId, job.getBuildParameters().stream()
                    .map(param -> param.getKey() + "=" + convertParamValue(param.getValue()))
                    .collect(Collectors.joining("\\r\\n"))
            )
            .add(TOKEN_PREFIX + jobId, job.getToken())
            .add(BRANCH_PREFIX + jobId, job.getBranchRegex())
            .add(PATH_PREFIX + jobId, job.getPathRegex())
            .add(PRDEST_PREFIX + jobId, job.getPrDestRegex())
            .add(ISPIPELINE_PREFIX + jobId, job.getIsPipeline())
            .add(IGNORE_COMMITTERS_PREFIX + jobId, job.getIgnoreComitters())
            .add(IGNORE_COMMIT_MSG_PREFIX + jobId, job.getIgnoreCommitMsg());
    }

    public SettingsBuilder addJobToSettings(Settings settings, Job job) {
        SettingsBuilder builder = hookService.createSettingsBuilder().addAll(settings);
        return addJobToSettings(builder, job);
    }

    private String convertParamValue(Object value){
        if (value instanceof List) {
            return ((List<Object>) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";"));  
        } else {
            return value.toString();
        }
    }

    public boolean fetchValue(String attr, Map<String, Object> parameterMap, boolean defaultVal){
        boolean val = defaultVal;
        Object fetchedVal = parameterMap.get(attr);
        if (fetchedVal != null) {
            val = Boolean.parseBoolean(fetchedVal.toString());
        }
        return val;
    }

    public String fetchValue(String attr, Map<String, Object> parameterMap, String defaultVal){
        String val = defaultVal;
        Object fetchedVal = parameterMap.get(attr);
        if (fetchedVal != null) {
            val = fetchedVal.toString();
        }
        return val;
    }
}
