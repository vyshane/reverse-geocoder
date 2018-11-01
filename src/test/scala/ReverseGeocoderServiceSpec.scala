// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.thesamet.spatial.KDTreeMap
import monix.execution.Scheduler.Implicits.global
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, Matchers}

class ReverseGeocoderServiceSpec extends AsyncWordSpec with Matchers with AsyncMockFactory {

  "ReverseGeocoderService" when {
    "asked to reverse geocode a location it can't find" should {
      "return an empty response" in {
        val reverseGeocodeService = new ReverseGeocoderService(KDTreeMap())
        reverseGeocodeService.reverseGeocodeLocation(ReverseGeocodeLocationRequest(0, 0)).runAsync map { r =>
          r shouldEqual ReverseGeocodeLocationResponse()
        }
      }
    }
    "asked to reverse geocode a location it can find" should {
      "return the matching place in the response" in {
        val reverseGeocodeService = new ReverseGeocoderService(KDTreeMap(
          (0.0, 0.0) -> Place()
        ))
        reverseGeocodeService.reverseGeocodeLocation(ReverseGeocodeLocationRequest(0, 0)).runAsync map { r =>
          r shouldEqual ReverseGeocodeLocationResponse(Option(Place()))
        }
      }
    }
  }
}
