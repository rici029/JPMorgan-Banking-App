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

    public ObjectNode toJson(ObjectMapper mapper, double spent, double deposited) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("username", this.name);
        objectNode.put("spent", spent);
        objectNode.put("deposited", deposited);
        return objectNode;
    }
}
