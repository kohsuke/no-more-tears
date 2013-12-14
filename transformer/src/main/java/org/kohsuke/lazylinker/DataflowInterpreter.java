package org.kohsuke.lazylinker;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

/**
 * @author Kohsuke Kawaguchi
 */
public class DataflowInterpreter extends SourceInterpreter {
    // see through the load/store instructions
    @Override
    public SourceValue copyOperation(AbstractInsnNode insn, SourceValue value) {
        return value;
    }
}
