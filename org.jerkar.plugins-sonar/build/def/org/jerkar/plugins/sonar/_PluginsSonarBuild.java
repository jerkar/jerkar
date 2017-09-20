package org.jerkar.plugins.sonar;


import org.jerkar.V07CoreBuild;
import org.jerkar.api.depmanagement.JkDependencies;
import org.jerkar.api.project.java.JkJavaProject;
import org.jerkar.tool.JkImportBuild;
import org.jerkar.tool.JkInit;
import org.jerkar.tool.builtins.javabuild.JkJavaProjectBuild;

import java.io.File;

import static org.jerkar.api.depmanagement.JkJavaDepScopes.PROVIDED;

public class _PluginsSonarBuild extends JkJavaProjectBuild {

    @JkImportBuild("../org.jerkar.core")
    private V07CoreBuild core;

    @Override
    protected JkJavaProject createProject(File baseDir) {
        JkJavaProject project = new JkJavaProject(baseDir);
        project.setDependencies(JkDependencies.of(PROVIDED, core.project().asDependency()));
        return project;
    }

    public static void main(String[] args) {
        JkInit.instanceOf(_PluginsSonarBuild.class, args).doDefault();
    }

}
