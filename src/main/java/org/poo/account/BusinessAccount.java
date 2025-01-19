package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.appOperations.ExchangeOperations;
import org.poo.businessUser.BusinessUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Getter @Setter
public class BusinessAccount extends Account {
    private double spendingLimit;
    private double depositLimit;
    private LinkedHashMap<String, BusinessUser> managers;
    private LinkedHashMap<String, BusinessUser> employees;
    private ArrayList<String> commerciants;

    public BusinessAccount(final String email, final String currency, final String accountType,
                           final HashMap<String, HashMap<String, Double>> exchangeRates) {
        super(email, currency, accountType);
        this.spendingLimit = 500 * ExchangeOperations.getExchangeRate(exchangeRates, "RON", currency);
        this.depositLimit = 500 * ExchangeOperations.getExchangeRate(exchangeRates, "RON", currency);
        this.managers = new LinkedHashMap<>();
        this.employees = new LinkedHashMap<>();
        this.commerciants = new ArrayList<>();
    }

}
