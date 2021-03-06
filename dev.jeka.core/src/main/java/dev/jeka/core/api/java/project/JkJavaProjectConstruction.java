package dev.jeka.core.api.java.project;

import dev.jeka.core.api.depmanagement.JkDependencySet;
import dev.jeka.core.api.depmanagement.JkModuleId;
import dev.jeka.core.api.depmanagement.JkRepo;
import dev.jeka.core.api.depmanagement.artifact.JkArtifactId;
import dev.jeka.core.api.depmanagement.resolution.JkDependencyResolver;
import dev.jeka.core.api.file.JkPathMatcher;
import dev.jeka.core.api.file.JkPathSequence;
import dev.jeka.core.api.file.JkPathTreeSet;
import dev.jeka.core.api.java.JkJarPacker;
import dev.jeka.core.api.java.JkJavaCompiler;
import dev.jeka.core.api.java.JkJavaVersion;
import dev.jeka.core.api.java.JkManifest;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Responsible to produce jar files. It involves compilation and unit testing.
 * Compilation and tests can be run independently without creating jars.
 * <p>
 * Java Project Jar Production has common characteristics :
 * <ul>
 *     <li>Contains Java source files to be compiled</li>
 *     <li>All Java sources file (prod + test) are wrote against the same Java version and encoding</li>
 *     <li>The project may contain unit tests</li>
 *     <li>It can depends on any accepted dependencies (Maven module, other project, files on fs, ...)</li>
 *     <li>Part of the sources/resources may be generated</li>
 *     <li>By default, passing test suite is required to produce Jars.</li>
 * </ul>
 * Integration tests are outside of {@link JkJavaProjectConstruction} scope.
 */
public class JkJavaProjectConstruction {

    private static final JkJavaVersion DEFAULT_JAVA_VERSION = JkJavaVersion.V8;

    private static final String DEFAULT_ENCODING = "UTF-8";

    private final JkJavaProject project;

    private final JkJavaCompiler<JkJavaProjectConstruction> compiler;

    private JkJavaVersion javaVersion = DEFAULT_JAVA_VERSION;

    private String sourceEncoding = DEFAULT_ENCODING;

    private final JkDependencyResolver<JkJavaProjectConstruction> dependencyResolver;

    private final JkJavaProjectCompilation<JkJavaProjectConstruction> compilation;

    private final JkJavaProjectTesting testing;

    private PathMatcher fatJarFilter = JkPathMatcher.of(); // take all

    private final JkManifest manifest;

    private JkPathTreeSet extraFilesToIncludeInFatJar = JkPathTreeSet.ofEmpty();

    private UnaryOperator<JkDependencySet> dependencySetModifier = x -> x;
    
    /**
     * For Parent chaining
     */
    public JkJavaProject __;

    JkJavaProjectConstruction(JkJavaProject project) {
        this.project = project;
        this.__ = project;
        dependencyResolver = JkDependencyResolver.ofParent(this).addRepos(JkRepo.ofLocal(), JkRepo.ofMavenCentral());
        compiler = JkJavaCompiler.ofParent(this);
        compilation = JkJavaProjectCompilation.ofProd(this);
        testing = new JkJavaProjectTesting(this);
        manifest = JkManifest.ofParent(this);
    }

    public JkJavaProjectConstruction apply(Consumer<JkJavaProjectConstruction> consumer) {
        consumer.accept(this);
        return this;
    }

    public JkDependencyResolver<JkJavaProjectConstruction> getDependencyResolver() {
        return dependencyResolver;
    }

    /**
     * Returns the compiler compiling Java sources for this project. The returned instance is mutable
     * so users can modify it from this method return.
     */
    public JkJavaCompiler<JkJavaProjectConstruction> getCompiler() {
        return compiler;
    }

    /**
     * Sets the Java version used for both source and target.
     */
    public JkJavaProjectConstruction setJavaVersion(JkJavaVersion javaVersion) {
        this.javaVersion = javaVersion;
        return this;
    }

    /**
     * Gets the Java version used as source and target version
     */
    public JkJavaVersion getJavaVersion() {
        return javaVersion != null ? javaVersion : DEFAULT_JAVA_VERSION;
    }

    /**
     * Returns encoding to use to read Java source files
     */
    public String getSourceEncoding() {
        return sourceEncoding;
    }

    /**
     * Set the encoding to use to read Java source files
     */
    public JkJavaProjectConstruction setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
        return this;
    }

    public JkJavaProjectCompilation<JkJavaProjectConstruction> getCompilation() {
        return compilation;
    }

    public JkJavaProjectTesting getTesting() {
        return testing;
    }

    public JkManifest<JkJavaProjectConstruction> getManifest() {
        return manifest;
    }

    JkJavaProject getProject() {
        return project;
    }

    private void addManifestDefaults() {
        JkModuleId moduleId = project.getPublication().getModuleId();
        String version = project.getPublication().getVersion();
        if (manifest.getMainAttribute(JkManifest.IMPLEMENTATION_TITLE) == null && moduleId != null) {
            manifest.addMainAttribute(JkManifest.IMPLEMENTATION_TITLE, moduleId.getName());
        }
        if (manifest.getMainAttribute(JkManifest.IMPLEMENTATION_VENDOR) == null && moduleId != null) {
            manifest.addMainAttribute(JkManifest.IMPLEMENTATION_VENDOR, moduleId.getGroup());
        }
        if (manifest.getMainAttribute(JkManifest.IMPLEMENTATION_VERSION) == null && version != null) {
            manifest.addMainAttribute(JkManifest.IMPLEMENTATION_VERSION, version);
        }
    }

    public void createBinJar(Path target) {
        compilation.runIfNecessary();
        testing.runIfNecessary();
        addManifestDefaults();
        JkJarPacker.of(compilation.getLayout().resolveClassDir())
                .withManifest(manifest)
                .withExtraFiles(getExtraFilesToIncludeInJar())
                .makeJar(target);
    }

    public void createBinJar() {
        createBinJar(project.getArtifactPath(JkArtifactId.ofMainArtifact("jar")));
    }

    public void createFatJar(Path target) {
        compilation.runIfNecessary();
        testing.runIfNecessary();
        Iterable<Path> classpath = fetchRuntimeDependencies();
        addManifestDefaults();
        JkJarPacker.of(compilation.getLayout().resolveClassDir())
                .withManifest(manifest)
                .withExtraFiles(getExtraFilesToIncludeInJar())
                .makeFatJar(target, classpath, this.fatJarFilter);
    }

    public void createFatJar() {
        createFatJar(project.getArtifactPath(JkArtifactId.of("fat", "jar")));
    }

    public JkPathTreeSet getExtraFilesToIncludeInJar() {
        return this.extraFilesToIncludeInFatJar;
    }

    /**
     * File trees specified here will be added to the fat jar.
     */
    public JkJavaProjectConstruction setExtraFilesToIncludeInFatJar(JkPathTreeSet extraFilesToIncludeInFatJar) {
        this.extraFilesToIncludeInFatJar = extraFilesToIncludeInFatJar;
        return this;
    }

    /**
     * Specify the dependencies to add or remove from the production compilation dependencies to
     * get the runtime dependencies.
     * @param modifier An function that define the runtime dependencies from the compilation ones.
     */
    public JkJavaProjectConstruction setRuntimeDependencies(UnaryOperator<JkDependencySet> modifier) {
        this.dependencySetModifier = modifier;
        return this;
    }

    public JkDependencySet getRuntimeDependencies() {
        return dependencySetModifier.apply(compilation.getDependencies());
    }

    public JkPathSequence fetchRuntimeDependencies() {
        return dependencyResolver.resolve(getRuntimeDependencies()
                .normalised(project.getDuplicateConflictStrategy())).getFiles();
    }


}
