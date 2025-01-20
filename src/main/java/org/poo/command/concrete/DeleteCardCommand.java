package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.businessUser.BusinessUser;
import org.poo.card.Card;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionCard;
import org.poo.transactions.Transactions;
import org.poo.user.User;

import java.util.HashMap;

public class DeleteCardCommand extends BaseCommand {
    private HashMap<String, User> usersMap;
    public DeleteCardCommand(final CommandInput command, final AppContext context,
                             final HashMap<String, User> usersMap) {
        super(command, context);
        this.usersMap = usersMap;
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        String cardNumber = command.getCardNumber();
        if (!cardAccountMap.containsKey(cardNumber)) {
            return;
        }
        Account account = cardAccountMap.get(cardNumber);
        for (Card card : account.getCards()) {
            if (card.getCardNumber().equals(cardNumber)) {
                if (!usersMap.containsKey(command.getEmail())) {
                    return;
                }
                User user = usersMap.get(command.getEmail());
                if (account.getAccountType().equals("business")) {
                    BusinessAccount businessAccount = (BusinessAccount) account;
                    HashMap<String, BusinessUser> employees = businessAccount.getEmployees();
                    if (employees.containsKey(user.getEmail())
                            && !user.getEmail().equals(card.getEmail())) {
                        return;
                    }
                }
                if (!account.getAccountType().equals("business")
                        && !card.getEmail().equals(user.getEmail())) {
                    return;
                }
                if (account.getBalance() > 0) {
                    return;
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
     * Add a transaction to the observers
     * @param transaction transaction
     * @param user user
     * @param account account
     */
    private static void addTransactionToObservers(final Transactions transaction,
                                                 final User user, final Account account) {
        transaction.registerObserver(user);
        transaction.registerObserver(account);
        transaction.notifyObservers();
    }
}
