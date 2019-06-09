package org.jerkar.samples;

import org.jerkar.api.depmanagement.JkDependencySet;
import org.jerkar.api.depmanagement.JkJavaDepScopes;
import dev.jeka.core.tool.JkInit;
import dev.jeka.core.tool.JkRun;
import dev.jeka.core.tool.builtins.jacoco.JkPluginJacoco;
import dev.jeka.core.tool.builtins.java.JkPluginJava;

import static org.jerkar.api.depmanagement.JkPopularModules.GUAVA;
import static org.jerkar.api.depmanagement.JkPopularModules.JUNIT;

/**
 * This build deletes artifacts, compiles, tests and launches SonarQube analyse.
 */
public class JacocoPluginBuild extends JkRun {

    JkPluginJava javaPlugin = getPlugin(JkPluginJava.class);

    @Override
    protected void setup() {
        javaPlugin.getProject()
                .addDependencies(JkDependencySet.of()
                .and(GUAVA, "18.0")
                .and(JUNIT, "4.11", JkJavaDepScopes.TEST));
        getPlugins().get(JkPluginJacoco.class);
    }

    public static void main(String[] args) {
        JkInit.instanceOf(JacocoPluginBuild.class, args).javaPlugin.test();
    }

}
