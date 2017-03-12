package com.github.k24.deferred;

import io.reactivex.Scheduler;

/**
 * {@link Deferred.Factory} with RxJava2.
 * <p>
 * Created by k24 on 2017/02/25.
 */
@SuppressWarnings("unused")
public class RxJava2DeferredFactory implements Deferred.Factory {
    private final Scheduler scheduler;

    private RxJava2DeferredFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Create Factory without Scheduler.
     *
     * @return an instance.
     */
    public static RxJava2DeferredFactory create() {
        return new RxJava2DeferredFactory(null);
    }

    /**
     * Create Factory with Scheduler.
     *
     * @return an instance.
     */
    public static RxJava2DeferredFactory createWithScheduler(Scheduler scheduler) {
        return new RxJava2DeferredFactory(scheduler);
    }

    @Override
    public Deferred deferred() {
        return new RxJava2Deferred(scheduler);
    }
}
