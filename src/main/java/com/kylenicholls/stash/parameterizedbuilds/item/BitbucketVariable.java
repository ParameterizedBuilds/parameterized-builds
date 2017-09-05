package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.function.Supplier;

public final class BitbucketVariable<T> {

    private T value;
    private final Supplier<T> supplier;

    public BitbucketVariable(Supplier<T> supplier){
        this.supplier = supplier;
    }

    public T getOrCompute() {
        final T result = value;
        return result == null ? compute() : result;
    }

    private T compute() {
        try {
            value = supplier.get();
        } catch (NullPointerException e) {
            // this is probably due to a nested get returning null so refuse to resolve the variable
            return null;
        }
        return value;
    }
}
