package org.poo.command.concrete;

import org.poo.appOperations.CardOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.HashMap;

public class DeleteCardCommand extends BaseCommand {
    private HashMap<String, User> usersMap;
    public DeleteCardCommand(final CommandInput command, final AppContext context,
                             final HashMap<String, User> usersMap) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
        this.usersMap = usersMap;
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        CardOperations.deleteCard(cardAccountMap, command, usersMap);
    }
}
