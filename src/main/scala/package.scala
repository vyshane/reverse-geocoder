// Copyright 2018 Vy-Shane Xie

package mu.node
import monix.reactive.Observable

package object reversegeocoder {
  type Latitude = Double
  type Longitude = Double
  type Location = (Latitude, Longitude)
  type LinesFileLoader = String => Observable[String]
}
