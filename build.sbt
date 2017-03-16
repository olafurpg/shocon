import SonatypeKeys._
import sbtcrossproject.{crossProject, CrossType}



val commonSettings = Vector(
  name := "shocon",
  organization := "eu.unicredit",
  version := "0.1.7-native",
  scalaVersion := "2.11.8",
  crossScalaVersions  := Vector("2.11.8", "2.12.0", "2.12.1")
)

lazy val root = project.in(file(".")).
  settings(commonSettings: _*).
  aggregate(shoconJS, shoconJVM, shoconNative)

  lazy val fixResources = taskKey[Unit](
    "Fix application.conf presence on first clean build.")

lazy val shocon = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file(".")).
  settings(commonSettings: _*).
  settings(
    scalacOptions ++=
      Seq(
        "-feature",
        "-unchecked",
        "-language:implicitConversions"
      )
  ).
  settings(
    sonatypeSettings: _*
  ).
  settings(
    fixResources := {
      val compileConf = (resourceDirectory in Compile).value / "application.conf"
      if (compileConf.exists)
        IO.copyFile(
          compileConf,
          (classDirectory in Compile).value / "application.conf"
        )
      val testConf = (resourceDirectory in Test).value / "application.conf"
      if (testConf.exists) {
        IO.copyFile(
          testConf,
          (classDirectory in Test).value / "application.conf"
        )
      }
    },
    compile in Compile <<= (compile in Compile) dependsOn fixResources,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "0.4.3-native",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    ),
    pomExtra := {
      <url>https://github.com/unicredit/shocon</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/unicredit/shocon</connection>
        <developerConnection>scm:git:git@github.com:unicredit/shocon</developerConnection>
        <url>github.com/unicredit/shocon</url>
      </scm>
      <developers>
        <developer>
          <id>evacchi</id>
          <name>Edoardo Vacchi</name>
          <url>https://github.com/evacchi/</url>
        </developer>
        <developer>
          <id>andreaTP</id>
          <name>Andrea Peruffo</name>
          <url>https://github.com/andreaTP/</url>
        </developer>
      </developers>
    }
  ).
  jvmSettings(
  	libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test"
  ).
  jsConfigure(
    _.enablePlugins(ScalaJSJUnitPlugin)
  ).
  jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    scalaJSUseRhino in Global := true,
    parallelExecution in Test := true
  )

lazy val shoconJVM = shocon.jvm
lazy val shoconJS = shocon.js
lazy val shoconNative = shocon.native

publishMavenStyle := true

pomIncludeRepository := { x => false }

credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.credentials")
