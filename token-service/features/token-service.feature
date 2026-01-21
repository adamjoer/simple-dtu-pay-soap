Feature: Token Service

  Scenario: Successful token generation
    When a "CustomerTokenReplenishRequested" event for customer requesting 3 tokens is received
    Then the "CustomerTokenReplenishCompleted" event is sent
    And the event contains 3 unique tokens

  Scenario: Token request denied - customer has more than 1 unused token
    Given a customer has 2 unused tokens
    When a "CustomerTokenReplenishRequested" event for that customer requesting 3 tokens is received
    Then the "CustomerTokenReplenishCompleted" event is sent with error message

  Scenario: Token request denied - exceeds maximum
    Given a customer has 1 unused token
    When a "CustomerTokenReplenishRequested" event for that customer requesting 6 tokens is received
    Then the "CustomerTokenReplenishCompleted" event is sent with error message

  Scenario: Token validation - valid unused token
    Given a customer has a valid unused token
    When a "TokenValidationRequested" event is received for that token
    Then the "TokenValidationProvided" event is sent with valid=true and customerId

  Scenario: Token validation - already used token
    Given a customer has a used token
    When a "TokenValidationRequested" event is received for that token
    Then the "TokenValidationProvided" event is sent with valid=false

  Scenario: Mark token as used
    Given a customer has a valid unused token
    When a "TokenMarkUsedRequested" event is received for that token
    Then the "TokenMarkUsedCompleted" event is sent with success=true
    And the token is marked as used
