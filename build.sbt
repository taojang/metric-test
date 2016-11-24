name := "metric-test"

scalaVersion := "2.11.8" // Also supports 2.10.x

lazy val http4sVersion = "0.15.0-SNAPSHOT"

// Only necessary for SNAPSHOT releases
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-server-metrics" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-scala-xml" % http4sVersion
)
