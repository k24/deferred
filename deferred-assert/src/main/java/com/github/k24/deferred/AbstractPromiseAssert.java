package com.github.k24.deferred;

import org.assertj.core.api.AbstractAssert;

/**
 * Created by k24 on 2017/02/25.
 */
public abstract class AbstractPromiseAssert<SELF extends AbstractPromiseAssert<SELF, ACTUAL, ELEMENT, ELEMENT_ASSERT>,
        ACTUAL extends Deferred.Promise<? extends ELEMENT>,
        ELEMENT,
        ELEMENT_ASSERT extends AbstractAssert<ELEMENT_ASSERT, ELEMENT>> extends AbstractAssert<SELF, ACTUAL> {
    protected AbstractPromiseAssert(ACTUAL actual, Class<?> selfType) {
        super(actual, selfType);
    }
}
