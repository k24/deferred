package com.github.k24.deferred;

import rx.Scheduler;

/**
 * Factory for Deferred with RxJava.
 * <p>
 * Created by k24 on 2017/03/01.
 */
@SuppressWarnings("unused")
public class RxJavaDeferredFactory implements Deferred.Factory {
    private final Scheduler scheduler;

    private RxJavaDeferredFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Create Factory without Scheduler.
     *
     * @return an instance.
     */
    public static RxJavaDeferredFactory create() {
        return new RxJavaDeferredFactory(null);
    }

    /**
     * Create Factory with Scheduler.
     *
     * @return an instance.
     */
    public static RxJavaDeferredFactory createWithScheduler(Scheduler scheduler) {
        return new RxJavaDeferredFactory(scheduler);
    }

    @Override
    public Deferred deferred() {
        return new RxJavaDeferred(scheduler);
    }
}
