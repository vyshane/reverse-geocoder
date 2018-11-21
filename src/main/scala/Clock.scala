// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder
import java.time.Instant

trait Clock[F[_]] {

  def now(): F[Instant]
}

