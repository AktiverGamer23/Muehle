val scala3Version = "3.7.3"

lazy val root = (project in file("."))
  .settings(
    name := "Scalaerstes",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
   // run / fork := true,
    // Dependencies
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.scalafx" %% "scalafx" % "20.0.0-R31",   // ScalaFX for Scala 3
      "net.codingwell" %% "scala-guice" % "7.0.0",
      "org.scala-lang.modules" %% "scala-xml" % "2.1.0",  // XML support
      "com.typesafe.play" %% "play-json" % "2.10.1"       // JSON support
    ),
    Compile / mainClass := Some("de.htwg.se.muehle.Main")
  )
