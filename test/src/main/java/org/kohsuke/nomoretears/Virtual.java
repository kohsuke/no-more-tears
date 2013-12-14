package org.kohsuke.nomoretears;

import org.junit.Assert;

import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
public class Virtual extends Assert implements Callable {
    public static class Base {
        public String foo(int i) {
            return "base"+i;
        }
    }

    public static class Derived extends Base  {
        @Override
        public String foo(int i) {
            return "derived"+i;
        }
    }

    public Object call() throws Exception {
        assertEquals("derived3",new Derived().foo(3));
        assertEquals("base4",new Base().foo(4));
        return null;
    }
}
