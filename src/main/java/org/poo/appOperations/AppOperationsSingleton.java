package org.poo.appOperations;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.Command;
import org.poo.command.CommandFactory;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.splitPayment.SplitPayment;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;

@Getter @Setter
public final class AppOperationsSingleton {
    private static AppOperationsSingleton instance;

    private HashMap<String, HashMap<String, Double>> exchangeRates;
    private CommandInput[] commands;
    private ArrayList<User> users;
    private ArrayList<Commerciant> commerciants;
    private HashMap<String, User> usersAccountsMap;
    private HashMap<String, User> usersCardsMap;
    private HashMap<String, Account> cardAccountMap;
    private HashMap<String, Account> accountMap;
    private HashMap<String, Account> aliasAccountMap;
    private ArrayList<SplitPayment> splitPayments;
    private HashMap<String, User> usersMap;

    private AppOperationsSingleton(final HashMap<String, HashMap<String, Double>> exchangeRates,
                                   final CommandInput[] commands, final ArrayList<User> users,
                                   final ArrayList<Commerciant> commerciants,
                                   final HashMap<String, User> usersMap) {
        this.exchangeRates = exchangeRates;
        this.commands = commands;
        this.users = users;
        this.usersAccountsMap = new HashMap<>();
        this.usersCardsMap = new HashMap<>();
        this.cardAccountMap = new HashMap<>();
        this.accountMap = new HashMap<>();
        this.aliasAccountMap = new HashMap<>();
        this.commerciants = commerciants;
        this.splitPayments = new ArrayList<>();
        this.usersMap = usersMap;
    }

    /**
     * Method that creates a new instance of the AppOperationsSingleton class.
     * @param exchangeRates the exchange rates
     * @param commands the commands
     * @param users the users
     * @return the instance of the AppOperationsSingleton class
     */
    public static AppOperationsSingleton getInstance(final HashMap<String, HashMap<String,
                                                             Double>> exchangeRates,
                                                     final CommandInput[] commands,
                                                     final ArrayList<User> users,
                                                     final ArrayList<Commerciant> commerciants,
                                                     final HashMap<String, User> usersMap) {
        if (instance == null) {
            synchronized (AppOperationsSingleton.class) {
                if (instance == null) {
                    instance = new AppOperationsSingleton(exchangeRates, commands, users,
                            commerciants, usersMap);
                }
            }
        }
        return instance;
    }

    /**
     * Method that starts the application and processes the commands.
     * @param output the output array
     */
    public void startApp(final ArrayNode output) {
        for (CommandInput commandInput : commands) {
            Command command = CommandFactory.createCommand(
                    commandInput.getCommand(),
                    commandInput,
                    new AppContext(
                            output,
                            exchangeRates,
                            users,
                            usersAccountsMap,
                            usersCardsMap,
                            cardAccountMap,
                            accountMap,
                            aliasAccountMap
                    ),
                    commerciants,
                    splitPayments,
                    usersMap
            );
            if (command != null) {
                command.execute();
            }
        }
    }

    /**
     * Method that resets the static instance of the AppOperationsSingleton class.
     */
    public static void resetInstance() {
        instance = null;
    }
}

