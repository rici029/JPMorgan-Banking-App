package org.poo.businessUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BusinessUser {
    private String name;
    private String email;
    private double deposited;
    private double spent;
    private String role;

    public BusinessUser(final String name, final String email, final String role) {
        this.name = name;
        this.email = email;
        this.deposited = 0;
        this.spent = 0;
        this.role = role;
    }

    public ObjectNode toJson(ObjectMapper mapper) {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("name", this.name);
        objectNode.put("spent", this.spent);
        objectNode.put("deposited", this.deposited);
        return objectNode;
    }
}
