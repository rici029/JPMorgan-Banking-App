package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.appOperations.ExchangeOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.TransactionUpdatePlan;
import org.poo.transactions.Transactions;
import org.poo.user.User;

public class UpgradePlanCommand extends BaseCommand {
    private static final int SILVER_UPGRADE = 100;
    private static final int GOLD_UPGRADE = 250;
    private static final int STANDARD_TO_GOLD_UPGRADE = 350;
    private static final int GOLD_IDX = 3;
    public UpgradePlanCommand(final CommandInput command, final AppContext context) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        String newPlan = command.getNewPlanType();
        if (!accountMap.containsKey(command.getAccount())) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "upgradePlan");
            ObjectNode out = mapper.createObjectNode();
            out.put("description", "Account not found");
            out.put("timestamp", command.getTimestamp());
            error.set("output", out);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }
        Account account = accountMap.get(command.getAccount());
        User user = usersAccountsMap.get(command.getAccount());
        if (user == null) {
            return;
        }

        int userPlanIdx = getPlanIdx(user.getPlan());
        int newPlanIdx = getPlanIdx(newPlan);
        if (userPlanIdx < newPlanIdx) {
            if (newPlan.equals("silver")) {
                payUpgrade(account, SILVER_UPGRADE, user, newPlan);
                return;
            } else if (userPlanIdx != 1) {
                payUpgrade(account, GOLD_UPGRADE, user, newPlan);
                return;
            } else {
                payUpgrade(account, STANDARD_TO_GOLD_UPGRADE, user, newPlan);
                return;
            }
        }
        if (userPlanIdx > newPlanIdx) {
            addTransactionError(command.getAccount(), "You cannot downgrade your plan.");
        } else {
            addTransactionError(command.getAccount(), "The user already has the "
                    + getPlanType(newPlanIdx) + " plan.");
        }
    }

    /**
     * Get the index of the plan
     * @param plan the plan
     * @return the index of the plan
     */
    private int getPlanIdx(final String plan) {
        if (plan.equals("student") || plan.equals("standard")) {
            return 1;
        } else if (plan.equals("silver")) {
            return 2;
        } else {
            return GOLD_IDX;
        }
    }

    /**
     * Get the plan type
     * @param idx the index of the plan
     * @return the plan type
     */
    private String getPlanType(final int idx) {
        if (idx == 2) {
            return "silver";
        } else {
            return "gold";
        }
    }

    /**
     * Add a transaction error
     * @param account the account
     * @param message the message
     */
    private void addTransactionError(final String account, final String message) {
        Transactions transaction = new TransactionAction(command.getTimestamp(), message);
        transaction.registerObserver(usersAccountsMap.get(account));
        transaction.registerObserver(accountMap.get(account));
        transaction.notifyObservers();
    }

    /**
     * Add a transaction update plan
     * @param account the account
     * @param newPlan the new plan
     */
    private void addTransactionUpdatePlan(final String account, final String newPlan) {
        Transactions transaction = new TransactionUpdatePlan(account, newPlan,
                "Upgrade plan", command.getTimestamp());
        transaction.registerObserver(usersAccountsMap.get(account));
        transaction.registerObserver(accountMap.get(account));
        transaction.notifyObservers();
    }

    /**
     * Pay the upgrade
     * @param account the account
     * @param amount the amount
     * @param user the user
     * @param newPlan the new plan
     */
    private void payUpgrade(final Account account, final double amount,
                            final User user, final String newPlan) {
        if (account.getCurrency().equals("RON")) {
            if (account.getBalance() < amount) {
                addTransactionError(command.getAccount(), "Insufficient funds");
                return;
            }
            addTransactionUpdatePlan(command.getAccount(), newPlan);
            account.pay(amount);
            user.setPlan(newPlan);
        } else {
            double convertedAmount = amount * ExchangeOperations.getExchangeRate(exchangeRates,
                    "RON", account.getCurrency());
            if (account.getBalance() < convertedAmount) {
                addTransactionError(command.getAccount(), "Insufficient funds");
                return;
            }
            addTransactionUpdatePlan(command.getAccount(), newPlan);
            account.pay(convertedAmount);
            user.setPlan(newPlan);
        }
    }
}
