// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import cats.Id
import com.thesamet.spatial.KDTreeMap
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

class PlacesLoaderInterpreterSpec extends WordSpec with MockFactory with Matchers {

  "PlacesLoader" when {
    "loading an empty file" should {
      "return an empty KDTreeMap" in {
        val fileReader = mock[FileReader[Id]]
        val placesLoader = new PlacesLoaderInterpreter(fileReader)

        (fileReader.readLines _)
          .expects("path")
          .returns(Seq())

        placesLoader.load("path") shouldEqual KDTreeMap.empty[Location, Place]
      }
    }
    "loading a non-empty file" should {
      "skip invalid lines and only load well-formed lines" in {
        val fileReader = mock[FileReader[Id]]
        val placesLoader = new PlacesLoaderInterpreter(fileReader)

        (fileReader.readLines _)
          .expects("path")
          .returns(Seq(
            "100\tInvalid Line\t\t\t\t\t",
            "934131\tQuatre Bornes\tQuatre Bornes\tQuatre " +
              "Bornes\t-20.26381\t57.4791\tP\tPPL\tMU\t\t17\t\t\t\t80961\t\t335\tIndian/Mauritius\t2012-05-18"
          ))

        val places = placesLoader.load("path")
        places.size shouldEqual 1

        places.get((-20.26381, 57.4791)).get shouldEqual Place(
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
