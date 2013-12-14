Background
=============

In a modular Java program or in a large Java project that has lots of dependencies, you often end up
a version of library that's different from the version used to compile the code.

This often results in `LinkageError`, where a method/field that was present when the code was compiled
do not exist any more in the version being loaded at the runtime.
This restriction applies to seemingly trivial safe changes, such as changing the return type of the method
to its subtype.

Previously, the only way to deal with this is not to remove any signatures that matter. In other words,
you count on library/module developers to be more disciplined. Over the time, Java programmers have accepted
this as a way of life, but there are some notorious offenders (Guava and ASM, I'm looking at you.) Besides,
it makes it difficult to evolve code.

That is where this library comes into play.


What is this?
=============
This library comes with two major parts. One is the class file transformation tool, which replaces
every field/method reference/invocation by an `invokedynamic` call.

The second part is the runtime linker, which "links" these `invokedynamic` calls to the target. In most
cases, the referenced target exists in the runtime, so that's what you'll want to link to.

But because you can bring your own `Linker` implementation, this library enables you to relax the linker
resolution to be flexible and relaxed in all sorts of ways. You can convert static field access to static
method access, you can add/remove parameters to method calls, or you can even intercept a call and do
something completely different.

Normally, this kind of lazy resolution requires you to load class files into your own classloader, but
since this library only relies on `invokedynamic`, the resulting code can be used just like a normal jar
file and loaded into any classloader.

So long as you use `ConstantCallSite`, `invokedynamic` does not incur any runtime penalty on
the code that gets executed --- just one time overhead of running custom linking logic when the code is
run for the first time.


How to use this library
========================
To benefit from lazy linking behavior, you must transform class files at the build time. This project
comes with a Maven plugin to simplify this. Add the following fragment in your POM to automatically
post-process class files after compilation:

    <build>
      <plugins>
        <plugin>
          <groupId>org.kohsuke.no-more-tears</groupId>
          <artifactId>no-more-tears-maven-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>process</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

Just to be clear, the idea is to transform your code to protect yourself from libraries that break compatibility,
not to transform library code that might evolve.

You must also add the runtime as your dependency, because the runtime contains the code necessary to
perform linking at runtime.

    <dependency>
      <groupId>org.kohsuke.no-more-tears</groupId>
      <artifactId>no-more-tears-runtime</artifactId>
      <version>...</version>
    </dependency>


Restrictions
============

The class file transformation process rewrites every method call (instance of static, interface or not),
every field access (get or set, static or not), and every object instantiation. It even rewrites the
'super' call of the following form:

    void foo() {
        ...
        super.foo();
    }

Unfortunately, because of the JVM byte code verification requirement, this library cannot rewrite a constructor body
where it calls this/super constructor such as this:

    class Foo extends Bar {
        Foo() {
            super(  // this super call remains statically linked
                Zot.getValue()      // this gets rewritten and gets linked at runtime
            );

            foo();  // this gets rewritten and gets linked at runtime
        }
    }


TODO
====

I intend to gather common patterns for code evolution so that this library comes out-of-the-box with useful
dynamic linking rules.