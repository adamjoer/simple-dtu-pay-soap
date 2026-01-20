package dtu.fm22.payment;

import dtu.fm22.payment.record.Customer;
import dtu.fm22.payment.record.Merchant;

public record PaymentInfo(Customer customer, Merchant merchant) {
}
