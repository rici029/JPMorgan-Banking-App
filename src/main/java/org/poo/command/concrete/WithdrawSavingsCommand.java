package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.appOperations.ExchangeOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.TransactionSavingsWithdraw;
import org.poo.transactions.Transactions;
import org.poo.user.User;

public class WithdrawSavingsCommand extends BaseCommand {
    private static final int MIN_AGE = 21;
    public WithdrawSavingsCommand(final CommandInput command,
                                  final AppContext context) {
        super(command, context);
    }

    /**
     * Method that executes the withdraw savings command.
     */
    @Override
    public void execute() {
        String iban = command.getAccount();
        if (!accountMap.containsKey(iban)) {
            return;
        }
        Account accountSavings = accountMap.get(iban);
        User user = usersAccountsMap.get(iban);
        if (user.getAge() < MIN_AGE) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "You don't have the minimum age required.");
            transaction.registerObserver(user);
            transaction.registerObserver(accountSavings);
            transaction.notifyObservers();
            return;
        }
        if (!accountSavings.getAccountType().equals("savings")) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Account is not of type savings.");
            transaction.registerObserver(user);
            transaction.registerObserver(accountSavings);
            transaction.notifyObservers();
            return;
        }
        double amountToWithdraw = command.getAmount();
        String toCurrency = command.getCurrency();
        if (!accountSavings.getCurrency().equals(toCurrency)) {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    accountSavings.getCurrency(), toCurrency);
            amountToWithdraw *= exchangeRate;
        }
        for (Account acc : user.getAccounts()) {
            if (acc.getCurrency().equals(toCurrency) && acc.getAccountType().equals("classic")) {
                if (accountSavings.getBalance() < amountToWithdraw) {
                    Transactions transaction = new TransactionAction(command.getTimestamp(),
                            "Insufficient funds");
                    transaction.registerObserver(user);
                    transaction.registerObserver(accountSavings);
                    transaction.notifyObservers();
                    return;
                }
                acc.setBalance(acc.getBalance() + amountToWithdraw);
                accountSavings.setBalance(accountSavings.getBalance() - amountToWithdraw);
                Transactions transaction = new TransactionSavingsWithdraw("Savings withdrawal",
                        command.getTimestamp(), accountSavings.getIban(),
                        amountToWithdraw, acc.getIban());
                transaction.registerObserver(user);
                transaction.registerObserver(user);
                transaction.registerObserver(acc);
                transaction.registerObserver(accountSavings);
                transaction.notifyObservers();
                return;
            }
        }
        Transactions transaction = new TransactionAction(command.getTimestamp(),
                "You do not have a classic account.");
        transaction.registerObserver(user);
        transaction.registerObserver(accountSavings);
        transaction.notifyObservers();
    }
}
