
package org.poo.command.concrete;

import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.appOperations.PrintOperations;
import org.poo.fileio.CommandInput;

public class PrintUsersCommand extends BaseCommand {
    public PrintUsersCommand(final CommandInput command, final AppContext context) {
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
        PrintOperations.printUsers(output, users, command);
    }
}
