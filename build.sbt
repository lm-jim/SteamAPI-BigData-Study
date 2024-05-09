ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "PruebasSpark"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.2",
  "org.apache.spark" %% "spark-sql" % "3.3.2",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.7.0",
  "com.typesafe.akka" %% "akka-stream" % "2.7.0",
  "com.typesafe.akka" %% "akka-http" % "10.4.0",
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "org.plotly-scala" %% "plotly-render" % "0.8.1"
)