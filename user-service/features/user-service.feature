Feature: User Service

  Scenario: Customer registration
    When a "CustomerRegistrationRequested" event for a new customer is received
    Then the "CustomerRegistrationCompleted" event is sent
    And the customer has a non-empty id

  Scenario: Customer info retrieval
    Given a registered customer
    When a "CustomerInfoRequested" event is received for that customer
    Then the "CustomerInfoProvided" event is sent with customer data

  Scenario: Customer info retrieval - not found
    When a "CustomerInfoRequested" event is received for non-existing customer
    Then the "CustomerInfoProvided" event is sent with null customer

  Scenario: Merchant registration
    When a "MerchantRegistrationRequested" event for a new merchant is received
    Then the "MerchantRegistrationCompleted" event is sent
    And the merchant has a non-empty id

  Scenario: Payment info request
    Given a registered customer and merchant
    When a "PaymentInfoRequested" event is received
    Then the "PaymentInfoProvided" event is sent with customer and merchant data
