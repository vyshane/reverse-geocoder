// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.io.{BufferedReader, FileInputStream, InputStreamReader}

import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import monix.reactive.Observable

class FileReaderInterpreter extends FileReader[Task] with LazyLogging {

  override def readLines(path: String): Task[Seq[String]] = {
    logger.info(s"Loading places from $path")
    val reader = new BufferedReader(
      new InputStreamReader(new FileInputStream(path), "UTF-8")
    )
    Observable.fromLinesReader(reader).toListL
  }
}
