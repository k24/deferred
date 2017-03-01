package com.github.k24.deferred;

import io.reactivex.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.github.k24.deferred.Assertions.assertThat;

/**
 * Created by k24 on 2017/02/25.
 */
public class RxJava2DeferredTest {

    private RxJava2Deferred defaultDeferred;
    private RxJava2Deferred newThreadDeferred;

    @Before
    public void setUp() throws Exception {
        defaultDeferred = new RxJava2Deferred();
        newThreadDeferred = new RxJava2Deferred(Schedulers.newThread());
    }

    @Test
    public void promise() throws Exception {
        assertThat(defaultDeferred.promise()).isNotNull();
        assertThat(newThreadDeferred.promise()).isNotNull();
    }

    @Test
    public void resolved() throws Exception {
        assertThat(defaultDeferred.resolved(100))
                .isNotNull()
                .verify(100);

        assertThat(newThreadDeferred.resolved(100))
                .isNotNull()
                .verify(100);
    }

    @Test
    public void rejected() throws Exception {
        IOException reason = new IOException();
        assertThat(defaultDeferred.rejected(reason))
                .isNotNull()
                .assertThrowable()
                .hasCause(reason);

        assertThat(newThreadDeferred.rejected(reason))
                .isNotNull()
                .assertThrowable()
                .hasCause(reason);
    }

    @Test
    public void promise_callback() throws Exception {
        assertThat(defaultDeferred.promise(new Deferred.PromiseCallback<String>() {
            @Override
            public void call(@Nonnull Deferred.Result<String> result) {
                result.resolve("resolved");
            }
        }))
                .isNotNull()
                .verify("resolved");

        assertThat(newThreadDeferred.promise(new Deferred.PromiseCallback<String>() {
            @Override
            public void call(@Nonnull Deferred.Result<String> result) {
                result.resolve("resolved");
            }
        }))
                .isNotNull()
                .verify("resolved");
    }

}