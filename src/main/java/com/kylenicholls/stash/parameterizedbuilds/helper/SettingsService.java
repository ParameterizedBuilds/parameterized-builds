package com.kylenicholls.stash.parameterizedbuilds.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.util.Operation;
import com.atlassian.bitbucket.user.SecurityService;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;

public class SettingsService {
	private static final String KEY = "com.kylenicholls.stash.parameterized-builds:parameterized-build-hook";
	public static final String JOB_PREFIX = "jobName-";
	public static final String ISTAG_PREFIX = "isTag-";
	public static final String TRIGGER_PREFIX = "triggers-";
	public static final String TOKEN_PREFIX = "token-";
	public static final String PARAM_PREFIX = "buildParameters-";
	public static final String BRANCH_PREFIX = "branchRegex-";
	public static final String PATH_PREFIX = "pathRegex-";
	
	private RepositoryHookService hookService;
	private SecurityService securityService;

	public SettingsService(RepositoryHookService hookService,
			SecurityService securityService) {
		this.hookService = hookService;
		this.securityService = securityService;
	}

	public Settings getSettings(final Repository repository) {

		Settings settings = null;
		try {
			settings = securityService.withPermission(Permission.REPO_ADMIN,
					"Get respository settings").call(
					new Operation<Settings, Exception>() {
						@Override
						public Settings perform() throws Exception {
							return hookService.getSettings(repository, KEY);
						}
					});
		} catch (Exception e) {
			return null;
		}

		return settings;
	}

	public RepositoryHook getHook(final Repository repository) {
		RepositoryHook hook = null;
		try {
			hook = securityService.withPermission(Permission.REPO_ADMIN,
					"Get respository settings").call(
					new Operation<RepositoryHook, Exception>() {
						@Override
						public RepositoryHook perform() throws Exception {
							return hookService.getByKey(repository, KEY);
						}
					});
		} catch (Exception e1) {
			return null;
		}
		return hook;
	}
	
	public List<Job> getJobs(final Map<String, Object> parameterMap) {
		if (parameterMap.keySet().isEmpty()){return null;}
		List<Job> jobsList = new ArrayList<Job>();
		for (String key : parameterMap.keySet()) {
			if (key.startsWith(JOB_PREFIX)) {
				boolean isTag = false;
				Object isTagObj = parameterMap.get(key.replace(JOB_PREFIX, ISTAG_PREFIX));
				if (isTagObj != null){
					isTag = Boolean.parseBoolean(isTagObj.toString());
				}
				Job job = new Job
						.JobBuilder(jobsList.size())
						.jobName(parameterMap.get(key).toString())
						.isTag(isTag)
						.triggers(parameterMap.get(key.replace(JOB_PREFIX, TRIGGER_PREFIX)).toString().split(";"))
						.buildParameters(parameterMap.get(key.replace(JOB_PREFIX, PARAM_PREFIX)).toString())
						.token(parameterMap.get(key.replace(JOB_PREFIX, TOKEN_PREFIX)).toString())
						.branchRegex(parameterMap.get(key.replace(JOB_PREFIX, BRANCH_PREFIX)).toString())
						.pathRegex(parameterMap.get(key.replace(JOB_PREFIX, PATH_PREFIX)).toString())
						.createJob();
				
				jobsList.add(job);
			}
		}
		return jobsList;
	}
}
