organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies ++= {
  val akkaV = "2.5.6"
  val sprayV = "1.3.3"
  val akkaHttpV = "10.0.10"

  Seq(
    "com.typesafe.akka"   %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka"   %% "akka-http"      % akkaHttpV,
    "com.typesafe.akka"   %% "akka-http-spray-json" % akkaHttpV,
    "io.spray"            %%  "spray-json" % sprayV,
    "com.typesafe.akka"   %%  "akka-http-testkit" % akkaHttpV  % "test",
    "org.scalatest"       %%  "scalatest"   % "3.0.1" % "test",
    "org.mockito" % "mockito-all" % "1.8.4"
  )
}

Revolver.settings
