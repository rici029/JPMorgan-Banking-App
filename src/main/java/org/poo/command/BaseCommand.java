
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

    protected BaseCommand(final CommandInput command, final ArrayNode output,
                          final HashMap<String, HashMap<String, Double>> exchangeRates,
                          final ArrayList<User> users,
                          final HashMap<String, User> usersAccountsMap,
                          final HashMap<String, User> usersCardsMap,
                          final HashMap<String, Account> cardAccountMap,
                          final HashMap<String, Account> accountMap,
                          final HashMap<String, Account> aliasAccountMap) {
        this.command = command;
        this.output = output;
        this.exchangeRates = exchangeRates;
        this.users = users;
        this.usersAccountsMap = usersAccountsMap;
        this.usersCardsMap = usersCardsMap;
        this.cardAccountMap = cardAccountMap;
        this.accountMap = accountMap;
        this.aliasAccountMap = aliasAccountMap;
    }
}
