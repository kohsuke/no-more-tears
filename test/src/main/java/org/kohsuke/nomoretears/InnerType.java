package org.kohsuke.nomoretears;

import org.junit.Assert;

import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
public class InnerType extends Assert implements Callable {
    public static class Base {
        public String foo(int i) {
            return "base"+i;
        }
    }

    public /*not static*/ class Derived extends Base {
        @Override
        public String foo(int i) {
            return "derived"+i;
        }
    }

    public Object call() throws Exception {
        new Derived();   // constructor call without using the resulting object
        assertEquals("derived3",new Derived().foo(3));
        return null;
    }
}
