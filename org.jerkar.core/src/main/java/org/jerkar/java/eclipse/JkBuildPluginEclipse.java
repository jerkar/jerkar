package org.jerkar.java.eclipse;

import java.io.File;
import java.util.List;

import org.jerkar.JkBuild;
import org.jerkar.JkDirSet;
import org.jerkar.JkDoc;
import org.jerkar.JkOption;
import org.jerkar.depmanagement.JkDependencies;
import org.jerkar.java.build.JkJavaBuild;
import org.jerkar.java.build.JkJavaBuildPlugin;

@JkDoc({"Add capabilities for getting project information as source location and dependencies "
		+ "directly form the Eclipse files (.project, .classspath).",
		" This plugin also features method to genetate eclipse files from build class."
})
public class JkBuildPluginEclipse extends JkJavaBuildPlugin {

	static final String OPTION_VAR_PREFIX = "eclipse.var.";

	private JkJavaBuild javaBuild;

	public static boolean candidate(File baseDir) {
		final File dotClasspathFile = new File(baseDir, ".classpath");
		final File dotProject = new File(baseDir, ".project");
		return (dotClasspathFile.exists() && dotProject.exists());
	}

	@JkOption({"Flag for resolving dependencies against the eclipse classpath",
		"but trying to segregate test from production code considering path names : ",
	"if path contains 'test' then this is considered as an entry source for scope 'test'."})
	public boolean smartScope = true;

	@JkOption({"If not null, this value will be used as the JRE container path when generating .classpath file."})
	public String jreContainer = null;

	private DotClasspath cachedClasspath = null;

	@JkDoc("Generates Eclipse .classpath file according project dependencies.")
	public void generateFiles() {
		final File dotClasspathFile = this.javaBuild.baseDir(".classpath");
		try {
			DotClasspath.generate(this.javaBuild, dotClasspathFile, jreContainer);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JkDirSet alterSourceDirs(JkDirSet original) {
		final Sources.TestSegregator segregator = smartScope ? Sources.SMART : Sources.ALL_PROD;
		return dotClasspath().sourceDirs(javaBuild.baseDir(""), segregator).prodSources;
	}

	@Override
	public JkDirSet alterTestSourceDirs(JkDirSet original) {
		final Sources.TestSegregator segregator = smartScope ? Sources.SMART : Sources.ALL_PROD;
		return dotClasspath().sourceDirs(javaBuild.baseDir(""), segregator).testSources;
	}

	@Override
	public JkDirSet alterResourceDirs(JkDirSet original) {
		final Sources.TestSegregator segregator = smartScope ? Sources.SMART : Sources.ALL_PROD;
		return dotClasspath().sourceDirs(javaBuild.baseDir(""), segregator).prodSources.andFilter(JkJavaBuild.RESOURCE_FILTER);
	}

	@Override
	public JkDirSet alterTestResourceDirs(JkDirSet original) {
		final Sources.TestSegregator segregator = smartScope ? Sources.SMART : Sources.ALL_PROD;
		return dotClasspath().sourceDirs(javaBuild.baseDir(""), segregator).testSources.andFilter(JkJavaBuild.RESOURCE_FILTER);
	}

	@Override
	protected JkDependencies alterDependencies(JkDependencies original) {
		final ScopeResolver scopeResolver = scopeResolver();
		final List<Lib> libs = dotClasspath().libs(javaBuild.baseDir().root(), scopeResolver);
		return Lib.toDependencies(this.javaBuild, libs, scopeResolver);
	}

	private ScopeResolver scopeResolver() {
		if (smartScope) {
			if (WstCommonComponent.existIn(javaBuild.baseDir().root())) {
				final WstCommonComponent wstCommonComponent = WstCommonComponent.of(javaBuild.baseDir().root());
				return new ScopeResolverSmart(wstCommonComponent);
			}
			return null;
		}
		return new ScopeResolverAllCompile();
	}

	private DotClasspath dotClasspath() {
		if (cachedClasspath == null) {
			final File dotClasspathFile = new File(javaBuild.baseDir(""), ".classpath");
			cachedClasspath = DotClasspath.from(dotClasspathFile);
		}
		return cachedClasspath;
	}

	@Override
	public void configure(JkBuild build) {
		this.javaBuild = (JkJavaBuild) build;
	}

}