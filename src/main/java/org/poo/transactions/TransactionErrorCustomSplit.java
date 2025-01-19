package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class TransactionErrorCustomSplit extends Transactions {
    private List<Double> amountForUsers;
    private String currency;
    private List<String> accounts;
    private String splitPaymentType;
    private String errorMessage;

    public TransactionErrorCustomSplit(final String description, final int timestamp,
                                       final List<Double> amountForUsers, final String currency,
                                       final List<String> accounts, final String errorMessage,
                                       final String splitPaymentType) {
        super(description, timestamp);
        this.amountForUsers = amountForUsers;
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
        ArrayNode amountForUsersArray = mapper.createArrayNode();
        for (Double amount : getAmountForUsers()) {
            amountForUsersArray.add(amount);
        }
        transactionNode.set("amountForUsers", amountForUsersArray);
        transactionNode.put("currency", getCurrency());
        ArrayNode accountsArray = mapper.createArrayNode();
        for (String account : getAccounts()) {
            accountsArray.add(account);
        }
        transactionNode.put("involvedAccounts", accountsArray);
        transactionNode.put("error", errorMessage);
        transactionNode.put("splitPaymentType", getSplitPaymentType());
        transactionNode.put("description", getDescription());
        transactionNode.put("timestamp", getTimestamp());
        return transactionNode;
    }
}
