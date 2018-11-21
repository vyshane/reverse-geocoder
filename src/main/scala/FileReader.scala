// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

trait FileReader[F[_]] {

  def readLines(path: String): F[Seq[String]]
}
