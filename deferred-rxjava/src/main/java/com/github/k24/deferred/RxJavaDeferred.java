package com.github.k24.deferred;

import rx.Scheduler;
import rx.Single;
import rx.subjects.AsyncSubject;

import javax.annotation.Nonnull;

/**
 * Created by k24 on 2017/03/01.
 */
public class RxJavaDeferred implements Deferred {
    private final Scheduler scheduler;

    public RxJavaDeferred() {
        this(null);
    }

    public RxJavaDeferred(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Nonnull
    @Override
    public <T> Promise<T> promise(@Nonnull PromiseCallback<T> promiseCallback) {
        final AsyncSubject<T> subject = AsyncSubject.create();
        promiseCallback.call(new Result<T>() {
            @Override
            public void resolve(T value) {
                subject.onNext(value);
                subject.onCompleted();
            }

            @Override
            public void reject(@Nonnull Throwable reason) {
                subject.onError(reason);
            }
        });
        if (scheduler == null) {
            return new RxJavaPromise<>(subject.toSingle());
        } else {
            return new RxJavaPromise<>(subject.subscribeOn(scheduler).toSingle());
        }
    }

    @Nonnull
    @Override
    public <T> Promise<T> resolved(T value) {
        if (scheduler == null) {
            return new RxJavaPromise<>(Single.just(value));
        } else {
            return new RxJavaPromise<>(Single.just(value).subscribeOn(scheduler));
        }
    }

    @Nonnull
    @Override
    public <T> Promise<T> rejected(@Nonnull Throwable reason) {
        if (scheduler == null) {
            return new RxJavaPromise<>(Single.<T>error(reason));
        } else {
            return new RxJavaPromise<>(Single.<T>error(reason).subscribeOn(scheduler));
        }
    }

    @Nonnull
    @Override
    public <T> DeferredPromise<T> promise() {
        return new RxJavaDeferredPromise<>(AsyncSubject.<T>create(), scheduler);
    }
}
