package org.poo.appOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import org.poo.fileio.CommandInput;

public final class PrintOperations {

    private PrintOperations() {
        //not called
    }

    /**
     *
     * @param command the command
     * @return the account
     */
    public static ObjectNode printSuccessJson(final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode outNode = mapper.createObjectNode();
        outNode.put("command", command.getCommand());
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", "Account deleted");
        successNode.put("timestamp", command.getTimestamp());
        outNode.set("output", successNode);
        outNode.put("timestamp", command.getTimestamp());
        return outNode;
    }

    /**
     *
     * @param output the output
     * @param command the command
     */
    public static void cardNotFound(final ArrayNode output, final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("timestamp", command.getTimestamp());
        errorNode.put("description", "Card not found");
        objectNode.set("output", errorNode);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);
    }

}
