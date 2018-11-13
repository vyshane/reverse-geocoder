// Copyright 2018 Vy-Shane Xie

package mu.node

import java.time.Instant

import monix.reactive.Observable

package object reversegeocoder {
  type Latitude = Double
  type Longitude = Double
  type Location = (Latitude, Longitude)
  type LinesFileReader = () => Observable[String]
  type Clock = Observable[Instant]
}
