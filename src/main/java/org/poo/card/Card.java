package org.poo.card;

import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

@Getter @Setter
public class Card {
    private String accountIBAN;
    private String email;
    private String cardNumber;
    private String cardType;
    private String cardStatus;

    public Card(final String accountIBAN, final String email, final String cardType) {
        this.accountIBAN = accountIBAN;
        this.email = email;
        this.cardNumber = Utils.generateCardNumber();
        this.cardType = cardType;
        this.cardStatus = "active";
    }
}
