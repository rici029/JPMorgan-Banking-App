package org.poo.transactions;

public interface TransactionObserver {
    /**
     * Method for notifying the observers
     * @param transaction transaction to be added
     */
    void onTransactionAdded(Transactions transaction);
}
