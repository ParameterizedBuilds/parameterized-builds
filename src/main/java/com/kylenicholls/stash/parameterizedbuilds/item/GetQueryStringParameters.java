package com.kylenicholls.stash.parameterizedbuilds.item;

import com.google.common.base.Preconditions;

public class GetQueryStringParameters {
	private String branch;
	private String commit;
	private String prDestination;
	private String repoName;
	private String projectName;

	public String getBranch() {
		return branch;
	}

	public String getCommit() {
		return commit;
	}

	public String getPrDestination() {
		return prDestination;
	}

	public String getRepoName() {
		return repoName;
	}

	public String getProjectName() {
		return projectName;
	}

	public static class Builder {
		private String branch = "";
		private String commit = "";
		private String prDestination = "";
		private String repoName = "";
		private String projectName = "";

		public Builder branch(String branch) {
			Preconditions.checkNotNull(branch);
			this.branch = branch;
			return this;
		}

		public Builder commit(String commit) {
			Preconditions.checkNotNull(commit);
			this.commit = commit;
			return this;
		}

		public Builder prDestination(String prDestination) {
			Preconditions.checkNotNull(prDestination);
			this.prDestination = prDestination;
			return this;
		}

		public Builder repoName(String repoName) {
			Preconditions.checkNotNull(repoName);
			this.repoName = repoName;
			return this;
		}

		public Builder projectName(String projectName) {
			Preconditions.checkNotNull(projectName);
			this.projectName = projectName;
			return this;
		}

		public GetQueryStringParameters build() {
			return new GetQueryStringParameters(this);
		}
	}

	private GetQueryStringParameters(Builder builder) {
		this.branch = builder.branch;
		this.commit = builder.commit;
		this.prDestination = builder.prDestination;
		this.repoName = builder.repoName;
		this.projectName = builder.projectName;
	}
}