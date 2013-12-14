package org.kohsuke.lazylinker;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Arrays.*;

/**
 * Invokedynamic bootstrap methods.
 *
 * @author Kohsuke Kawaguchi
 */
public class LinkerBootstrap {

    public static CallSite invokeVirtual(Lookup caller, String methodName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> o = resolve(caller, owner);
        signature = signature.dropParameterTypes(0,1);  // drop 'this'
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.invokeVirtual(caller, methodName, signature, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link invokeVirtual "+owner+"."+methodName+signature);
    }

    public static CallSite invokeStatic(Lookup caller,  String methodName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> o = resolve(caller, owner);
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.invokeStatic(caller, methodName, signature, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link invokeStatic "+owner+"."+methodName+signature);
    }

    public static CallSite invokeInterface(Lookup caller,  String methodName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> o = resolve(caller, owner);
        signature = signature.dropParameterTypes(0,1);  // drop 'this'
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.invokeInterface(caller, methodName, signature, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link invokeInterface "+owner+"."+methodName+signature);
    }

    public static CallSite invokeSpecial(Lookup caller,  String methodName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> o = resolve(caller, owner);
        signature = signature.dropParameterTypes(0,1);  // drop 'this'
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.invokeSpecial(caller, methodName, signature, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link invokeSpecial "+owner+"."+methodName+signature);
    }

    public static CallSite invokeConstructor(Lookup caller,  String methodName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> o = resolve(caller, owner);
        signature = signature.changeReturnType(void.class);
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.invokeConstructor(caller, signature, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link invokeConstructor "+owner+"."+methodName+signature);

    }

    public static CallSite getField(Lookup caller,  String fieldName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> rt = signature.returnType();
        Class<?> o = resolve(caller, owner);
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.getField(caller, fieldName, rt, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link getField "+owner+"."+fieldName+" "+ rt);
    }

    public static CallSite putField(Lookup caller,  String fieldName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> rt = signature.parameterType(1);
        Class<?> o = resolve(caller, owner);
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.putField(caller, fieldName, rt, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link puttField "+owner+"."+fieldName+" "+ rt);
    }

    public static CallSite getStatic(Lookup caller,  String fieldName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> rt = signature.returnType();
        Class<?> o = resolve(caller, owner);
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.getStatic(caller, fieldName, rt, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link getStatic "+owner+"."+fieldName+" "+ rt);
    }

    public static CallSite putStatic(Lookup caller,  String fieldName, MethodType signature, String owner) throws ReflectiveOperationException {
        Class<?> rt = signature.parameterType(0);
        Class<?> o = resolve(caller, owner);
        for (Linker linker : Linker.LINKERS) {
            CallSite c = linker.putStatic(caller, fieldName, rt, o);
            if (c!=null)    return c;
        }
        throw new LinkageError("Unable to link putStatic "+owner+"."+fieldName+" "+ rt);
    }

    /**
     * Resolves the class name in the context of the caller.
     */
    private static Class<?> resolve(Lookup caller, String owner) throws ClassNotFoundException {
        return caller.lookupClass().getClassLoader().loadClass(owner);
    }
}
