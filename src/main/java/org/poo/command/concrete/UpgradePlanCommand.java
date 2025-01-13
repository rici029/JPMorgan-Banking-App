package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
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
            //Account not found
        }
        Account account = accountMap.get(command.getAccount());
        User user = usersAccountsMap.get(command.getAccount());
        int getUserPlanIdx = getPlanIdx(user.getPlan());
        int getNewPlanIdx = getPlanIdx(newPlan);
        if(getUserPlanIdx < getNewPlanIdx) {
            //Upgrade plan
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
}
