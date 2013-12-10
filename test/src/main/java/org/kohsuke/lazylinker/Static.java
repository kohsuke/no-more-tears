package org.kohsuke.lazylinker;

import org.junit.Assert;

import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
public class Static extends Assert implements Callable {
    public static int compute(int x) {
        return A+B("four")+x;
    }

    public static int A = 1;

    public static int B(String s) {
        return 2+s.length();
    }

    @Override
    public Object call() throws Exception {
        assertEquals(A, 1);
        assertEquals(7, B("12345"));
        assertEquals(10, compute(3));

        // assignment
        A = 0;
        assertEquals(0,A);

        return null;
    }
}
