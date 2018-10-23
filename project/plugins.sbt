addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.11")
addSbtPlugin("com.lucidchart"   % "sbt-scalafmt"        % "1.15")

// gRPC and Protocol Buffers
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.18")
resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.7.4",
  "beyondthelines" %% "grpcmonixgenerator" % "0.0.7"
)
