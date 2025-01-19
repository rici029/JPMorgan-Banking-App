package org.poo.command;

import org.poo.command.concrete.*;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.splitPayment.SplitPayment;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;

public final class CommandFactory {

    private CommandFactory() {
    }

    /**
     * Create a command based on the command type
     * @param commandType the type of the command
     * @param commandInput the input of the command
     * @param context the context of the command
     * @return
     */
    public static Command createCommand(final String commandType, final CommandInput commandInput,
                                        final AppContext context,
                                        final ArrayList<Commerciant> commerciants,
                                        final ArrayList<SplitPayment> splitPayments,
                                        final HashMap<String, User> usersMap) {
        return switch (commandType) {
            case "printUsers" -> new PrintUsersCommand(commandInput, context);
            case "addAccount" -> new AddAccountCommand(commandInput, context);
            case "createCard", "createOneTimeCard" -> new CreateCardCommand(commandInput, context);
            case "addFunds" -> new AddFundsCommand(commandInput, context);
            case "deleteAccount" -> new DeleteAccountCommand(commandInput, context);
            case "deleteCard" -> new DeleteCardCommand(commandInput, context);
            case "setMinimumBalance" -> new SetMinimumBalanceCommand(commandInput, context);
            case "payOnline" -> new PayOnlineCommand(commandInput, context, commerciants);
            case "sendMoney" -> new SendMoneyCommand(commandInput, context);
            case "printTransactions" -> new PrintTransactionsCommand(commandInput, context);
            case "setAlias" -> new SetAliasCommand(commandInput, context);
            case "checkCardStatus" -> new CheckCardStatusCommand(commandInput, context);
            case "splitPayment" -> new SplitPaymentCommand(commandInput, context, splitPayments);
            case "report" -> new ReportCommand(commandInput, context);
            case "spendingsReport" -> new SpendingsReportCommand(commandInput, context);
            case "changeInterestRate" -> new ChangeInterestRateCommand(commandInput, context);
            case "addInterest" -> new AddInterestCommand(commandInput, context);
            case "withdrawSavings" -> new WithdrawSavingsCommand(commandInput, context);
            case "upgradePlan" -> new UpgradePlanCommand(commandInput, context);
            case "cashWithdrawal" -> new CashWithdrawalCommand(commandInput, context);
            case "acceptSplitPayment" -> new AcceptSplitPaymentCommand(commandInput, context,
                    splitPayments, usersMap);
            case "rejectSplitPayment" -> new RejectSplitPaymentCommand(commandInput, context,
                    splitPayments, usersMap);
            case "addNewBusinessAssociate" -> new AddNewBusinessAssociateCommand(commandInput, context, usersMap);
            case "changeSpendingLimit" -> new ChangeSpendingLimitCommand(commandInput, context, usersMap);
            default -> {
                System.out.println("Command " + commandType + " not found");
                yield null;
            }
        };
    }
}
