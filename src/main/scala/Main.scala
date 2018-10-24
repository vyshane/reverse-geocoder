// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import mu.node.healthttpd.Healthttpd
import pureconfig._
import wvlet.airframe._

object Main extends App {

  // Main entry point for the application
  override def main(args: Array[String]): Unit = {
    val config = loadConfigOrThrow[Config]

    // Wire up dependencies
    val design = newDesign
      .bind[Config].toInstance(config)
      .bind[Healthttpd].toInstance(Healthttpd(config.statusPort))

    design.withSession { session =>
      val app = session.build[Application]
      app.run
    }
  }
}
