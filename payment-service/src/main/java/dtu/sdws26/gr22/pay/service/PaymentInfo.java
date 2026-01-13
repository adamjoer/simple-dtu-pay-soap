package dtu.sdws26.gr22.pay.service;

import dtu.sdws26.gr22.pay.service.record.Customer;
import dtu.sdws26.gr22.pay.service.record.Merchant;

public record PaymentInfo(Customer customer, Merchant merchant) {
}
