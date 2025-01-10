package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionPayment extends Transactions {
    private double amount;
    private String commerciant;

    public TransactionPayment(final int timestamp, final String description,
                              final double amount, final String commerciant) {
        super(description, timestamp);
        this.amount = amount;
        this.commerciant = commerciant;
    }

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode paymentNode = mapper.createObjectNode();
        paymentNode.put("amount", getAmount());
        paymentNode.put("commerciant", getCommerciant());
        paymentNode.put("description", getDescription());
        paymentNode.put("timestamp", getTimestamp());
        return paymentNode;
    }

}
