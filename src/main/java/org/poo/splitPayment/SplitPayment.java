package org.poo.splitPayment;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class SplitPayment {
    private ArrayList<String> accounts;
    private double amount;
    private String type;
    private int timestamp;
    private String currency;
    private int noOfAccepts;

    public SplitPayment(final ArrayList<String> accounts, final double amount, final String type,
                        final int timestamp, final String currency, final int noOfAccepts) {
        this.accounts = accounts;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.currency = currency;
        this.noOfAccepts = noOfAccepts;
    }

}
