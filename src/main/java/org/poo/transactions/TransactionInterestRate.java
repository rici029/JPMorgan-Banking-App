package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionInterestRate extends Transactions {
    private double amount;
    private String currency;

    public TransactionInterestRate(final int timestamp, final String description, final double amount, final String currency) {
        super(description, timestamp);
        this.amount = amount;
        this.currency = currency;
    }

    public ObjectNode printJson(ObjectMapper mapper) {
        ObjectNode interestRateNode = mapper.createObjectNode();
        interestRateNode.put("amount", getAmount());
        interestRateNode.put("currency", getCurrency());
        interestRateNode.put("description", getDescription());
        interestRateNode.put("timestamp", getTimestamp());
        return interestRateNode;
    }
}
