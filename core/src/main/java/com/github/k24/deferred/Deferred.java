package com.github.k24.deferred;

import javax.annotation.Nonnull;

/**
 * Deferred Interface.
 * <p>
 * Created by k24 on 2016/11/03.
 */
@SuppressWarnings("SameParameterValue") // Fix resolved value is always 100...
public interface Deferred {
    /**
     * Promise to defer an operation.
     *
     * @param promiseCallback for deferred
     * @param <T>             type to resolve
     * @return an instance of the promise
     */
    @Nonnull
    <T> Promise<T> promise(@Nonnull PromiseCallback<T> promiseCallback);

    /**
     * Promise for resolved.
     *
     * @param value resolved
     * @param <T>   type to resolve
     * @return an instance of the promise
     */
    @Nonnull
    <T> Promise<T> resolved(T value);

    /**
     * Promise for rejected.
     *
     * @param reason rejected
     * @param <T>    type to resolve
     * @return an instance of the promise
     */
    @Nonnull
    <T> Promise<T> rejected(@Nonnull Throwable reason);

    /**
     * Promise to defer an operation.
     *
     * @param <T> type to resolve
     * @return an instance of the promise
     */
    @Nonnull
    <T> DeferredPromise<T> promise();

    /**
     * Callback for Promise.
     */
    interface PromiseCallback<R> {
        /**
         * Called when prepared to promise.
         *
         * @param result to pass a value or a reason
         */
        void call(@Nonnull Result<R> result);
    }

    /**
     * Promise as thenable.
     *
     * @param <T> type to resolve
     */
    interface Promise<T> {
        /**
         * Do after completion.
         *
         * @param onResolvedPromise to handle a value when resolved
         * @param onRejectedPromise to handle an error when rejected
         * @return an instance of the promise
         */
        @Nonnull
        <R> Promise<R> then(OnResolvedPromise<T, R> onResolvedPromise, OnRejectedPromise<R> onRejectedPromise);

        /**
         * Do after resolved.
         *
         * @param onResolvedPromise to handle a value when resolved
         * @return an instance of the promise
         */
        @Nonnull
        <R> Promise<R> then(OnResolvedPromise<T, R> onResolvedPromise);

        /**
         * Do after rejected.
         *
         * @param onRejectedPromise to handle an error when rejected
         * @return an instance of the promise
         */
        @Nonnull
        Promise<T> rescue(OnRejectedPromise<T> onRejectedPromise);

        /**
         * Do after completion.
         *
         * @param onResolved to handle a value when resolved
         * @param onRejected to handle an error when rejected
         * @return an instance of the promise
         */
        @Nonnull
        <R> Promise<R> then(OnResolved<T, R> onResolved, OnRejected<R> onRejected);

        /**
         * Do after resolved.
         *
         * @param onResolved to handle a value when resolved
         * @return an instance of the promise
         */
        @Nonnull
        <R> Promise<R> then(OnResolved<T, R> onResolved);

        /**
         * Do after rejected.
         *
         * @param onRejected to handle an error when rejected
         * @return an instance of the promise
         */
        @Nonnull
        Promise<T> rescue(OnRejected<T> onRejected);

        /**
         * Wait for completion.
         *
         * @throws InterruptedException if interrupted.
         */
        void waitForCompletion() throws InterruptedException;

        /**
         * Wait for completion.
         *
         * @param millis to timeout
         * @return true: completed, false: not completed
         * @throws InterruptedException if interrupted
         */
        boolean waitForCompletion(long millis) throws InterruptedException;

        /**
         * Wait and get value.
         *
         * @return value if success
         * @throws Exception if error
         */
        T waitAndGet() throws Exception;
    }

    interface DeferredPromise<T> extends Promise<T>, Result<T> {
        boolean isPending();
    }

    /**
     * @param <T>
     */
    interface Result<T> {
        void resolve(T value);

        void reject(@Nonnull Throwable reason);
    }

    /**
     * Handler on resolved.
     *
     * @param <T> type to resolve
     */
    interface OnResolvedPromise<T, R> {
        /**
         * Called when resolved.
         * <p>
         * Return a value to keep resolved.
         * Or for notifying an error, throw Exception.
         *
         * @param value resolved
         * @return an instance of a value to resolve
         * @throws Exception to reject
         */
        Promise<R> onResolved(T value) throws Exception;
    }

    /**
     * Handler on rejected.
     *
     * @param <R> type to resolve
     */
    interface OnRejectedPromise<R> {
        /**
         * Called when rejected.
         * <p>
         * Rethrow Exception to keep rejected.
         * Or for twisting to success, return a value.
         *
         * @param reason rejected
         * @return an instance of a value to resolve
         * @throws Exception to reject
         */
        Promise<R> onRejected(Throwable reason) throws Exception;
    }

    /**
     * Handler on resolved.
     *
     * @param <T> type to resolve
     */
    interface OnResolved<T, R> {
        /**
         * Called when resolved.
         * <p>
         * Return a value to keep resolved.
         * Or for notifying an error, throw Exception.
         *
         * @param value resolved
         * @return an instance of a value to resolve
         * @throws Exception to reject
         */
        R onResolved(T value) throws Exception;
    }

    /**
     * Handler on rejected.
     *
     * @param <R> type to resolve
     */
    interface OnRejected<R> {
        /**
         * Called when rejected.
         * <p>
         * Rethrow Exception to keep rejected.
         * Or for twisting to success, return a value.
         *
         * @param reason rejected
         * @return an instance of a value to resolve
         * @throws Exception to reject
         */
        R onRejected(@Nonnull Throwable reason) throws Exception;
    }

    /**
     * Factory for Deferred
     */
    interface Factory {
        /**
         * Create Deferred.
         *
         * @return new instance
         */
        Deferred deferred();
    }
}
