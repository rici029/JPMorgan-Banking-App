package org.poo.appOperations;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.businessUser.BusinessUser;
import org.poo.card.Card;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.TransactionCard;
import org.poo.transactions.Transactions;
import org.poo.user.User;

import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.HashMap;

public final class CardOperations {
    private CardOperations() {
        //not called
    }

    private static void addTransactionToObservers(final Transactions transaction,
                                                  final User user, final Account account) {
        transaction.registerObserver(user);
        transaction.registerObserver(account);
        transaction.notifyObservers();
    }

    /**
     * Create a card for a user
     * @param command command input
     * @param usersCardsMap map of users and their cards
     * @param cardAccountMap map of cards and their accounts
     * @param accountMap map of users and their accounts
     * @param usersMap map of users
     */
    public static void createCard(final CommandInput command,
                                  final HashMap<String, User> usersCardsMap,
                                  final HashMap<String, Account> cardAccountMap,
                                  final HashMap<String, Account> accountMap,
                                  final HashMap<String, User> usersMap) {
        String email = command.getEmail();
        String cardType;
        if (command.getCommand().equals("createCard")) {
            cardType = "classic";
        } else {
            cardType = "onetime";
        }
        String iban = command.getAccount();
        User user = usersMap.get(email);
        Account account = accountMap.get(iban);
        if(user == null) {
            return;
        }
        if (!user.getEmail().equals(email) && !account.getAccountType().equals("business")) {
            return;
        }
        Card card = new Card(iban, email, cardType);
        account.addCard(card);
        usersCardsMap.put(card.getCardNumber(), user);
        cardAccountMap.put(card.getCardNumber(), account);
        Transactions transaction = new TransactionCard(card.getCardNumber(),
                account.getIban(), user.getEmail(), "New card created",
                command.getTimestamp());
        addTransactionToObservers(transaction, user, account);
    }

    /**
     * Delete a card
     * @param cardAccountMap map of cards and their accounts
     * @param command command input
     * @param usersCardsMap map of users and their cards
     */
    public static void deleteCard(final HashMap<String, Account> cardAccountMap,
                                  final CommandInput command,
                                  final  HashMap<String, User> usersCardsMap,
                                  final HashMap<String, User> usersMap) {
        String cardNumber = command.getCardNumber();
        if (!cardAccountMap.containsKey(cardNumber)) {
            return;
        }
        Account account = cardAccountMap.get(cardNumber);
        for (Card card : account.getCards()) {
            if (card.getCardNumber().equals(cardNumber)) {
                if(!usersMap.containsKey(command.getEmail())) {
                    return;
                }
                User user = usersMap.get(command.getEmail());
                if(account.getAccountType().equals("business")) {
                    BusinessAccount businessAccount = (BusinessAccount) account;
                    HashMap<String, BusinessUser> employees = businessAccount.getEmployees();
                    if(employees.containsKey(user.getEmail()) && !user.getEmail().equals(card.getEmail())) {
                        return;
                    }
                }
                account.removeCard(card);
                cardAccountMap.remove(cardNumber);
                Transactions transaction = new TransactionCard(card.getCardNumber(),
                        account.getIban(), user.getEmail(), "The card has been destroyed",
                        command.getTimestamp());
                addTransactionToObservers(transaction, user, account);
                break;
            }
        }
    }

    /**
     * Check the status of a card
     * @param usersCardsMap map of users and their cards
     * @param command command input
     * @param output output array
     * @param cardAccountMap map of cards and their accounts
     */
    public static void checkCardStatus(final HashMap<String, User> usersCardsMap,
                                       final CommandInput command,
                                       final ArrayNode output,
                                       final HashMap<String, Account> cardAccountMap) {
        String cardNumber = command.getCardNumber();
        if (!usersCardsMap.containsKey(cardNumber)) {
            PrintOperations.cardNotFound(output, command);
            return;
        }

        User user = usersCardsMap.get(cardNumber);
        Account account = cardAccountMap.get(cardNumber);
        for (Card card : account.getCards()) {
            if (card.getCardNumber().equals(cardNumber)) {
                if (account.getBalance() <= account.getMinimumBalance()) {
                    Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "You have reached the minimum amount of funds, the card will be frozen");
                    addTransactionToObservers(transaction, user, account);
                    card.setCardStatus("frozen");
                }
                return;
            }
        }
        PrintOperations.cardNotFound(output, command);
    }
}
