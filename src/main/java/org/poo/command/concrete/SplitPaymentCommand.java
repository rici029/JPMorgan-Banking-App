package org.poo.command.concrete;

import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.splitPayment.CustomSplitPayment;
import org.poo.splitPayment.SplitPayment;

import java.util.ArrayList;

public class SplitPaymentCommand extends BaseCommand {
    private ArrayList<SplitPayment> splitPayments;
    public SplitPaymentCommand(final CommandInput command, final AppContext context,
                               final ArrayList<SplitPayment> splitPayments) {
        super(command, context);
        this.splitPayments = splitPayments;
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        String type = command.getSplitPaymentType();
        if (type.equals("equal")) {
            SplitPayment splitPayment = new SplitPayment(command.getAccounts(),
                    command.getAmount(), type, command.getTimestamp(), command.getCurrency());
            splitPayments.add(splitPayment);
        } else {
            SplitPayment splitPayment = new CustomSplitPayment(command.getAccounts(),
                    command.getAmount(), type, command.getTimestamp(), command.getCurrency(),
                    command.getAmountForUsers());
            splitPayments.add(splitPayment);
        }
    }
}
