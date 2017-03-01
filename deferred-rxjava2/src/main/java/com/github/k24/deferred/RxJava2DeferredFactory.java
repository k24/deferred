package com.github.k24.deferred;

import io.reactivex.Scheduler;

/**
 * Created by k24 on 2017/02/25.
 */
public class RxJava2DeferredFactory implements Deferred.Factory {
    private final Scheduler scheduler;

    public RxJava2DeferredFactory() {
        this(null);
    }

    public RxJava2DeferredFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Deferred deferred() {
        return new RxJava2Deferred(scheduler);
    }
}
