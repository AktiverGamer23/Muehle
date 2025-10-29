val scala3Version = "3.7.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Scalaerstes",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    
    coverageEnabled := true,

    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
    

  )
