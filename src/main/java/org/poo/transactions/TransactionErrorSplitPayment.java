package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class TransactionErrorSplitPayment extends Transactions {

    private double amount;
    private String currency;
    private List<String> accounts;
    private String errorMessage;
    private String splitPaymentType;

    public TransactionErrorSplitPayment(final String description, final int timestamp,
                                        final double amount, final String currency,
                                        final List<String> accounts, final String errorMessage,
                                        final String splitPaymentType) {
        super(description, timestamp);
        this.amount = amount;
        this.currency = currency;
        this.accounts = accounts;
        this.errorMessage = errorMessage;
        this.splitPaymentType = splitPaymentType;
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
        transactionNode.set("involvedAccounts", accountsArray);
        transactionNode.put("error", errorMessage);
        transactionNode.put("splitPaymentType", getSplitPaymentType());
        transactionNode.put("description", getDescription());
        transactionNode.put("timestamp", getTimestamp());
        return transactionNode;
    }

}
