package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

public class BitbucketVariables {
	private List<Entry<String, String>> variables;

	private BitbucketVariables(Builder builder) {
		this.variables = builder.variables;
	}

	public List<Entry<String, String>> getVariables() {
		return variables;
	}

	public static class Builder {
		private List<Entry<String, String>> variables;

		public Builder() {
			this.variables = new ArrayList<>();
		}

		public Builder branch(String branch) {
			Preconditions.checkNotNull(branch);
			variables.add(new SimpleEntry<>("$BRANCH", branch));
			return this;
		}

		public Builder commit(String commit) {
			Preconditions.checkNotNull(commit);
			variables.add(new SimpleEntry<>("$COMMIT", commit));
			return this;
		}

		public Builder prDestination(String prDestination) {
			Preconditions.checkNotNull(prDestination);
			variables.add(new SimpleEntry<>("$PRDESTINATION", prDestination));
			return this;
		}

		public Builder repoName(String repoName) {
			Preconditions.checkNotNull(repoName);
			variables.add(new SimpleEntry<>("$REPOSITORY", repoName));
			return this;
		}

		public Builder projectName(String projectName) {
			Preconditions.checkNotNull(projectName);
			variables.add(new SimpleEntry<>("$PROJECT", projectName));
			return this;
		}

		public BitbucketVariables build() {
			return new BitbucketVariables(this);
		}

		public Builder prId(long prId) {
		    Preconditions.checkNotNull(prId);
		    variables.add(new SimpleEntry<>("$PRID", Long.toString(prId)));
		    return this;
		}

		public Builder prAuthor(String prAuthor) {
		    Preconditions.checkNotNull(prAuthor);
		    variables.add(new SimpleEntry<>("$PRAUTHOR", prAuthor));
		    return this;
		}

		public Builder prTitle(String prTitle) {
		    Preconditions.checkNotNull(prTitle);
		    variables.add(new SimpleEntry<>("$PRTITLE", prTitle));
		    return this;
		}

		public Builder prDescription(String prDescription) {
		    Preconditions.checkNotNull(prDescription);
		    variables.add(new SimpleEntry<>("$PRDESCRIPTION", prDescription));
		    return this;
		}

		public Builder prUrl(String prUrl) {
		    Preconditions.checkNotNull(prUrl);
		    variables.add(new SimpleEntry<>("$PRURL", prUrl));
		    return this;
		}

		public Builder url(String url) {
		    Preconditions.checkNotNull(url);
		    variables.add(new SimpleEntry<>("$URL", url));
		    return this;
		}
	}
}
