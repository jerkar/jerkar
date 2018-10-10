package org.jerkar;

import org.jerkar.api.file.JkPathTree;
import org.jerkar.api.system.JkLog;
import org.jerkar.api.system.JkProcess;
import org.jerkar.api.utils.JkUtilsAssert;
import org.jerkar.api.utils.JkUtilsPath;
import org.jerkar.api.utils.JkUtilsString;
import org.jerkar.api.utils.JkUtilsSystem;
import org.jerkar.tool.JkConstants;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Full black-box tests on sample projects
 */
class SampleTester {

    private final JkPathTree sampleBaseDir;

    private final JkPathTree sampleDependeeBaseDir;

    private final JkPathTree output;

    private Path launchScript;
    
    boolean restoreEclipseClasspathFile;

    SampleTester(JkPathTree buildDir) {
        super();
        this.sampleBaseDir = buildDir.goTo("../org.jerkar.samples");
        this.sampleDependeeBaseDir = buildDir.goTo("../org.jerkar.samples-dependee");
        this.output = sampleBaseDir.goTo(JkConstants.OUTPUT_PATH);
        String scriptName = JkUtilsSystem.IS_WINDOWS ? "jerkar.bat" : "jerkar";
        launchScript = buildDir.goTo(JkConstants.OUTPUT_PATH).get("distrib/" + scriptName);
    }

    void doTest()  {
        testSamples("AClassicBuild");
        testSamples("AntStyleBuild");
        testSamples("MavenStyleBuild");
        testSamples("OpenSourceJarBuild");
        testSamples("HttpClientTaskBuild");
        testSamples("SimpleScopeBuild");
        testDependee("FatJarBuild");
        Path classpathFile = sampleBaseDir.get(".classpath");
        Path classpathFile2 = sampleBaseDir.get(".classpath2");

        // Test eclipse
        try {
            Files.copy(classpathFile, classpathFile2, StandardCopyOption.REPLACE_EXISTING);
            testSamples("", "eclipse#generateAll");
            if (restoreEclipseClasspathFile) {
                Files.move(classpathFile2, classpathFile, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.delete(classpathFile2);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        testDependee("NormalJarBuild");
        testFork();
        testScaffoldJava();
    }


    private void testSamples(String className, String... args) {
        JkLog.info("Test " + className + " " + Arrays.toString(args));
        JkProcess.of(launchScript.toAbsolutePath().toString()).withWorkingDir(sampleBaseDir.root().toAbsolutePath().normalize())
                .withParametersIf(!JkUtilsString.isBlank(className), "-LV=true -RC=" + className)
                .andParameters("clean", "java#pack", "java#publish", "-java#publish.localOnly")
                .andParameters(args)
                .failOnError(true).runSync();
    }

    private void testDependee(String className, String... args) {
        JkLog.info("Test " + className + " " + Arrays.toString(args));
        JkProcess.of(launchScript.toAbsolutePath().toString()).withWorkingDir(this.sampleDependeeBaseDir.root())
                .withParametersIf(!JkUtilsString.isBlank(className), "-RC=" + className)
                .withParameters("clean", "java#pack")
                .andParameters(args)
                .failOnError(true).runSync();
    }

    private void testScaffoldJava() {
        JkLog.info("Test scaffold Java");
        Path root = JkUtilsPath.createTempDirectory("jerkar");
        process().withWorkingDir(root).andParameters("scaffold#run", "java#").runSync();
        process().withWorkingDir(root).andParameters("java#pack").runSync();
        JkPathTree.of(root).deleteRoot();
    }

    private void scaffoldAndEclipse() {
        Path scafoldedProject = output.root().resolve("scaffolded");
        JkProcess scaffoldProcess = process().withWorkingDir(scafoldedProject);
        JkUtilsPath.createDirectories(scafoldedProject);
        scaffoldProcess.withParameters("scaffold").runSync(); // scaffold
        // project
        scaffoldProcess.runSync(); // Build the scaffolded project
        JkLog.info("Test eclipse generation and compile            ");
        scaffoldProcess.withParameters("eclipse#generateAll").runSync();
        scaffoldProcess.withParameters("eclipse#").runSync(); // build using the .classpath for resolving classpath
        scaffoldProcess.withParameters("idea#generateIml", "idea#generateModulesXml").runSync();
    }

    private JkProcess process() {
        return JkProcess.of(launchScript.toAbsolutePath().toString()).failOnError(true);
    }

    private void testFork() {
        testSamples("", "-java#tests.fork");
        JkUtilsAssert.isTrue(output.goTo("test-reports/junit").exists(), "No test report generated in test fork mode.");
    }

}
