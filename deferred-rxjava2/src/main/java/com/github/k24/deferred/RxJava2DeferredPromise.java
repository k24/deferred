package com.github.k24.deferred;

import io.reactivex.Scheduler;
import io.reactivex.subjects.MaybeSubject;

import javax.annotation.Nonnull;

/**
 * {@link Deferred.DeferredPromise} with RxJava2.
 * <p>
 * Created by k24 on 2017/02/25.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RxJava2DeferredPromise<T> implements Deferred.DeferredPromise<T> {
    private final MaybeSubject<T> subject;
    private final Deferred.Promise<T> promise;

    /**
     * Construct with default values.
     */
    public RxJava2DeferredPromise() {
        this(MaybeSubject.<T>create(), null);
    }

    /**
     * Construct with Subject.
     *
     * @param subject for {@link RxJava2Promise}
     */
    public RxJava2DeferredPromise(MaybeSubject<T> subject) {
        this(subject, null);
    }

    /**
     * Construct with Scheduler.
     *
     * @param scheduler for {@link RxJava2Promise}
     */
    public RxJava2DeferredPromise(Scheduler scheduler) {
        this(MaybeSubject.<T>create(), scheduler);
    }

    /**
     * Construct with Subject and Scheduler.
     *
     * @param subject   for {@link RxJava2Promise}
     * @param scheduler for {@link RxJava2Promise}
     */
    public RxJava2DeferredPromise(MaybeSubject<T> subject, Scheduler scheduler) {
        this.subject = subject;
        if (scheduler == null) {
            this.promise = new RxJava2Promise<>(subject);
        } else {
            this.promise = new RxJava2Promise<>(subject.subscribeOn(scheduler));
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
