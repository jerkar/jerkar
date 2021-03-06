package dev.jeka.core.tool.builtins.maven;

import dev.jeka.core.api.depmanagement.JkDependencySet;
import dev.jeka.core.api.depmanagement.JkQualifiedDependencySet;
import dev.jeka.core.api.tooling.JkPom;
import dev.jeka.core.api.utils.JkUtilsAssert;
import dev.jeka.core.tool.JkClass;
import dev.jeka.core.tool.JkDoc;
import dev.jeka.core.tool.JkPlugin;

import java.nio.file.Files;
import java.nio.file.Path;

@JkDoc("Provides method to help migration from Maven.")
public class JkPluginPom extends JkPlugin {

    protected JkPluginPom(JkClass jkClass) {
        super(jkClass);
    }

    @JkDoc("Margin to print dependency code.")
    public int margin = 4;

    @JkDoc("Displays Java code for declaring dependencies on console based on pom.xml. The pom.xml file is supposed to be in root directory.")
    public void dependencyCode() {
        Path pomPath = getJkClass().getBaseDir().resolve("pom.xml");
        JkUtilsAssert.state(Files.exists(pomPath), "No pom file found at " + pomPath);
        JkPom pom = JkPom.of(pomPath);
        System.out.println("Compile");
        System.out.println(JkDependencySet.toJavaCode(margin, pom.getDependencies().getDependenciesHavingQualifier(null,
                JkQualifiedDependencySet.COMPILE_SCOPE, JkQualifiedDependencySet.PROVIDED_SCOPE), true));

        System.out.println("Runtime");
        System.out.print(JkDependencySet.toJavaCode(margin, pom.getDependencies().getDependenciesHavingQualifier(
                JkQualifiedDependencySet.RUNTIME_SCOPE), true));
        System.out.println(JkDependencySet.toJavaCode(margin, pom.getDependencies().getDependenciesHavingQualifier(
                JkQualifiedDependencySet.PROVIDED_SCOPE), false));

        System.out.println("Test");
        System.out.println(JkDependencySet.toJavaCode(margin, pom.getDependencies().getDependenciesHavingQualifier(
                JkQualifiedDependencySet.TEST_SCOPE), true));
    }
}
