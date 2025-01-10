package org.poo.command;

import org.poo.command.concrete.*;
import org.poo.fileio.CommandInput;

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
                                        final AppContext context) {
        return switch (commandType) {
            case "printUsers" -> new PrintUsersCommand(commandInput, context);
            case "addAccount" -> new AddAccountCommand(commandInput, context);
            case "createCard", "createOneTimeCard" -> new CreateCardCommand(commandInput, context);
            case "addFunds" -> new AddFundsCommand(commandInput, context);
            case "deleteAccount" -> new DeleteAccountCommand(commandInput, context);
            case "deleteCard" -> new DeleteCardCommand(commandInput, context);
            case "setMinimumBalance" -> new SetMinimumBalanceCommand(commandInput, context);
            case "payOnline" -> new PayOnlineCommand(commandInput, context);
            case "sendMoney" -> new SendMoneyCommand(commandInput, context);
            case "printTransactions" -> new PrintTransactionsCommand(commandInput, context);
            case "setAlias" -> new SetAliasCommand(commandInput, context);
            case "checkCardStatus" -> new CheckCardStatusCommand(commandInput, context);
            case "splitPayment" -> new SplitPaymentCommand(commandInput, context);
            case "report" -> new ReportCommand(commandInput, context);
            case "spendingsReport" -> new SpendingsReportCommand(commandInput, context);
            case "changeInterestRate" -> new ChangeInterestRateCommand(commandInput, context);
            case "addInterest" -> new AddInterestCommand(commandInput, context);
            default -> throw new IllegalArgumentException("Unknown command type: " + commandType);
        };
    }
}
