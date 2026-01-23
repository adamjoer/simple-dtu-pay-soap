Feature: Token Management
  Scenario: Token request by customer
    Given a customer with name "Emily", last name "Clark", and CPR "231242-2133"
    And the customer is registered with the bank with an initial balance of "1500" kr
    And the customer is registered with Simple DTU Pay using their bank account
    When the customer requests 4 tokens
    Then the customer has 4 unused tokens left

  Scenario: Token exhaustion after multiple payments
    Given a customer with name "Frank", last name "Miller", and CPR "454323-1234"
    And the customer is registered with the bank with an initial balance of "3000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 3 unused tokens
    And a merchant with name "Grace", last name "Lee", and CPR "111222-3333"
    And the merchant is registered with the bank with an initial balance of "1000" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the customer initiates a payment for "100" kr using a token
    Then the payment is successful
    And the customer has 2 unused tokens left
    When the customer initiates a payment for "150" kr using a token
    Then the payment is successful
    And the customer has 1 unused tokens left
    When the customer initiates a payment for "200" kr using a token
    Then the payment is successful
    And the customer has 0 unused tokens left
    And the balance of the customer at the bank is "2550" kr
    And the balance of the merchant at the bank is "1450" kr

  Scenario: Payment attempt with invalid token
    Given a customer with name "Hannah", last name "Davis", and CPR "567890-4321"
    And the customer is registered with the bank with an initial balance of "2000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And a merchant with name "Ian", last name "Wilson", and CPR "444555-6666"
    And the merchant is registered with the bank with an initial balance of "800" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the customer initiates a payment for "50" kr using an invalid token
    Then the payment fails
    And the error message is "Invalid or used token"
    And the balance of the customer at the bank is "2000" kr
    And the balance of the merchant at the bank is "800" kr

  Scenario: Token request denied when customer has more than 1 unused token
    Given a customer with name "Test", last name "User", and CPR "111111-1111"
    And the customer is registered with the bank with an initial balance of "1000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 3 unused tokens
    When the customer requests 2 tokens
    Then the token request fails with error containing "unused tokens"
    And the customer has 3 unused tokens left

  Scenario: Token request denied when requesting more than 5 tokens
    Given a customer with name "Test", last name "User", and CPR "222222-2222"
    And the customer is registered with the bank with an initial balance of "1000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    When the customer requests 6 tokens
    Then the token request fails with error containing "between 1 and 5"

  Scenario: Token request denied when it would exceed maximum 6 unused tokens
    Given a customer with name "Test", last name "User", and CPR "333333-3333"
    And the customer is registered with the bank with an initial balance of "1000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 1 unused tokens
    When the customer requests 5 tokens
    Then the token request fails with error containing "exceed maximum"
