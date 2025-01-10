package org.poo.command.concrete;

import org.poo.appOperations.AccountOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;

public class ChangeInterestRateCommand extends BaseCommand {
    public ChangeInterestRateCommand(final CommandInput command, final AppContext context) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
        AccountOperations.changeInterestRate(accountMap, command, output, usersAccountsMap);
    }
}
