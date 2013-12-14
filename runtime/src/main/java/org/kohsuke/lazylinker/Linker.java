package org.kohsuke.lazylinker;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

/**
 * Invokedynamic bootstrap methods.
 *
 * @author Kohsuke Kawaguchi
 */
public class Linker {
    public static CallSite invokeVirtual(Lookup caller,  String methodName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        signature = signature.dropParameterTypes(0,1);  // drop 'this'
        return new ConstantCallSite(caller.findVirtual(resolve(caller, owner), methodName, signature));
    }

    public static CallSite invokeStatic(Lookup caller,  String methodName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        return new ConstantCallSite(caller.findStatic(resolve(caller, owner), methodName, signature));
    }

    public static CallSite invokeInterface(Lookup caller,  String methodName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        signature = signature.dropParameterTypes(0,1);  // drop 'this'
        return new ConstantCallSite(caller.findVirtual(resolve(caller, owner), methodName, signature));
    }

    public static CallSite invokeSpecial(Lookup caller,  String methodName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        signature = signature.dropParameterTypes(0,1);  // drop 'this'
        return new ConstantCallSite(caller.findSpecial(resolve(caller, owner), methodName, signature, caller.lookupClass()));
    }

    public static CallSite invokeConstructor(Lookup caller,  String methodName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        signature = signature.changeReturnType(void.class);
        return new ConstantCallSite(caller.findConstructor(resolve(caller,owner), signature));
    }


    public static CallSite getField(Lookup caller,  String fieldName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return new ConstantCallSite(caller.findGetter(resolve(caller, owner),fieldName,signature.returnType()));
    }

    public static CallSite putField(Lookup caller,  String fieldName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return new ConstantCallSite(caller.findSetter(resolve(caller, owner), fieldName, signature.parameterType(1)));
    }

    public static CallSite getStatic(Lookup caller,  String fieldName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return new ConstantCallSite(caller.findStaticGetter(resolve(caller, owner), fieldName, signature.returnType()));
    }

    public static CallSite putStatic(Lookup caller,  String fieldName, MethodType signature, String owner) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return new ConstantCallSite(caller.findStaticSetter(resolve(caller, owner), fieldName, signature.parameterType(0)));
    }

    /**
     * Resolves the class name in the context of the caler.
     */
    private static Class<?> resolve(Lookup caller, String owner) throws ClassNotFoundException {
        return caller.lookupClass().getClassLoader().loadClass(owner);
    }
}
