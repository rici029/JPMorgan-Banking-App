package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.appOperations.ExchangeOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.Transactions;
import org.poo.user.User;

public class WithdrawSavingsCommand extends BaseCommand {
    public WithdrawSavingsCommand(CommandInput command, AppContext context) {
        super(command, context.getOutput(), context.getExchangeRates(), context.getUsers(),
                context.getUsersAccountsMap(), context.getUsersCardsMap(), context.getCardAccountMap(),
                context.getAccountMap(), context.getAliasAccountMap());
    }

    @Override
    public void execute() {
        String iban = command.getAccount();
        if(!accountMap.containsKey(iban)) {
            // Account not found
            return;
        }
        Account account = accountMap.get(iban);
        User user = usersAccountsMap.get(iban);
        if(user.getAge() < 21) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "You don't have the minimum age required.");
            transaction.registerObserver(user);
            transaction.registerObserver(account);
            transaction.notifyObservers();
            return;
        }
        if(!account.getAccountType().equals("savings")){
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Account is not of type savings.");
            transaction.registerObserver(user);
            transaction.registerObserver(account);
            transaction.notifyObservers();
            return;
        }
        double amountToWithdraw = command.getAmount();
        String toCurrency = command.getCurrency();
        if(!account.getCurrency().equals(toCurrency)) {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates, account.getCurrency(), toCurrency);
            amountToWithdraw *= exchangeRate;
        }
        if(account.getBalance() < amountToWithdraw) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Insufficient funds");
            transaction.registerObserver(user);
            transaction.registerObserver(account);
            transaction.notifyObservers();
            return;
        }
        for(Account acc : user.getAccounts()) {
            if(acc.getCurrency().equals(toCurrency) && acc.getAccountType().equals("classic")) {
                acc.setBalance(acc.getBalance() + amountToWithdraw);
                account.setBalance(account.getBalance() - amountToWithdraw);
                Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "Savings withdrawal");
                transaction.registerObserver(user);
                transaction.registerObserver(account);
                transaction.notifyObservers();
                return;
            }
        }
        Transactions transaction = new TransactionAction(command.getTimestamp(),
                "You do not have a classic account.");
        transaction.registerObserver(user);
        transaction.registerObserver(account);
        transaction.notifyObservers();
    }
}
