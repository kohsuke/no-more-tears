package org.kohsuke.lazylinker;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * Transforms the entire class file.
 *
 * @author Kohsuke Kawaguchi
 */
public class ClassTransformer extends ClassVisitor {
    public ClassTransformer(ClassVisitor cv) {
        super(ASM4, new ClassAnnotationInjectorImpl(cv));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(TRANSFORMED))
            throw new AlreadyUpToDate();    // no need to process this class
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodTransformer(super.visitMethod(access, name, desc, signature, exceptions));
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
}
