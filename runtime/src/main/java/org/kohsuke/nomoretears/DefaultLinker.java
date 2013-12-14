package org.kohsuke.nomoretears;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

/**
 * {@link Linker} that resolves to the original target specified in the code.
 *
 * @author Kohsuke Kawaguchi
 */
public class DefaultLinker extends Linker {
    @Override
    public CallSite invokeVirtual(Lookup caller, String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findVirtual(owner, methodName, signature));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite invokeStatic(Lookup caller, String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findStatic(owner, methodName, signature));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite invokeInterface(Lookup caller, String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findVirtual(owner, methodName, signature));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite invokeSpecial(Lookup caller, String methodName, MethodType signature, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findSpecial(owner, methodName, signature, caller.lookupClass()));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite invokeConstructor(Lookup caller, MethodType signature, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findConstructor(owner, signature));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite getField(Lookup caller, String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findGetter(owner, fieldName, fieldType));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite putField(Lookup caller, String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findSetter(owner, fieldName, fieldType));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite getStatic(Lookup caller, String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findStaticGetter(owner, fieldName, fieldType));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public CallSite putStatic(Lookup caller, String fieldName, Class<?> fieldType, Class<?> owner) throws ReflectiveOperationException {
        try {
            return new ConstantCallSite(caller.findStaticSetter(owner, fieldName, fieldType));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
