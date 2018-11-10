FROM hseeberger/scala-sbt:8u181_2.12.7_1.2.6 as builder
WORKDIR /build
# Cache dependencies first
COPY project project
COPY build.sbt .
RUN sbt update
# Then build
COPY . .
RUN sbt stage
# Download Geonames file
RUN wget http://download.geonames.org/export/dump/cities500.zip
RUN unzip cities500.zip

FROM openjdk:8u181-jre-slim
WORKDIR /app
COPY --from=builder /build/target/universal/stage/. .
COPY --from=builder /build/cities500.txt .
ENV PLACES_FILE_PATH=/app/cities500.txt
RUN mv bin/$(ls bin | grep -v .bat) bin/start
CMD ["./bin/start"]