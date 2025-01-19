package org.poo.appOperations;

import org.poo.account.Account;
import org.poo.commerciant.Commerciant;
import org.poo.user.User;

import java.util.HashMap;

public final class CashbackOperations {
    private static final int CASHBACK_THRESHOLD_INFERIOR_LIMIT = 100;
    private static final int CASHBACK_THRESHOLD_MIDDLE_LIMIT = 300;
    private static final int CASHBACK_THRESHOLD_SUPERIOR_LIMIT = 500;
    private static final double FOOD_CASHBACK = 0.02;
    private static final double CLOTHES_CASHBACK = 0.05;
    private static final double TECH_CASHBACK = 0.1;
    private static final int NR_OF_TRANSACTIONS_FOOD = 2;
    private static final int NR_OF_TRANSACTIONS_CLOTHES = 5;
    private static final int NR_OF_TRANSACTIONS_TECH = 10;
    private static final double CASHBACK_STANDARD1 = 0.001;
    private static final double CASHBACK_SILVER1 = 0.003;
    private static final double CASHBACK_GOLD1 = 0.005;
    private static final double CASHBACK_STANDARD2 = 0.002;
    private static final double CASHBACK_SILVER2 = 0.004;
    private static final double CASHBACK_GOLD2 = 0.0055;
    private static final double CASHBACK_STANDARD3 = 0.0025;
    private static final double CASHBACK_SILVER3 = 0.005;
    private static final double CASHBACK_GOLD3 = 0.007;
    private static final double CASHBACK_THRESHOLD = 100;
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
        if (discounts.containsKey(commerciant.getType())
                && discounts.get(commerciant.getType()) > 0) {
            account.deposit(amount * discounts.get(commerciant.getType()));
            discounts.put(commerciant.getType(), 0.0);
        }
        if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            addNrOfTransactionsCashback(account, commerciant);
        } else {
            addSpendingThresholdCashback(account, amount, user, exchangeRates);
        }
    }

    /**
     * Method that adds cashback based on the number of transactions.
     * @param account the account
     * @param commerciant the commerciant
     */
    private static void addNrOfTransactionsCashback(final Account account,
                                                    final Commerciant commerciant) {
        HashMap<Commerciant, Integer> nrOfTransactions = account.getNrOfTransactions();
        if (nrOfTransactions.containsKey(commerciant)) {
            nrOfTransactions.put(commerciant, nrOfTransactions.get(commerciant) + 1);
        } else {
            nrOfTransactions.put(commerciant, 1);
        }
        int nrOfTransToCommerciant = nrOfTransactions.get(commerciant);
        HashMap<String, Double> discounts = account.getDiscounts();
        if (nrOfTransToCommerciant == NR_OF_TRANSACTIONS_FOOD) {
            if (!discounts.containsKey("Food")) {
                discounts.put("Food", FOOD_CASHBACK);
            }
        } else if (nrOfTransToCommerciant == NR_OF_TRANSACTIONS_CLOTHES) {
            if (!discounts.containsKey("Clothes")) {
                discounts.put("Clothes", CLOTHES_CASHBACK);
            }
        } else if (nrOfTransToCommerciant == NR_OF_TRANSACTIONS_TECH) {
            if (!discounts.containsKey("Tech")) {
                discounts.put("Tech", TECH_CASHBACK);
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
                                                        HashMap<String, Double>> exchangeRates) {
        double amountInRON = amount;
        if (!account.getCurrency().equals("RON")) {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    account.getCurrency(), "RON");
            amountInRON = amount * exchangeRate;
        }
        account.setSpendingThreshold(account.getSpendingThreshold() + amountInRON);
        double spendingThreshold = account.getSpendingThreshold();
        if (spendingThreshold >= CASHBACK_THRESHOLD) {
            account.deposit(amount
                    * getCashabckPercentageBySpendings(user, spendingThreshold));
        }
    }

    /**
     * Method that returns the cashback percentage based on the spendings.
     * @param user the user
     * @param threshold the threshold
     * @return the cashback percentage
     */
    private static double getCashabckPercentageBySpendings(final User user,
                                                           final double threshold) {
        if (threshold >= CASHBACK_THRESHOLD_INFERIOR_LIMIT
                && threshold < CASHBACK_THRESHOLD_MIDDLE_LIMIT) {
            switch (user.getPlan()) {
                case "student":
                case "standard":
                    return CASHBACK_STANDARD1;
                case "silver":
                    return CASHBACK_SILVER1;
                case "gold":
                    return CASHBACK_GOLD1;
                default:
                    return 0;
            }
        } else if (threshold >= CASHBACK_THRESHOLD_MIDDLE_LIMIT
                && threshold < CASHBACK_THRESHOLD_SUPERIOR_LIMIT) {
            return switch (user.getPlan()) {
                case "student", "standard" -> CASHBACK_STANDARD2;
                case "silver" -> CASHBACK_SILVER2;
                case "gold" -> CASHBACK_GOLD2;
                default -> 0;
            };
        } else if (threshold >= CASHBACK_THRESHOLD_SUPERIOR_LIMIT) {
            return switch (user.getPlan()) {
                case "student", "standard" -> CASHBACK_STANDARD3;
                case "silver" -> CASHBACK_SILVER3;
                case "gold" -> CASHBACK_GOLD3;
                default -> 0;
            };
        }
        return 0;
    }

}
