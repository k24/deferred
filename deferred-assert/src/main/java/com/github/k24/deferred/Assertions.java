package com.github.k24.deferred;

/**
 * Created by k24 on 2017/02/25.
 */
public class Assertions {
    public static <T> PromiseAssert<T> assertThat(Deferred.Promise<T> promise) {
        return new PromiseAssert<>(promise);
    }
}
