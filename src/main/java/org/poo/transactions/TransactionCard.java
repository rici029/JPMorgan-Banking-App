package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionCard extends Transactions {
    private String cardNumber;
    private String account;
    private String cardHolder;

    public TransactionCard(final String cardNumber, final String account, final String cardHolder,
                           final String description, final int timestamp) {
        super(description, timestamp);
        this.cardNumber = cardNumber;
        this.account = account;
        this.cardHolder = cardHolder;
    }

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode newCardNode = mapper.createObjectNode();
        newCardNode.put("account", getAccount());
        newCardNode.put("card", getCardNumber());
        newCardNode.put("cardHolder", getCardHolder());
        newCardNode.put("description", getDescription());
        newCardNode.put("timestamp", getTimestamp());
        return newCardNode;
    }
}
