package com.github.k24.deferred;

import io.reactivex.Scheduler;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subjects.Subject;

import javax.annotation.Nonnull;

/**
 * Created by k24 on 2017/02/25.
 */
public class RxJava2DeferredPromise<T> implements Deferred.DeferredPromise<T> {
    private final MaybeSubject<T> subject;
    private final Deferred.Promise<T> promise;

    public RxJava2DeferredPromise() {
        this(MaybeSubject.<T>create(), null);
    }

    public RxJava2DeferredPromise(Subject<T> subject) {
        this(MaybeSubject.<T>create(), null);
    }

    public RxJava2DeferredPromise(Scheduler scheduler) {
        this(MaybeSubject.<T>create(), scheduler);
    }

    private RxJava2DeferredPromise(MaybeSubject<T> subject, Scheduler scheduler) {
        this.subject = subject;
        if (scheduler == null) {
            this.promise = new RxJava2Promise<T>(subject);
        } else {
            this.promise = new RxJava2Promise<T>(subject.subscribeOn(scheduler));
        }
    }

    private RxJava2DeferredPromise(Deferred.Promise<T> promise) {
        this.subject = null;
        this.promise = promise;
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(Deferred.OnResolvedPromise<T, R> onResolvedPromise, Deferred.OnRejectedPromise<R> onRejectedPromise) {
        return promise.then(onResolvedPromise, onRejectedPromise);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(Deferred.OnResolvedPromise<T, R> onResolvedPromise) {
        return promise.then(onResolvedPromise);
    }

    @Nonnull
    @Override
    public Deferred.Promise<T> rescue(Deferred.OnRejectedPromise<T> onRejectedPromise) {
        return promise.rescue(onRejectedPromise);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(Deferred.OnResolved<T, R> onResolved, Deferred.OnRejected<R> onRejected) {
        return promise.then(onResolved, onRejected);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(Deferred.OnResolved<T, R> onResolved) {
        return promise.then(onResolved);
    }

    @Nonnull
    @Override
    public Deferred.Promise<T> rescue(Deferred.OnRejected<T> onRejected) {
        return promise.rescue(onRejected);
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        promise.waitForCompletion();
    }

    @Override
    public boolean waitForCompletion(long millis) throws InterruptedException {
        return promise.waitForCompletion(millis);
    }

    @Override
    public T waitAndGet() throws Exception {
        return promise.waitAndGet();
    }

    @Override
    public boolean isPending() {
        return !subject.hasComplete() && !subject.hasThrowable() && !subject.hasValue();
    }

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

    Deferred.Promise<T> promise() {
        return promise;
    }
}
