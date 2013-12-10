package org.kohsuke.lazylinker;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * @author Kohsuke Kawaguchi
 * @goal process
 * @phase process-classes
 * @requiresDependencyResolution runtime
 */
public class ProcessMojo extends AbstractMojo {
    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            process(classesDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to process @WithBridgeMethods",e);
        }
    }

    private void process(File dir) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                process(f);
            else
            if (f.getName().endsWith(".class"))
                try {
                    transform(f);
                } catch (IOException e) {
                    throw new IOException("Failed to process "+f,e);
                }
        }
    }

    private void transform(File f) throws IOException {
        byte[] image;

        try (InputStream in = new FileInputStream(f)) {
            ClassReader cr = new ClassReader(in);
            ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS);
            cr.accept(new ClassTransformer(cw),0);
            image = cw.toByteArray();
        } catch (AlreadyUpToDate _) {
            // no need to process this class. it's already up-to-date.
            return;
        }

        // write it back
        try (OutputStream out = new FileOutputStream(f)) {
            out.write(image);
        }
    }

}
