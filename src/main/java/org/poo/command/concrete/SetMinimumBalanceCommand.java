package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

public class SetMinimumBalanceCommand extends BaseCommand {
    public SetMinimumBalanceCommand(final CommandInput command, final AppContext context) {
        super(command, context);
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        String iban = command.getAccount();
        double minimumBalance = command.getMinBalance();
        if (!usersAccountsMap.containsKey(iban)) {
            return;
        }
        User user = usersAccountsMap.get(iban);
        for (Account account : user.getAccounts()) {
            if (account.getIban().equals(iban)) {
                if (!account.getEmail().equals(command.getEmail())) {
                    return;
                }
                account.setMinimumBalance(minimumBalance);
                break;
            }
        }
    }
}
