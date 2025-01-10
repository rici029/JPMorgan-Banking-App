package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public abstract class Transactions {
    private int timestamp;
    private String description;
    private List<TransactionObserver> observers;

    /**
     *
     * @param mapper object mapper for creating json objects
     * @return object for json printing
     */
    public abstract ObjectNode printJson(ObjectMapper mapper);

    public Transactions(final String description, final int timestamp) {
        this.description = description;
        this.timestamp = timestamp;
        this.observers = new ArrayList<>();
    }

    /**
     * Method for adding an observer to the transaction
     * @param observer observer to be added
     */
    public void registerObserver(final TransactionObserver observer) {
        observers.add(observer);
    }

    /**
     * Method for notifying the observers
     */
    public void notifyObservers() {
        for (TransactionObserver observer : observers) {
            observer.onTransactionAdded(this);
        }
    }
}
