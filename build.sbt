name := "metric-test"

scalaVersion := "2.11.8" // Also supports 2.10.x

lazy val http4sVersion = "0.15.0-SNAPSHOT"
lazy val slf4jVersion = "1.7.21"

// Only necessary for SNAPSHOT releases
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-server-metrics" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-scala-xml" % http4sVersion,
  "io.circe"   %% "circe-generic" % "0.6.0",
  "org.slf4j"                   % "log4j-over-slf4j" % slf4jVersion,
  "org.slf4j"                   % "jcl-over-slf4j"   % slf4jVersion,
  "org.slf4j"                   % "jul-to-slf4j"     % slf4jVersion,
  "ch.qos.logback"              % "logback-classic"  % "1.1.7"
)

lazy val metrictest = (project in file (".")).
  settings(
    version in ThisBuild := "0.1.0",
    promulgateVersionSettings,
    assemblyJarName in assembly := "metric-test.jar"
  )

