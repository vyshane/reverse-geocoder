name := "reverse-geocoder"
scalaVersion := "2.12.7"

enablePlugins(JavaAppPackaging)

resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
resolvers += Resolver.bintrayRepo("vyshane", "maven")

libraryDependencies ++= Seq(
  // Configuration
  "com.github.pureconfig" %% "pureconfig" % "0.9.2",
  // Dependency Injection
  "org.wvlet.airframe" %% "airframe" % "0.69",
  // Effects
  "io.monix" %% "monix" % "2.3.3",
  // gRPC and Protocol Buffers
  "io.grpc" % "grpc-netty-shaded" % "1.15.1",
  "io.grpc" % "grpc-stub" % "1.15.1",
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "beyondthelines" %% "grpcmonixruntime" % "0.0.7",
  // Monitoring
  "mu.node" %% "healthttpd" % "0.1.0"
)

// Protobuf/gRPC code generation
PB.targets in Compile := Seq(
  scalapb.gen(grpc = true, flatPackage = true) -> (sourceManaged in Compile).value,
  grpcmonix.generators.GrpcMonixGenerator(flatPackage = true) -> (sourceManaged in Compile).value
)

// Code formatting
scalafmtConfig := file(".scalafmt.conf")
