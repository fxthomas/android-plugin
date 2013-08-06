---
layout: post
title: Annex B. Cookbook
subtitle: Commonly used rules and settings
---

This page lists a few configuration snippets we found useful when using
SBT-Android in our projects. Feel free to request additions to this list by
pull requests or the mailing list!

Unless specified, all code snippets are to be added to `build.sbt`.

# Commonly used dependencies

## Google repositories

These settings add some useful Google libraries (Play Services,...) to your
project.

Starting from SDK r22, these dependencies are bundled within the SDK components
called *Google Repository* and *Android Support Repository*, and SBT-Android is
setup to use them automatically.

```scala
// Google Play services
libraryDependencies += aarlib("com.google.android.gms" % "play-services" % "3.1.36")

// Support v4
libraryDependencies += "com.google.android.support" % "support-v4" % "13.0.0"

// Support v13
libraryDependencies += "com.google.android.support" % "support-v13" % "13.0.0"

// GridLayout v7
libraryDependencies += "com.google.android.support" % "gridlayout-v7" % "13.0.0"
```

## Google SDK add-ons

Libraries that are provided by Android (Maps,...) need to be specified with
`<uses-sdk>` in your manifest.

As far as I know, unless you find a specific version in your Maven repository,
you have to find the right library JAR in your SDK path, and manually copy it
to your project's `libs/` folder.

These libraries usually are in (for platform version 16, YMMV) :
`${ANDROID_SDK_HOME}/add-ons/addon-google_apis-google-16/libs`

## ActionBarSherlock

Add this to your configuration :

```scala
// ActionBarSherlock
libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.3.1")

// Keep ActionBarSherlock classes
proguardOptions += "-keep classes com.actionbarsherlock.** { *; }"
```

## Scaloid

```scala
// Scaloid
libraryDependencies += "org.scaloid" % "scaloid_2.10" % "2.0.16-SNAPSHOT"
```

_**Note:** Preloading doesn't work with Scaloid 2.0 yet. Stick to the default
configuration with ProGuard enabled and you should be fine._

# Build configuration

## Logging level

By default, SBT only logs errors, warnings and information messages. Telling it
to go a little further and output debug messages can help, in particular if you
want to see the commands executed or some additional information.

Just run this command in the SBT console :

```scala
> set logLevel in Global := Level.Debug
```

## Building regular projects

Instead of having everything under `src/main` (like SBT or Gradle do), regular
Ant Java layouts put everything on the root of the project, and the sources in
`src/`.

```scala
androidJavaLayout
```

_**Note:** This is the old build system, soon to be replaced by Gradle, which
uses more or less the same directory structure as SBT. This setting is only
provided for convenience for existing projects._

## Why are you keeping?

In some cases, you will want to ask ProGuard why it is keeping a given class.
You can use the `-whyareyoukeeping` swtich like this :

```scala
proguardOptions += "-whyareyoukeeping class com.mypackage.MyKlass"
```

You might also want to use `-printseeds` (refer to the documentation). This
prints the seeds to a file called `seeds.txt` at the root of your project :

```scala
proguardOptions <+= (baseDirectory) (p => "-printseeds " + (p / "seeds.txt").getAbsolutePath)
```

After changing the ProGuard options, `clean` and rebuild to see the requested
informations and print the seeds.

_**Note:** In my experience, this makes ProGuard run much slower than usual, so
keep it for debugging hard cases!_

# ProGuard recipes

As noted [here](https://groups.google.com/forum/#!topic/scala-on-android/z9ygY9ucW7o),
coming up with the right ProGuard configuration for a particular library can be tedious
and takes time. In this section we intend to collect community-submitted recipes that
should put you on track in no time.

##### Jackson 1.9.x (“old”, http://jackson.codehaus.org/)

```scala
proguardOptions ++= Seq(
  "-keepattributes *Annotation*,EnclosingMethod",
  "-keepnames class com.codehaus.jackson.** { *; }"
)
```

##### Ektorp (https://github.com/helun/Ektorp/)

```scala
proguardOptions ++= Seq(
  "-keep class org.ektorp.Attachment { *; }"
)
```

Additionally needs settings from *Jackson 1.9.x* (see above).

##### Couchbase-Lite (https://github.com/couchbase/couchbase-lite-android)


```scala
proguardOptions ++= Seq(
  "-keep class com.couchbase.cblite.router.CBLRouter { *; }",
  "-keep class com.couchbase.touchdb.TDCollateJSON { *; }",
  "-keepclasseswithmembers class * { native <methods>; }"
)
```

Additionally needs settings from *Ektorp* and *Jackson 1.9.x* (see above).

##### Akka (http://akka.io)

https://gist.github.com/bjornharrtell/3307987
