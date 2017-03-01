package com.github.k24.deferred;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

/**
 * Created by k24 on 2017/02/25.
 */
public class PromiseAssert<ELEMENT> extends AbstractPromiseAssert<PromiseAssert<ELEMENT>, Deferred.Promise<ELEMENT>, ELEMENT, ObjectAssert<ELEMENT>> {

    protected PromiseAssert(Deferred.Promise<ELEMENT> elementPromise) {
        super(elementPromise, PromiseAssert.class);
    }

    public void verify(ELEMENT expected) throws Exception {
        Assertions.assertThat(actual.waitAndGet())
                .isEqualTo(expected);
    }

    public AbstractThrowableAssert assertThrowable() {
        try {
            actual.waitAndGet();
            failWithMessage("No thrown");
            return null; // Not reached here.
        } catch (Exception e) {
            return Assertions.assertThat(e);
        }
    }
}
