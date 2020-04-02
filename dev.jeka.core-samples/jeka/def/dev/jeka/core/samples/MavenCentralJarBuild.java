package dev.jeka.core.samples;

import dev.jeka.core.api.depmanagement.JkDependencySet;
import dev.jeka.core.api.depmanagement.JkMavenPublicationInfo;
import dev.jeka.core.tool.JkCommandSet;
import dev.jeka.core.tool.builtins.java.JkPluginJava;

import static dev.jeka.core.api.depmanagement.JkJavaDepScopes.TEST;
import static dev.jeka.core.api.depmanagement.JkPopularModules.GUAVA;
import static dev.jeka.core.api.depmanagement.JkPopularModules.JUNIT;

/**
 * This build demonstrates how to specify project metadata required to publish on
 * Maven central ( see https://maven.apache.org/guides/mini/guide-central-repository-upload.html )
 * 
 * @author Jerome Angibaud
 */
public class MavenCentralJarBuild extends JkCommandSet {

    JkPluginJava javaPlugin = getPlugin(JkPluginJava.class);

    @Override
    protected void setup() {
        javaPlugin.getProject()
            .getDependencyManagement()
                .addDependencies(JkDependencySet.of()
                    .and(GUAVA, "18.0")
                    .and(JUNIT, "4.13", TEST)).__
            .getSteps()
                .getPublishing()
                    .setVersionedModule("org.jerkar:sample-open-source", "1.3.1-SNAPSHOT")
                    .setMavenPublicationInfo(JkMavenPublicationInfo
                        .of("my project", "my description", "https://github.com/jerkar/jeka/samples")
                        .withScm("https://github.com/jerkar/sample.git")
                        .andApache2License()
                        .andGitHubDeveloper("John Doe", "johndoe6591@gmail.com")
                    );
    }
   
}