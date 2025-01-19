package org.poo.businessUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter @Setter
public class BusinessUser {
    private String name;
    private String email;
    private HashMap<Integer, Double> deposited;
    private HashMap<Integer, String> toCommerciantsSpendingList;
    private HashMap<Integer, Double> spent;
    private String role;

    public BusinessUser(final String name, final String email, final String role) {
        this.name = name;
        this.email = email;
        this.deposited = new HashMap<>();
        this.toCommerciantsSpendingList = new HashMap<>();
        this.spent = new HashMap<>();
        this.role = role;
    }

    /**
     * Method that creates a JSON object from the business user.
     * @param mapper the object mapper
     * @param spentTotal the amount spent
     * @param depositedTotal the amount deposited
     * @return the object node
     */
    public ObjectNode toJson(final ObjectMapper mapper, final double spentTotal,
                             final double depositedTotal) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("username", this.name);
        objectNode.put("spent", spentTotal);
        objectNode.put("deposited", depositedTotal);
        return objectNode;
    }
}
