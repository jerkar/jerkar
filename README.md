![Build Status](https://github.com/jerkar/jeka/actions/workflows/push-master.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/dev.jeka/jeka-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.jeka%22%20AND%20a:%22jeka-core%22) 
[![Gitter](https://badges.gitter.im/jeka-tool/community.svg)](https://gitter.im/jeka-tool/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Twitter Follow](https://img.shields.io/twitter/follow/JekaBuildTool.svg?style=social)](https://twitter.com/JekaBuildTool)  
<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="http://jeka.dev/images/large-social-logo.png" align='middle'/>

# What is Jeka ?

<strong>Jeka</strong>(formerly Jerkar) is a complete **Java build system** ala _Ant_, _Maven_, _Gradle_ or _Buildr_ using only Java code to automate builds, tasks or pipelines.

Builds/tasks definition are expressed in pure Java code to leverage of IDE power and Java ecosystem directly.
Users can code, run, debug, model, distribute buid features as they would do for production code.

```java
@JkDefClasspath("dev.jeka:springboot-plugin:3.0.0.RC7")
class Build extends JkClass {

    private final JkPluginSpringboot springboot = getPlugin(JkPluginSpringboot.class);

    public boolean runIT = true;

    @Override
    protected void setup() {
        springboot.setSpringbootVersion("2.2.6.RELEASE");
        springboot.javaPlugin().getProject().simpleFacade()
            .setCompileDependencies(deps -> deps
                .and("org.springframework.boot:spring-boot-starter-web")
                .and("org.projectlombok:lombok:1.18.20")
            )
            .setRuntimeDependencies(deps -> deps
                .minus("org.projectlombok:lombok")
            )
            .setTestDependencies(deps -> deps
                .and("org.springframework.boot:spring-boot-starter-test")
                    .withLocalExclusions("org.junit.vintage:junit-vintage-engine")
            )
            .addTestExcludeFilterSuffixedBy("IT", !runIT);
    }

    public void cleanPack() {
        clean();
        springboot.javaPlugin().pack();
    }

}
```

Also, Jeka comes with powerful conventions and dynamic plugin mechanism allowing to perform exotic tasks without requiring a single line of code/configuration.
For example `jeka java#pack jacoco# sonar#run -sonar#host.url=http://myserver/sonar`
performs a complete build running unit tests under Jacoco coverage tools and performs SonarQube analysis on a Java project free 
of any build-code / configuration / script. 

For better user experience, please use [Jeka Plugin for Intellij](https://github.com/jerkar/jeka-ide-intellij)



# News 

* Jeka 0.9.x serie is out. 0.9.x aims at providing intermediate API polishes and improvements
  prior to go to 1.0.0.alpha. It will also provide necessary features to interact with a first class Intellij plugin.
     
* Jeka has joined OW2 organisation on january 2020 : https://projects.ow2.org/view/jeka/

Last major additions :

* Reworked [dependency management](https://github.com/jerkar/jeka/blob/master/dev.jeka.core/src/main/doc/Reference%20Guide/2.3.%20Dependency%20management.md)
* Completely renewed API, now embracing widely *Parent Chaining*.
* Test engine now relies on Junit 5 (still compatible with Junit 3&4)
* Release of a [plugin for Intellij](https://github.com/jerkar/jeka-ide-intellij)
* Upgraded to Ivy 2.5.0
* Wrapper to run Jeka independently of the Jeka version installed on the host machine
* Jdk9+ compatibility
* Deploying on Maven central though a modern release process (version numbering based on Git instead of being hardcoded).
Jeka now uses these features to release itself.

# Roadmap/Ideas
 
* Stabilise api from user feedback. API is quite workable now but may be improved.
* Provides a graphical plugin for better integration with Eclipse
* Enhance existing graphical [plugin for Intellij](https://github.com/jerkar/jeka-ide-intellij)
* Provide a plugin for Android
* Integrate Kotlin as a first citizen language for both building Kotlin projects and write Jeka command classes.

Please visit [release note](https://github.com/jerkar/jeka/blob/master/release-note.md) and [issues](issues) for roadmap.

# Get Jeka

* Snapshots : https://oss.sonatype.org/content/repositories/snapshots/dev/jeka/jeka-core/
* Releases : https://repo1.maven.org/maven2/dev/jeka/jeka-core/

The distribution is the file named jeka-core-x.x.x-distrib.zip. 

# How to use Jeka ?

Jeka is designed to be easy to master for Java developers. It is easy to figure out how it works by knowing few 
concepts and navigate in source code.

That said, documentation is needed for a starting point.

Visit following pages according your expectation :
* [Getting Started](dev.jeka.core/src/main/doc/Getting%20Started.md)
* [Reference Guide](dev.jeka.core/src/main/doc/Reference%20Guide)
* [Frequently Asked Questions](dev.jeka.core/src/main/doc/FAQ.md)
* [Javadoc](https://jeka.dev/docs/javadoc)
* [Working examples](https://github.com/jerkar/working-examples)

# External plugins

Jeka comes with plugins out of the box. These plugins covers the most common points a Java developer need to address 
when building a project. This includes plugins for IDE metadata generation (IntelliJ, Eclipse), dependency management, git, , java project building, testing, PGP signing, binary repositories, Maven interaction, scaffolding, sonarQube and web archives.

Nevertheless, Jeka is extensible and other plugins exist outside the main distib among :
* [Springboot Plugin](https://github.com/jerkar/springboot-plugin)
* [Protobuf Plugin](https://github.com/jerkar/protobuf-plugin)

# Community

<a class="btn btn-link btn-neutral" href="https://projects.ow2.org/view/jeka">
              <img src="https://jeka.dev/images/ow2.svg" alt="Image" height="60" width="60"></a>
              
This project is supported by OW2 consortium.
              
              

You can ask question using regular using [this repository issues](https://github.com/jerkar/jerkar/issues).

You can also use direct emailing for questions and support : djeangdev@yahoo.fr

A twitter account also exist : https://twitter.com/djeang_dev

# How to build Jeka ?

Jeka is made of following projects :
* dev.jeka.core : complete Jeka project
* dev.jeka.samples : A sample project with several build classes to illustrate how Jeka can be used in different ways
* dev.jeka.depender-samples : A sample project depending on the above sample project to illustrate multi-project builds. 
These sample projects are also used to run some black-box tests

Jeka builds itself. To build Jeka full distrib from sources, the simpler is to use your IDE.

Once distrib created, add the distrib folder to your PATH environment variable.

## Build Jeka from Eclipse

* Clone this repository in Eclipse. Project is already configured ( *.project* and *.classpath* are stored in git).
* Add the `JEKA_USER_HOME` classpath variable pointing on [USER_HOME]/.jeka 
* Make sure the project is configured to compile using a JDK8 or higher and not a JRE.
* Run `dev.jeka.core.CoreBuildAndIT` class main method. This class is located in *jeka/def* folder. 
* This creates the full distrib in *dev.jeka.core/jeka/output/distrib* folder and run full Integration test suite.

## Build Jeka from IntelliJ

Note that it exists a now an IntelliJ plugin to integrate Jeka to get rid of the following setup.

* Clone this repository into IntelliJ. Project is already configured (.iml and modules.xml are stored in git).
* Add the `JEKA_USER_HOME` variable pointing on [USER_HOME]/.jeka 
* Make sure the project is configured with a JDK8 or higher.
* Run `dev.jeka.core.CoreBuildAndIT` class main method. This class is located in *jeka/def* folder, inside *dev.jeka.core* module.
  Make sure to run it using `$MODULE_WORKING_DIR$` as working directory.
* This creates the full distrib in *dev.jeka.core/jeka/output/distrib* folder  and run full Integration test suite.

To build the project without running whole integration test suite, run `dev.jeka.core.CoreBuild` class main method.

## How to Release ?

Release is done automatically by Github action on PUSH on *master*.
If the last commit message title contains a word like 'Release:XXX' (case matters ) then a tag XXX is created and 
the binaries will be published on Maven Central.
Otherwise, the binary wll be simply pushed on OSSRH snapshot.

To really deploy to Maven central, a manual action it still needed to [close/release repository](https://oss.sonatype.org).

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="http://jeka.dev/images/logo-whole-bg.jpg" width='420' height='420' align='center'/>

