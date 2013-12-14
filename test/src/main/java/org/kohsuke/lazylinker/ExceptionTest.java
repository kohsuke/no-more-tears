package org.kohsuke.lazylinker;

import org.junit.Assert;

import java.util.concurrent.Callable;

/**
 * Make sure the line numbers are preserved.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExceptionTest extends Assert implements Callable {
    @Override
    public Object call() throws Exception {
        try {
            foo();
        } catch (ArithmeticException e) {
            assertTrue(e.getStackTrace()[0].getLineNumber()==29);
            assertTrue(e.getStackTrace()[1].getLineNumber()==25);
        }
        return null;
    }

    void foo() {
        bar();
    }

    void bar() {
        throw new ArithmeticException();
    }
}
