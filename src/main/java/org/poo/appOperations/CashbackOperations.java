package org.poo.appOperations;

import org.poo.account.Account;
import org.poo.commerciant.Commerciant;
import org.poo.user.User;

import java.util.HashMap;

public final class CashbackOperations {
    private CashbackOperations() {
    }

    /**
     * Method that adds cashback to the account.
     * @param account the account
     * @param commerciant the commerciant
     * @param amount the amount
     * @param user the user
     * @param exchangeRates the exchange rates
     */
    public static void getCashback(final Account account, final Commerciant commerciant,
                                   final double amount, final User user,
                                   final HashMap<String, HashMap<String, Double>> exchangeRates) {
        if(commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            addNrOfTransactionsCashback(account, commerciant, amount);
        } else {
            addSpendingThresholdCashback(account, commerciant, amount, user, exchangeRates);
        }
    }

    /**
     * Method that adds cashback based on the number of transactions.
     * @param account the account
     * @param commerciant the commerciant
     * @param amount the amount
     */
    private static void addNrOfTransactionsCashback(final Account account,
                                                    final Commerciant commerciant, double amount) {
        HashMap<String, Integer> nrOfTransactions = account.getNrOfTransactions();
        if(nrOfTransactions.containsKey(commerciant.getType())) {
            if(nrOfTransactions.get(commerciant.getType()) + 1 == getCashbackNoOfTransactionsNeeded(commerciant)) {
                account.deposit(amount * getCashabckPercentage(commerciant));
                nrOfTransactions.put(commerciant.getType(), 1);
            } else {
                nrOfTransactions.put(commerciant.getType(), nrOfTransactions.get(commerciant.getType()) + 1);
            }
        } else {
            nrOfTransactions.put(commerciant.getType(), 1);
        }
    }

    /**
     * Method that returns the number of transactions needed for cashback.
     * @param commerciant the commerciant
     * @return the number of transactions needed for cashback
     */
    private static int getCashbackNoOfTransactionsNeeded(final Commerciant commerciant) {
        return switch (commerciant.getType()) {
            case "Food" -> 3;
            case "Clothes" -> 6;
            case "Tech" -> 11;
            default -> 0;
        };
    }

    /**
     * Method that returns the cashback percentage.
     * @param commerciant the commerciant
     * @return the cashback percentage
     */
    private static double getCashabckPercentage(final Commerciant commerciant) {
        return switch (commerciant.getType()) {
            case "Food" -> 0.02;
            case "Clothes" -> 0.05;
            case "Tech" -> 0.1;
            default -> 0;
        };
    }

    /**
     * Method that adds cashback based on the spending threshold.
     * @param account the account
     * @param commerciant the commerciant
     * @param amount the amount
     * @param user the user
     * @param exchangeRates the exchange rates
     */
    private static void addSpendingThresholdCashback(final Account account,
                                                     final Commerciant commerciant,
                                                     final double amount,
                                                     final User user, final HashMap<String,
                                                        HashMap<String, Double>> exchangeRates ) {
        HashMap<String, Double> spendingThreshold = account.getSpendingThreshold();
        double amountInRON = amount;
        if(!account.getCurrency().equals("RON")) {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    account.getCurrency(), "RON");
            amountInRON = amount * exchangeRate;
        }
        if(spendingThreshold.containsKey(commerciant.getType())) {
            double threshold = spendingThreshold.get(commerciant.getType()) + amountInRON;
            if(threshold >= 100) {
                account.deposit(amount * getCashabckPercentageBySpendings(commerciant, user,
                        threshold));
            } else {
                spendingThreshold.put(commerciant.getType(),
                        spendingThreshold.get(commerciant.getType()) + amountInRON);
            }
        } else {
            spendingThreshold.put(commerciant.getType(), amountInRON);
            if(amountInRON >= 100) {
                account.deposit(amount * getCashabckPercentageBySpendings(commerciant, user,
                        amountInRON));
            }
        }
    }

    /**
     * Method that returns the cashback percentage based on the spendings.
     * @param commerciant the commerciant
     * @param user the user
     * @param threshold the threshold
     * @return the cashback percentage
     */
    private static double getCashabckPercentageBySpendings(final Commerciant commerciant,
                                                           final User user, final double threshold) {
        if (threshold >= 100 && threshold < 300) {
            switch (user.getPlan()) {
                case "student":
                case "standard":
                    return 0.001;
                case "silver":
                    return 0.003;
                case "gold":
                    return 0.005;
                default:
                    return 0;
            }
        } else if (threshold >= 300 && threshold < 500) {
            return switch (user.getPlan()) {
                case "student", "standard" -> 0.002;
                case "silver" -> 0.004;
                case "gold" -> 0.0055;
                default -> 0;
            };
        } else if (threshold >= 500) {
            return switch (user.getPlan()) {
                case "student", "standard" -> 0.0025;
                case "silver" -> 0.005;
                case "gold" -> 0.007;
                default -> 0;
            };
        }
        return 0;
    }

}
