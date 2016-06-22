package com.kylenicholls.stash.parameterizedbuilds.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

public class Job {
	private static final Logger logger = LoggerFactory.getLogger(Job.class);
	private final int jobId;
	private final String jobName;
	private final boolean isTag;
	private final List<Trigger> triggers;
	private final String token;
	private final List<Entry<String, Object>> buildParameters;
	private final String branchRegex;
	private final String pathRegex;

	private Job(JobBuilder builder) {
		this.jobId = builder.jobId;
		this.jobName = builder.jobName;
		this.isTag = builder.isTag;
		this.triggers = builder.triggers;
		this.token = builder.token;
		this.buildParameters = builder.buildParameters;
		this.branchRegex = builder.branchRegex;
		this.pathRegex = builder.pathRegex;
	}

	public int getJobId() {
		return jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public boolean getIsTag() {
		return isTag;
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public String getToken() {
		return token;
	}

	public List<Entry<String, Object>> getBuildParameters() {
		return buildParameters;
	}

	public String getBranchRegex() {
		return branchRegex;
	}

	public String getPathRegex() {
		return pathRegex;
	}

	public Map<String, Object> asMap(BitbucketVariables bitbucketVariables) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", jobId);
		map.put("jobName", jobName);
		List<Map<String, Object>> parameterMap = new ArrayList<>();
		for (Entry<String, Object> parameter : buildParameters) {
			Object value = parameter.getValue();
			for (Entry<String, String> variable : bitbucketVariables.getVariables()) {
				if (parameter.getValue() instanceof String) {
					value = value.toString().replace(variable.getKey(), variable.getValue());
				}
			}
			Map<String, Object> mapped = new HashMap<>();
			mapped.put(parameter.getKey(), value);
			parameterMap.add(mapped);
		}
		map.put("buildParameters", parameterMap);
		return map;
	}

	public static class JobBuilder {
		private final int jobId;
		private String jobName;
		private boolean isTag;
		private List<Trigger> triggers;
		private String token;
		private List<Entry<String, Object>> buildParameters;
		private String branchRegex;
		private String pathRegex;

		public JobBuilder(int jobId) {
			this.jobId = jobId;
		}

		public JobBuilder jobName(String jobName) {
			this.jobName = jobName;
			return this;
		}

		public JobBuilder isTag(boolean isTag) {
			this.isTag = isTag;
			return this;
		}

		public JobBuilder triggers(String[] triggersAry) {
			List<Trigger> triggers = new ArrayList<>();
			for (String trig : triggersAry) {
				try {
					triggers.add(Trigger.valueOf(trig.toUpperCase()));
				} catch (IllegalArgumentException e) {
					logger.error("IllegalArgumentException in Job.triggers: " + e.getMessage(), e);
					triggers.add(Trigger.NULL);
				}
			}
			this.triggers = triggers;
			return this;
		}

		public JobBuilder token(String token) {
			this.token = token;
			return this;
		}

		public JobBuilder buildParameters(String parameterString) {
			List<Entry<String, Object>> parameterList = new ArrayList<>();
			if (!parameterString.isEmpty()) {
				String[] lines = parameterString.split("\\r?\\n");
				for (String line : lines) {
					String[] pair = line.split("=");
					String key = pair[0];
					if (pair.length > 1) {
						if (pair[1].split(";").length > 1) {
							parameterList
									.add(new SimpleEntry<String, Object>(key, pair[1].split(";")));
						} else {
							if (pair[1].matches("true|false")) {
								parameterList.add(new SimpleEntry<String, Object>(key,
										Boolean.parseBoolean(pair[1])));
							} else {
								parameterList.add(new SimpleEntry<String, Object>(key, pair[1]));
							}
						}
					} else {
						parameterList.add(new SimpleEntry<String, Object>(key, ""));
					}
				}
			}
			this.buildParameters = parameterList;
			return this;
		}

		public JobBuilder branchRegex(String branchRegex) {
			this.branchRegex = branchRegex;
			return this;
		}

		public JobBuilder pathRegex(String pathRegex) {
			this.pathRegex = pathRegex;
			return this;
		}

		public Job build() {
			return new Job(this);
		}
	}

	public String buildUrl(Server jenkinsServer, BitbucketVariables bitbucketVariables,
			boolean useUserToken) {
		if (jenkinsServer == null) {
			return null;
		}

		UriBuilder builder = setUrlPath(jenkinsServer, useUserToken, this.buildParameters
				.size() != 0);

		for (Entry<String, Object> param : this.buildParameters) {
			String key = param.getKey();
			String value;
			if (param.getValue() instanceof String[]) {
				value = ((String[]) param.getValue())[0];
			} else {
				value = param.getValue().toString();
			}
			builder.queryParam(key, value);
		}

		String buildUrl = builder.build().toString();

		for (Entry<String, String> variable : bitbucketVariables.getVariables()) {
			buildUrl = buildUrl.replace(variable.getKey(), variable.getValue());
		}

		return buildUrl;
	}

	public String buildManualUrl(Server jenkinsServer,
			MultivaluedMap<String, String> manualParameters, boolean useUserToken) {
		if (jenkinsServer == null) {
			return null;
		}

		UriBuilder builder = setUrlPath(jenkinsServer, useUserToken, manualParameters.size() > 0);

		for (Entry<String, List<String>> param : manualParameters.entrySet()) {
			builder.queryParam(param.getKey(), param.getValue().get(0));
		}

		return builder.build().toString();
	}

	private UriBuilder setUrlPath(Server jenkinsServer, boolean useUserToken,
			boolean hasParameters) {
		UriBuilder builder = UriBuilder.fromUri(jenkinsServer.getBaseUrl());
		if (useUserToken || !jenkinsServer.getAltUrl()) {
			builder.path("job").path(this.jobName);
		} else {
			builder.path("buildByToken").queryParam("job", this.jobName);
		}

		if (hasParameters) {
			builder.path("buildWithParameters");
		} else {
			builder.path("build");
		}

		if (!useUserToken && this.token != null && !this.token.isEmpty()) {
			builder.queryParam("token", this.token);
		}
		return builder;
	}

	public enum Trigger {
		ADD, PUSH, PULLREQUEST, MANUAL, DELETE, PRMERGED, PRDECLINED, NULL;
	}
}
