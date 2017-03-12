package com.github.k24.deferred;

import org.junit.Before;
import org.junit.Test;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.github.k24.deferred.Assertions.assertThat;

/**
 * Created by k24 on 2017/03/12.
 */
public class RxJavaDeferredPromiseTest {
    public static final int TEST_ITEM = 123;
    public static final String TEST_ITEM_STRING = "123";
    public static final Exception TEST_ERROR = new Exception();

    private RxJavaDeferredPromise<Integer> emptyPromise;
    private RxJavaDeferredPromise<Integer> intPromise;
    private RxJavaDeferredPromise<Integer> errorPromise;

    @Before
    public void setUp() throws Exception {
        emptyPromise = new RxJavaDeferredPromise<>();
        emptyPromise.resolve(null);
        intPromise = new RxJavaDeferredPromise<>();
        intPromise.resolve(TEST_ITEM);
        errorPromise = new RxJavaDeferredPromise<>();
        errorPromise.reject(TEST_ERROR);
    }

    @Test
    public void then_promise() throws Exception {
        Deferred.OnResolvedPromise<Integer, String> onResolvedPromise = new Deferred.OnResolvedPromise<Integer, String>() {
            @Override
            public Deferred.Promise<String> onResolved(Integer value) throws Exception {
                return RxJavaPromise.just(String.valueOf(value));
            }
        };
        Deferred.OnRejectedPromise<String> onRejectedPromise = new Deferred.OnRejectedPromise<String>() {
            @Override
            public Deferred.Promise<String> onRejected(Throwable reason) throws Exception {
                return RxJavaPromise.error(reason);
            }
        };
        assertThat(emptyPromise.then(onResolvedPromise, onRejectedPromise))
                .isNotNull()
                .verify("null");
        assertThat(intPromise.then(onResolvedPromise, onRejectedPromise))
                .isNotNull()
                .verify(TEST_ITEM_STRING);
        assertThat(errorPromise.then(onResolvedPromise, onRejectedPromise))
                .isNotNull()
                .assertThrowable()
                .hasCause(TEST_ERROR);
    }

    @Test
    public void then_promise_resolved() throws Exception {
        Deferred.OnResolvedPromise<Integer, String> onResolvedPromise = new Deferred.OnResolvedPromise<Integer, String>() {
            @Override
            public Deferred.Promise<String> onResolved(Integer value) throws Exception {
                return RxJavaPromise.just(String.valueOf(value));
            }
        };
        assertThat(emptyPromise.then(onResolvedPromise))
                .isNotNull()
                .verify("null");
        assertThat(intPromise.then(onResolvedPromise))
                .isNotNull()
                .verify(TEST_ITEM_STRING);
    }

    @Test
    public void rescue_promise() throws Exception {
        Deferred.OnRejectedPromise<Integer> onRejectedPromise = new Deferred.OnRejectedPromise<Integer>() {
            @Override
            public Deferred.Promise<Integer> onRejected(Throwable reason) throws Exception {
                return RxJavaPromise.just(999);
            }
        };
        assertThat(emptyPromise.rescue(onRejectedPromise))
                .isNotNull()
                .verify(null);
        assertThat(intPromise.rescue(onRejectedPromise))
                .isNotNull()
                .verify(123);
        assertThat(errorPromise.rescue(onRejectedPromise))
                .isNotNull()
                .verify(999);
    }

    @Test
    public void then() throws Exception {
        Deferred.OnResolved<Integer, String> onResolved = new Deferred.OnResolved<Integer, String>() {
            @Override
            public String onResolved(Integer value) throws Exception {
                return String.valueOf(value);
            }
        };
        Deferred.OnRejected<String> onRejected = new Deferred.OnRejected<String>() {
            @Override
            public String onRejected(@Nonnull Throwable reason) throws Exception {
                return "rejected";
            }
        };
        assertThat(emptyPromise.then(onResolved, onRejected))
                .isNotNull()
                .verify("null");
        assertThat(intPromise.then(onResolved, onRejected))
                .isNotNull()
                .verify("123");
        assertThat(errorPromise.then(onResolved, onRejected))
                .isNotNull()
                .verify("rejected");
    }

    @Test
    public void then_resolved() throws Exception {
        Deferred.OnResolved<Integer, String> onResolved = new Deferred.OnResolved<Integer, String>() {
            @Override
            public String onResolved(Integer value) throws Exception {
                return String.valueOf(value);
            }
        };
        assertThat(emptyPromise.then(onResolved))
                .isNotNull()
                .verify("null");
        assertThat(intPromise.then(onResolved))
                .isNotNull()
                .verify("123");
        assertThat(errorPromise.then(onResolved))
                .isNotNull()
                .assertThrowable()
                .hasCause(TEST_ERROR);
    }

    @Test
    public void rescue() throws Exception {
        Deferred.OnRejected<Integer> onRejected = new Deferred.OnRejected<Integer>() {
            @Override
            public Integer onRejected(@Nonnull Throwable reason) throws Exception {
                return 999;
            }
        };
        assertThat(emptyPromise.rescue(onRejected))
                .isNotNull()
                .verify(null);
        assertThat(intPromise.rescue(onRejected))
                .isNotNull()
                .verify(123);
        assertThat(errorPromise.rescue(onRejected))
                .isNotNull()
                .verify(999);
    }

    @Test
    public void waitForCompletion() throws Exception {
        emptyPromise.waitForCompletion();
        intPromise.waitForCompletion();
        errorPromise.waitForCompletion();
    }

    @Test
    public void waitForCompletion_millis() throws Exception {
        // In time
        org.assertj.core.api.Assertions.assertThat(emptyPromise.waitForCompletion(1)).isTrue();
        org.assertj.core.api.Assertions.assertThat(intPromise.waitForCompletion(1)).isTrue();
        // Time out
        final PublishSubject<Object> subject = PublishSubject.create();
        org.assertj.core.api.Assertions.assertThat(new RxJavaPromise<>(subject.toSingle().subscribeOn(Schedulers.newThread())).waitForCompletion(500)).isFalse();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subject.onNext("hoge");
                subject.onCompleted();
            }
        }).start();
        org.assertj.core.api.Assertions.assertThat(new RxJavaPromise<>(subject.subscribeOn(Schedulers.newThread()).toSingle()).waitForCompletion(1000)).isTrue();
    }

    @Test
    public void waitAndGet() throws Exception {
        org.assertj.core.api.Assertions.assertThat(emptyPromise.waitAndGet())
                .isNull();
        assertThat(new RxJavaPromise<>(Single.just(123)))
                .verify(123);
    }

    @Test
    public void waitAndGet_error() throws Exception {
        assertThat(new RxJavaPromise<>(Single.error(new IOException())))
                .assertThrowable()
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void isPending() throws Exception {
        org.assertj.core.api.Assertions.assertThat(emptyPromise.isPending()).isFalse();
        org.assertj.core.api.Assertions.assertThat(intPromise.isPending()).isFalse();
        org.assertj.core.api.Assertions.assertThat(errorPromise.isPending()).isFalse();
        RxJavaDeferredPromise<Integer> deferredPromise = new RxJavaDeferredPromise<>();
        org.assertj.core.api.Assertions.assertThat(deferredPromise.isPending()).isTrue();
    }
}