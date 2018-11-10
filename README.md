# reverse-geocoder

Reverse geocoding served over gRPC, written in Scala. Location data is obtained from [Geonames](http://download.geonames.org/export/dump/).

> Reverse geocoding is the process of back (reverse) coding of a point location (latitude, longitude) to a readable address or place name. [[Wikipedia](https://en.wikipedia.org/wiki/Reverse_geocoding)]

## API

The API is defined in [reverse-geocoder.proto](src/main/protobuf/reverse-geocoder.proto):

```protobuf
service ReverseGeocoder {
    rpc ReverseGeocodeLocation (ReverseGeocodeLocationRequest) returns (ReverseGeocodeLocationResponse) {};
}

message ReverseGeocodeLocationRequest {
    double latitude = 1;
    double longitude = 2;
}

message ReverseGeocodeLocationResponse {
    // Empty if none found
    Place place = 1;
}

message Place {
    string name = 1;
    string country_code = 2;
    double latitude = 3;
    double longitude = 4;
    uint32 elevation_meters = 5;
    string timezone = 6;
    uint64 population = 7;
}
```

## Building

```shell
make docker
```

By default, the Docker image will download and use the [cities500.zip](http://download.geonames.org/export/dump/cities500.zip) data file from Geonames.

## Running

```shell
docker run vyshane/reverse-geocoder
```

By default the application will serve the reverse geocoder gRPC service over port 8080.

It will also report health and readiness via HTTP. At /health and /readiness respectively, on port 3401.

These can be configured via environment variables. See [application.conf](src/main/resources/application.conf) for 
configuration options.