val akkaVersion = "2.5.13"

lazy val root = (project in file("."))
  .settings(
    name := "scala-akka-essentials",
    scalaVersion := "2.12.7",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.5",
    )
  )
