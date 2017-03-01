package com.github.k24.deferred;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * Created by k24 on 2017/02/25.
 */
public class RxJava2Promise<T> implements Deferred.Promise<T> {
    private final Maybe<T> maybe;

    public RxJava2Promise(@NonNull Maybe<T> maybe) {
        this(maybe, null);
    }

    public RxJava2Promise(@NonNull Maybe<T> maybe, Scheduler scheduler) {
        if (scheduler == null) {
            this.maybe = maybe;
        } else {
            this.maybe = maybe.subscribeOn(scheduler);
        }
    }

    public static <T> RxJava2Promise<T> empty() {
        return new RxJava2Promise<>(Maybe.<T>empty());
    }

    public static <T> RxJava2Promise<T> just(T item) {
        return new RxJava2Promise<>(Maybe.just(item));
    }

    public static <T> RxJava2Promise<T> error(Throwable throwable) {
        return new RxJava2Promise<>(Maybe.<T>error(throwable));
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(final Deferred.OnResolvedPromise<T, R> onResolvedPromise, final Deferred.OnRejectedPromise<R> onRejectedPromise) {
        MaybeTransformer<T, R> onNext = null;
        Maybe<T> current = this.maybe;
        if (onResolvedPromise != null) {
            onNext = new MaybeTransformer<T, R>() {
                @Override
                public MaybeSource<R> apply(Maybe<T> maybe) {
                    return maybe.flatMap(new Function<T, MaybeSource<R>>() {
                        @Override
                        public MaybeSource<R> apply(@NonNull T t) throws Exception {
                            try {
                                Deferred.Promise<R> promise = onResolvedPromise.onResolved(t);
                                return toMaybe(promise);
                            } catch (Exception e) {
                                return Maybe.error(e);
                            }
                        }
                    });
                }
            };
        }

        Maybe<R> result = null;
        if (onNext != null) {
            result = current.compose(onNext);
        }

        if (onRejectedPromise != null) {
            if (result != null) {
                // Twist: onErrorResumeNext -> O<R>
                // Error: throw
                result = result.onErrorResumeNext(new Function<Throwable, MaybeSource<? extends R>>() {
                    @Override
                    public MaybeSource<? extends R> apply(@NonNull Throwable throwable) throws Exception {
                        Deferred.Promise<R> promise = onRejectedPromise.onRejected(throwable);
                        return toMaybe(promise);
                    }
                });
            } else {
                // Twist: onErrorResumeNext -> O<T> : map -> O<R>
                //noinspection unchecked
                result = Maybe.class.cast(current.onErrorResumeNext(new Function<Throwable, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(@NonNull Throwable throwable) throws Exception {
                        Deferred.Promise<R> promise = onRejectedPromise.onRejected(throwable);
                        //noinspection unchecked
                        return MaybeSource.class.cast(toMaybe(promise));
                    }
                }));
            }
        }

        return new RxJava2Promise<>(result == null ? Maybe.<R>empty() : result);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(Deferred.OnResolvedPromise<T, R> onResolvedPromise) {
        return then(onResolvedPromise, null);
    }

    @Nonnull
    @Override
    public Deferred.Promise<T> rescue(final Deferred.OnRejectedPromise<T> onRejectedPromise) {
        return then(null, onRejectedPromise);
    }

    @Nonnull
    @Override
    public <R> Deferred.Promise<R> then(final Deferred.OnResolved<T, R> onResolved, final Deferred.OnRejected<R> onRejected) {
        Maybe<R> result = null;
        if (onResolved != null) {
            result = maybe.map(new Function<T, R>() {
                @Override
                public R apply(@NonNull T t) throws Exception {
                    return onResolved.onResolved(t);
                }
            });
        }

        if (onRejected != null) {
            if (result != null) {
                result = result.onErrorResumeNext(new Function<Throwable, MaybeSource<? extends R>>() {
                    @Override
                    public MaybeSource<? extends R> apply(@NonNull Throwable throwable) throws Exception {
                        R value = onRejected.onRejected(throwable);
                        return value == null ? Maybe.<R>empty() : Maybe.just(value);
                    }
                });
            } else {
                //noinspection unchecked
                result = Maybe.class.cast(maybe.onErrorResumeNext(new Function<Throwable, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(@NonNull Throwable throwable) throws Exception {
                        R value = onRejected.onRejected(throwable);
                        //noinspection unchecked
                        return Maybe.just((T) value);
                    }
                }));
            }
        }

        return new RxJava2Promise<>(result == null ? Maybe.<R>empty() : result);
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
            maybe.toSingle().toCompletable().blockingAwait();
        } catch (Exception e) {
            // Ignore empty or error
        }
    }

    @Override
    public boolean waitForCompletion(long millis) throws InterruptedException {
        try {
            return maybe.toSingle().toCompletable().blockingAwait(millis, TimeUnit.MILLISECONDS);
        } catch (NoSuchElementException e) {
            return true;
        } catch (Exception e) {
            // Empty or error
            return false;
        }
    }

    @Override
    public T waitAndGet() throws Exception {
        return maybe.blockingGet();
    }

    private static <R> Maybe<R> toMaybe(Deferred.Promise<R> promise) {
        if (promise == null) {
            return Maybe.empty();
        }
        if (promise instanceof RxJava2Promise) {
            return ((RxJava2Promise<R>) promise).maybe;
        }
        final PublishSubject<R> subject = PublishSubject.create();
        promise.then(new Deferred.OnResolved<R, R>() {
            @Override
            public R onResolved(R value) throws Exception {
                subject.onNext(value);
                subject.onComplete();
                return null;
            }
        }, new Deferred.OnRejected<R>() {
            @Override
            public R onRejected(@Nonnull Throwable reason) throws Exception {
                subject.onError(reason);
                return null;
            }
        });
        return subject.singleElement();
    }

    public static <T> Maybe<T> maybe(Deferred.Promise<T> promise) {
        if (promise instanceof RxJava2Promise) {
            RxJava2Promise<T> rxPromise = (RxJava2Promise<T>) promise;
            return rxPromise.maybe;
        }
        if (promise instanceof RxJava2DeferredPromise) {
            RxJava2DeferredPromise<T> rxDeferredPromise = (RxJava2DeferredPromise<T>) promise;
            RxJava2Promise<T> rxPromise = (RxJava2Promise<T>) rxDeferredPromise.promise();
            return rxPromise.maybe;
        }

        return Maybe.empty();
    }
}
