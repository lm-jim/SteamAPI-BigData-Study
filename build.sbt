ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "SteamAPI_BigData_Study"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.2",
  "org.apache.spark" %% "spark-sql" % "3.3.2",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.7.0",
  "com.typesafe.akka" %% "akka-stream" % "2.7.0",
  "com.typesafe.akka" %% "akka-http" % "10.4.0",
  "com.typesafe.play" %% "play-json" % "2.9.4"
)

assembly / assemblyMergeStrategy := {
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("org", "aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("google", "protobuf", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case x if x.endsWith("descriptor.proto") => MergeStrategy.discard
  case x if x.endsWith("arrow-git.properties") => MergeStrategy.discard
  case x if x.endsWith("module-info.class") => MergeStrategy.last
  case x if x.endsWith("AuthenticationType.class") => MergeStrategy.last
  case x if x.endsWith("Log4j2Plugins.dat") => MergeStrategy.last
  case "git.properties" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

