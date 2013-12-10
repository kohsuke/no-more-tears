package org.kohsuke.lazylinker;

import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class VirtualTest {
    @Test
    public void test1() throws Exception {
        new Virtual().call();
    }
}
