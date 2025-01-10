package org.poo.command.concrete;

import org.poo.appOperations.PrintOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;

public class ReportCommand extends BaseCommand {
    public ReportCommand(final CommandInput command, final AppContext context) {
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
        PrintOperations.report(command, accountMap, output);
    }
}
