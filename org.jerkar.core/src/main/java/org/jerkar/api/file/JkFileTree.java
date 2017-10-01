package org.jerkar.api.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jerkar.api.utils.JkUtilsAssert;
import org.jerkar.api.utils.JkUtilsFile;
import org.jerkar.api.utils.JkUtilsIO;
import org.jerkar.api.utils.JkUtilsPath;
import org.jerkar.api.utils.JkUtilsThrowable;

/**
 * Provides a view on files and sub-folders contained in a given directory. A
 * <code>JkFileTree</code> may have some include/exclude filters to include only
 * or exclude some files based on ANT pattern matching. <br/>
 *
 * <p>
 * When speaking about files contained in a {@link JkFileTree}, we mean all
 * files contained in its root directory or sub-directories, matching positively
 * the filter defined on it.
 *
 * @author Jerome Angibaud
 */
public final class JkFileTree  {

    /**
     * Creates a {@link JkFileTree} having the specified root directory.
     */
    @Deprecated
    public static JkFileTree of(File rootDir) {
        return new JkFileTree(rootDir.toPath());
    }

    /**
     * Creates a {@link JkFileTree} having the specified root directory.
     */
    public static JkFileTree of(Path rootDir) {
        return new JkFileTree(rootDir);
    }

    private final Path root;

    private final JkPathFilter filter;

    private JkFileTree(Path rootDir) {
        this(rootDir, JkPathFilter.ACCEPT_ALL);
    }

    private JkFileTree(Path rootDir, JkPathFilter filter) {
        JkUtilsAssert.notNull(rootDir, "Root dir can't be null.");
        JkUtilsAssert.notNull(filter, "ilter can't be null.");
        JkUtilsAssert.isTrue(!Files.exists(rootDir) || Files.isDirectory(rootDir), rootDir + " is not a directory.");
        this.root = rootDir;
        this.filter = filter;
    }

    /**
     * Returns the root directory.
     */
    public Path root() {
        return root;
    }


    /**
     * Returns the filter defined on this {@link JkFileTree}, never <code>null</code>.
     */
    public JkPathFilter filter() {
        return filter;
    }

    // ------------------------- check exists --------------------------------------

    /**
     * Returns if the asScopedDependency directory exists. (Short hand for #asScopedDependency.exists()).
     */
    public boolean exists() {
        return Files.exists(root);
    }

    private JkFileTree createIfNotExist() {
        if (!Files.exists(root)) {
            JkUtilsPath.createDirectories(root);
        }
        return this;
    }


    // ----------------------- iterate over files ---------------------------------------------------

    public Stream<Path> stream(FileVisitOption ...options) {
        if(!exists()) {
            return new LinkedList<Path>().stream();
        }
        final JkPathMatcher matcher = JkPathMatcher.of(filter);
        return JkUtilsPath.walk(root, options).filter(path -> matcher.matches(root.relativize(path)));
    }

    /**
     * Returns absolute path of all files contained in this {@link JkFileTree}.
     * Result does not contains directory entries.
     */
    public List<Path> filesOnlyRelative() {
        return stream()
                .filter(JkPathMatcher.noDirectory())
                .map(path -> root.relativize(path))
                .collect(Collectors.toList());
    }

    /**
     * Returns absolute path of all files contained in this {@link JkFileTree}.
     * Result does not contains directory entries.
     */
    public List<Path> filesOnly() {
        return stream().filter(JkPathMatcher.noDirectory()).collect(Collectors.toList());
    }

    /**
     * Returns the file contained in this {@link JkFileTree}.
     */
    public List<Path> filesAndDirs() {
        return stream().collect(Collectors.toList());
    }

    /**
     * Returns the file contained in this {@link JkFileTree}.
     */
    public List<File> files(boolean includeFolders) {
        if (!root.toFile().exists()) {
            return Collections.emptyList();
        }
        return JkUtilsFile.filesOf(root.toFile(), filter.toFileFilter(root.toFile()), includeFolders);
    }

    // ---------------------- Navigate -----------------------------------------------------------

    /**
     * Returns the root directory.
     */
    public File rootDir() {
        return root.toFile();
    }

    /**
     * Creates a {@link JkFileTree} having the default filter and the specified
     * relative path to this root as root directory.
     */
    public JkFileTree go(String relativePath) {
        final Path path = root.resolve(relativePath);
        return JkFileTree.of(path);
    }

    /**
     * Returns path relative to this root of the specified relative path.
     */
    public Path get(String relativePath) {
        return root.resolve(relativePath);
    }

    // ----------------------- Write ----------------------------------------------------------------

    /**
     * Copies files contained in this {@link JkFileTree} to the specified
     * directory.
     */
    public int copyTo(Path destinationDir) {
        if (!Files.exists(destinationDir)) {
            JkUtilsPath.createDirectories(destinationDir);
        }
        return JkUtilsPath.copyDirContent(root, destinationDir, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies files contained in this {@link JkFileTree} to the specified directory.
     */
    public int copyTo(File destinationDir) {
        return copyTo(destinationDir.toPath());
    }

    /**
     * Copies the content of the specified directory at the root of this tree.
     * Specified directories to copy might not exist.
     */
    public JkFileTree importDirContent(Path... dirsToCopy) {
        createIfNotExist();
        for (final Path dirToCopy : dirsToCopy) {
            if (!Files.exists(dirToCopy)) {
                continue;
            }
            JkUtilsPath.copyDirContent(dirToCopy, root, StandardCopyOption.REPLACE_EXISTING);
        }
        return this;
    }

    /**
     * Copies the specified files at the root of this tree
     */
    public JkFileTree importFiles(Iterable<Path> files) {
        createIfNotExist();
        for (final Path file : files) {
            JkUtilsPath.copy(file, root.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        return this;
    }

    /**
     * Copies the specified files at the root of this tree
     */
    // WARN : importFiles(Iterable<Path>) is invoked when calling #importFiles with a single parameters
    // so it has been renamed with distinct name
    public JkFileTree importFile(Path... files) {
        createIfNotExist();
        for (final Path file : files) {
            JkUtilsPath.copy(file, root.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        return this;
    }

    /**
     * Returns a {@link JkZipper} of this {@link JkFileTree}.
     */
    public JkZipper zip() {
        return JkZipper.of(this);
    }

    /**
     * Merges the content of all files to the specified output.
     */
    public JkFileTree mergeTo(OutputStream outputStream) {
        for (final Path file : this.filesOnly()) {
            try (final InputStream fileInputStream = Files.newInputStream(file)) {
                JkUtilsIO.copy(fileInputStream, outputStream);
            } catch (final IOException e) {
                throw JkUtilsThrowable.unchecked(e);
            }
        }
        return this;
    }

    /**
     * Deletes each and every files in this tree. Files excluded to this tree
     * are not deleted.
     */
    public JkFileTree deleteAll() {
        JkUtilsPath.walkFileTree(root, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                return visit(file);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return visit(dir);
            }

            private FileVisitResult visit(Path path) {
                if (!JkUtilsPath.isSameFile(root, path) && filter.matches(path)) {
                    JkUtilsPath.deleteFile(path);
                }
                return FileVisitResult.CONTINUE;
            }

        });
        return this;
    }

    // ------------------------- Filter ----------------------------------------------

    /**
     * Creates a {@link JkFileTree} which is a copy of this {@link JkFileTree}
     * augmented with the specified {@link JkPathFilter}
     */
    public JkFileTree andFilter(JkPathFilter filter) {
        if (this.filter == JkPathFilter.ACCEPT_ALL) {
            return new JkFileTree(root, filter);
        }
        return new JkFileTree(root, this.filter.and(filter));
    }

    /**
     * Short hand to {@link #andFilter(JkPathFilter)} defining an include Ant
     * pattern filter. This will include any file matching at least one of the
     * specified <code>antPatterns</code>.
     */
    public JkFileTree include(String... antPatterns) {
        return andFilter(JkPathFilter.include(antPatterns));
    }


    /**
     * Short hand to {@link #andFilter(JkPathFilter)} defining an exclude Ant
     * pattern filter. This will exclude any file matching at least one of
     * specified <code>antPatterns</code>.
     */
    public JkFileTree exclude(String... antPatterns) {
        return andFilter(JkPathFilter.exclude(antPatterns));
    }

    // ------------------------ Misc ---------------------------------------

    /**
     * Returns the file count contained in this {@link JkFileTree}.
     */
    public int fileCount(boolean includeFolder) {
        return JkUtilsFile.count(root.toFile(), filter.toFileFilter(root.toFile()), includeFolder);
    }


    /**
     * Returns a {@link JkFileTreeSet} containing this tree as its single
     * element.
     */
    public JkFileTreeSet asSet() {
        return JkFileTreeSet.of(this);
    }

    @Override
    public String toString() {
        return root + ":" + filter;
    }

}
