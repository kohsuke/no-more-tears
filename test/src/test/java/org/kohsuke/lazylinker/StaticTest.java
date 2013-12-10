package org.kohsuke.lazylinker;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaticTest extends Assert {
    @Test
    public void test1() {
        assertEquals(10, Static.compute(3));
    }
}
