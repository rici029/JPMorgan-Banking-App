package org.poo.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.transactions.Transactions;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.poo.transactions.TransactionObserver;

import java.util.ArrayList;

@Getter @Setter
public class User implements TransactionObserver {
    private String firstName;
    private String lastName;
    private String email;
    private ArrayList<Account> accounts;
    private ArrayList<Transactions> transactions;
    private String birthDate;
    private String occupation;
    private int age;
    private String plan;
    private int nrOfPaymentsForUpgrade;

    public User(final String firstName, final String lastName, final String email,
                final String birthDate, final String occupation, final int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.age = age;
        if (occupation.equals("student")) {
            this.plan = "student";
        } else {
            this.plan = "standard";
        }
        this.nrOfPaymentsForUpgrade = 0;
    }

    /**
     *
     * @param account account to be added to the user
     */
    public void addAccount(final Account account) {
        this.accounts.add(account);
    }

    /**
     *
     * @param account account to be removed from the user
     */
    public void removeAccount(final Account account) {
        this.accounts.remove(account);
    }

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode userNode = mapper.createObjectNode();
        userNode.put("firstName", getFirstName());
        userNode.put("lastName", getLastName());
        userNode.put("email", getEmail());
        return userNode;
    }

    /**
     * Method for adding a transaction to the user
     * @param transaction
     */
    @Override
    public void onTransactionAdded(final Transactions transaction) {
        transactions.add(transaction);
    }
}
