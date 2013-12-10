package org.kohsuke.lazylinker;

import org.junit.Assert;

import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
public class Interface extends Assert implements Callable {
    public interface Animal {
        String bark(int a, long b, char c);
    }

    public class Dog implements Animal {
        @Override
        public String bark(int a, long b, char c) {
            return "bowwow";
        }
    }

    public static class Cat implements Animal {
        @Override
        public String bark(int a, long b, char c) {
            return "meow";
        }
    }

    @Override
    public Object call() throws Exception {
        assertEquals("bowwow",bark(new Dog()));
        assertEquals("meow",bark(new Cat()));
        return null;
    }

    private String bark(Animal a) {
        return a.bark(0,1,'2');
    }
}
