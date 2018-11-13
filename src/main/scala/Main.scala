// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.io.{BufferedReader, FileInputStream, InputStreamReader}
import java.time.Instant

import com.thesamet.spatial.KDTreeMap
import com.typesafe.scalalogging.LazyLogging
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import mu.node.healthttpd.Healthttpd
import pureconfig._
import wvlet.airframe._

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App with LazyLogging {

  override def main(args: Array[String]): Unit = {
    val config = loadConfigOrThrow[Config]

    // Wire up dependencies
    newDesign
      .bind[Config].toInstance(config)
      .bind[Clock].toInstance(clock)
      .bind[Healthttpd].toInstance(Healthttpd(config.statusPort))
      .bind[LinesFileReader].toInstance(fileReader)

      // Load places from disk immediately upon startup
      .bind[KDTreeMap[Location, Place]].toEagerSingletonProvider(loadPlacesBlocking)

      // Startup
      .withProductionMode
      .noLifeCycleLogging
      .withSession(_.build[Application].run())

    // Side effects are injected at the edge:

    lazy val fileReader: LinesFileReader = () => {
      logger.info(s"Loading places from ${config.placesFilePath}")
      val reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(config.placesFilePath), "UTF-8")
      )
      Observable.fromLinesReader(reader)
    }

    lazy val loadPlacesBlocking: PlacesLoader => KDTreeMap[Location, Place] = { loader =>
      Await.result(loader.load().runAsync, 1 minute)
    }

    lazy val clock: Clock = {
      Observable
        .interval(1 second)
        .map(_ => Instant.now())
    }
  }
}
