package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.appOperations.AccountOperations;
import org.poo.appOperations.ExchangeOperations;
import org.poo.card.Card;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.TransactionWithdraw;
import org.poo.transactions.Transactions;
import org.poo.user.User;

import java.util.ArrayList;

public class CashWithdrawalCommand extends BaseCommand {
    public CashWithdrawalCommand(final CommandInput command, final AppContext context) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
    }

    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        if (!cardAccountMap.containsKey(command.getCardNumber())) {
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("description", "Card not found");
            objectNode.put("timestamp", command.getTimestamp());
            commandOutput.set("output", objectNode);
            commandOutput.put("timestamp", command.getTimestamp());
            output.add(commandOutput);
            return;
        }
        Account account = cardAccountMap.get(command.getCardNumber());
        Card card = null;
        for (Card c : account.getCards()) {
            if(c.getCardNumber().equals(command.getCardNumber())) {
                card = c;
                break;
            }
        }
        if (card.getCardStatus().equals("frozen")){
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("description", "The card is frozen");
            objectNode.put("timestamp", command.getTimestamp());
            commandOutput.set("output", objectNode);
            commandOutput.put("timestamp", command.getTimestamp());
            output.add(commandOutput);
            return;
        }
        for (User user : users) {
            if(user.getEmail().equals(command.getEmail())){
                continueCashWithdrawal(account, user);
                return;
            }
        }
        ObjectNode commandOutput = mapper.createObjectNode();
        commandOutput.put("command", command.getCommand());
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("description", "User not found");
        objectNode.put("timestamp", command.getTimestamp());
        commandOutput.set("output", objectNode);
        commandOutput.put("timestamp", command.getTimestamp());
        output.add(commandOutput);
    }

    private void continueCashWithdrawal(final Account account, final User user) {
        ObjectMapper mapper = new ObjectMapper();
        double amount = command.getAmount();
        double balance = account.getBalance();
        if (!account.getCurrency().equals("RON"))
            amount = amount * ExchangeOperations.getExchangeRate(exchangeRates, "RON",
                    account.getCurrency());
        if (balance < amount) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Insufficient funds");
            transaction.registerObserver(account);
            transaction.registerObserver(user);
            transaction.notifyObservers();
            return;
        }
        if (balance - amount < account.getMinimumBalance()) {
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("description",
                    "Cannot perform payment due to a minimum balance being set");
            objectNode.put("timestamp", command.getTimestamp());
            commandOutput.set("output", objectNode);
            commandOutput.put("timestamp", command.getTimestamp());
            output.add(commandOutput);
        }
        account.setBalance(balance - amount);
        double commission = AccountOperations.checkForCommission(account, amount, user.getPlan(),exchangeRates);
        String description = "Cash withdrawal of " + command.getAmount();
        account.setBalance(account.getBalance() - commission);
        Transactions transaction = new TransactionWithdraw(command.getTimestamp(), description,
                command.getAmount());
        transaction.registerObserver(account);
        transaction.registerObserver(user);
        transaction.notifyObservers();
    }
}
