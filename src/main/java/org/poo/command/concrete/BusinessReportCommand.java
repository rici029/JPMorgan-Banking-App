package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.BusinessAccount;
import org.poo.businessUser.BusinessUser;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BusinessReportCommand extends BaseCommand {
    public BusinessReportCommand(final CommandInput command, final AppContext context) {
        super(command, context);
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        if (command.getType().equals("transaction")) {
            businessReportTransaction();
        } else {
            businessReportCommerciants();
        }
    }

    /**
     * Create the report for the transaction type
     */
    private void businessReportTransaction() {
        ObjectMapper mapper = new ObjectMapper();
        double totalSpent = 0;
        double toatalDeposited = 0;
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        BusinessAccount account = (BusinessAccount) accountMap.get(command.getAccount());
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", command.getAccount());
        outputNode.put("balance", account.getBalance());
        outputNode.put("currency", account.getCurrency());
        outputNode.put("spending limit", account.getSpendingLimit());
        outputNode.put("deposit limit", account.getDepositLimit());
        outputNode.put("statistics type", "transaction");
        ArrayNode managersNode = mapper.createArrayNode();
        HashMap<String, BusinessUser> managers = account.getManagers();
        for (BusinessUser manager : managers.values()) {
            double spent = 0;
            double deposited = 0;
            for (int i = startTimestamp; i <= endTimestamp; i++) {
                if (manager.getSpent().containsKey(i)) {
                    spent += manager.getSpent().get(i);
                }

                if (manager.getDeposited().containsKey(i)) {
                    deposited += manager.getDeposited().get(i);
                }
            }
            totalSpent += spent;
            toatalDeposited += deposited;
            managersNode.add(manager.toJson(mapper, spent, deposited));
        }
        outputNode.set("managers", managersNode);
        ArrayNode employeesNode = mapper.createArrayNode();
        HashMap<String, BusinessUser> employees = account.getEmployees();
        for (BusinessUser employee : employees.values()) {
            double spent = 0;
            double deposited = 0;
            for (int i = startTimestamp; i <= endTimestamp; i++) {
                if (employee.getSpent().containsKey(i)) {
                    spent += employee.getSpent().get(i);
                }

                if (employee.getDeposited().containsKey(i)) {
                    deposited += employee.getDeposited().get(i);
                }
            }
            totalSpent += spent;
            toatalDeposited += deposited;
            employeesNode.add(employee.toJson(mapper, spent, deposited));
        }
        outputNode.set("employees", employeesNode);
        outputNode.put("total spent", totalSpent);
        outputNode.put("total deposited", toatalDeposited);
        objectNode.set("output", outputNode);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);

    }

    /**
     * Create the report for the commerciants type
     */
    private void businessReportCommerciants() {
        ObjectMapper mapper = new ObjectMapper();
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        BusinessAccount account = (BusinessAccount) accountMap.get(command.getAccount());
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", command.getAccount());
        outputNode.put("balance", account.getBalance());
        outputNode.put("currency", account.getCurrency());
        outputNode.put("spending limit", account.getSpendingLimit());
        outputNode.put("deposit limit", account.getDepositLimit());
        outputNode.put("statistics type", "commerciant");
        ArrayNode commerciantsNode = mapper.createArrayNode();
        ArrayList<String> commerciants = account.getCommerciants();
        Collections.sort(commerciants);
        for (String commerciant : commerciants) {
            double spent = 0;
            ArrayList<String> managersUsernames = new ArrayList<>();
            for (BusinessUser manager : account.getManagers().values()) {
                for (Map.Entry<Integer, String> entry
                        : manager.getToCommerciantsSpendingList().entrySet()) {
                    if (entry.getValue().equals(commerciant) && entry.getKey() >= startTimestamp
                            && entry.getKey() <= endTimestamp) {
                        spent += manager.getSpent().get(entry.getKey());
                        managersUsernames.add(manager.getName());
                    }
                }
            }
            ArrayList<String> employeesUsernames = new ArrayList<>();
            for (BusinessUser employee : account.getEmployees().values()) {
                for (Map.Entry<Integer, String> entry
                        : employee.getToCommerciantsSpendingList().entrySet()) {
                    if (entry.getValue().equals(commerciant) && entry.getKey() >= startTimestamp
                            && entry.getKey() <= endTimestamp) {
                        spent += employee.getSpent().get(entry.getKey());
                        employeesUsernames.add(employee.getName());
                    }
                }
            }
            ObjectNode commerciantNode = mapper.createObjectNode();
            commerciantNode.put("commerciant", commerciant);
            commerciantNode.put("total received", spent);
            Collections.sort(managersUsernames);
            ArrayNode managersNode = mapper.createArrayNode();
            for (String manager : managersUsernames) {
                managersNode.add(manager);
            }
            commerciantNode.set("managers", managersNode);
            Collections.sort(employeesUsernames);
            ArrayNode employeesNode = mapper.createArrayNode();
            for (String employee : employeesUsernames) {
                employeesNode.add(employee);
            }
            commerciantNode.set("employees", employeesNode);
            commerciantsNode.add(commerciantNode);
        }
        outputNode.set("commerciants", commerciantsNode);
        objectNode.set("output", outputNode);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);
    }
}
