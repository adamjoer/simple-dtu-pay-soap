# Token Service

The Token Service manages token generation, validation, and lifecycle for DTU Pay customers.

## Features

- **Token Generation**: Generates unique, non-guessable tokens (32 characters)
- **Token Validation**: Validates tokens before payment processing
- **Token Lifecycle**: Tracks token usage and marks tokens as used after payment
- **Business Rules Enforcement**:
  - Customers can request 1-5 tokens at a time
  - Maximum of 6 unused tokens per customer
  - Can only request tokens when having 0 or 1 unused token
  - Each token can only be used once

## Message Topics

The service listens to and publishes the following topics:

- `CUSTOMER_TOKEN_REPLENISH_REQUESTED`: Request to generate new tokens
- `CUSTOMER_TOKEN_REPLENISH_COMPLETED`: Response with new tokens or error message
- `CUSTOMER_TOKEN_REQUESTED`: Request to get existing unused tokens
- `CUSTOMER_TOKEN_PROVIDED`: Response with list of unused tokens
- `TOKEN_VALIDATION_REQUESTED`: Request to validate a token
- `TOKEN_VALIDATION_PROVIDED`: Response with validation result
- `TOKEN_MARK_USED_REQUESTED`: Request to mark a token as used
- `TOKEN_MARK_USED_COMPLETED`: Confirmation that token was marked as used

## Building and Running

### Build
```bash
mvn clean package
```

### Run with Docker Compose
```bash
docker compose up --build
```

### Run Standalone
```bash
java -jar target/token-service-1.0.0-SNAPSHOT.jar
```

## Dependencies

- RabbitMQ (for message queue communication)
- messaging-utilities (for Event and MessageQueue interfaces)
