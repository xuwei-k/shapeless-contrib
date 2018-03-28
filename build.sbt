import com.typesafe.sbt.pgp.PgpKeys.publishSigned
import sbtcrossproject.{crossProject, CrossType}
import ReleaseTransformations._

val Scala212 = "2.12.4"

lazy val buildSettings = Seq(
  organization := "com.github.xuwei-k",
  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala212, "2.11.12")
)

val scalapropsVersion = "0.5.4"
val shapelessVersion = "2.3.3"
val scalazVersion = "7.2.20"
val scalatestVersion = "3.0.5-M1"
val specs2Version = "4.0.3"
val scalazSpecs2Version = "0.5.2"

lazy val commonSettings = Seq(
  scalacOptions := Seq(
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-deprecation",
    "-unchecked"
  ),
  scmInfo :=
    Some(ScmInfo(
      url("https://github.com/xuwei-k/shapeless-scalaz"),
      "scm:git:git://github.com/xuwei-k/shapeless-scalaz.git"
    ))
)

lazy val commonJsSettings = Seq(
  parallelExecution in Test := false
)

lazy val commonJvmSettings = Seq(
  parallelExecution in Test := false
)

lazy val coreSettings = buildSettings ++ commonSettings ++ publishSettings

coreSettings
sources in Compile := Nil
sources in Test := Nil
noPublishSettings

lazy val shapelessScalaz = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    moduleName := "shapeless-scalaz",
    coreSettings,
    scalapropsCoreSettings,
    libraryDependencies ++= Seq(
      "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % "1.1.8" % "test",
      "com.github.scalaprops" %%% "scalaprops-magnolia" % "0.1.2" % "test",
      "com.chuusai" %%% "shapeless" % shapelessVersion,
      "org.scalaz" %%% "scalaz-core" % scalazVersion,
      "com.github.scalaprops" %%% "scalaprops" % scalapropsVersion % "test",
      "com.github.scalaprops" %%% "scalaprops-scalazlaws" % scalapropsVersion % "test",
      "org.specs2" %%% "specs2-core" % specs2Version % "test",
      "org.scalaz" %%% "scalaz-scalacheck-binding" % scalazVersion % "test",
      "org.typelevel" %%% "shapeless-scalacheck" % "0.6.1" % "test",
      "org.typelevel" %%% "scalaz-specs2" % scalazSpecs2Version % "test"
    )
  )
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val jvm = shapelessScalaz.jvm.withId("jvm")
lazy val js = shapelessScalaz.js.withId("js")

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }.value,
  homepage := Some(url("https://github.com/xuwei-k/shapeless-scalaz")),
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
  scmInfo :=
    Some(ScmInfo(
      url("https://github.com/xuwei-k/shapeless-scalaz"),
      "scm:git:git://github.com/xuwei-k/shapeless-scalaz.git"
    )),
  pomExtra := (
    <developers>
      <developer>
        <id>larsrh</id>
        <name>Lars Hupel</name>
        <url>https://github.com/larsrh</url>
      </developer>
    </developers>
  )
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val sharedReleaseProcess = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
