package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;

public class SetAliasCommand extends BaseCommand {
    public SetAliasCommand(final CommandInput command, final AppContext context) {
        super(command, context);
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        String iban = command.getAccount();
        String alias = command.getAlias();
        if (!accountMap.containsKey(iban)) {
            return;
        }
        Account account = accountMap.get(iban);
        account.setAlias(alias);
        aliasAccountMap.put(alias, account);
    }
}
