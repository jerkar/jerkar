package org.jerkar.samples;

import static org.jerkar.api.depmanagement.JkJavaDepScopes.COMPILE;
import static org.jerkar.api.depmanagement.JkJavaDepScopes.PROVIDED;
import static org.jerkar.api.depmanagement.JkJavaDepScopes.RUNTIME;
import static org.jerkar.api.depmanagement.JkPopularModules.JERSEY_SERVER;

import java.io.File;

import org.jerkar.api.depmanagement.JkDependencies;
import org.jerkar.api.depmanagement.JkScope;
import org.jerkar.api.project.java.JkJavaProject;
import org.jerkar.tool.builtins.javabuild.JkJavaProjectBuild;

/**
 * This build illustrate how one can use other dependency scopes then the standard ones.
 */
public class SimpleScopeBuild extends JkJavaProjectBuild {
	
    private static final JkScope FOO = JkScope.of("foo"); 
	
    private static final JkScope BAR = JkScope.of("bar"); 
	
    protected JkJavaProject createProject(File baseDir) {
	JkDependencies deps = JkDependencies.builder()
	        .on(file("libs/foo.jar"))  
		.on(JERSEY_SERVER, "1.19")
		    .mapScope(COMPILE).to(RUNTIME)
		    .and(FOO, PROVIDED).to(BAR, PROVIDED)
		.build();
	return new JkJavaProject(baseDir).setDependencies(deps);
    }	
}
	

