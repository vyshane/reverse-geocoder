// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.thesamet.spatial.KDTreeMap
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
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
      .bind[Healthttpd].toInstance(Healthttpd(config.statusPort))
      .bind[FileReader[Task]].toInstance(new FileReaderInterpreter)
      .bind[PlacesLoaderInterpreter[Task]].toInstance(new PlacesLoaderInterpreter(new FileReaderInterpreter))
      .bind[Clock[Task]].toInstance(new ClockInterpreter)

      // Load places from disk immediately upon startup
      .bind[KDTreeMap[Location, Place]].toEagerSingletonProvider(loadPlacesBlocking)

      // Startup
      .withProductionMode
      .noLifeCycleLogging
      .withSession(_.build[Application].run())

    // Side effects are injected at the edge:

    lazy val loadPlacesBlocking: PlacesLoaderInterpreter[Task] => KDTreeMap[Location, Place] = { loader =>
      Await.result(loader.load(config.placesFilePath).runAsync, 1 minute)
    }
  }
}
