package org.jerkar.api.depmanagement;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

import org.jerkar.api.java.JkJavaProcess;
import org.jerkar.api.system.JkLog;
import org.jerkar.api.system.JkProcess;
import org.jerkar.api.utils.JkUtilsFile;
import org.jerkar.api.utils.JkUtilsIterable;

/**
 * Dependency on computed resource. More concretely, this is a file dependency on files that might not
 * be present at the time of the build and that has to be generated. Instances of this class are
 * responsible to generate the missing files. <p>
 *
 * Computed dependencies are instantiated by providing expected files and a {@link Runnable} that
 * generates these files in case one of them misses. <p>
 *
 * This is yet simple but quite powerful mechanism, cause the runnable can be anything as Maven or ANT build
 * of another project, a Jerkar build of another project, ... <p>
 *
 * This is the way for creating multi-projet (and multi-techno if desired) builds.
 *
 */
public class JkComputedDependency implements JkFileDependency {

    private static final Supplier<Iterable<File>> EMPTY_SUPPLIER = () -> new LinkedList<>();

    /**
     * Creates a computed dependency to the specified files and {@link JkProcess} to run for
     * generating them.
     */
    public static final JkComputedDependency of(final JkProcess process, File... files) {
        final List<File> fileSet = JkUtilsIterable.listWithoutDuplicateOf(Arrays.asList(files));
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                process.runSync();
            }

            @Override
            public String toString() {
                return process.toString();
            }
        };
        return new JkComputedDependency(runnable, null, fileSet, EMPTY_SUPPLIER);
    }

    /**
     * Creates a computed dependency to the specified files and the specified {@link Runnable} to run for
     * generating them.
     */
    public static final JkComputedDependency of(Runnable runnable, File... files) {
        final List<File> fileSet = JkUtilsIterable.listWithoutDuplicateOf(Arrays.asList(files));
        return new JkComputedDependency(runnable, null, fileSet, EMPTY_SUPPLIER);
    }

    /**
     * Identical to {@link #of(File, JkJavaProcess, String, String...)} but you specified a set of files
     * instead of a single one.
     */
    public static final JkComputedDependency of(Iterable<File> files, final JkJavaProcess process,
            final String className, final String... args) {
        final List<File> fileSet = JkUtilsIterable.listWithoutDuplicateOf(files);
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                process.runClassSync(className, args);
            }
        };
        return new JkComputedDependency(runnable, null, fileSet, EMPTY_SUPPLIER);
    }

    /**
     * Creates a computed dependency to the specified file and the specified java program to run for
     * generating them.
     */
    public static final JkComputedDependency of(File file, final JkJavaProcess process,
            final String className, final String... args) {
        return of(JkUtilsIterable.setOf(file), process, className, args);
    }

    private static final long serialVersionUID = 1L;

    private final Runnable runnable;

    private final List<File> files;

    private final Supplier<Iterable<File>> extraFileSupplier;

    private final File ideProjectBaseDir; // Helps to generate ide metadata

    /**
     * Constructs a computed dependency to the specified files and the specified {@link Runnable} to run for
     * generating them.
     */
    protected JkComputedDependency(Runnable runnable, File ideProjectBaseDir, List<File> files, Supplier<Iterable<File>> extraFileSupplier)  {
        super();
        this.runnable = runnable;
        this.files = files;
        this.ideProjectBaseDir = ideProjectBaseDir;
        this.extraFileSupplier = extraFileSupplier;
    }

    /**
     * Constructs a computed dependency to the specified files and the specified {@link Runnable} to run for
     * generating them.
     */
    protected JkComputedDependency(Runnable runnable, File ideProjectBaseDir, List<File> files)  {
        this(runnable, ideProjectBaseDir, files, EMPTY_SUPPLIER);
    }

    /**
     * Returns a duplicate of this computed dependency but specifying that it can be replaced by a project dependency in a IDE.
     */
    public JkComputedDependency withIdeProjectBaseDir(File baseDir) {
        return new JkComputedDependency(this.runnable, baseDir, this.files, EMPTY_SUPPLIER);
    }

    /**
     * Returns a duplicate of this computed dependency but with specified extra files supplier. The supplier
     * will provide additional existing files constituting this dependency.
     */
    public JkComputedDependency withIdeProjectBaseDir(Supplier<Iterable<File>> extraFileSupplier) {
        return new JkComputedDependency(this.runnable, this.ideProjectBaseDir, this.files, extraFileSupplier);
    }

    /**
     * Returns <code>true</code> if at least one of these files is missing or one of these directory is empty.
     */
    public final boolean hasMissingFilesOrEmptyDirs() {
        return !missingFilesOrEmptyDirs().isEmpty();
    }



    /**
     * Returns the missing files or empty directory for this dependency.
     */
    public final Set<File> missingFilesOrEmptyDirs() {
        final Set<File> files = new LinkedHashSet<>();
        for (final File file : this.files) {
            if (!file.exists() || (file.isDirectory() && JkUtilsFile.filesOf(file, true).isEmpty())) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Returns <code>true</code> if one of this file or more is located under or below the specified folder.
     * @param folder
     * @return
     */
    public final boolean hasFileWithin(File folder) {
        for (final File file : this.files) {
            if (JkUtilsFile.isAncestor(folder, file)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<File> files() {
        if (this.hasMissingFilesOrEmptyDirs()) {
            JkLog.startHeaded("Building depending project " + this);
            runnable.run();
            JkLog.done();
        }
        final Set<File> missingFiles = this.missingFilesOrEmptyDirs();
        if (!missingFiles.isEmpty()) {
            throw new IllegalStateException("Project " + this + " does not generate "
                    + missingFiles);
        }
        return JkUtilsIterable.concatLists(files, this.extraFileSupplier.get());
    }

    /**
     * If the dependency can be represented as a project dependency in a IDE,
     * this field mentions the root dir of the project.
     */
    public File ideProjectBaseDir() {
        return ideProjectBaseDir;
    }

    @Override
    public String toString() {
        return this.runnable.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JkComputedDependency that = (JkComputedDependency) o;

        return files.equals(that.files);
    }

    @Override
    public int hashCode() {
        return files.hashCode();
    }


}
