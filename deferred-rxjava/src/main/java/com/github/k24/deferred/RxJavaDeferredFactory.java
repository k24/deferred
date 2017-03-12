package com.github.k24.deferred;

import rx.Scheduler;

/**
 * Created by k24 on 2017/03/01.
 */
public class RxJavaDeferredFactory implements Deferred.Factory{
    private final Scheduler scheduler;

    private RxJavaDeferredFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static RxJavaDeferredFactory create() {
        return new RxJavaDeferredFactory(null);
    }

    public static RxJavaDeferredFactory createWithScheduler(Scheduler scheduler) {
        return new RxJavaDeferredFactory(scheduler);
    }

    @Override
    public Deferred deferred() {
        return new RxJavaDeferred(scheduler);
    }
}
