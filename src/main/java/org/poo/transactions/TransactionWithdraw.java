package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionWithdraw extends Transactions {
    private double amount;

    public TransactionWithdraw(final int timestamp, final String description, final double amount) {
        super(description, timestamp);
        this.amount = amount;
    }

    public ObjectNode printJson(ObjectMapper mapper) {
        ObjectNode withdrawNode = mapper.createObjectNode();
        withdrawNode.put("amount", getAmount());
        withdrawNode.put("description", getDescription());
        withdrawNode.put("timestamp", getTimestamp());
        return withdrawNode;
    }
}
