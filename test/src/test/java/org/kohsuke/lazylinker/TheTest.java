package org.kohsuke.lazylinker;

import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class TheTest {
    @Test
    public void testStatic() throws Exception {
        new Static().call();
    }

    @Test
    public void testVirtual() throws Exception {
        new Virtual().call();
    }

    @Test
    public void testInterface() throws Exception {
        new Interface().call();
    }

    @Test
    public void testField() throws Exception {
        new Field().call();
    }

    @Test
    public void testException() throws Exception {
        new ExceptionTest().call();
    }
}
