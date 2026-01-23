package dtu.fm22.e2e.steps;

import dtu.fm22.e2e.SharedState;
import dtu.fm22.e2e.record.Customer;
import dtu.fm22.e2e.record.Merchant;
import dtu.fm22.e2e.service.CustomerService;
import dtu.fm22.e2e.service.MerchantService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author s205135, s200718, s215206
 */
public class UserServiceSteps {

    private final SharedState state;

    private Customer customerInformation;
    private Merchant merchantInformation;

    private String errorMessage;
    private boolean success = false;

    private final CustomerService customerService = new CustomerService();
    private final MerchantService merchantService = new MerchantService();

    public UserServiceSteps(SharedState state) {
        this.state = state;
    }

    @When("the customer requests their profile information")
    public void theCustomerRequestsTheirProfileInformation() {
        try {
            customerInformation = customerService.getProfileInformation(state.customer);
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            success = false;
        }
    }

    @When("the merchant requests their profile information")
    public void theMerchantRequestsTheirProfileInformation() {
        try {
            merchantInformation = merchantService.getProfileInformation(state.merchant);
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            success = false;
        }
    }

    @Then("the profile retrieval is successful")
    public void theProfileRetrievalIsSuccessful() {
        assertTrue(success);
    }

    @Then("the profile retrieval fails")
    public void theProfileRetrievalFails() {
        assertFalse(success);
    }

    @Then("the customer information includes name {string}, last name {string}, and CPR {string}")
    public void theCustomerInformationIncludesNameLastNameAndCPR(String firstName, String lastName, String cpr) {
        assertNotNull(customerInformation);
        assertEquals(firstName, customerInformation.firstName);
        assertEquals(lastName, customerInformation.lastName);
        assertEquals(cpr, customerInformation.cprNumber);
    }

    @Then("the merchant information includes name {string}, last name {string}, and CPR {string}")
    public void theMerchantInformationIncludesNameLastNameAndCPR(String firstName, String lastName, String cpr) {
        assertNotNull(merchantInformation);
        assertEquals(firstName, merchantInformation.firstName);
        assertEquals(lastName, merchantInformation.lastName);
        assertEquals(cpr, merchantInformation.cprNumber);
    }

    @When("a request is made for a non-existing customer's profile information")
    public void aRequestIsMadeForANonExistingCustomerSProfileInformation() {
        try {
            customerInformation = customerService.getProfileInformation(
                    new Customer(UUID.randomUUID(),
                            "doesntExist",
                            "noLastName",
                            "fake",
                            "000000-0000"));
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            success = false;
        }
    }

    @Then("an error message {string} is returned")
    public void theProfileRetrievalFailsWithErrorMessage(String expectedMessage) {
        assertFalse(success);
        assertEquals(expectedMessage, errorMessage);
    }

    @When("a request is made for a non-existing merchant's profile information")
    public void aRequestIsMadeForANonExistingMerchantSProfileInformation() {
        try {
            merchantInformation = merchantService.getProfileInformation(
                    new Merchant(UUID.randomUUID(),
                            "doesntExist",
                            "noLastName",
                            "fake",
                            "000000-0000"));
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            success = false;
        }
    }

    @When("the customer deregisters from Simple DTU Pay")
    public void theCustomerDeregistersFromSimpleDTUPay() {
        try {
            customerService.unregister(state.customer);
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            success = false;
        }
    }

    @When("the merchant deregisters from Simple DTU Pay")
    public void theMerchantDeregistersFromSimpleDTUPay() {
        try {
            merchantService.unregister(state.merchant);
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            success = false;
        }
    }

    @Then("the deregistration is successful")
    public void theDeregistrationIsSuccessful() {
        assertTrue("Expected deregistration to be successful", success);
    }

    @Then("the customer profile is no longer accessible")
    public void theCustomerProfileIsNoLongerAccessible() {
        try {
            customerService.getProfileInformation(state.customer);
            fail("Expected customer profile to be inaccessible after deregistration");
        } catch (Exception e) {
            // Expected - profile should not be accessible
            assertTrue("Expected 'Customer not found' error but got: " + e.getMessage(),
                    e.getMessage().contains("Customer not found"));
        }
        // Set customer ID to null so teardown doesn't try to unregister again
        state.customer.id = null;
    }

    @Then("the merchant profile is no longer accessible")
    public void theMerchantProfileIsNoLongerAccessible() {
        try {
            merchantService.getProfileInformation(state.merchant);
            fail("Expected merchant profile to be inaccessible after deregistration");
        } catch (Exception e) {
            // Expected - profile should not be accessible
            assertTrue("Expected 'Merchant not found' error but got: " + e.getMessage(),
                    e.getMessage().contains("Merchant not found"));
        }
        // Set merchant ID to null so teardown doesn't try to unregister again
        state.merchant.id = null;
    }
}
