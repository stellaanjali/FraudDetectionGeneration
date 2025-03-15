package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FraudDataGeneration {
    public static void main(String[] args) {
        int numSamples = 1000;
        List<String[]> data = new ArrayList<>();

        // Add CSV headers
        data.add(new String[]{"timestamp", "debitor_balance", "creditor_balance", "amount",
                "debitor_txn_history", "creditor_txn_history", "debitor_avg_txn", "fraudulent"});

        Random rand = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Base timestamp for transactions
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

        for (int i = 0; i < numSamples; i++) {
            // Generate a readable timestamp within the last 30 days
            LocalDateTime timestamp = startDate.plusMinutes(rand.nextInt(60 * 24 * 30));
            String formattedTimestamp = "\"" + timestamp.format(formatter) + "\""; // Ensures Excel displays correctly

            // Generate random financial values using BigDecimal to avoid scientific notation
            BigDecimal debitorBalance = new BigDecimal(rand.nextDouble() * 1e9 + 10).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal creditorBalance = new BigDecimal(rand.nextDouble() * 1e9 + 10).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal amount = new BigDecimal(rand.nextDouble() * 1e6 + 10).setScale(2, BigDecimal.ROUND_HALF_UP);

            // Ensure numbers are formatted as plain strings without scientific notation
            String debitorBalanceStr = "\"" + debitorBalance.toPlainString() + "\"";
            String creditorBalanceStr = "\"" + creditorBalance.toPlainString() + "\"";
            String amountStr = "\"" + amount.toPlainString() + "\"";

            int debitorTxnHistory = rand.nextInt(200);
            int creditorTxnHistory = rand.nextInt(200);

            // Calculate an estimated average transaction amount for the debitor
            BigDecimal debitorAvgTxn = (debitorTxnHistory == 0) ? BigDecimal.TEN : debitorBalance.divide(new BigDecimal(debitorTxnHistory + 1), BigDecimal.ROUND_HALF_UP);
            String debitorAvgTxnStr = "\"" + debitorAvgTxn.toPlainString() + "\"";

            // Apply fraud rules
            int fraud = 0;
            if (debitorBalance.compareTo(new BigDecimal("100000000")) > 0 && debitorTxnHistory < 10) {
                fraud = 1;
            } else if (creditorBalance.compareTo(new BigDecimal("100000000")) > 0 && creditorTxnHistory < 10) {
                fraud = 1;
            } else if (debitorBalance.compareTo(new BigDecimal("100")) < 0 && creditorBalance.compareTo(new BigDecimal("100000000")) > 0) {
                fraud = 1;
            } else if (creditorBalance.compareTo(new BigDecimal("100")) < 0 && debitorBalance.compareTo(new BigDecimal("100000000")) > 0) {
                fraud = 1;
            } else if (debitorTxnHistory > 100) {
                fraud = 1;
            } else if (amount.compareTo(new BigDecimal("5000")) > 0 && rand.nextDouble() < 0.7) { // 70% chance of fraud for large amounts
                fraud = 1;
            } else if (amount.compareTo(debitorAvgTxn.multiply(new BigDecimal("70"))) > 0) { // New Rule: Amount is 70x average
                fraud = 1;
            }

            // Add row to data (using quotes to prevent formatting issues in Excel)
            data.add(new String[]{
                    formattedTimestamp,
                    debitorBalanceStr,
                    creditorBalanceStr,
                    amountStr,
                    String.valueOf(debitorTxnHistory),
                    String.valueOf(creditorTxnHistory),
                    debitorAvgTxnStr,
                    String.valueOf(fraud)
            });
        }

        // Write to CSV file
        try (FileWriter writer = new FileWriter("fraud_dataset.csv")) {
            for (String[] row : data) {
                writer.append(String.join(",", row)).append("\n");
            }
            System.out.println("Dataset generated and saved as 'fraud_dataset.csv' successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
