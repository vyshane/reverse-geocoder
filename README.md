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
    google.protobuf.Timestamp sunrise_today = 8;
    google.protobuf.Timestamp sunset_today = 9;
}
```

## Building

```text
make docker
```

By default, the Docker image will download and use the [cities500.zip](http://download.geonames.org/export/dump/cities500.zip) data file from Geonames.

## Running

The easiest way to run reverse-geocoder is using Docker:

```text
docker run -d vyshane/reverse-geocoder
```

You then can make a test request to the service using [grpcurl](https://github.com/fullstorydev/grpcurl). The application serves the reverse geocoder gRPC service on port 8080.

```text
grpcurl -plaintext \
    -proto src/main/protobuf/reverse-geocoder.proto \
    -d '{"latitude": -20.2664803, "longitude": 57.4679569}' \
    localhost:8080 \
    mu.node.reversegeocoder.ReverseGeocoder/ReverseGeocodeLocation
```

Sample output of above call, in JSON format:

```JSON
{
  "place": {
    "name": "Quatre Bornes",
    "countryCode": "MU",
    "latitude": -20.26381,
    "longitude": 57.4791,
    "timezone": "Indian/Mauritius",
    "population": 80961,
    "sunriseToday": "2018-11-13T01:23:08Z",
    "sunsetToday": "2018-11-13T14:25:42Z"
  }
}
```

### Health and Readiness Status

reverse-geocoder will report health and readiness on port 3401 via HTTP, at /health and /readiness respectively. You can poll /readiness during startup to know when reverse-geocoder is ready to be added to your load balancer.

```text
❱ curl -i http://localhost:3401/readiness                                                                                                                                                                                              master 
HTTP/1.1 200 OK 
Content-Type: text/html; charset=utf-8
Date: Tue, 13 Nov 2018 13:48:52 GMT
Connection: keep-alive
Content-Length: 5

Ready%
```

## Configuration

reverse-geocoder can be configured through the following environment variables.

| Environment Variable | Default Value |
| -------------------- | ------------- |
| GRPC_PORT            | 8080          |
| STATUS_PORT          | 3401          |
| PLACES_FILE_PATH     | N/A           |
