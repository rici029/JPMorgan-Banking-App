package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionAction extends Transactions {

    public TransactionAction(final int timestamp, final String description) {
        super(description, timestamp);
    }

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode actionNode = mapper.createObjectNode();
        actionNode.put("timestamp", getTimestamp());
        actionNode.put("description", getDescription());
        return actionNode;
    }
}
