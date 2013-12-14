package org.kohsuke.nomoretears;

import org.junit.Assert;

import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
public class Field extends Assert implements Callable {
    int x;
    String y;

    @Override
    public Object call() throws Exception {
        x = 3;
        assertEquals(3,x);

        y = "str";
        assertEquals("str",y);

        return null;
    }
}
