# DTU Pay

## Requirements
- Java 21 or higher
- Maven 3.8.1 or higher
- Docker (for building native executables in a container)

## Running the project in dev mode

To run the project in dev mode, navigate to the `service` directory and execute:

```shell
mvn quarkus:dev
```

To test the application, navigate to the `service` directory and execute:

```shell
mvn test
```

> **_NOTE:_**  The service needs to be running on `localhost:8080` before executing the tests.


## Production service

The service is currently running on http://fm-22.compute.dtu.dk:80.
