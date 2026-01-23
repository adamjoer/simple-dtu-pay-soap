package dtu.fm22.e2e;

import dtu.fm22.e2e.record.Customer;
import dtu.fm22.e2e.record.Merchant;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author s205135, s200718, s215206
 */
public class SharedState {
    public Customer customer;
    public Merchant merchant;
    public List<String> tokens;
    public boolean successful = false;
    public String errorMessage;

    public final BankService bank = new BankService_Service().getBankServicePort();
    public final List<String> accounts = new ArrayList<>();
}
