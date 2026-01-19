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
    And the customer has 2 unused tokens

#  Scenario: Customer makes multiple payments using different tokens
#    Given a customer with name "Alice", last name "Johnson", and CPR "123456-7890"
#    And the customer is registered with the bank with an initial balance of "2000" kr
#    And the customer is registered with Simple DTU Pay using their bank account
#    And a merchant with name "Bob", last name "Smith", and CPR "987654-3210"
#    And the merchant is registered with the bank with an initial balance of "500" kr
#    And the merchant is registered with Simple DTU Pay using their bank account
#    When the customer requests 5 tokens
#    Then the customer has 5 unused tokens
#    When the customer initiates a payment for "100" kr using a token
#    Then the payment is successful
#    And the customer has 4 unused tokens
#    When the customer initiates a payment for "50" kr using a token
#    Then the payment is successful
#    And the customer has 3 unused tokens
#    And the balance of the customer at the bank is "1850" kr
#    And the balance of the merchant at the bank is "650" kr
#
#  Scenario: Customer requests more tokens after using some
#    Given a customer with name "Charlie", last name "Brown", and CPR "555555-5555"
#    And the customer is registered with the bank with an initial balance of "5000" kr
#    And the customer is registered with Simple DTU Pay using their bank account
#    And a merchant with name "Diana", last name "Prince", and CPR "777777-7777"
#    And the merchant is registered with the bank with an initial balance of "1000" kr
#    And the merchant is registered with Simple DTU Pay using their bank account
#    When the customer requests 2 tokens
#    Then the customer has 2 unused tokens
#    When the customer initiates a payment for "200" kr using a token
#    Then the payment is successful
#    And the customer has 1 unused tokens
#    When the customer requests 5 tokens
#    Then the customer has 6 unused tokens
#    And the balance of the customer at the bank is "4800" kr
#    And the balance of the merchant at the bank is "1200" kr
