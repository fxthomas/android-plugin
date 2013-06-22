package sbtandroid

import sbt._
import Keys._

import AndroidPlugin._

object AndroidHelpers {

  def directory(path: SettingKey[File]) = path map (IO.createDirectory(_))

  /**
   * Finds out where the Android SDK is located on your system, based on :
   *   * Environment variables
   *   * The local.properties files
   */
  def determineAndroidSdkPath(envs: Seq[String], dir: File): File = {
    // Try to find the SDK path in the default environment variables
    val paths = for ( e <- envs; p = System.getenv(e); if p != null) yield p
    if (!paths.isEmpty) Path(paths.head).asFile

    // If not found, try to read the `local.properties` file
    else {
      val local = new File(dir, "local.properties")
      if (local.exists()) {
        (for (sdkDir <- (for (l <- IO.readLines(local);
             if (l.startsWith("sdk.dir")))
             yield l.substring(l.indexOf('=')+1)))
             yield new File(sdkDir)).headOption.getOrElse(
              sys.error("local.properties did not contain sdk.dir")
             )

      // If nothing is found either, display an error
      } else {
        sys.error(
          "Android SDK not found. You might need to set %s".format(envs.mkString(" or "))
        )
      }
    }
  }

  /**
   * Finds out which versions of the build-tools, if any, is the most recent
   */
  def determineBuildToolsVersion(sdkPath: File): Option[String] = {
    // Find out which versions of the build tools are installed
    val buildToolsPath = (sdkPath / "build-tools")

    // If this path doesn't exist, just set the version to ""
    if (!buildToolsPath.exists) None

    // Else, sort the installed versions and take the most recent
    else Some(buildToolsPath.listFiles.map(_.name).reduceLeft((s1, s2) => {

      // Convert the version numbers to arrays of integers
      val v1 = s1 split '.' map (_.toInt)
      val v2 = s2 split '.' map (_.toInt)

      // Compare them
      // (Will crash if the version numbers don't contain 3 digits)
      if((v1(0) > v2(0)) ||
        (v1(0) == v2(0) && v1(1) > v2(1)) ||
        (v1(0) == v2(0) && v1(1) == v2(1) && v1(2) > v2(1)))
        s1 else s2
    }))
  }

  def isWindows = System.getProperty("os.name").startsWith("Windows")
  def osBatchSuffix = if (isWindows) ".bat" else ""

  def dxMemoryParameter(javaOpts: String) = {
    // per http://code.google.com/p/android/issues/detail?id=4217, dx.bat
    // doesn't currently support -JXmx arguments.  For now, omit them in windows.
    if (isWindows) "" else javaOpts
  }

  /**
   * Retrieves an attribute from the AndroidManifest.xml file
   */
  def usesSdk(mpath: File, schema: String, key: String) =
    (manifest(mpath) \ "uses-sdk").head.attribute(schema, key).map(_.text.toInt)

  /**
   * Returns the name of the app's main activity.
   */
  def launcherActivity(schema: String, amPath: File, mPackage: String) = {

    val launcher = for (
      activity <- (manifest(amPath) \\ "activity");
      action <- (activity \\ "action");
      name <- action.attribute(schema, "name")
      if name == "android.intent.action.MAIN"
    ) yield {
      if (name.text.contains(".")) name.text else mPackage + "." + name.text
    }

    launcher.headOption.getOrElse(sys.error("Cannot find main activity"))
  }

  /**
   * Loads the AndroidManifest.xml file at path `mpath`
   */
  def manifest(mpath: File) = xml.XML.loadFile(mpath)
}
