package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionSavingsWithdraw extends Transactions {
    private String savingsIban;
    private double amount;
    private String classicIban;

    public TransactionSavingsWithdraw(final String description, final int timestamp,
                                      final String savingsIban, final double amount,
                                      final String classicIban) {
        super(description, timestamp);
        this.savingsIban = savingsIban;
        this.amount = amount;
        this.classicIban = classicIban;
    }

    /**
     * Method that prints the transaction in json format.
     * @param mapper object mapper for creating json objects
     * @return the json object
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode transactionNode = mapper.createObjectNode();
        transactionNode.put("amount", getAmount());
        transactionNode.put("savingsAccountIBAN", getSavingsIban());
        transactionNode.put("classicAccountIBAN", getClassicIban());
        transactionNode.put("description", getDescription());
        transactionNode.put("timestamp", getTimestamp());
        return transactionNode;
    }
}
