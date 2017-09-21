package org.jerkar.tool;

import org.jerkar.api.depmanagement.JkDependencies;
import org.jerkar.api.depmanagement.JkScopedDependency;
import org.jerkar.api.utils.JkUtilsFile;
import org.jerkar.api.utils.JkUtilsIterable;

import java.io.File;
import java.util.*;

/**
 * Defines importedBuilds of a given master build.
 * 
 * @author Jerome Angibaud
 */
public final class JkImportedBuilds {

    static JkImportedBuilds of(File masterRootDir, List<JkBuild> builds) {
        return new JkImportedBuilds(masterRootDir, new ArrayList<>(builds));
    }

    private final List<JkBuild> directImports;

    private List<JkBuild> transitiveImports;

    private final File masterBuildRoot;

    private JkImportedBuilds(File masterDir, List<JkBuild> buildDeps) {
        super();
        this.masterBuildRoot = masterDir;
        this.directImports = Collections.unmodifiableList(buildDeps);
    }

    /**
     * Returns a {@link JkImportedBuilds} identical to this one but augmented with
     * specified slave builds.
     */
    @SuppressWarnings("unchecked")
    public JkImportedBuilds and(List<JkBuild> slaves) {
        return new JkImportedBuilds(this.masterBuildRoot, JkUtilsIterable.concatLists(
                this.directImports, slaves));
    }

    /**
     * Returns a {@link JkImportedBuilds} identical to this one but augmented with
     * the {@link JkBuildDependency} contained in the the specified dependencies.
     */
    public JkImportedBuilds and(JkDependencies dependencies) {
        final List<JkBuild> list = projectBuildDependencies(dependencies);
        return this.and(list);
    }

    /**
     * Returns only the direct slave of this master build.
     */
    public List<JkBuild> directs() {
        return Collections.unmodifiableList(directImports);
    }

    /**
     * Returns direct and transitive importedBuilds. Transitive importedBuilds are resolved by
     * invoking recursively <code>JkBuildDependencySupport#importedBuilds()</code> on
     * direct importedBuilds.
     * 
     */
    public List<JkBuild> all() {
        if (transitiveImports == null) {
            transitiveImports = resolveTransitiveBuilds(new HashSet<>());
        }
        return transitiveImports;
    }

    /**
     * Same as {@link #all()} but only returns builds instance of the specified class or its subclasses.
     */
    public <T extends JkBuild> List<T> allOf(Class<T> ofClass) {
        List<T> result = new LinkedList<>();
        for (JkBuild build : all()) {
            if (ofClass.isAssignableFrom(build.getClass())) {
                result.add((T) build);
            }
        }
        return result;
    }

    private List<JkBuild> resolveTransitiveBuilds(Set<File> files) {
        final List<JkBuild> result = new LinkedList<>();
        for (final JkBuild build : directImports) {
            final File dir = JkUtilsFile.canonicalFile(build.baseDir().root());
            if (!files.contains(dir)) {
                result.addAll(build.importedBuilds().resolveTransitiveBuilds(files));
                result.add(build);
                files.add(dir);
            }
        }
        return result;
    }

    private static List<JkBuild> projectBuildDependencies(JkDependencies dependencies) {
        final List<JkBuild> result = new LinkedList<>();
        for (final JkScopedDependency scopedDependency : dependencies) {
            if (scopedDependency.dependency() instanceof JkBuildDependency) {
                final JkBuildDependency projectDependency = (JkBuildDependency) scopedDependency
                        .dependency();
                result.add(projectDependency.projectBuild());
            }
        }
        return result;
    }



}