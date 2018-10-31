// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.io.{BufferedReader, FileInputStream, InputStreamReader}

import com.typesafe.scalalogging.LazyLogging
import monix.reactive.Observable
import mu.node.healthttpd.Healthttpd
import pureconfig._
import wvlet.airframe._

object Main extends App with LazyLogging {

  override def main(args: Array[String]): Unit = {
    val config = loadConfigOrThrow[Config]

    val linesFileLoader: LinesFileLoader = (filePath) => {
      logger.info(s"Loading places from ${filePath}")
      val reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))
      Observable.fromLinesReader(reader)
    }

    newDesign
      .bind[Config].toInstance(config)
      .bind[Healthttpd].toInstance(Healthttpd(config.statusPort))
      .bind[LinesFileLoader].toInstance(linesFileLoader)
      .noLifeCycleLogging
      .withSession(_.build[Application].run)
  }
}
