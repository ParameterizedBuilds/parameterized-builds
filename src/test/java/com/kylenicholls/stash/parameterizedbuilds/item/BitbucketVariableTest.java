package com.kylenicholls.stash.parameterizedbuilds.item;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class BitbucketVariableTest {

    @Test
    public void testGet() {
        String expected = "test_val";
        BitbucketVariable<String> variable = new BitbucketVariable<>(() -> expected);
        assert variable.getOrCompute() == expected;
    }

    @Test
    public void testOnlyComputeOnce() {

        Object mock_val = mock(Object.class);
        when(mock_val.toString()).thenReturn("first_val").thenReturn("second_val");
        BitbucketVariable<String> variable = new BitbucketVariable<>(() -> mock_val.toString());
        assert variable.getOrCompute() == "first_val";
        assert variable.getOrCompute() == "first_val";
    }

}