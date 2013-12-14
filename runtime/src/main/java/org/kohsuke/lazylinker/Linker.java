package org.kohsuke.lazylinker;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Member;

/**
 * Performs the linking.
 *
 * Different call back method is provided for each kind of operation that the lazy-linker can defer the linking.
 *
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
 * <dd>Signature of the method that was specified in the original code before it was modified by lazy-linker.
 *
 * <dt>fieldType
 * <dd>Type of the field in the original code.
 *
 * <dt>owner
 * <dd>Class that the method/field is supposed to belong to.
 * </dl>
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
}