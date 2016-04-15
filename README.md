Oracle Coherence Reactive Extensions (RX)
=========================================

Overview
--------

  This document describes how to build the Coherence Reactive Extensions, based on
  the [Reactive Paradigm](https://en.wikipedia.org/wiki/Reactive_programming) and the
  [Reactive Extensions for Java](http://reactivex.io/)

  Building the Coherence Reactive Extensions results in a single coherence-rx jar together with javadoc and source.

Prerequisites
-------------

  In order to build or use the Coherence Reactive Extensions you must have the following installed:

  1. Java 8 SE Development Kit or Runtime environment.

     You can download the software from:
     - Java SE Development Kit - http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
     - JAVA SE Runtime Environment - http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

  2. Maven version 3.2.5 or above installed and configured.
  3. Coherence 12.2.1.0.1 or above installed.

  Ensure the following environment variables are set:

  JAVA_HOME
    Make sure that the JAVA_HOME environment variable points to the location of a JDK supported by the
    Oracle Coherence version you are using.

  COHERENCE_HOME
    Make sure COHERENCE_HOME is set to point to your Coherence install directory.
    This is only required for the Maven install-file commands.

  MAVEN_HOME
    If mvn command is not in your path then you should set MAVEN_HOME and then add MAVEN_HOME\bin to your PATH
    in a similar way to Java being added to the path below.

  You must also ensure the java command is in the path.
    E.g. for Linux/UNIX:
      export PATH=$JAVA_HOME/bin:$PATH

    For Windows:
      set PATH=%JAVA_HOME%\bin;%PATH%

  You must have Coherence installed into your local maven repository. If you
  do not, then carry out the following, replacing the version number with the version
  of Coherence you have installed.

  E.g. for Linux/UNIX/Mac:

    mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence.jar      -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence/12.2.1/coherence.12.2.1.pom

  E.g. for Windows:

    mvn install:install-file -Dfile=%COHERENCE_HOME%\lib\coherence.jar      -DpomFile=%COHERENCE_HOME%\plugins\maven\com\oracle\coherence\coherence\12.2.1\coherence.12.2.1.pom

Build Instructions
------------------

  Build the Coherence Reactive Extensions by using:
     "mvn clean install"

  The target directory will contain a number of files:

     - coherence-rx-x.y.z.jar          - Executable JAR file, see instructions below
     - coherence-rx-x.y.z-javadoc.jar  - javadoc
     - coherence-rx-x.y.z-sources.jar  - sources

    (where x.y.x are the current version of the Coherence Reactive Extensions)

References
----------
   For more information on Oracle Coherence, please see the following links:
   - Download Coherence - http://www.oracle.com/technetwork/middleware/coherence/downloads/index.html
   - Coherence Documentation - http://docs.oracle.com/middleware/1221/coherence/index.html
   - Coherence Community - http://coherence.java.net/