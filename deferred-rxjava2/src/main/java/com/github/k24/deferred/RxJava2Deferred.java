package com.github.k24.deferred;

import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.subjects.MaybeSubject;

import javax.annotation.Nonnull;

/**
 * Created by k24 on 2017/02/25.
 */
public class RxJava2Deferred implements Deferred {
    private final Scheduler scheduler;

    public RxJava2Deferred() {
        this(null);
    }

    public RxJava2Deferred(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Nonnull
    @Override
    public <T> Promise<T> promise(@Nonnull PromiseCallback<T> promiseCallback) {
        final MaybeSubject<T> subject = MaybeSubject.create();
        promiseCallback.call(new Result<T>() {
            @Override
            public void resolve(T value) {
                if (value != null) {
                    subject.onSuccess(value);
                }
                subject.onComplete();
            }

            @Override
            public void reject(@Nonnull Throwable reason) {
                subject.onError(reason);
            }
        });
        if (scheduler == null) {
            return new RxJava2Promise<>(subject);
        } else {
            return new RxJava2Promise<>(subject.subscribeOn(scheduler));
        }
    }

    @Nonnull
    @Override
    public <T> Promise<T> resolved(T value) {
        return new RxJava2Promise<>(Maybe.just(value), scheduler);
    }

    @Nonnull
    @Override
    public <T> Promise<T> rejected(@Nonnull Throwable reason) {
        return new RxJava2Promise<>(Maybe.<T>error(reason), scheduler);
    }

    @Nonnull
    @Override
    public <T> DeferredPromise<T> promise() {
        return new RxJava2DeferredPromise<>(scheduler);
    }
}
