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
        HashMap<String, Double> discounts = account.getDiscounts();
        if(discounts.containsKey(commerciant.getType()) && discounts.get(commerciant.getType()) > 0) {
            account.deposit(amount * discounts.get(commerciant.getType()));
            discounts.put(commerciant.getType(), 0.0);
        }
        if(commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            addNrOfTransactionsCashback(account, commerciant, amount);
        } else {
            addSpendingThresholdCashback(account, amount, user, exchangeRates);
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
        HashMap<Commerciant, Integer> nrOfTransactions = account.getNrOfTransactions();
        if(nrOfTransactions.containsKey(commerciant)) {
            nrOfTransactions.put(commerciant, nrOfTransactions.get(commerciant) + 1);
        } else {
            nrOfTransactions.put(commerciant, 1);
        }
        int nrOfTransToCommerciant = nrOfTransactions.get(commerciant);
        HashMap<String, Double> discounts = account.getDiscounts();
        if(nrOfTransToCommerciant == 2) {
            if(!discounts.containsKey("Food")){
                discounts.put("Food", 0.02);
            }
        } else if( nrOfTransToCommerciant == 5) {
            if(!discounts.containsKey("Clothes")){
                discounts.put("Clothes", 0.05);
            }
        } else if(nrOfTransToCommerciant == 10) {
            if(!discounts.containsKey("Tech")){
                discounts.put("Tech", 0.1);
            }
        }
    }

    /**
     * Method that adds cashback based on the spending threshold.
     * @param account the account
     * @param amount the amount
     * @param user the user
     * @param exchangeRates the exchange rates
     */
    private static void addSpendingThresholdCashback(final Account account,
                                                     final double amount,
                                                     final User user, final HashMap<String,
                                                        HashMap<String, Double>> exchangeRates ) {
        double amountInRON = amount;
        if(!account.getCurrency().equals("RON")) {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    account.getCurrency(), "RON");
            amountInRON = amount * exchangeRate;
        }
        account.setSpendingThreshold(account.getSpendingThreshold() + amountInRON);
        double spendingThreshold = account.getSpendingThreshold();
        if(spendingThreshold >= 100) {
            account.deposit(amount * getCashabckPercentageBySpendings(user, spendingThreshold));
        }
    }

    /**
     * Method that returns the cashback percentage based on the spendings.
     * @param user the user
     * @param threshold the threshold
     * @return the cashback percentage
     */
    private static double getCashabckPercentageBySpendings(final User user, final double threshold) {
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
