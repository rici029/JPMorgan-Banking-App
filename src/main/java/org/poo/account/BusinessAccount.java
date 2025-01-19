package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.businessUser.BusinessUser;

import java.util.HashMap;

@Getter @Setter
public class BusinessAccount extends Account {
    private double spendingLimit;
    private double depositLimit;
    private HashMap<String, BusinessUser> managers;
    private HashMap<String, BusinessUser> employees;
    private double totalSpent;
    private double totalDeposited;

    public BusinessAccount(final String email, final String currency, final String accountType) {
        super(email, currency, accountType);
        this.spendingLimit = 0;
        this.depositLimit = 0;
        this.managers = new HashMap<>();
        this.employees = new HashMap<>();
        this.totalSpent = 500;
        this.totalDeposited = 500;
    }

    @Override
    public void pay(final double amount) {
        if(this.spendingLimit == 0 || amount <= this.spendingLimit) {
            setBalance(getBalance() - amount);
        }
    }

}
