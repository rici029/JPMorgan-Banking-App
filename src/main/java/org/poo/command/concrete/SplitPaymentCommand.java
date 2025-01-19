package org.poo.command.concrete;

import org.poo.appOperations.AccountOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.splitPayment.CustomSplitPayment;
import org.poo.splitPayment.SplitPayment;

import java.util.ArrayList;

public class SplitPaymentCommand extends BaseCommand {
    ArrayList<SplitPayment> splitPayments;
    public SplitPaymentCommand(final CommandInput command, final AppContext context,
                               final ArrayList<SplitPayment> SplitPayments) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
        this.splitPayments = SplitPayments;
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        String type = command.getSplitPaymentType();
        if(type.equals("equal")) {
            SplitPayment splitPayment = new SplitPayment(command.getAccounts(), command.getAmount(),
                    type, command.getTimestamp(), command.getCurrency());
            splitPayments.add(splitPayment);
        } else {
            SplitPayment splitPayment = new CustomSplitPayment(command.getAccounts(), command.getAmount(),
                    type, command.getTimestamp(), command.getCurrency(), command.getAmountForUsers());
            splitPayments.add(splitPayment);
        }
    }
}
