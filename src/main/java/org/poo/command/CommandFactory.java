package org.poo.command;

import org.poo.command.concrete.*;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

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
                                        final ArrayList<Commerciant> commerciants) {
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
            case "splitPayment" -> new SplitPaymentCommand(commandInput, context);
            case "report" -> new ReportCommand(commandInput, context);
            case "spendingsReport" -> new SpendingsReportCommand(commandInput, context);
            case "changeInterestRate" -> new ChangeInterestRateCommand(commandInput, context);
            case "addInterest" -> new AddInterestCommand(commandInput, context);
            case "withdrawSavings" -> new WithdrawSavingsCommand(commandInput, context);
            case "upgradePlan" -> new UpgradePlanCommand(commandInput, context);
            default -> {
                System.out.println("Command " + commandType + " not found");
                yield null;
            }
        };
    }
}
