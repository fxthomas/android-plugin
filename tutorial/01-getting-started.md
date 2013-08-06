---
layout: post
title: 1. Getting Started
subtitle: Creating a simple project from scratch
---

**Welcome to SBT-Android!**

The following tutorial is aimed at people who want to discover Scala on
Android. Since Android apps rely heavily on Java, other languages targeting
the Java Virtual Machine can also be used on Android, and among them is Scala.

[Scala](http://www.scala-lang.org) is a functional, strongly-typed language that brings quite a number of
handy features for app development, and this tutorial will try to list a few of
them.

Scala is quite close to Java, and you can freely mix Java and Scala code if you
wish. Although it's recommended you at least know a little bit of Scala before
starting, you don't really need to know Scala or SBT ([_Scala Build
Tool_](http://www.scala-sbt.org), which we use to build Scala apps) to follow
this tutorial, so we'll try to explain the most commonly-used commands and
features.

If you find something unclear or lacking, the whole tutorial is written in
Markdown and [hosted on
GitHub](https://github.com/fxthomas/android-plugin/tree/gh-pages/). Don't
hesitate to send pull requests there!

If you have trouble, most of us hang out in
[scala-on-android](http://groups.google.com/group/scala-on-android) or
[simple-build-tool](http://groups.google.com/group/simple-build-tool).

There is also an IRC channel at `#sbt-android` on
[Freenode](http://www.freenode.net).

# Installation

The basic requirements are :

  * [SBT](http://www.scala-sbt.org/) (at least version 0.12.0)

    - On OSX, install it with [Homebrew](http://mxcl.github.io/homebrew/) :
      `brew install sbt`

    - Follow the [installation
      instructions](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)
      for other systems

  * The [Android SDK](http://developer.android.com/sdk/index.html)

    - On OSX, install it with [Homebrew](http://mxcl.github.io/homebrew/) :
      `brew install android-sdk`

    - On some Linux distributions, you can find it in third-party repositories
      (in Archlinux, for instance, there is an [AUR
      package](https://aur.archlinux.org/packages/android-sdk/))

    - Alternatively, you can download the archive, unzip it and run the SDK
      installer directly

After installing them, you should make sure you can run the `sbt` command, as
well as install and configure at least one Android SDK platform. The latter is
covered by the Android documentation, and is not different in SBT-Android.

You might have to set an environment variable called `ANDROID_HOME` pointing to
where your SDK is located, SBT will tell you if it can't find it.

# Creating a project from scratch

To better understand how things work, we're going to create a project entirely
from scratch.

Later, you can just clone a Git repository or use
[Giter8](https://github.com/n8han/giter8) template projects.  If you want to
play with it right now, clone
[fxthomas/android-scratch](https://github.com/fxthomas/android-scratch.git) and
skip to the next section.

SBT-Android closely follows the Gradle structure :

  * Put your Java sources in `src/main/java`
  * Put your Scala sources in `src/main/scala`
  * Put your resources in `src/main/res`
  * Put your assets in `src/main/assets`
  * Put your manifest in `src/main/AndroidManifest.xml`
  * Put your JAR libraries in `libs`
  * Put your native (`.so`) libraries in `lib`

Typical Gradle projects are expected to build and run out of the box by just
editing the SBT configuration.

-----------

So, without further ado, open a Terminal, and create the directory structure :

```bash
mkdir scratch                  # Create the project directory
cd scratch                     # Change to the project directory
mkdir project                  # Project configuration
mkdir -p src/main/scala        # Scala sources
mkdir -p src/main/res/layout   # Layout resources
touch build.sbt                # Build configuration
touch project/plugins.sbt      # SBT Plugins
```

_**Note:** Every path is relative to the `scratch` directory we just created,
unless explicitely specified._

Plugin definitions go in `project/plugins.sbt` :

```scala
addSbtPlugin("org.scala-sbt" % "sbt-android" % "0.7")
```

App configuration goes in `build.sbt` :

```scala
// Include the Android plugin
androidDefaults

// Name of your app
name := "Scratch"

// Version of your app
version := "0.1"

// Version number of your app
versionCode := 0

// Version of Scala
scalaVersion := "2.10.1"

// Version of the Android platform SDK
platformName := "android-16"
```

This is enough for SBT. You can now add the Android manifest file in
`src/main/AndroidManifest.xml` :

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.scratch"
    android:versionCode="0"
    android:versionName="0.1">

    <uses-sdk
        android:minSdkVersion="16"
        android:targetSdkVersion="16"/>

    <application
        android:label="Scratch"
        android:icon="@drawable/android:star_big_on"
        android:theme="@android:style/Theme.Holo.Light"
        android:debuggable="true">

        <activity
          android:label="Scratch"
          android:name=".ScratchActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Add a layout file to describe the user interface in `src/main/res/layout/main.xml` :

```xml
<?xml version="1.0" encoding="utf-8"?>

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
       android:layout_width="wrap_content"
       android:layout_height="wrap_content">

  <TextView xmlns:android="http://schemas.android.com/apk/res/android"
            android:id="@+id/textview"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>
</LinearLayout>
```

And, finally, display "Hello, world!" on the main activity in `src/main/scala/ScratchActivity.scala` :

```scala
package com.scratch

import android.app.Activity
import android.os.Bundle

class ScratchActivity extends Activity with TypedActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    findView(TR.textview).setText("hello, world!")
  }
}
```

You might wonder what `TypedActivity`, `findView` and `TR` are, and we'll see
that later when we see [Typed Activities](). Other than that, it's very similar to the Java version, which you can see here :

```java
package com.scratch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

class ScratchActivity extends Activity {
  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.main);
    ((TextView)findView(R.textview)).setText("hello, world!");
  }
}
```

# Building the project

Now that we've created our first project, we can of course build it. Start a
SBT console by running `sbt` in a terminal :

```
$ sbt
[info] Loading project definition from /Users/fx/Documents/Projects/android-scratch/project
[info] Updating {file:/Users/fx/Documents/Projects/android-scratch/project/}default-b61a5b...
[info] Resolving org.scala-sbt#precompiled-2_10_0;0.12.2 ...
[info] Done updating.
[info] Set current project to Scratch (in build file:/Users/fx/Documents/Projects/android-scratch/)
>
```

It will automatically fetch and prepare dependencies and give you a nice prompt
where you can type in commands and inspect the configuration. For now, let's
type the `start` command to package our application and start it on a connected
device or emulator :

```
> start
[info] Generated /Users/fx/Documents/Projects/android-scratch/target/scala-2.10/src_managed/main/AndroidManifest.xml
[info] Running AAPT for package com.scratch
[info] Wrote /Users/fx/Documents/Projects/android-scratch/target/scala-2.10/src_managed/main/scala/com/scratch/TR.scala
[info] Compiling 2 Scala sources and 2 Java sources to /Users/fx/Documents/Projects/android-scratch/target/scala-2.10/classes...
[info] Executing Proguard (configuration written to /Users/fx/Documents/Projects/android-scratch/target/scala-2.10/src_managed/main/proguard.txt)
ProGuard, version 4.8
ProGuard is released under the GNU General Public License. You therefore
...
Preparing output jar [/Users/fx/Documents/Projects/android-scratch/target/classes-scratch-compile-0.1.min.jar]
  Copying resources from program directory [/Users/fx/Documents/Projects/android-scratch/target/scala-2.10/classes]
  Copying resources from program jar [/Users/fx/.sbt/boot/scala-2.10.1/lib/scala-library.jar] (filtered)
[info] Dexing /Users/fx/Documents/Projects/android-scratch/target/classes-scratch-compile-0.1.dex
[info] Packaging /Users/fx/Documents/Projects/android-scratch/target/scratch-compile-0.1.apk
[info] Installing scratch-compile-0.1.apk
[success] Total time: 32 s, completed Jun 2, 2013 5:00:22 PM
>
```

_**Note:** You can run the SBT command `emulator-start <avd-name>` to start an
emulator._

_**Note:** If you just want to generate an APK package, use the `apk` task
instead of `start`._

The default behavior of `start` is to use the first device/emulator it finds.
If that is not suitable, you can set the ADB target by using one of these
settings :

```
adbTarget := Target.Auto        -- Use the first available target
adbTarget := Target.Device      -- Use the first available device
adbTarget := Target.Emulator    -- Use the first available emulator
adbTarget := Target.UID("uid")  -- Use a target matching the given UID
```

Use the `set` SBT command to set them while running SBT. For example :

```
> set adbTarget := Target.Emulator
[info] Defining *:adb-target
[info] Reapplying settings...
[info] Set current project to Scratch (in ...

> start    -- Will use "adb -e" to select an emulator
```
