package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

import org.junit.Test;

public class BitbucketVariablesTest {
	@Test
	public void testAddBranch() {
		String branch = "branch";
		BitbucketVariables actual = new BitbucketVariables.Builder().add("$BRANCH", () -> branch).build();

		assertEquals(1, actual.getVariables().size());
		assertEquals(branch, actual.fetch("$BRANCH"));
	}
}
