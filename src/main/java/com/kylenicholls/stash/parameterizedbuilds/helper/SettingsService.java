package com.kylenicholls.stash.parameterizedbuilds.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.Operation;
import com.kylenicholls.stash.parameterizedbuilds.item.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsService {
	private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);
	private static final String KEY = "com.kylenicholls.stash.parameterized-builds:parameterized-build-hook";
	public static final String JOB_PREFIX = "jobName-";
	public static final String ISTAG_PREFIX = "isTag-";
	public static final String TRIGGER_PREFIX = "triggers-";
	public static final String TOKEN_PREFIX = "token-";
	public static final String PARAM_PREFIX = "buildParameters-";
	public static final String BRANCH_PREFIX = "branchRegex-";
	public static final String PATH_PREFIX = "pathRegex-";
	public static final String PERMISSIONS_PREFIX = "requirePermission-";
	public static final String PRDEST_PREFIX = "prDestinationRegex-";

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
							return hookService.getSettings(repository, KEY);
						}
					});
		} catch (Exception e) {
			logger.error("Exception in SettingsService.getSettings: " + e.getMessage(), e);
			return null;
		}

		return settings;
	}

	public RepositoryHook getHook(final Repository repository) {
		RepositoryHook hook = null;
		try {
			hook = securityService.withPermission(Permission.REPO_ADMIN, "Get respository settings")
					.call(new Operation<RepositoryHook, Exception>() {
						@Override
						public RepositoryHook perform() throws Exception {
							return hookService.getByKey(repository, KEY);
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
						.isTag(fetchValue(entry.getKey().replace(JOB_PREFIX, ISTAG_PREFIX),
								parameterMap, false))
						.triggers(parameterMap
								.get(entry.getKey().replace(JOB_PREFIX, TRIGGER_PREFIX)).toString()
								.split(";"))
						.buildParameters(parameterMap
								.get(entry.getKey().replace(JOB_PREFIX, PARAM_PREFIX)).toString())
						.token(parameterMap.get(entry.getKey().replace(JOB_PREFIX, TOKEN_PREFIX))
								.toString())
						.branchRegex(parameterMap
								.get(entry.getKey().replace(JOB_PREFIX, BRANCH_PREFIX)).toString())
						.pathRegex(parameterMap.get(entry.getKey().replace(JOB_PREFIX, PATH_PREFIX))
								.toString())
						.permissions(fetchValue(entry.getKey().replace(JOB_PREFIX, PERMISSIONS_PREFIX),
								parameterMap, "REPO_READ"))
						.prDestRegex(parameterMap.get(entry.getKey().replace(JOB_PREFIX, PRDEST_PREFIX))
								.toString())
						.build();

				jobsList.add(job);
			}
		}
		return jobsList;
	}

	public boolean fetchValue(String attr, Map parameterMap, boolean defaultVal){
		boolean val = defaultVal;
		Object fetchedVal = parameterMap.get(attr);
		if (fetchedVal != null) {
			val = Boolean.parseBoolean(fetchedVal.toString());
		}
		return val;
	}

	public String fetchValue(String attr, Map parameterMap, String defaultVal){
		String val = defaultVal;
		Object fetchedVal = parameterMap.get(attr);
		if (fetchedVal != null) {
			val = fetchedVal.toString();
		}
		return val;
	}
}
