Feature: User management

  Scenario: Customer information retrieval
    Given a customer with name "John", last name "Doe", and CPR "649023-5364"
    And the customer is registered with the bank with an initial balance of "2000" kr
    And the customer is registered with Simple DTU Pay using their bank account
    When the customer requests their profile information
    Then the profile retrieval is successful
    And the customer information includes name "John", last name "Doe", and CPR "649023-5364"

  Scenario: Retrieval of non-existing customer
    When a request is made for a non-existing customer's profile information
    Then the profile retrieval fails
    Then an error message "Customer not found" is returned

  Scenario: Merchant information retrieval
    Given a merchant with name "John", last name "Doe", and CPR "649023-5364"
    And the merchant is registered with the bank with an initial balance of "2000" kr
    And the merchant is registered with Simple DTU Pay using their bank account
    When the merchant requests their profile information
    Then the profile retrieval is successful
    And the merchant information includes name "John", last name "Doe", and CPR "649023-5364"

  Scenario: Retrieval of non-existing merchant
    When a request is made for a non-existing merchant's profile information
    Then the profile retrieval fails
    Then an error message "Merchant not found" is returned
