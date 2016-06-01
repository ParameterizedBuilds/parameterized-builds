package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Job {
	private static final String JOB = "/job/";
	private int jobId;
	private String jobName;
	private boolean isTag;
	private List<Trigger> triggers;
	private String token;
	private Map<String, String> buildParameters;
	private String branchRegex;
	private String pathRegex;

	public Job(int jobId, String jobName, boolean isTag, List<Trigger> triggers, String token,
			Map<String, String> buildParameters, String branchRegex, String pathRegex) {
		this.jobId = jobId;
		this.jobName = jobName;
		this.isTag = isTag;
		this.triggers = triggers;
		this.token = token;
		this.buildParameters = buildParameters;
		this.branchRegex = branchRegex;
		this.pathRegex = pathRegex;
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

	public Map<String, String> getBuildParameters() {
		return buildParameters;
	}

	public String getBranchRegex() {
		return branchRegex;
	}

	public String getPathRegex() {
		return pathRegex;
	}

	public static class JobBuilder {
		private int nestedJobId;
		private String nestedJobName;
		private boolean nestedIsTag;
		private List<Trigger> nestedTriggers;
		private String nestedToken;
		private Map<String, String> nestedBuildParameters;
		private String nestedBranchRegex;
		private String nestedPathRegex;

		public JobBuilder(final int jobId) {
			this.nestedJobId = jobId;
		}

		public JobBuilder jobName(String jobName) {
			this.nestedJobName = jobName;
			return this;
		}

		public JobBuilder isTag(boolean isTag) {
			this.nestedIsTag = isTag;
			return this;
		}

		public JobBuilder triggers(String[] triggersAry) {
			List<Trigger> triggers = new ArrayList<Trigger>();
			for (String trig : triggersAry) {
				try {
					triggers.add(Trigger.valueOf(trig.toUpperCase()));
				} catch (IllegalArgumentException e) {
					triggers.add(Trigger.NULL);
				}
			}
			this.nestedTriggers = triggers;
			return this;
		}

		public JobBuilder token(String token) {
			this.nestedToken = token;
			return this;
		}

		public JobBuilder buildParameters(String parameterString) {
			Map<String, String> parameterMap = new LinkedHashMap<String, String>();
			if (!parameterString.isEmpty()) {
				String lines[] = parameterString.split("\\r?\\n");
				for (String line : lines) {
					String[] pair = line.split("=");
					String key = pair[0];
					String value = pair.length > 1 ? pair[1] : "";
					parameterMap.put(key, value);
				}
			}
			this.nestedBuildParameters = parameterMap;
			return this;
		}

		public JobBuilder branchRegex(String branchRegex) {
			this.nestedBranchRegex = branchRegex;
			return this;
		}

		public JobBuilder pathRegex(String pathRegex) {
			this.nestedPathRegex = pathRegex;
			return this;
		}

		public Job createJob() {
			return new Job(nestedJobId, nestedJobName, nestedIsTag, nestedTriggers, nestedToken,
					nestedBuildParameters, nestedBranchRegex, nestedPathRegex);
		}
	}

	public String getQueryString(GetQueryStringParameters parameterObject) {
		String queryParams = "";
		Iterator<Entry<String, String>> it = buildParameters.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
			queryParams += pair.getKey() + "=" + pair.getValue().split(";")[0]
					+ (it.hasNext() ? "&" : "");
			it.remove();
		}

		if (!parameterObject.getBranch().isEmpty()) {
			queryParams = queryParams.replace("$BRANCH", parameterObject.getBranch());
		}
		if (!parameterObject.getCommit().isEmpty()) {
			queryParams = queryParams.replace("$COMMIT", parameterObject.getCommit());
		}
		if (!parameterObject.getPrDestination().isEmpty()) {
			queryParams = queryParams.replace("$PRDESTINATION", parameterObject.getPrDestination());
		}
		if (!parameterObject.getRepoName().isEmpty()) {
			queryParams = queryParams.replace("$REPOSITORY", parameterObject.getRepoName());
		}
		if (!parameterObject.getProjectName().isEmpty()) {
			queryParams = queryParams.replace("$PROJECT", parameterObject.getProjectName());
		}
		return queryParams;
	}
	
	public String buildUrl(boolean useAltUrl, String queryParams, String userToken) {
		String buildUrl = "";
		
		if (userToken == null && this.token != null && !this.token.isEmpty()) {
			if (queryParams.trim().isEmpty()) {
				queryParams = "token=" + this.token;
			} else {
				queryParams += "&token=" + this.token;
			}
		}
		
		if (queryParams.trim().isEmpty()) {
			buildUrl = JOB + this.jobName + "/build";
		} else if (queryParams.contains("token=") && queryParams.split("&").length < 2) {
			if (useAltUrl) {
				buildUrl = "/buildByToken/build?job=" + this.jobName + "&" + queryParams;
			} else {
				buildUrl = JOB + this.jobName + "/build?" + queryParams;
			}
		} else {
			if (useAltUrl && (userToken == null)) {
				buildUrl = "/buildByToken/buildWithParameters?job=" + this.jobName + "&"
						+ queryParams;
			} else {
				buildUrl = JOB + this.jobName + "/buildWithParameters?" + queryParams;
			}
		}
		
		return buildUrl;
	}

	public enum Trigger {
		ADD, PUSH, PULLREQUEST, MANUAL, DELETE, PRMERGED, PRDECLINED, NULL;
	}
}
