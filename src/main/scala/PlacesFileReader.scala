// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.io.{BufferedReader, FileInputStream, InputStreamReader}

import com.thesamet.spatial.KDTreeMap
import com.typesafe.scalalogging.Logger
import monix.eval.Task
import monix.reactive.Observable

import scala.util.Try

object PlacesFileReader {

  private val logger = Logger[this.type]
  type Location = (Double, Double)

  def load(filePath: String): Task[KDTreeMap[Location, Place]] = {
    implicit val ctx = monix.execution.Scheduler.Implicits.global
    val reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))

    Observable
      .fromLinesReader(reader)
      .map(toPlace)
      .foldLeftL(KDTreeMap.empty: KDTreeMap[Location, Place])(addPlace)
  }

  private def toLines(char: String, nextChar: String): String = {
    if (nextChar == "\n") char
    else char + nextChar
  }

  private def toPlace(line: String): Place = {
    val columns = line.split("\t")

    val place = Place(
      name = columns(1),
      admin1 = columns(10),
      admin2 = columns(11),
      admin3 = columns(12),
      countryCode = columns(8),
      longitude = columns(5).toDouble,
      latitude = columns(4).toDouble,
      elevationMeters = Try(columns(15).toInt).getOrElse(0),
      timezone = columns(17),
      population = Try(columns(14).toLong).getOrElse(0)
    )

    if (!columns(3).trim.isEmpty)
      place.copy(alternateNames = columns(3).split(","))
    else
      place
  }

  private def addPlace(map: KDTreeMap[Location, Place], place: Place): KDTreeMap[Location, Place] = {
//    logger.info(place.toProtoString)
    map + ((place.longitude, place.latitude) -> place)
  }
}
