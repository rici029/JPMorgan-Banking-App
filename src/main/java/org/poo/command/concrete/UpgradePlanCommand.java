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
        if(user == null) {
            return;
        }

        int userPlanIdx = getPlanIdx(user.getPlan());
        int newPlanIdx = getPlanIdx(newPlan);
        if(userPlanIdx < newPlanIdx) {
            if(newPlan.equals("silver")) {
                payUpgrade(account, 100, user, newPlan);
                return;
            } else if(userPlanIdx != 1) {
                payUpgrade(account, 250, user, newPlan);
                return;
            } else {
                payUpgrade(account, 350, user, newPlan);
                return;
            }
        }
        if(userPlanIdx > newPlanIdx) {
            addTransactionError(command.getAccount(), "You cannot downgrade your plan.");
        } else {
            addTransactionError(command.getAccount(), "The user already has the " +
                    getPlanType(newPlanIdx) + " plan.");
        }
    }

    private int getPlanIdx(String plan) {
        if (plan.equals("student") || plan.equals("standard")) {
            return 1;
        } else if (plan.equals("silver")) {
            return 2;
        } else
            return 3;
    }

    private String getPlanType(int idx) {
        if (idx == 2) {
            return "silver";
        } else
            return "gold";
    }

    private void addTransactionError(final String account, final String message) {
        Transactions transaction = new TransactionAction(command.getTimestamp(), message);
        transaction.registerObserver(usersAccountsMap.get(account));
        transaction.registerObserver(accountMap.get(account));
        transaction.notifyObservers();
    }

    private void addTransactionUpdatePlan(final String account, final String newPlan) {
        Transactions transaction = new TransactionUpdatePlan(account, newPlan,
                "Upgrade plan", command.getTimestamp());
        transaction.registerObserver(usersAccountsMap.get(account));
        transaction.registerObserver(accountMap.get(account));
        transaction.notifyObservers();
    }

    private void payUpgrade(Account account, double amount, User user, String newPlan) {
        if(account.getCurrency().equals("RON")) {
            if(account.getBalance() < amount) {
                addTransactionError(command.getAccount(), "Insufficient funds");
                return;
            }
            addTransactionUpdatePlan(command.getAccount(), newPlan);
            account.pay(amount);
            user.setPlan(newPlan);
        } else {
            double convertedAmount = amount * ExchangeOperations.getExchangeRate(exchangeRates,
                    "RON", account.getCurrency());
            if(account.getBalance() < convertedAmount) {
                addTransactionError(command.getAccount(), "Insufficient funds");
                return;
            }
            addTransactionUpdatePlan(command.getAccount(), newPlan);
            account.pay(convertedAmount);
            user.setPlan(newPlan);
        }
    }
}
