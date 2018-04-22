package com.kylenicholls.stash.parameterizedbuilds.item;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final String permissions;
	private final String prDestRegex;
	private final boolean isPipeline;

	private Job(JobBuilder builder) {
		this.jobId = builder.jobId;
		this.jobName = builder.jobName;
		this.isTag = builder.isTag;
		this.triggers = builder.triggers;
		this.token = builder.token;
		this.buildParameters = builder.buildParameters;
		this.branchRegex = builder.branchRegex;
		this.pathRegex = builder.pathRegex;
		this.permissions = builder.permissions;
		this.prDestRegex = builder.prDestRegex;
		this.isPipeline = builder.isPipeline;
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

	public String getPermissions() {
		return permissions;
	}

	public String getPrDestRegex() {
		return prDestRegex;
	}

	public boolean getIsPipeline() { return isPipeline; }

	public Map<String, Object> asMap(BitbucketVariables bitbucketVariables) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", jobId);
		map.put("jobName", jobName);
		List<Map<String, Object>> parameterMap = new ArrayList<>();
		for (Entry<String, Object> parameter : buildParameters) {
			Object value = parameter.getValue();
			for (String variable : bitbucketVariables.getVariables().keySet()) {
				if (parameter.getValue() instanceof String && value.toString().contains(variable)
						&& bitbucketVariables.fetch(variable) != null) {
					value = value.toString().replace(variable, bitbucketVariables.fetch(variable));
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
		private String permissions;
		private String prDestRegex;
		private boolean isPipeline;

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
			return triggers(triggers);
		}

		public JobBuilder triggers(List<Trigger> triggers) {
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
					String[] pair = line.split("=", 2);
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
			return buildParameters(parameterList);
		}

		public JobBuilder buildParameters(List<Entry<String, Object>> buildParameters) {
			this.buildParameters = buildParameters;
			return this;
		}

		public JobBuilder buildParameters(Map<String, Object> buildParameters) {
			return buildParameters(buildParameters.entrySet().stream().collect(Collectors.toList()));
		}

		public JobBuilder branchRegex(String branchRegex) {
			this.branchRegex = branchRegex;
			return this;
		}

		public JobBuilder pathRegex(String pathRegex) {
			this.pathRegex = pathRegex;
			return this;
		}

		public JobBuilder permissions(String permissions) {
			this.permissions = permissions;
			return this;
		}

		public JobBuilder prDestRegex(String prDestRegex) {
			this.prDestRegex = prDestRegex;
			return this;
		}

		public JobBuilder isPipeline(boolean isPipeline){
			this.isPipeline = isPipeline;
			return this;
		}

		public Job build() {
			return new Job(this);
		}
	}

	public JobBuilder copy(){
		return new JobBuilder(jobId).jobName(jobName).isTag(isTag).triggers(triggers).token(token)
				.buildParameters(buildParameters).branchRegex(branchRegex).pathRegex(pathRegex).permissions(permissions)
				.prDestRegex(prDestRegex).isPipeline(isPipeline);
	}

	public String buildUrl(Server jenkinsServer, BitbucketVariables bitbucketVariables,
			boolean useUserToken) {
		if (jenkinsServer == null) {
			return null;
		}
		Trigger trigger =  Trigger.fromToString(bitbucketVariables.fetch("$TRIGGER"));
		UriBuilder builder = setUrlPath(jenkinsServer, useUserToken, !this.buildParameters.isEmpty(), trigger);

		String buildUrl = builder.build().toString();

		for (String variable : bitbucketVariables.getVariables().keySet()) {
			// only try to replace a variable if it is in the params. This allows optimal use of java 8 lazy initialization
			if (buildUrl.contains(variable) && bitbucketVariables.fetch(variable) != null) {
				try {
					buildUrl = buildUrl.replace(variable, URLEncoder.encode(bitbucketVariables.fetch(variable), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					buildUrl = buildUrl.replace(variable, bitbucketVariables.fetch(variable));
				}
			}
		}

		return buildUrl;
	}

	private UriBuilder setUrlPath(Server jenkinsServer, boolean useUserToken,
			boolean hasParameters, Trigger trigger) {
		UriBuilder builder = UriBuilder.fromUri(jenkinsServer.getBaseUrl());
		String jobBase;
		if ((isPipeline && trigger == Trigger.PULLREQUEST) ||
			  !isPipeline ||
			  trigger.isRefChange()) {
			jobBase = "%s";
		} else {
			jobBase = "%s%s%s";
		}
		hasParameters = !isPipeline || !trigger.isRefChange() ? hasParameters : false;

		if (useUserToken || !jenkinsServer.getAltUrl()) {
			builder.path("job").path(String.format(jobBase, this.jobName, "/job/", "$BRANCH"));
		} else {
			builder.path("buildByToken").queryParam("job", String.format(jobBase, this.jobName, "/", "$BRANCH"));
		}

		if (!useUserToken && this.token != null && !this.token.isEmpty()) {
			builder.queryParam("token", this.token);
		}

		if (hasParameters) {
			builder.path("buildWithParameters");
			appendBuildParams(builder);
		} else {
			builder.path("build");
		}
		return builder;
	}

	private void appendBuildParams(UriBuilder builder){
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
	}

	public enum Trigger {
		ADD, PUSH, PULLREQUEST, MANUAL, DELETE, PRMERGED, PRAUTOMERGED, PRDECLINED, PRDELETED, PRAPPROVED, NULL;

		@Override
		public String toString() {
			switch(this) {
				case ADD: return "REF CREATED";
				case PUSH: return "PUSH EVENT";
				case PULLREQUEST: return "PR OPENED";
				case DELETE: return "REF DELETED";
				case PRMERGED: return "PR MERGED";
				case PRAUTOMERGED: return "AUTO MERGED";
				case PRDECLINED: return "PR DECLINED";
				case PRDELETED: return "PR DELETED";
				case PRAPPROVED: return "PR APPROVED";
				default: return super.toString();
			}
		}

		public Boolean isRefChange(){
			return Stream.of(ADD, PUSH, DELETE).collect(Collectors.toList()).contains(this);
		}

		public static Trigger fromToString(String toString){
			switch(toString) {
				case "REF CREATED": return ADD;
				case "PUSH EVENT": return PUSH;
				case "PR OPENED": return PULLREQUEST;
				case "REF DELETED": return DELETE;
				case "PR MERGED": return PRMERGED;
				case "AUTO MERGED": return PRAUTOMERGED;
				case "PR DECLINED": return PRDECLINED;
				case "PR DELETED": return PRDELETED;
				case "PR APPROVED": return PRAPPROVED;
				default: return NULL;
			}
		}
	}
}
