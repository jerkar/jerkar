[![Build Status](https://travis-ci.org/jerkar/jerkar.svg?branch=master)](https://travis-ci.org/jerkar/jerkar)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jerkar/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jerkar/core) <br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="http://jerkar.github.io/img/logo/PNG-01.png" width='350' height='420' align='middle'/>

<strong>Jerkar</strong> is a complete **Java build system** ala _Ant_, _Maven_, _Gradle_ or _Buildr_ using **pure Java** to automate your tasks. No XML or scripts : only rock solid Java code.

Enjoy all the engineering power you are confortable with (Java code, IDE, 3rd party libs,  Maven repository, ...) to define, 
structure, run, debug, reuse your automated tasks. Exactly as you would do for regular code.

# News
 
Master version now relies on JDK 8 while 0.6 and prior are relying on JDK 6.
In order to leverage new features bring by these versions, **Jerkar is deeply reworked**. <br/>
Also, to avoid bloating API with legacy stuff, Jerkar 0.7 won't be compatible with Jerkar 0.6 (you can still run current builds using embedded mode). 
We expect from this move a much more clean, polished and intuitive product.

# How to use Jerkar

Jerkar is expected to have a very fast learning curve for Java developers. You can visit the following page in this order :

* http://jerkar.github.io/tell-me-more.html : introduction to Jerkar. Answer to the question : *What Jerkar is exactly ?*
* http://jerkar.gith ub.io/tour.html : to give a concrete idea on how Jerkar is working (version 0.6)
* http://jerkar.github.io/documentation/latest/getting_started.html : to get hand-on experience (version 0.6)
* http://jerkar.github.io/documentation/latest/reference.html : to know the details about Jerkar behavior (version 0.6)
* https://github.com/jerkar/jerkar-examples : Examples of project built with Jerkar (version 0.6)

However above links reflect documentation for version 0.6. 

To get documentation matching version you are using, please refer to documentation provided inside Jerkar distrib/doc
produced when you have built Jerkar.

Also you can navigate command line documentation executing `jerkar help`.

# How to build Jerkar

Jerkar is made of following projects :
* org.jerkar.core : complete Jerkar project
* org.jerkar.samples : A sample project with several build classes to illustrate how Jerkar can be used in different ways
* org.jerkar.samples-dependee : A sample project depending on the above sample project to illustrate multi-project builds. 
These sample projects are also used to run some black-box tests

Jerkar builds itself. To build Jerkar full distrib built from sources, the simpler is to use your IDE.

Once distrib created, add the distrib folder to your PATH environment variable.

## Build Jerkar from Eclipse

* Import *org.jerkar.core project* in Eclipse (it already holds *.project* and *.classpath* files) 
* Make sure the project is configured to compile using a JDK8 and not a JRE.
* Run `org.jerkar.CoreBuild` class main method. This class is located in *build/def* folder. 
* This creates the full distrib in *org.jerkar.core/build/output/distrib* folder

## Build Jerkar from IntelliJ

* The Jerkar project is preconfigured for Intellij (.iml and modules.xml are stored in git).
* Make sure the project is configured with a JDK8.
* Run `org.jerkar.CoreBuild` class main method. This class is located in *build/def* folder, inside *org.jerkar.core* project.
* This creates the full distrib in *org.jerkar.core/build/output/distrib* folder

# Status

Last release contains all features a self respecting build tool must have : from compilation to publication features.
Currently Jerkar is undergoing a deep reworking in order to leverage of Java8 features. API is getting more polished 
but cannot be considered as stable for now.

# Want to contribute ?

Jerkar welcomes contributors. As a new project there's plenty of free rooms to start : You can extends/debug the jerkar project itself but you van also write addin/plugin for integrate better Jerkar with your favorite technology. Don't be intimidated, it's relatively easy and you can provide great added value just by writing very few code. As an example, look at the [Spring Boot addin](https://github.com/jerkar/spring-boot-addin). 
Also do not hesitate to [contact contributors](https://github.com/djeang) to discuss about what is best to start with.

## Coding rule
If you contribute to Jerkar Core project, there's only 1 rule : try to mimic the current style :-).
More concretely :

* Make a class public only when really needed. If a class is public, it should be prefixed with `Jk` (The goal is to not pollute auto-completion in IDE when Jerkar is on the build path).
* Favor immutable objects when reasonable
* Embrace a fluent style API
* Don't use 3rd party dependencies (Use or enrich JkUtilsXxxxx classes for commons). 
* Jerkar 0.7.x relies on JDK8
