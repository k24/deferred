package com.github.k24.deferred;

import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.github.k24.deferred.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by k24 on 2017/02/25.
 */
public class RxJava2PromiseTest {

    public static final int TEST_ITEM = 123;
    public static final String TEST_ITEM_STRING = "123";
    public static final Exception TEST_ERROR = new Exception();

    private RxJava2Promise<Integer> emptyPromise;
    private RxJava2Promise<Integer> intPromise;
    private RxJava2Promise<Integer> errorPromise;

    @Before
    public void setUp() throws Exception {
        emptyPromise = RxJava2Promise.empty();
        intPromise = RxJava2Promise.just(TEST_ITEM);
        errorPromise = RxJava2Promise.error(TEST_ERROR);
    }

    @Test
    public void then_promise() throws Exception {
        Deferred.OnResolvedPromise<Integer, String> onResolvedPromise = new Deferred.OnResolvedPromise<Integer, String>() {
            @Override
            public Deferred.Promise<String> onResolved(Integer value) throws Exception {
                return RxJava2Promise.just(String.valueOf(value));
            }
        };
        Deferred.OnRejectedPromise<String> onRejectedPromise = new Deferred.OnRejectedPromise<String>() {
            @Override
            public Deferred.Promise<String> onRejected(Throwable reason) throws Exception {
                return RxJava2Promise.error(reason);
            }
        };
        assertThat(emptyPromise.then(onResolvedPromise, onRejectedPromise))
                .isNotNull()
                .verify(null);
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
                return RxJava2Promise.just(String.valueOf(value));
            }
        };
        assertThat(emptyPromise.then(onResolvedPromise))
                .isNotNull()
                .verify(null);
        assertThat(intPromise.then(onResolvedPromise))
                .isNotNull()
                .verify(TEST_ITEM_STRING);
    }

    @Test
    public void rescue_promise() throws Exception {
        Deferred.OnRejectedPromise<Integer> onRejectedPromise = new Deferred.OnRejectedPromise<Integer>() {
            @Override
            public Deferred.Promise<Integer> onRejected(Throwable reason) throws Exception {
                return RxJava2Promise.just(999);
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
                .verify(null);
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
                .verify(null);
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
        assertThat(emptyPromise.waitForCompletion(1)).isTrue();
        assertThat(intPromise.waitForCompletion(1)).isTrue();
        // Time out
        final PublishSubject<Object> subject = PublishSubject.create();
        assertThat(new RxJava2Promise<>(subject.singleElement().subscribeOn(Schedulers.newThread())).waitForCompletion(500)).isFalse();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subject.onNext("hoge");
                subject.onComplete();
            }
        }).start();
        assertThat(new RxJava2Promise<>(subject.subscribeOn(Schedulers.newThread()).singleElement()).waitForCompletion(1000)).isTrue();
    }

    @Test
    public void waitAndGet() throws Exception {
        assertThat(emptyPromise.waitAndGet())
                .isNull();
        assertThat(new RxJava2Promise<>(Maybe.just(123)))
                .verify(123);
    }

    @Test
    public void waitAndGet_error() throws Exception {
        assertThat(new RxJava2Promise<>(Maybe.error(new IOException())))
                .assertThrowable()
                .hasCauseInstanceOf(IOException.class);
    }
}