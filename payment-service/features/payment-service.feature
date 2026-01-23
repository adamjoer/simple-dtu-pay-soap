Feature: Payment Service


  Scenario: Valid token triggers payment info request
    Given a pending payment request
    When a "TokenValidationProvided" event with valid=true is received
    Then a "PaymentInfoRequested" event is published

  Scenario: Invalid token fails payment
    Given a pending payment request
    When a "TokenValidationProvided" event with valid=false is received
    Then a "PaymentCreated" event is published with error

  Scenario: Customer report generation
    Given completed payments for a customer
    When a "CustomerReportRequested" event is received
    Then a "CustomerReportProvided" event with filtered payments is published

  Scenario: Merchant report generation (no customer info)
    Given completed payments for a merchant
    When a "MerchantReportRequested" event is received
    Then a "MerchantReportProvided" event with obfuscated customer is published
