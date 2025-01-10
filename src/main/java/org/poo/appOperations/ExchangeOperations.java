package org.poo.appOperations;

import java.util.HashMap;


public final class ExchangeOperations {
    private ExchangeOperations() {
        //not called
    }

    /**
     * Method that returns the exchange rate between two currencies.
     * @param exchangeRates the exchange rates
     * @param fromCurrency the currency to convert from
     * @param toCurrency the currency to convert to
     * @return the exchange rate
     */
    public static double getExchangeRate(final HashMap<String, HashMap<String,
                                                 Double>> exchangeRates,
                                         final String fromCurrency, final String toCurrency) {

        if (exchangeRates.containsKey(fromCurrency)
                && exchangeRates.get(fromCurrency).containsKey(toCurrency)) {
            return exchangeRates.get(fromCurrency).get(toCurrency);
        }

        if (exchangeRates.containsKey(toCurrency)
                && exchangeRates.get(toCurrency).containsKey(fromCurrency)) {
            return 1 / exchangeRates.get(toCurrency).get(fromCurrency);
        }

        for (String intermediateCurrency : exchangeRates.keySet()) {

            if (exchangeRates.containsKey(fromCurrency)
                    && exchangeRates.get(fromCurrency).containsKey(intermediateCurrency)) {
                if (exchangeRates.containsKey(intermediateCurrency)
                        && exchangeRates.get(intermediateCurrency).containsKey(toCurrency)) {
                    double toIntermediate = exchangeRates.get(fromCurrency).
                            get(intermediateCurrency);
                    double fromIntermediate = exchangeRates.get(intermediateCurrency).
                            get(toCurrency);
                    return toIntermediate * fromIntermediate;
                }
            }

            if (exchangeRates.containsKey(toCurrency)
                    && exchangeRates.get(toCurrency).containsKey(intermediateCurrency)) {
                if (exchangeRates.containsKey(intermediateCurrency)
                        && exchangeRates.get(intermediateCurrency).containsKey(fromCurrency)) {
                    double rateToIntermediate = exchangeRates.get(toCurrency).
                            get(intermediateCurrency);
                    double rateFromIntermediate = exchangeRates.get(intermediateCurrency).
                            get(fromCurrency);
                    return 1 / (rateToIntermediate * rateFromIntermediate);
                }
            }

            if (exchangeRates.get(intermediateCurrency).containsKey(fromCurrency)) {
                if (exchangeRates.containsKey(intermediateCurrency)
                        && exchangeRates.get(intermediateCurrency).containsKey(toCurrency)) {
                    double rateToIntermediate = exchangeRates.get(intermediateCurrency).
                            get(toCurrency);
                    double rateFromIntermediate = exchangeRates.get(intermediateCurrency).
                            get(fromCurrency);
                    return 1 / rateFromIntermediate * rateToIntermediate;
                }
            }

            if (exchangeRates.containsKey(fromCurrency)
                    && exchangeRates.get(fromCurrency).containsKey(intermediateCurrency)) {
                if (exchangeRates.containsKey(toCurrency)
                        && exchangeRates.get(toCurrency).containsKey(intermediateCurrency)) {
                    double rateToIntermediate = exchangeRates.get(toCurrency).
                            get(intermediateCurrency);
                    double rateFromIntermediate = exchangeRates.get(fromCurrency).
                            get(intermediateCurrency);
                    return rateFromIntermediate / rateToIntermediate;
                }
            }
        }

        return -1;
    }

}
