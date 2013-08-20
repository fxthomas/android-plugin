---
layout: post
title: 3. Advanced topics
subtitle: On advanced build processes and interoperability
---

This section will talk in a little more detail about how to use SBT-Android for
more advanced purposes, as well as integration with existing tools (IntelliJ
IDEA, Gradle,...).

# Advanced build configuration

Up until now, we have used the `build.sbt` file to describe how to build our
apps. While this works well in practice for simple projects, for more complex
build situations you'll probably need something else.

You should note, first, that `build.sbt` files are essentially lines of Scala
code that are put together in one unique setting list.

`build.scala` files, on the contrary, let you build your own projects and
setting lists.

To create one, you first must import the required SBT components :

```scala
import sbt._
import Keys._
import Defaults._
```

Then, import the SBT-Android keys and helper methods :

```scala
import sbtandroid.AndroidPlugin._
```

Then, finally, create a `Build` object that will include your build settings :

```scala
object AndroidBuild extends Build {
  ...
}
```

Inside this object, you can put as many `Project`s as you like, describing
where your source files are, and what the relationships between them are.

As an example, here is how a standard Android project is defined :

```scala
object AndroidBuild extends Build {

  // Global settings
  val globalSettings = Seq(
    name := "Scratch",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.10.1",
    platformName := "android-16",
    keyalias := "change-me"
  )

  // Main project (equivalent to defining something in `build.sbt`)
  lazy val main = AndroidProject(
    "main",                     // Project name
    file("."),                  // Project base directory
    settings=globalSettings)    // Project settings

  lazy val tests = AndroidTestProject(
    "tests",                         // Project name
    file("tests"),                   // Project base directory
    settings=globalSettings)         // Project settings
    .dependsOn(main % "provided")    // Main is "provided"
    .settings(name := "MockaTests")  // Application name
}
```

Here, the `main` project is the equivalent to what you've defined previously in
the `build.sbt` file.

On the other hand, `tests` is a "testing" project, that depends on code from
`main` at runtime (`main`'s classpath will be loaded by Dalvik when running the
Android instrumentation framework), but must not be included in the APK.

We use a few methods that may need explanation :

  * `AndroidProject` and `AndroidTestProject` are two helper methods letting you
    create Android projects more easily. Standard Java or Scala projects use the
    original SBT `Project` method, as in `val main = Project("main", file(."))`.

  * `dependsOn` is a method of a `Project` that defines dependencies on other projects.

  * `settings` is a method of a `Project` object that defines additional settings.

# IntelliJ integration

[SBT-Idea](https://github.com/mpeltonen/sbt-idea) can be used to generate
Android IntelliJ projects from SBT.

For now, you need to use a the android-support branch of
https://github.com/fxthomas/sbt-idea:

```bash
> git clone https://github.com/fxthomas/sbt-idea
Cloning into 'sbt-idea'...
> cd sbt-idea
> checkout android-support
Branch android-support set up to track remote branch android-support from origin.
Switched to a new branch 'android-support'
> sbt publish-local
[info] Loading global plugins from ...
...
```

This will create a `1.5.0-SNAPSHOT` release in your local ivy repository.

You can include it, like any plugin, in your `project/plugins.sbt` :

```scala
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.0-SNAPSHOT")
```

## Project generation

Just run the `gen-idea` command to generate an IntelliJ project :

```bash
> sbt gen-idea
[info] Loading project definition from .../scratch/project
[info] Set current project to Scratch (in build file:.../scratch/)
[info] Creating IDEA module for project 'Scratch' ...
...
[info] Created .../scratch/.idea_modules/Scratch.iml
[info] Created .../scratch/.idea_modules/Scratch-build.iml
```

Then, start IntelliJ and open the project :

![IntelliJ Welcome Screen](images/IntelliJ-OpenProject.png)
![Choose project](images/IntelliJ-ChooseProject.png)

Let IntelliJ read and index everything, and you'll be able to open the main
class :

![Main IntelliJ view](images/IntelliJ-OpenedProject.png)

Now, to start your application, use the _Run_ menu :

![IntelliJ Run Menu](images/IntelliJ-Run.png)

Create a new run configuration :

![IntelliJ New configuration](images/IntelliJ-Configuration.png)

I generally use the "Chooser dialog" to select the device I want my project to
run on, because IntelliJ has the habit of starting an emulator by default,
which I don't want :

![IntelliJ Edit Configuration](images/IntelliJ-RunConfig.png)

Finally, click "Run" and you're good to go. In the future, just use the "Run"
and "Debug" icons on IntelliJ's toolbar :

![IntelliJ Play](images/IntelliJ-Play.png)

_**Note:** The very first time you use a SBT-generated project, you might have to
configure the Android target. It has to be called something like **Android
4.1.2 Platform** (change the version to suit your needs) and will look like this :_

![IntelliJ Target](images/IntelliJ-AndroidPlatform.png)

## Known limitations

Currently, projects generated by `sbt-idea` do not handle :

  * Resources in ApkLibs and AAR libraries
  * NDK sources

There's also a [plugin](http://plugins.jetbrains.com/plugin/?id=5007) for
IntelliJ that can run SBT tasks directly from the IDE before launching your
application, thus removing all these limitations.

# Eclipse integration

There's a _giter8_ template that can generate Eclipse projects correctly, but
it's been a while since I've updated it.

Try the `sbt-android-0.7` branch of
[fxthomas/android-app.g8](https://github.com/fxthomas/android-app.g8) : it
compiles correctly, but hasn't been tested a lot.

_If you ever manage to improve Eclipse support, let us know by a mail or a pull
request!_

# Gradle integration

The new Android Build System uses Gradle instead of Ant to build Android apps.

Running SBT-Android on a Gradle project is easy : drop the simple `build.sbt`
and `plugins.sbt` files we created earlier, add dependencies, and you're pretty
much set.

Running Gradle on a SBT-Android project should be pretty easy as well, but the
Android and Scala Gradle plugins do not play (yet?) nice together.

See [this discussion](https://groups.google.com/forum/#!topic/adt-dev/1a5cAuSuOkg)
for more information.

_If you improve Gradle support by yourself, let us know by a mail or a pull
request!_

# Preloading libraries

You probably noticed that the Proguard step takes a lot of time when including
large libraries (such as the Scala library) in the APK.

SBT-Android supports _preloading_ libraries, which simply uploads specific
libraries to the target device or emulator.

This allows applications to skip packaging the preloaded libraries in the APK,
allowing you, in most cases, to skip Proguard entirely for development.

_**Note:** This is for **development** only. APKs generated with preloading
enabled won't work on normal devices!_

## Setting up preloaded libraries

You will need either a fully rooted device or a preconfigured emulator.

A popular choice for rooting your device is
[CyanogenMod](http://www.cyanogenmod.org). After installing it, enable the
Developper options by going to **Settings**, then **About Phone** (or **About
Tablet**), then tapping **Build number** 7 times. Once you are notified that
developper settings have been enabled, go to **Settings**, then **Developer
Options**, then **Root access**, and allow access to ADB :

![Allow access to ADB](images/DevOptions-Root.png)

Emulator AVD images are rooted by default, but make sure you aren't using a
modified system image before preloading. SBT-Android _will_ overwrite it. Newly
created AVDs should be fine. Also, during preloading, SBT-Android will _kill
any running emulator_ to make sure the process can complete safely, so you
probably should shut them down yourself.

Once you meet the above requirements, start a SBT command prompt and run either :

  * `preload-device` to preload a real device connected _via_ USB

  * `preload-emulator avdName` to preload an emulator named `avdName`

_**Note:** SBT-Android will use a **root shell** to upload the libraries on the
`/system/` partition. If that is not okay with you or if you don't understand
what that means, don't run one of these commands and inform yourself first!_

Once the preloading is done, target devices will be rebooted.

## Using preloaded libraries

Add the `preload` scope to the usual tasks and settings. For example, `start`
becomes `preload:start`.

The `preload` scope essentially works the same way as the default, but
additionally includes the following :

```scala
usePreloaded := true
useProguard := false
```

If you want IntelliJ projects generated by SBT-Idea to use the `preload` scope,
set `ideaConfiguration` to `Preload` in either `build.sbt` or `build.scala` :

```scala
ideaConfiguration := Preload
```

In practice, preloaded libraries behave the same way as additional libraries
bundled with Android, such as Google Maps : a `uses-library` tag is added to
the manifest to tell Android to load them in the app classpath.

_**Note:** By default, SBT-Android only preloads the Scala library, and it will
automatically take care of adding the `uses-library` tag to the manifest._

If you use libraries big enough to benefit from preloading, in addition to
`scala-library`, you can add a filter like in these examples :

```scala
preloadFilters += filterName("my-library")
preloadFilters += filterModule("org.blah" % "hello" % "1.7")
preloadFilters += filterFile(file("/.../.../my-lib.jar"))
preloadFilters += filterFilename("my-lib.jar")
```
