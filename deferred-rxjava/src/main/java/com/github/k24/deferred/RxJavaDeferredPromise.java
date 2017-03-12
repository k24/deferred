package com.github.k24.deferred;

import rx.Scheduler;
import rx.subjects.AsyncSubject;

import javax.annotation.Nonnull;

/**
 * Created by k24 on 2017/03/01.
 */
public class RxJavaDeferredPromise<T> implements Deferred.DeferredPromise<T> {
    private final AsyncSubject<T> subject;
    private final RxJavaPromise<T> promise;

    public RxJavaDeferredPromise() {
        this(AsyncSubject.<T>create());
    }

    public RxJavaDeferredPromise(Scheduler scheduler) {
        this(AsyncSubject.<T>create(), scheduler);
    }

    public RxJavaDeferredPromise(AsyncSubject<T> subject) {
        this(subject, null);
    }

    public RxJavaDeferredPromise(AsyncSubject<T> subject, Scheduler scheduler) {
        this.subject = subject;
        if (scheduler == null) {
            promise = new RxJavaPromise<>(subject.single().toSingle());
        } else {
            promise = new RxJavaPromise<>(subject.subscribeOn(scheduler).single().toSingle());
        }
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
    public T waitAndGet() throws InterruptedException {
        return promise.waitAndGet();
    }

    @Override
    public boolean isPending() {
        return !subject.hasCompleted() && !subject.hasThrowable();
    }

    @Override
    public void resolve(T value) {
        subject.onNext(value);
        subject.onCompleted();
    }

    @Override
    public void reject(@Nonnull Throwable reason) {
        subject.onError(reason);
    }
}
