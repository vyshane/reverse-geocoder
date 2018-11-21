// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder
import java.time.Instant

import monix.eval.Task

class ClockInterpreter extends Clock[Task] {

  override def now(): Task[Instant] = Task.now(Instant.now())
}
