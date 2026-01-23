Feature: Report management

  Scenario: Merchant requests a transaction report
    Given a merchant with name "Eve", last name "Adams", and CPR "69420-6767"
    And the merchant is registered with the bank with an initial balance of "1500" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    And a customer with name "Frank", last name "Miller", and CPR "923744-7843"
    And the customer is registered with the bank with an initial balance of "3000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 3 unused tokens
    When the customer initiates a payment for "250" kr using a token
    Then the payment is successful
    When the merchant requests a transaction report
    Then the merchant report includes a transaction of "250" kr to "Eve Adams" and contains no customer information
    When the customer requests a transaction report
    Then the customer report includes a transaction of "250" kr from "Frank Miller" to "Eve Adams"

  Scenario: Manager requests a complete transaction report
    Given a customer with name "Alice", last name "Test", and CPR "457404-2346"
    And the customer is registered with the bank with an initial balance of "2000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    And the customer has 2 unused tokens
    And a merchant with name "Bob", last name "Shop", and CPR "663810-3237"
    And the merchant is registered with the bank with an initial balance of "500" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the customer initiates a payment for "100" kr using a token
    Then the payment is successful
    When the customer initiates a payment for "200" kr using a token
    Then the payment is successful
    When the manager requests a transaction report
    Then the manager report includes at least 2 transactions
    And the manager report includes a transaction of "100" kr from "Alice Test" to "Bob Shop"
    And the manager report includes a transaction of "200" kr from "Alice Test" to "Bob Shop"
