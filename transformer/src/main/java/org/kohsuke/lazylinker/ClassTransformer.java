package org.kohsuke.lazylinker;

import org.kohsuke.lazylinker.asm.Analyzer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * Transforms the entire class file.
 *
 * @author Kohsuke Kawaguchi
 */
public class ClassTransformer extends ClassVisitor {
    private String className;
    public ClassTransformer(ClassVisitor cv) {
        super(ASM4, new ClassAnnotationInjectorImpl(cv));
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        version = Math.min(version,51); // we'll be adding invokeDynamic, so minimum Java7 is required
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(TRANSFORMED))
            throw new AlreadyUpToDate();    // no need to process this class
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodNode(ASM4, access, name, desc, signature, exceptions) {
            @Override
            public void visitEnd() {
                super.visitEnd();

                final boolean inConstructor = name.equals("<init>");
                // set to true when we invoke the super constructor
                // beyond that point, all the method/field instructions can be rewritten
                boolean initializedThis = false;

                try {
                    Analyzer<SourceValue> a = new Analyzer<SourceValue>(new DataflowInterpreter()) {
                        @Override
                        protected SourceValue newThisValue(Type ctype) {
                            return new SourceValue(1,IMPLICIT_THIS);
                        }
                    };
                    Frame<SourceValue>[] frames = a.analyze(className, this);

                    int fidx=0;
                    ListIterator <AbstractInsnNode> itr = this.instructions.iterator();
                    while (itr.hasNext()) {
                        AbstractInsnNode insn =  itr.next();
                        Frame<SourceValue> f = frames[fidx++];
                        int op = insn.getOpcode();

                        if (insn.getType()==AbstractInsnNode.METHOD_INSN) {
                            MethodInsnNode m = (MethodInsnNode) insn;

                            if (inConstructor && op ==INVOKESPECIAL && m.name.equals("<init>")) {
                                SourceValue v = getArgumentValue(f, m, -1);
                                if (v.insns.contains(IMPLICIT_THIS)) {
                                    // super/this constructor call. cannot rewrite this, but this lifts the rewrite restrictions
                                    initializedThis = true;
                                    continue;
                                }
                            }

                            String d = m.desc;
                            Handle handle = LINK_METHODS.get(op);
                            if (handle==null) {
                                // leave this instruction as is
                            } else {
                                Type o = Type.getObjectType(m.owner);

                                // intercept constructor call requires getting rid of NEW
                                if (op==INVOKESPECIAL && m.name.equals("<init>")) {
                                    SourceValue lhs = getArgumentValue(f, m, -1);
                                    if (lhs.insns.size() != 1) {
                                        throw new UnsupportedOperationException("constructor invocation with non-deterministic LHS: "+className+"#"+this.name+this.desc);
                                    }
                                    AbstractInsnNode source = lhs.insns.iterator().next();
                                    if (source.getOpcode()!=NEW) {
                                        throw new UnsupportedOperationException("constructor invocation but LHS isn't from NEW"+className+"#"+this.name+this.desc);
                                    }

                                    // in the signature of <init>, the object to be initialized is in the first argument.
                                    // we change that to the return type.
                                    d = d.substring(0,d.lastIndexOf(')'))+')'+o.getDescriptor();
                                    // JVM doesn't seem to like the name '<init>', so we change it to another name.
                                    InvokeDynamicInsnNode inv = new InvokeDynamicInsnNode("init", d, CONSTRUCTOR_LINKER, o.getClassName());

                                    SourceValue top = getArgumentValue(f, m, -2);
                                    if (top==null) {
                                        // NEW INVOKESPECIAL -> INVOKEDYNAMIC POP
                                        instructions.remove(source);
                                        this.instructions.set(m, inv);
                                        this.instructions.insert(inv,new InsnNode(POP));
                                    } else {
                                        // NEW DUP INVOKESPECIAL -> INVOKEDYNAMIC
                                        AbstractInsnNode n = source.getNext();
                                        if (n.getOpcode()!=DUP) {
                                            throw new UnsupportedOperationException("NEW followed by "+ n.getOpcode()+" at "+className+"#"+this.name+this.desc);
                                        }
                                        instructions.remove(n);
                                        instructions.remove(source);
                                        instructions.set(m,inv);
                                    }
                                } else {
                                    if (op!=INVOKESTATIC) {
                                        // need to add the 'this' argument to the left.
                                        // (P1P2P3)R => (ThisP1P2P3)R
                                        d = "("+o+d.substring(1);
                                    }

                                    this.instructions.set(m, new InvokeDynamicInsnNode(m.name, d, handle, o.getClassName()));
                                }
                            }
                        }
                        if (insn.getType()==AbstractInsnNode.FIELD_INSN) {
                            FieldInsnNode fi = (FieldInsnNode) insn;
                            Type o = Type.getObjectType(fi.owner);
                            Type t = Type.getType(fi.desc);
                            String desc;

                            switch (op) {
                            case GETFIELD:
                                desc = Type.getMethodDescriptor(t,o);
                                break;
                            case PUTFIELD:
                                if (inConstructor && !initializedThis && f.getStack(f.getStackSize()-2).insns.contains(IMPLICIT_THIS)) {
                                    continue;   // can't rewrite field access before we initialize the 'this' object
                                }
                                desc = Type.getMethodDescriptor(VOID_TYPE,o,t);
                                break;
                            case GETSTATIC:
                                desc = Type.getMethodDescriptor(t);
                                break;
                            case PUTSTATIC:
                                desc = Type.getMethodDescriptor(VOID_TYPE, t);
                                break;
                            default:
                                throw new IllegalArgumentException("Unexpected opcode: "+op);
                            }

                            Handle handle = LINK_METHODS.get(op);
                            this.instructions.set(fi, new InvokeDynamicInsnNode(fi.name, desc, handle, o.getClassName()));
                        }
                    }

                    // write out this method
                    this.accept(ClassTransformer.super.visitMethod(access, name, desc, signature, exceptions.toArray(new String[0])));
                } catch (AnalyzerException e) {
                    throw new Error("Failed to analyze "+className+"#"+name+desc,e);
                }
            }

            /**
             * Gets the {@link SourceValue} on the stack that corresponds to i-th argument.
             * For non-static method, the 'this' reference counts as -1.
             */
            private SourceValue getArgumentValue(Frame<SourceValue> f, MethodInsnNode m, int i) {
                Type[] args = Type.getArgumentTypes(m.desc);
                int pos = f.getStackSize() - args.length + i;
                if (pos>=0)
                    return f.getStack(pos);
                else
                    return null;
            }
        };
    }

    static class ClassAnnotationInjectorImpl extends ClassAnnotationInjector {
        ClassAnnotationInjectorImpl(ClassVisitor cv) {
            super(cv);
        }

        @Override
        protected void emit() {
            AnnotationVisitor av = cv.visitAnnotation(TRANSFORMED, false);
            av.visitEnd();
        }
    }

    private static final String TRANSFORMED = Type.getDescriptor(LazilyLinked.class);

    /**
     * Used to track the uninitialized 'this' value set to the local variable #0 of the constructor call.
     */
    private static final AbstractInsnNode IMPLICIT_THIS = new ImplicitThisInsnNode();

    private static final class ImplicitThisInsnNode extends InsnNode {
        private ImplicitThisInsnNode() {
            super(-1);
        }
    }

    /**
     * From opcode to the bootstrap method of invokeDynamic.
     */
    static final Map<Integer,Handle> LINK_METHODS = new HashMap<>();

    /**
     * Bootstrap method for an object instantiation.
     */
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
