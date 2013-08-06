---
layout: post
title: 2. Configuring the build
subtitle: Common SBT-Android features
---

This section will explain the SBT-Android build process into a little more
detail.

# Starting SBT

SBT is the build tool our plugin runs on. It is used by the Scala community and
can be used to replace tools like Maven or Gradle.

People generally use SBT through its command prompt, started by running `sbt`
in a terminal. In there, you can inspect the build settings and run tasks and
commands.

Before starting, SBT first checks for either `build.sbt` (which is the simplest
way of starting a SBT project), or a `project/build.scala` file (which can be
used to configure more complex builds).

_**Note:** Remember that we have already created a
`build.sbt` file in section 1!_

The build files contain settings, which generally look like this :

```scala
useProguard := false
```

`useProguard` is a _setting key_, and `:= false` assigns the value `false` to
that key.

Instead of changing `build.sbt`, you can set a key on the fly while running SBT
by using the `set` command, like :

```scala
set useProguard := false
```

SBT-Android is a SBT _plugin_, and as such is set up in `project/plugins.sbt`
by adding a line that looks like this :

```scala
addSbtPlugin("org.scala-sbt" % "sbt-android" % "0.7")
```

_**Note:** Plugins are essentially additional classes loaded by SBT on the
project classpath. Here we use a Maven-style reference : `org.scala-sbt` is the
artifact organization, `sbt-android` is the artifact name, and
`0.7` is the version._

# Using SBT

Within the SBT prompt, you can :

  * Run a task, such as the `start` task we saw earlier, or `apk`, which will
    generate an APK package.

  * Show the values returned by settings and tasks. For example, running `show
    use-proguard` will show `true` or `false`, depending on whether Proguard is
    enabled for your build.

  * Change settings and tasks on the fly. You could just type `set useProguard
    := false` to disable Proguard for a particular session.

SBT will place all the build artifacts and temporary files somewhere under the
`target/` directory.

SBT can watch your sources for changes and run a command each time something
happens if you use the `~` (tilda) operator before running a task, such as
`~compile`.

# General build process

The SBT-Android build process is very similar to a standard Android build. Here
are the most important parts :

  1. It first runs _source generators_ such as the `aapt` and `aidl` commands
     to parse resources and generate, among others, `R.java` files, and extracts
     ApkLib and AAR dependencies.

     Generated sources end up in `target/scala-2.10/src_managed`, while ApkLib
     and AAR libraries dependencies have their own managed folders in
     `target/scala-2.10/apklib_managed` and `target/scala-2.10/aarlib_managed`,
     respectively.

     You can show every managed source file by typing `show managed-sources` in
     SBT.

  2. It compiles both unmanaged sources (the ones you wrote yourself) and
     managed sources from the previous step.

     The compilation step can be started by typing `compile` in SBT.

  3. It optionally runs Proguard to remove unneeded classes. Note that
     _Proguard is necessary when including the Scala library in the APK_.

     You can run this step with the `proguard` task in SBT.

  4. It runs the `dex` command to transform JVM class files into Dalvik bytecode
     that can run on Android.

     You can run this step with the `dx` task.

  5. Finally, it creates an APK with everything above. This is what you get
     when you run the `apk` command.

The plugin uses a lot of secondary settings and tasks, but these are the main
ones.

Optionally, you can also generate an ApkLib from your project by running the
`apklib-package` task.

# Building standard Java projects

Standard (Ant) Java projects have a different layout from the one
SBT-Android and Gradle use.

The directory layout usually looks like this :

```
| src
   | com
      | yourpackage
         | Activity.java
         ...
| res
   | layout
   | drawable
   | xml
   ...
| assets
| AndroidManifest.xml
...
```

To build such a project with the plugin, use the `androidJavaLayout` additional settings in your `build.sbt` file :

```scala
// Default Android settings
androidDefaults

// Use the Java layout
androidJavaLayout

// Configure the project
name := "YourProject"
...
```

# Depndencies

The _classpath_ is a set of class files loaded by a Java or Dalvik VM.

## Inspecting the classpath

Some of these classes are _provided_ by the runtime environment (for example,
the libraries you specify in your Android manifest with `uses-library`, or the
Android runtime provided by `android.jar`)

The rest must of course be _included_ in your APK, and Android will load them
when running your app.

You can inspect the classpath by using the `show full-classpath`, `show
included-classpath` and `show provided-classpath` commands :

```
> show included-classpath
[info] List(.../scala-library.jar)

> show provided-classpath
[info] List(.../android.jar)
```

## Using libraries

Single JAR files can be added to the project by simply putting them in the
`libs/` directory at the root of the project.

Maven-style library dependencies can be added to the project in a standard SBT
fashion :

```scala
libraryDependencies += "org.scaloid" %% "scaloid" % "2.0-16-SNAPSHOT"
```

If you want to add _provided_ dependencies (used at compile-time only, but not
included in the APK), use the `provided` scope :

```scala
libraryDependencies += "org.scaloid" %% "scaloid" % "2.0-16-SNAPSHOT" % "provided"
```

_**Note:** For those unfamiliar with it, I recommend looking at
[MVNRepository](http://mvnrepository.com). Package maintainers usually include
the right information on their project page._

## Using ApkLib and AAR dependencies

ApkLib are Maven dependencies that can include Android resources, unlike
regular JARs. Popular Android libraries, such as
[ActionBarSherlock](http://www.actionbarsherlock.com), are distributed that
way.

AAR libraries are the library packages used in the new Google build system,
based on Gradle.

To include such a dependency, use the `apklib` and `aarlib` helper functions :

```scala
libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.3.1")
libraryDependencies += aarlib("com.google.android.gms" % "play-services" % "3.1.36")
```

# Proguard

As we've seen before, Proguard can be turned on/off with the `useProguard` key.

Here are a few additional Proguard options :

  * **`proguardOptions`**: Additional Proguard options.

    The plugin automatically generates options for your main classes, but you
    may need it if you use additional libraries, use reflection or allocate
    classes dynamically. Example :

    ```scala
    proguardOptions += "-keep class com.yourpackage.Klass { *; }"
    ```

    Refer to the [ProGuard
    documentation](http://proguard.sourceforge.net/index.html#manual/usage.html)
    for more information about which rules you can use.

    _**Note:** The `+=` operator adds an option to a list setting key._

    _**Note:** `NoSuchMethodError` errors in your LogCat are often a sign
    that you need to add ProGuard rules._

  * **`proguardConfiguration`**: Path to the generated Proguard configuration.

    You can, of course, override it with another file, but you're on your own
    if you do that. Look at the `-include` directive if you want it to be less
    painful.

  * **`proguardOutputPath`**: The output path JAR file generated by
    Proguard.

Proguard will, by default, use the _included_ classpath as _input classes_ (see
the `-injars` directive), and the _provided_ classes as _library_ classes (the
`-libraryjars` directive).
