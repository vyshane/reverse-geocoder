// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.io.{BufferedReader, FileInputStream, InputStreamReader}

import com.typesafe.scalalogging.Logger
import monix.reactive.Observable
import mu.node.healthttpd.Healthttpd
import pureconfig._
import wvlet.airframe._

object Main extends App {
  // Main entry point
  // Wire up dependencies and start the application
  override def main(args: Array[String]): Unit = {
    val logger = Logger[this.type]
    val config = loadConfigOrThrow[Config]

    val linesFileLoader: LinesFileLoader = (filePath) => {
      logger.info(s"Loading places from ${filePath} ...")
      val reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))
      Observable.fromLinesReader(reader)
    }

    val design = newDesign
      .bind[Config].toInstance(config)
      .bind[Healthttpd].toInstance(Healthttpd(config.statusPort))
      .bind[LinesFileLoader].toInstance(linesFileLoader)

    design.withSession { session =>
      val app = session.build[Application]
      app.run
    }
  }
}
