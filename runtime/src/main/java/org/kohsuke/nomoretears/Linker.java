package org.kohsuke.nomoretears;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Member;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Arrays.asList;

/**
 * Performs the linking.
 *
 * <p>
 * The idea of a linker is as follows: normally, java compiler determines the exact method/field that are referenced
 * in the compiled source code, and at runtime, the said method/field must exist in its exact signature.
 * no-more-tears relaxes this constraint. At compile time, the exact signature is computed just like before,
 * but at runtime, {@link Linker} instances get to choose the actual method/field that are linked, based on the
 * signature information calculated at the compile time.
 *
 * <p>
 * This is handy for example to modular systems where the version of the library used for the compilation and
 * the version of the library used at runtime is different. In such environment, the method/field of the exact
 * same signature might not exist, but a lax "linker" can pick up other methods/fields that superseded the original
 * one. (Such as resolving "X Foo.getX()" to "X2 Foo.getX()" provided that X2 extends from X.)
 *
 * <p>
 * To register a linker, add an instance to {@link #LINKERS}. This list by default contains the
 * {@linkplain DefaultLinker default linker}, which tries to link to the method/field of the exact signature.
 * Normally, you should insert your linkers after this default linker.
 *
 * <p>
 * Different call back method is provided for each kind of operation that the no-more-tears can defer the linking.
 * The meaning of the parameters is as follows:
 *
 * <dl>
 * <dt>caller
 * <dd>Provide information about the call site that requires linking. {@link Lookup#lookupClass()} in particular.
 *
 * <dt>methodName/fieldName
 * <dd>Name of the method/field specified in the original code to link to. Equivalent of {@link Member#getName()}
 *
 * <dt>signature
 * <dd>Signature of the method that was specified in the original code before it was modified by no-more-tears.
 *
 * <dt>fieldType
 * <dd>Type of the field in the original code.
 *
 * <dt>owner
 * <dd>Class that the method/field is supposed to belong to.
 * </dl>
 *
 * In any of the method, return <tt>null</tt> to let the other linkers attempt at linking. The first one
 * that returns a non-null method will determine the final outcome of the link.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Linker {
    public abstract CallSite invokeVirtual(Lookup caller,  String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite invokeStatic(Lookup caller,  String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite invokeInterface(Lookup caller,  String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite invokeSpecial(Lookup caller,  String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite invokeConstructor(Lookup caller,  MethodType signature, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite getField(Lookup caller,  String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite putField(Lookup caller,  String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite getStatic(Lookup caller,  String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException;

    public abstract CallSite putStatic(Lookup caller,  String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException;

    public void register() {
        LINKERS.add(this);
    }

    public void unregister() {
        LINKERS.remove(this);
    }

    /**
     * Registered linkers that get consulted.
     */
    public static List<Linker> LINKERS = new CopyOnWriteArrayList<Linker>(asList(new DefaultLinker()));
}