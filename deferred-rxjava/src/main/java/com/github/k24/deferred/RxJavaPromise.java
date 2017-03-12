package com.github.k24.deferred;

import rx.Single;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * {@link Deferred.Promise} with RxJava.
 * <p>
 * Created by k24 on 2017/03/01.
 */
@SuppressWarnings("WeakerAccess")
public class RxJavaPromise<T> implements Deferred.Promise<T> {
    private final Single<T> single;

    /**
     * Construct with Single.
     *
     * @param single for Promise operation
     */
    public RxJavaPromise(Single<T> single) {
        this.single = single;
    }

    /**
     * Create with Single.just.
     *
     * @param value for Single.just
     * @param <T>   Type
     * @return an instance with Single.just
     */
    public static <T> RxJavaPromise<T> just(T value) {
        return new RxJavaPromise<>(Single.just(value));
    }

    /**
     * Create with Single null.
     *
     * @param <T> Type
     * @return an instance with Single.just
     */
    public static <T> RxJavaPromise<T> empty() {
        return new RxJavaPromise<>(Single.<T>just(null));
    }

    /**
     * Create with Single.error.
     *
     * @param throwable for error
     * @param <T>       Type
     * @return an instance with Single.error
     */
    public static <T> RxJavaPromise<T> error(Throwable throwable) {
        return new RxJavaPromise<>(Single.<T>error(throwable));
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(final Deferred.OnResolvedPromise<T, R> onResolved, final Deferred.OnRejectedPromise<R> onRejected) {
        Single.Transformer<T, R> onNext = null;
        Single<T> current = this.single;
        if (onResolved != null) {
            onNext = new Single.Transformer<T, R>() {
                @Override
                public Single<R> call(Single<T> tSingle) {
                    return tSingle.flatMap(new Func1<T, Single<R>>() {
                        @Override
                        public Single<R> call(T t) {
                            try {
                                Deferred.Promise<R> promise = onResolved.onResolved(t);
                                return toSingle(promise);
                            } catch (Exception e) {
                                return Single.error(e);
                            }
                        }
                    });
                }
            };
        }

        Single<R> result = null;
        if (onNext != null) {
            result = current.compose(onNext);
        }

        if (onRejected != null) {
            if (result != null) {
                // Twist: onErrorResumeNext -> O<R>
                // Error: throw
                result = result.onErrorResumeNext(new Func1<Throwable, Single<? extends R>>() {
                    @Override
                    public Single<? extends R> call(Throwable throwable) {
                        try {
                            Deferred.Promise<R> promise = onRejected.onRejected(throwable);
                            return toSingle(promise);
                        } catch (Exception e) {
                            return Single.error(e);
                        }
                    }
                });
            } else {
                // Twist: onErrorResumeNext -> O<T> : map -> O<R>
                //noinspection unchecked
                result = Single.class.cast(current.onErrorResumeNext(new Func1<Throwable, Single<? extends T>>() {
                    @Override
                    public Single<? extends T> call(final Throwable throwable) {
                        try {
                            //noinspection unchecked
                            return Single.class.cast(toSingle(onRejected.onRejected(throwable)));
                        } catch (Exception e) {
                            return Single.error(e);
                        }
                    }
                }));
            }
        }

        return new RxJavaPromise<>(result == null ? Single.<R>just(null) : result);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(Deferred.OnResolvedPromise<T, R> onResolvedPromise) {
        return then(onResolvedPromise, null);
    }

    @Nonnull
    @Override
    public Deferred.Promise<T> rescue(Deferred.OnRejectedPromise<T> onRejectedPromise) {
        return then(null, onRejectedPromise);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(final Deferred.OnResolved<T, R> onResolved, final Deferred.OnRejected<R> onRejected) {
        Single<T> current = this.single;
        Single<R> result = null;
        if (onResolved != null) {
            result = current.flatMap(new Func1<T, Single<? extends R>>() {
                @Override
                public Single<? extends R> call(final T t) {
                    return Single.fromCallable(new Callable<R>() {
                        @Override
                        public R call() throws Exception {
                            return onResolved.onResolved(t);
                        }
                    });
                }
            });
        }

        if (onRejected != null) {
            if (result != null) {
                // Twist: onErrorResumeNext -> O<R>
                // Error: throw
                result = result.onErrorResumeNext(new Func1<Throwable, Single<? extends R>>() {

                    @Override
                    public Single<? extends R> call(final Throwable throwable) {
                        return Single.fromCallable(new Callable<R>() {
                            @Override
                            public R call() throws Exception {
                                return onRejected.onRejected(throwable);
                            }
                        });
                    }
                });
            } else {
                //noinspection unchecked
                result = Single.class.cast(current.onErrorResumeNext(new Func1<Throwable, Single<? extends T>>() {
                    @Override
                    public Single<? extends T> call(final Throwable throwable) {
                        return Single.fromCallable(new Callable<T>() {
                            @Override
                            public T call() throws Exception {
                                //noinspection unchecked
                                return (T) onRejected.onRejected(throwable);
                            }
                        });
                    }
                }));
            }
        }

        return new RxJavaPromise<>(result == null ? Single.<R>just(null) : result);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(Deferred.OnResolved<T, R> onResolved) {
        return then(onResolved, null);
    }

    @Nonnull
    @Override
    public Deferred.Promise<T> rescue(Deferred.OnRejected<T> onRejected) {
        return then(null, onRejected);
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        try {
            single.toCompletable().await();
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public boolean waitForCompletion(long millis) throws InterruptedException {
        try {
            return single.toCompletable().await(millis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public T waitAndGet() throws InterruptedException {
        try {
            return single.toBlocking().value();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private static <R> Single<R> toSingle(Deferred.Promise<R> promise) {
        if (promise instanceof RxJavaPromise) {
            return ((RxJavaPromise<R>) promise).single;
        }
        final PublishSubject<R> subject = PublishSubject.create();
        promise.then(new Deferred.OnResolvedPromise<R, R>() {
            @Override
            public Deferred.Promise<R> onResolved(R value) throws Exception {
                subject.onNext(value);
                subject.onCompleted();
                return null;
            }
        }, new Deferred.OnRejectedPromise<R>() {
            @Override
            public Deferred.Promise<R> onRejected(Throwable reason) throws Exception {
                subject.onError(reason);
                return null;
            }
        });
        return subject.toSingle();
    }
}
