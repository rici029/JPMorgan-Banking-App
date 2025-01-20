
package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.account.Account;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseCommand implements Command {
    protected final CommandInput command;
    protected final ArrayNode output;
    protected final ArrayList<User> users;
    protected final HashMap<String, User> usersAccountsMap;
    protected final HashMap<String, User> usersCardsMap;
    protected final HashMap<String, Account> cardAccountMap;
    protected final HashMap<String, Account> accountMap;
    protected final HashMap<String, Account> aliasAccountMap;
    protected final HashMap<String, HashMap<String, Double>> exchangeRates;

    protected BaseCommand(final CommandInput command, final AppContext context) {
        this.command = command;
        this.output = context.getOutput();
        this.exchangeRates = context.getExchangeRates();
        this.users = context.getUsers();
        this.usersAccountsMap = context.getUsersAccountsMap();
        this.usersCardsMap = context.getUsersCardsMap();
        this.cardAccountMap = context.getCardAccountMap();
        this.accountMap = context.getAccountMap();
        this.aliasAccountMap = context.getAliasAccountMap();
    }
}
