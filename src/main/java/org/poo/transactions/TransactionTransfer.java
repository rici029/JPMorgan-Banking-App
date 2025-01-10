package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionTransfer extends Transactions {
    private String senderIBAN;
    private String receiverIBAN;
    private String amount;
    private String transferType;

    public TransactionTransfer(final int timestamp, final String senderIBAN,
                               final String receiverIBAN, final String amount,
                               final String description, final String transferType) {
        super(description, timestamp);
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.transferType = transferType;
    }

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public ObjectNode printJson(final ObjectMapper mapper) {
        ObjectNode transferNode = mapper.createObjectNode();
        transferNode.put("timestamp", getTimestamp());
        transferNode.put("description", getDescription());
        transferNode.put("senderIBAN", getSenderIBAN());
        transferNode.put("receiverIBAN", getReceiverIBAN());
        transferNode.put("amount", getAmount());
        transferNode.put("transferType", getTransferType());
        return transferNode;
    }
}
