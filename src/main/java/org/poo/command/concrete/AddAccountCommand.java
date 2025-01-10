
package org.poo.command.concrete;

import org.poo.command.BaseCommand;
import org.poo.command.AppContext;
import org.poo.fileio.CommandInput;
import org.poo.appOperations.AccountOperations;

public class AddAccountCommand extends BaseCommand {
    public AddAccountCommand(final CommandInput command, final AppContext context) {
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
        AccountOperations.addAccount(users, command, usersAccountsMap, accountMap);
    }
}
