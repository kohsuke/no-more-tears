package org.kohsuke.lazylinker;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaticTest {
    @Test
    public void test1() throws Exception {
        new Static().call();
    }
}
