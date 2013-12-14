package org.kohsuke.lazylinker;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodHandles.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * Converts regular method invocations and field access into invokeDynamic calls
 * to defer the linkage resolution.
 *
 * @author Kohsuke Kawaguchi
 */
public class MethodTransformer extends MethodVisitor {
    public MethodTransformer(MethodVisitor writer) {
        super(ASM4, writer);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        Handle handle = LINK_METHODS.get(opcode);
        if (handle==null) {
            super.visitMethodInsn(opcode, owner, name, desc);
        } else {
            Type o = Type.getObjectType(owner);

            if (opcode!=INVOKESTATIC) {
                // need to add the 'this' argument to the left.
                // (P1P2P3)R => (ThisP1P2P3)R
                desc = "("+o+desc.substring(1);
            }

            mv.visitInvokeDynamicInsn(name, desc, handle, o.getClassName());
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        Type o = Type.getObjectType(owner);
        Type t = Type.getType(desc);

        switch (opcode) {
        case GETFIELD:
            desc = Type.getMethodDescriptor(t,o);
            break;
        case PUTFIELD:
            desc = Type.getMethodDescriptor(VOID_TYPE,o,t);
            break;
        case GETSTATIC:
            desc = Type.getMethodDescriptor(t);
            break;
        case PUTSTATIC:
            desc = Type.getMethodDescriptor(VOID_TYPE, t);
            break;
        default:
            throw new IllegalArgumentException("Unexpected opcode: "+opcode);
        }

        Handle handle = LINK_METHODS.get(opcode);
        mv.visitInvokeDynamicInsn(name, desc , handle, o.getClassName());
    }



    static final Map<Integer,Handle> LINK_METHODS = new HashMap<Integer,Handle>();

    static final Handle CONSTRUCTOR_LINKER;

    static {
        String sig = MethodType.methodType(CallSite.class,
            Lookup.class, String.class, MethodType.class, String.class).toMethodDescriptorString();
        String linkerName = Type.getInternalName(Linker.class);

        CONSTRUCTOR_LINKER = new Handle(H_INVOKESTATIC, linkerName, "invokeConstructor", sig);

        LINK_METHODS.put(INVOKEVIRTUAL,     new Handle(H_INVOKESTATIC, linkerName, "invokeVirtual", sig));
        LINK_METHODS.put(INVOKESPECIAL,     new Handle(H_INVOKESTATIC, linkerName, "invokeSpecial", sig));
        LINK_METHODS.put(INVOKESTATIC,      new Handle(H_INVOKESTATIC, linkerName, "invokeStatic", sig));
        LINK_METHODS.put(INVOKEINTERFACE,   new Handle(H_INVOKESTATIC, linkerName, "invokeInterface", sig));
        LINK_METHODS.put(GETSTATIC,         new Handle(H_INVOKESTATIC, linkerName, "getStatic", sig));
        LINK_METHODS.put(PUTSTATIC,         new Handle(H_INVOKESTATIC, linkerName, "putStatic", sig));
        LINK_METHODS.put(GETFIELD,          new Handle(H_INVOKESTATIC, linkerName, "getField", sig));
        LINK_METHODS.put(PUTFIELD,          new Handle(H_INVOKESTATIC, linkerName, "putField", sig));
    }
}
