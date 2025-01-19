package org.poo.command.concrete;

import org.poo.appOperations.AccountOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;

public class SendMoneyCommand extends BaseCommand {
    private ArrayList<Commerciant> commerciants;
    private HashMap<String, User> usersMap;
    public SendMoneyCommand(final CommandInput command, final AppContext context,
                            final ArrayList<Commerciant> commerciants,
                            final HashMap<String, User> usersMap) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
        this.commerciants = commerciants;
        this.usersMap = usersMap;
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        AccountOperations.sendMoney(command, exchangeRates, accountMap,
                usersAccountsMap, aliasAccountMap, output, commerciants, usersMap);
    }
}
