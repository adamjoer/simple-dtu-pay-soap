package dtu.fm22.e2e;

import dtu.fm22.e2e.record.Customer;
import dtu.fm22.e2e.record.Merchant;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;

import java.util.ArrayList;
import java.util.List;

public class SharedState {
    public Customer customer;
    public Merchant merchant;
    public List<String> tokens;

    public final BankService bank = new BankService_Service().getBankServicePort();
    public final List<String> accounts = new ArrayList<>();
}
