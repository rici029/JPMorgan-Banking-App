package org.poo.command.concrete;

import org.poo.appOperations.AccountOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class PayOnlineCommand extends BaseCommand {

    private ArrayList<Commerciant> commerciants;
    public PayOnlineCommand(final CommandInput command, final AppContext context,
                            final ArrayList<Commerciant> commerciants) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
        this.commerciants = commerciants;
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        AccountOperations.payOnline(command, exchangeRates, cardAccountMap,
                output, usersAccountsMap, usersCardsMap, commerciants);
    }
}
