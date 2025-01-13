package org.poo.utils;

import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommerciantInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.UserInput;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public final class Utils {
    private Utils() {
        // Checkstyle error free constructor
    }

    private static final int IBAN_SEED = 1;
    private static final int CARD_SEED = 2;
    private static final int DIGIT_BOUND = 10;
    private static final int DIGIT_GENERATION = 16;
    private static final String RO_STR = "RO";
    private static final String POO_STR = "POOB";


    private static Random ibanRandom = new Random(IBAN_SEED);
    private static Random cardRandom = new Random(CARD_SEED);

    /**
     * Utility method for generating an IBAN code.
     *
     * @return the IBAN as String
     */
    public static String generateIBAN() {
        StringBuilder sb = new StringBuilder(RO_STR);
        for (int i = 0; i < RO_STR.length(); i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        sb.append(POO_STR);
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Utility method for generating a card number.
     *
     * @return the card number as String
     */
    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(cardRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Resets the seeds between runs.
     */
    public static void resetRandom() {
        ibanRandom = new Random(IBAN_SEED);
        cardRandom = new Random(CARD_SEED);
    }


    /**
     * Creates a list of users from input.
     *
     * @param userInputs the list of user from input
     * @return the list of users
     */
    public static ArrayList<User> createUsers(final UserInput[] userInputs) {
        ArrayList<User> users = new ArrayList<>();
        for (UserInput userInput : userInputs) {
            int age = calculateAge(userInput.getBirthDate());
            User user = new User(userInput.getFirstName(), userInput.getLastName(),
                    userInput.getEmail(), userInput.getBirthDate(),
                    userInput.getOccupation(), age);
            users.add(user);
        }

        return users;
    }


    /**
     * Calculates the age of a user based on the birth date.
     * @param birthDate the birth date of the user
     * @return the age of the user
     */
    public static int calculateAge(final String birthDate) {
        LocalDate birthDateLocal = LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE);

        LocalDate currentDate = LocalDate.now();

        return Period.between(birthDateLocal, currentDate).getYears();
    }
    /**
     * Creates a HashMap of exchange rates from input.
     *
     * @param exchangeRates the list of exchange rates from input
     * @return the map of exchange rates
     */
    public static HashMap<String, HashMap<String,
            Double>> createExchangeRates(final ExchangeInput[] exchangeRates) {
        HashMap<String, HashMap<String, Double>> exchangeRatesMap = new HashMap<>();
        for (ExchangeInput exchangeRate : exchangeRates) {
            HashMap<String, Double> currencyMap = new HashMap<>();
            if (!exchangeRatesMap.containsKey(exchangeRate.getFrom())) {
                currencyMap.put(exchangeRate.getTo(), exchangeRate.getRate());
                exchangeRatesMap.put(exchangeRate.getFrom(), currencyMap);
            } else {
                exchangeRatesMap.get(exchangeRate.getFrom()).put(exchangeRate.getTo(),
                        exchangeRate.getRate());
            }
        }

        return exchangeRatesMap;
    }

    /**
     * Creates a list of commerciants from input.
     * @param commerciantInputs the list of commerciants from input
     * @return the list of commerciants
     */
    public static ArrayList<Commerciant> createCommerciant(final CommerciantInput[] commerciantInputs) {
        ArrayList<Commerciant> commerciants = new ArrayList<>();
        for (CommerciantInput commerciantInput : commerciantInputs) {
            Commerciant commerciant = new Commerciant(commerciantInput.getCommerciant(),
                    commerciantInput.getAccount(), commerciantInput.getType(),
                    commerciantInput.getCashbackStrategy());
            commerciants.add(commerciant);
        }

        return commerciants;
    }
}
