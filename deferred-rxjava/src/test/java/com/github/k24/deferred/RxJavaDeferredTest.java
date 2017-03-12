package com.github.k24.deferred;

import org.junit.Before;
import org.junit.Test;
import rx.schedulers.Schedulers;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.github.k24.deferred.Assertions.assertThat;

/**
 * Created by k24 on 2017/03/12.
 */
public class RxJavaDeferredTest {

    private RxJavaDeferred defaultDeferred;
    private RxJavaDeferred newThreadDeferred;

    @Before
    public void setUp() throws Exception {
        defaultDeferred = new RxJavaDeferred();
        newThreadDeferred = new RxJavaDeferred(Schedulers.newThread());
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