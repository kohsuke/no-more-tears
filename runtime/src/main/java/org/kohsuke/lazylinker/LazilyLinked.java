package org.kohsuke.lazylinker;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * This annotation is added after the class transformation to indicate that
 * the class has already been processed for lazy linking.
 *
 * <p>
 * Used for up-to-date check to avoid unnecessary transformations.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(CLASS)
@Target(TYPE)
public @interface LazilyLinked {
}