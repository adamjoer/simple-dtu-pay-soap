package dtu.fm22.e2e.steps;

import dtu.fm22.e2e.SharedState;
import dtu.fm22.e2e.record.Payment;
import dtu.fm22.e2e.service.CustomerService;
import dtu.fm22.e2e.service.MerchantService;
import dtu.fm22.e2e.service.PaymentService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author s215206, s205135, s200718
 */
public class ReportServiceSteps {

    private final SharedState state;

    private final MerchantService merchantService = new MerchantService();
    private final CustomerService customerService = new CustomerService();
    private final PaymentService paymentService = new PaymentService();

    private Collection<Payment> customerReport;
    private Collection<Payment> merchantReport;
    private Collection<Payment> managerReport;

    public ReportServiceSteps(SharedState state) {
        this.state = state;
    }

    @When("the merchant requests a transaction report")
    public void theMerchantRequestsATransactionReport() {
        merchantReport = merchantService.getReport(state.merchant);
    }

    @Then("the merchant report includes a transaction of {string} kr to {string} and contains no customer information")
    public void theMerchantReportIncludesATransactionOfKrFrom(String amount, String merchantFullName) {
        assertNotNull(merchantReport);
        assertFalse(merchantReport.isEmpty());

        var amountBigDecimal = new BigDecimal(amount);
        var merchantNames = merchantFullName.split(" ");
        boolean found = merchantReport.stream().anyMatch(payment ->
                payment.merchant().firstName.equals(merchantNames[0]) &&
                        payment.merchant().lastName.equals(merchantNames[1]) &&
                        payment.amount().compareTo(amountBigDecimal) == 0
        );

        boolean containsCustomerInfo = merchantReport.stream().anyMatch(payment -> payment.customer() != null);

        assertTrue(
                "Expected to find a transaction of "
                        + amount + " kr to " + merchantFullName +
                        " in the report, but did not.\nReport:\n"
                        + merchantReport,
                found
        );
        assertFalse(
                "Expected merchant report to contain no customer"
                        + "information, but it did. Report:"
                        + merchantReport,
                containsCustomerInfo
        );
    }

    @When("the customer requests a transaction report")
    public void theCustomerRequestsATransactionReport() {
        customerReport = customerService.getReport(state.customer);
    }

    @Then("the customer report includes a transaction of {string} kr from {string} to {string}")
    public void theCustomerReportIncludesATransactionOfKrFromTo(String amount, String customerFullName, String merchantFullName) {
        assertNotNull(customerReport);
        assertFalse(customerReport.isEmpty());

        var amountBigDecimal = new BigDecimal(amount);
        var customerNames = customerFullName.split(" ");
        var merchantNames = merchantFullName.split(" ");
        boolean found = customerReport.stream().anyMatch(payment ->
                payment.customer().firstName.equals(customerNames[0]) &&
                        payment.customer().lastName.equals(customerNames[1]) &&
                        payment.merchant().firstName.equals(merchantNames[0]) &&
                        payment.merchant().lastName.equals(merchantNames[1]) &&
                        payment.amount().compareTo(amountBigDecimal) == 0
        );

        assertTrue(
                "Expected to find a transaction of "
                        + amount + " kr from " + customerFullName +
                        " to " + customerFullName + " in the report, but did not. Report:"
                        + customerReport,
                found
        );
    }

    @When("the manager requests a transaction report")
    public void theManagerRequestsATransactionReport() {
        managerReport = paymentService.getAllPayments();
    }

    @Then("the manager report includes at least {int} transactions")
    public void theManagerReportIncludesAtLeastTransactions(Integer count) {
        assertNotNull("Manager report should not be null", managerReport);
        assertTrue("Expected manager report to include at least " + count + " transactions, but had " + managerReport.size(),
                managerReport.size() >= count);
    }

    @Then("the manager report includes a transaction of {string} kr from {string} to {string}")
    public void theManagerReportIncludesATransactionOfKrFromTo(String amount, String customerFullName, String merchantFullName) {
        assertNotNull("Manager report should not be null", managerReport);
        assertFalse("Manager report should not be empty", managerReport.isEmpty());

        var amountBigDecimal = new BigDecimal(amount);
        var customerNames = customerFullName.split(" ");
        var merchantNames = merchantFullName.split(" ");

        boolean found = managerReport.stream().anyMatch(payment ->
                payment.customer() != null &&
                        payment.customer().firstName.equals(customerNames[0]) &&
                        payment.customer().lastName.equals(customerNames[1]) &&
                        payment.merchant() != null &&
                        payment.merchant().firstName.equals(merchantNames[0]) &&
                        payment.merchant().lastName.equals(merchantNames[1]) &&
                        payment.amount().compareTo(amountBigDecimal) == 0
        );

        assertTrue(
                "Expected to find a transaction of " + amount + " kr from " + customerFullName +
                        " to " + merchantFullName + " in the manager report, but did not. Report: " + managerReport,
                found
        );
    }
}
