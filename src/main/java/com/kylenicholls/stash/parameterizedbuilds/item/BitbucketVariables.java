package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import java.util.function.Supplier;

public class BitbucketVariables {
	private List<Entry<String, BitbucketVariable<String>>> variables;

	private BitbucketVariables(Builder builder) {
		this.variables = builder.variables;
	}

	public List<Entry<String, BitbucketVariable<String>>> getVariables() {
		return variables;
	}

	public static class Builder {
		private List<Entry<String, BitbucketVariable<String>>> variables;

		public Builder() {
			this.variables = new ArrayList<>();
		}

		public Builder add(String key, Supplier<String> supplier) {
			Preconditions.checkNotNull(key);
			BitbucketVariable<String> variable = new BitbucketVariable<>(supplier);
			variables.add(new SimpleEntry<String, BitbucketVariable<String>>(key, variable));
			return this;
		}

		public BitbucketVariables build() {
			return new BitbucketVariables(this);
		}
	}
}
