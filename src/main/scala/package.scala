// Copyright 2018 Vy-Shane Xie

package mu.node
import monix.reactive.Observable

package object reversegeocoder {
  type Location = (Double, Double)
  type LinesFileLoader = String => Observable[String]
}
