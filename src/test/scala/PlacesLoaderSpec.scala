// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.thesamet.spatial.KDTreeMap
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.scalatest.{AsyncWordSpec, Matchers}

class PlacesLoaderSpec extends AsyncWordSpec with Matchers {

  "PlacesLoader" when {
    "loading an empty file" should {
      "return an empty KDTreeMap" in {
        val placesLoader = new PlacesLoader(() => Observable(""))
        placesLoader.load().runAsync.map { k =>
          k shouldEqual KDTreeMap.empty[Location, Place]
        }
      }
    }
    "loading a non-empty file" should {
      "skip invalid lines and only load well-formed lines" in {
        val placesLoader = new PlacesLoader(
          () =>
            Observable(
              "100\tInvalid Line\t\t\t\t\t",
              "934131\tQuatre Bornes\tQuatre Bornes\tQuatre " +
                "Bornes\t-20.26381\t57.4791\tP\tPPL\tMU\t\t17\t\t\t\t80961\t\t335\tIndian/Mauritius\t2012-05-18"
          ))
        placesLoader.load().runAsync.map { k =>
          k.size shouldEqual 1
          k.get((-20.26381, 57.4791)).get shouldEqual Place(
            name = "Quatre Bornes",
            countryCode = "MU",
            latitude = -20.26381,
            longitude = 57.4791,
            elevationMeters = 0,
            timezone = "Indian/Mauritius",
            population = 80961
          )
        }
      }
    }
  }
}
