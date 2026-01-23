Feature: Payment with Token Service

  Scenario: Customer requests tokens and makes a successful payment
    Given a customer with name "Susan", last name "Baldwin", and CPR "422342-4323"
    And the customer is registered with the bank with an initial balance of "1000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 3 unused tokens
    And a merchant with name "Daniel", last name "Oliver", and CPR "693829-2910"
    And the merchant is registered with the bank with an initial balance of "1000" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the customer initiates a payment for "10" kr using a token
    Then the payment is successful
    And the balance of the customer at the bank is "990" kr
    And the balance of the merchant at the bank is "1010" kr
    And the customer has 2 unused tokens left

  Scenario: Customer makes multiple payments using different tokens
    Given a customer with name "Alice", last name "Johnson", and CPR "532512-1253"
    And the customer is registered with the bank with an initial balance of "2000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 5 unused tokens
    And a merchant with name "Bob", last name "Smith", and CPR "532595-9212"
    And the merchant is registered with the bank with an initial balance of "500" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the customer initiates a payment for "100" kr using a token
    Then the payment is successful
    And the customer has 4 unused tokens left
    When the customer initiates a payment for "50" kr using a token
    Then the payment is successful
    And the customer has 3 unused tokens left
    And the balance of the customer at the bank is "1850" kr
    And the balance of the merchant at the bank is "650" kr

  Scenario: Customer requests more tokens after using some
    Given a customer with name "Charlie", last name "Brown", and CPR "849034-3575"
    And the customer is registered with the bank with an initial balance of "5000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 2 unused tokens
    And a merchant with name "Diana", last name "Prince", and CPR "420420-0420"
    And the merchant is registered with the bank with an initial balance of "1000" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the customer initiates a payment for "200" kr using a token
    Then the payment is successful
    And the balance of the customer at the bank is "4800" kr
    And the balance of the merchant at the bank is "1200" kr
    And the customer has 1 unused tokens left
    When the customer requests 5 tokens
    Then the customer has 6 unused tokens left

  Scenario: Payment fails with unknown merchant
    Given a customer with name "Test", last name "Customer", and CPR "432362-5635"
    And the customer is registered with the bank with an initial balance of "1000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 1 unused tokens
    When the customer initiates a payment for "50" kr to an unknown merchant using a token
    Then the payment fails
    And the error message contains "not found"
    And the balance of the customer at the bank is "1000" kr

  Scenario: Payment fails due to insufficient funds
    Given a customer with name "Broke", last name "Customer", and CPR "63416374-2234"
    And the customer is registered with the bank with an initial balance of "50" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 1 unused tokens
    And a merchant with name "Rich", last name "Merchant", and CPR "533809-6545"
    And the merchant is registered with the bank with an initial balance of "1000" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the customer initiates a payment for "100" kr using a token
    Then the payment fails
    And the error message contains "Debtor balance will be negative"
    And the balance of the customer at the bank is "50" kr
    And the balance of the merchant at the bank is "1000" kr
