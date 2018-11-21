// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.time.{Instant, ZoneId}

import com.google.protobuf.timestamp.Timestamp
import com.thesamet.spatial.KDTreeMap
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import net.time4j.PlainDate
import net.time4j.calendar.astro.{SolarTime, StdSolarCalculator}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, Matchers}

class ReverseGeocodeLocationRpcSpec extends AsyncWordSpec with AsyncMockFactory with Matchers {

  "ReverseGeocoderService" when {
    "asked to reverse geocode a location it can't find" should {
      "return an empty response" in {
        val clock = mock[Clock[Task]]
        val config = mock[Config]
        val rpc = new ReverseGeocodeLocationRpc(KDTreeMap(), clock, config)
        rpc.handle(ReverseGeocodeLocationRequest(0, 0)).runAsync map { r =>
          r shouldEqual ReverseGeocodeLocationResponse()
        }
      }
    }
    "asked to reverse geocode a location it can find" should {
      "find the nearest place and calculate the sunrise and sunset times at that place" in {
        val now = Instant.now()
        val perthZoneId = ZoneId.of("Australia/Perth")
        val perthLatitude = -31.9505
        val perthLongitude = 115.8605
        val perthElevation = 32

        val perthDate = PlainDate.from(now.atZone(perthZoneId).toLocalDate)
        val solarTime = SolarTime.ofLocation(perthLatitude, perthLongitude, perthElevation, StdSolarCalculator.TIME4J)
        val sunrise = solarTime.sunrise().apply(perthDate).get
        val sunset = solarTime.sunset().apply(perthDate).get

        val perth = Place(
          name = "Perth",
          countryCode = "AU",
          latitude = perthLatitude,
          longitude = perthLongitude,
          elevationMeters = perthElevation,
          timezone = "Australia/Perth",
          population = 1945000,
          sunriseToday = Some(Timestamp(sunrise.getPosixTime, sunrise.getNanosecond)),
          sunsetToday = Some(Timestamp(sunset.getPosixTime, sunset.getNanosecond))
        )

        val clock = mock[Clock[Task]]
        val config = mock[Config]

        (clock.now _)
          .expects()
          .returns(Task.now(now))

        val rpc = new ReverseGeocodeLocationRpc(
          KDTreeMap(
            (perthLatitude, perthLongitude) -> perth,
            (-20.26381, 57.4791) -> Place(name = "Quatre Bornes", countryCode = "MU")
          ),
          clock,
          config
        )
        rpc.handle(ReverseGeocodeLocationRequest(-30, 100)).runAsync map { r =>
          r shouldEqual ReverseGeocodeLocationResponse(Option(perth))
        }
      }
    }
  }
}
