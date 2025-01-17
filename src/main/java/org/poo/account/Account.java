package org.poo.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.card.Card;
import org.poo.commerciant.Commerciant;
import org.poo.transactions.Transactions;
import org.poo.utils.Utils;
import org.poo.transactions.TransactionObserver;

import java.util.ArrayList;
import java.util.HashMap;

@Getter @Setter
public abstract class Account implements TransactionObserver {
    private String email;
    private String currency;
    private String accountType;
    private double balance;
    private String iban;
    private double minimumBalance;
    private ArrayList<Card> cards;
    private String alias;
    private ArrayList<Transactions> transactions;
    private HashMap<Commerciant, Integer> nrOfTransactions;
    private double spendingThreshold;
    private HashMap<String, Double> discounts;

    public Account(final String email, final String currency, final String accountType) {
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        this.balance = 0;
        this.iban = Utils.generateIBAN();
        this.minimumBalance = 0;
        this.cards = new ArrayList<>();
        this.alias = "";
        this.transactions = new ArrayList<>();
        this.nrOfTransactions = new HashMap<>();
        this.discounts = new HashMap<>();
        this.spendingThreshold = 0;
    }


    /**
     *
     * @param amount the amount to be deposited
     */
    public void deposit(final double amount) {
        this.balance += amount;
    }

    /**
     *
     * @param amount the amount to be withdrawn
     */
    public void pay(final double amount) {
        this.balance -= amount;
    }

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode accountNode = mapper.createObjectNode();
        accountNode.put("IBAN", getIban());
        accountNode.put("balance", getBalance());
        accountNode.put("currency", getCurrency());
        accountNode.put("type", getAccountType());
        return accountNode;
    }

    /**
     *
     * @param card the card to be added
     */
    public void addCard(final Card card) {
        this.cards.add(card);
    }

    /**
     *
     * @param card the card to be removed
     */
    public void removeCard(final Card card) {
        this.cards.remove(card);
    }

    /**
     *
     * @param transaction the transaction to be added
     */
    @Override
    public void onTransactionAdded(final Transactions transaction) {
        transactions.add(transaction);
    }
}

