package org.kohsuke.lazylinker;

/**
 * @author Kohsuke Kawaguchi
 */
public class Static {
    public static int compute(int x) {
        return A+B("four")+x;
    }

    public static int A = 1;

    public static int B(String s) {
        return 2+s.length();
    }
}
