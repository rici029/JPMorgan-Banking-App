package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.businessUser.BusinessUser;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;

public class AddFundsCommand extends BaseCommand {
    public AddFundsCommand(final CommandInput command, final AppContext context) {
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
        String iban = command.getAccount();
        double amount = command.getAmount();
        if (!usersAccountsMap.containsKey(iban)) {
            return;
        }
        Account account = accountMap.get(iban);
        if (account.getAccountType().equals("business")
                && !account.getEmail().equals(command.getEmail())) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            BusinessUser businessUser = null;
            if (businessAccount.getManagers().containsKey(command.getEmail())) {
                businessUser = businessAccount.getManagers().get(command.getEmail());
            } else if (businessAccount.getEmployees().containsKey(command.getEmail())) {
                businessUser = businessAccount.getEmployees().get(command.getEmail());
            }
            if (businessUser == null) {
                return;
            }
            if (businessUser.getRole().equals("employee")) {
                if (businessAccount.getDepositLimit() != 0
                        && amount > businessAccount.getDepositLimit()) {
                    return;
                }
            }
            businessUser.getDeposited().put(command.getTimestamp(), amount);
        }
        account.deposit(amount);
    }
}
