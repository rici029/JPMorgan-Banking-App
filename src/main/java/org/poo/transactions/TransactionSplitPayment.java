package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class TransactionSplitPayment extends Transactions {
    private double amount;
    private String currency;
    private List<String> accounts;

    public TransactionSplitPayment(final double amount, final String currency,
                                   final List<String> accounts, final String description,
                                   final int timestamp) {
        super(description, timestamp);
        this.amount = amount;
        this.currency = currency;
        this.accounts = accounts;
    }

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode transactionNode = mapper.createObjectNode();
        transactionNode.put("amount", getAmount());
        transactionNode.put("currency", getCurrency());
        ArrayNode accountsArray = mapper.createArrayNode();
        for (String account : getAccounts()) {
            accountsArray.add(account);
        }
        transactionNode.put("involvedAccounts", accountsArray);
        transactionNode.put("description", getDescription());
        transactionNode.put("timestamp", getTimestamp());
        return transactionNode;
    }
}
