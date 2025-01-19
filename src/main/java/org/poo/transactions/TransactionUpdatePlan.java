package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionUpdatePlan extends Transactions {
    private String account;
    private String newPlanType;

    public TransactionUpdatePlan(final String account,
                                 final String newPlanType, final String description,
                                 final int timestamp) {
        super(description, timestamp);
        this.account = account;
        this.newPlanType = newPlanType;
    }

    /**
     * Method that prints the json object for the update plan transaction.
     * @param mapper object mapper for creating json objects
     * @return
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode newUpdatePlanNode = mapper.createObjectNode();
        newUpdatePlanNode.put("accountIBAN", getAccount());
        newUpdatePlanNode.put("newPlanType", getNewPlanType());
        newUpdatePlanNode.put("description", getDescription());
        newUpdatePlanNode.put("timestamp", getTimestamp());
        return newUpdatePlanNode;
    }
}
