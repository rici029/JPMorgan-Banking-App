
package org.poo.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import org.poo.account.Account;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public class AppContext {
    private final ArrayNode output;
    private final HashMap<String, HashMap<String, Double>> exchangeRates;
    private final ArrayList<User> users;
    private final HashMap<String, User> usersAccountsMap;
    private final HashMap<String, User> usersCardsMap;
    private final HashMap<String, Account> cardAccountMap;
    private final HashMap<String, Account> accountMap;
    private final HashMap<String, Account> aliasAccountMap;

    public AppContext(final ArrayNode output,
                      final HashMap<String, HashMap<String, Double>> exchangeRates,
                      final ArrayList<User> users,
                      final HashMap<String, User> usersAccountsMap,
                      final HashMap<String, User> usersCardsMap,
                      final HashMap<String, Account> cardAccountMap,
                      final HashMap<String, Account> accountMap,
                      final HashMap<String, Account> aliasAccountMap) {
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
