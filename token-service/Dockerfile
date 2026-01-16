FROM eclipse-temurin:21 as jre-build
COPY target/lib /usr/src/lib
COPY target/token-service-1.0.0-SNAPSHOT.jar /usr/src/
WORKDIR /usr/src/
CMD java -Xmx32m -jar ./token-service-1.0.0-SNAPSHOT.jar
