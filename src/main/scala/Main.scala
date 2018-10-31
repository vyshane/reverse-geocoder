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

    val fileReader: LinesFileReader = () => {
      logger.info(s"Loading places from ${config.placesFilePath}")
      val reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(config.placesFilePath), "UTF-8")
      )
      Observable.fromLinesReader(reader)
    }

    newDesign
      .bind[Config].toInstance(config)
      .bind[Healthttpd].toInstance(Healthttpd(config.statusPort))
      .bind[LinesFileReader].toInstance(fileReader)
      .noLifeCycleLogging
      .withSession(_.build[Application].run)
  }
}
