/**
 * ATM class represents a driver program for Bank project.
 * It allows users to perform transactions such as check balance, deposit, and withdraw money from their account.
 * Users are prompted to enter their ID and transaction type until they choose the fourth option to quit the program.
 * The program reads data from a file containing account information and writes the output to another file.
 */
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

public class ATM {
    /**
     * Main method of the program.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        try {
            // Read data from a file into a Bank.
            // Each line of the file has info for one account.
            BankInterface bank = readFromFile("accounts_list.txt");
            bank.sortAccounts();

            // Print all the data stored in the bank.
            System.out.println(bank);

            IOHandlerInterface ioh = new IOHandlerDialog();

            String id, transactionType;

            // Read user input.
            do {
                id = readUserID(ioh);
                if (id.equalsIgnoreCase("quit")) {
                    ioh.put("Thank you for using our ATM. Goodbye!");
                    System.exit(0);
                } else if (isValid(bank, id)) {
                    ioh.put("Account FOUND for ID: " + id);
                    ioh.put(bank.search(id).toString());
                    break;
                } else {
                    ioh.put("Account NOT FOUND for ID: " + id);
                }
            } while (true);

            // Prompt user to choose a transaction type until they choose to quit.
            boolean exit = false;

            do {
                // Read user input.
                transactionType = getTransaction(ioh);

                switch (transactionType) {
                    // Check balance.
                    case "1" -> checkBalance(bank, id, ioh);

                    // Deposit.
                    case "2" -> deposit(bank, id, ioh);

                    // Withdraw.
                    case "3" -> withdraw(bank, id, ioh);

                    // Quit.
                    case "4" -> {
                        ioh.put("Thank you for using our ATM. Goodbye!");
                        exit = true;
                    }

                    // Invalid transaction type.
                    default -> ioh.put("Invalid transaction type: " + transactionType);
                }
            } while (!exit);

            writeToFile("output_list.txt", bank);

        } catch (IOException ioe) {
            System.out.println("IOException in main: " + ioe.getMessage());
            ioe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception in main: " + e.getMessage());

            e.printStackTrace();
        } // end catch

    }   // end main

    /**
     * readFromFile: Read the accounts from a text file
     */
    public static BankInterface readFromFile(String fileName) throws IOException {
        // Create a bank.
        BankInterface bank = new Bank2("Bank of Kazakhstan");
        // Open a file for reading.
        Scanner inputSource = new Scanner(new File(fileName));

        // while there are more tokens to read from the input source...
        while (inputSource.hasNext()) {

            // Read one line of input from the file into an Account object
            Account account = InputManager.readOneAccountFrom(inputSource);

            // Store the account info in the bank.
            bank.addAccount(account);
        } // end while
        return bank;
    } // end readFromFile

    /**
     * readUserID:
     *
     * @param IOHandlerInterface ioh An IOHandlerInterface object to get user input
     * @return String The ID entered by the user
     *
     * Prompts the user to enter their ID and returns it as a String.
     */
    public static String readUserID(IOHandlerInterface ioh) {
        return ioh.get("Please enter your ID or 'quit': ");
    }

    /**
     * isValid:
     *
     * @param BankInterface bank A BankInterface object
     * @param String id The ID to check
     * @return boolean Returns true if the ID is valid, false otherwise
     *
     * Checks whether the given ID is valid in the given bank.
     */
    public static boolean isValid(BankInterface bank, String id) {
        return bank.search(id) != null;
    }

    /**
     * checkBalance:
     *
     * @param BankInterface bank A BankInterface object
     * @param String id The ID of the account to check
     * @param IOHandlerInterface ioh An IOHandlerInterface object to output the balance
     * @return void
     *
     * Retrieves the account with the given ID from the given bank and outputs its balance using the given IOHandlerInterface object.
     */
    public static void checkBalance(BankInterface bank, String id, IOHandlerInterface ioh) {
        ioh.put(bank.search(id).toString() + "\n");
    }

    /**
     * deposit:
     *
     * @param BankInterface bank A BankInterface object
     * @param String id The ID of the account to deposit to
     * @param IOHandlerInterface ioh An IOHandlerInterface object to get input and output
     * @return void
     *
     * Retrieves the account with the given ID from the given bank and prompts the user to enter an amount to deposit. Deposits the given amount to the account and outputs the new balance using the given IOHandlerInterface object.
     */
    public static void deposit(BankInterface bank, String id, IOHandlerInterface ioh) {
        Account account = bank.search(id);
        Money amount = readMoney(ioh);
        account.deposit(amount);
        ioh.put("Deposit successful. New balance for account (" + account.getId() + "): " + account.getBalance() + "\n");
    }

    /**
     * withdraw:
     *
     * @param BankInterface bank A BankInterface object
     * @param String id The ID of the account to withdraw from
     * @param IOHandlerInterface ioh An IOHandlerInterface object to get input and output
     * @return void
     *
     * Retrieves the account with the given ID from the given bank and prompts the user to enter an amount to withdraw. Withdraws the given amount from the account and outputs the new balance using the given IOHandlerInterface object.
     */
    public static void withdraw(BankInterface bank, String id, IOHandlerInterface ioh) {
        Account account = bank.search(id);
        Money amount = readMoney(ioh);
        if (account instanceof Checking checking) {
            if (checking.getBalance().compareTo(amount) >= 0 || checking.getBalance().add(checking.getOverdraftMaximum()).compareTo(amount) >= 0) {
                checking.withdraw(amount);
                ioh.put("Withdrawal successful. New balance for account (" + checking.getId() + "): " + checking.getBalance() + "\n");
            } else {
                ioh.put("Withdrawal failed. Insufficient funds in account (" + checking.getId() + ").\n");
            }
        } else {
            if (account.getBalance().compareTo(amount) >= 0) {
                account.withdraw(amount);
                ioh.put("Withdrawal successful. New balance for account (" + account.getId() + "): " + account.getBalance() + "\n");
            } else {
                ioh.put("Withdrawal failed. Insufficient funds in account (" + account.getId() + ").\n");
            }
        }
    }

    /**
     * getTransaction:
     *
     * @param IOHandlerInterface ioh An IOHandlerInterface object to get input
     * @return String The transaction type entered by the user
     *
     * Prompts the user to enter a transaction type and returns it as a String. The returned value will be one of "check balance", "deposit", "withdraw", or "quit".
     */
    public static String getTransaction(IOHandlerInterface ioh) {
        String transactionType = ioh.get("Please enter a transaction type (check balance (1) / deposit (2) / withdraw (3) / quit (4)): ");

        // Normalize transaction type to lowercase.
        transactionType = transactionType.toLowerCase();

        return transactionType;
    }

    /**
     * readMoney:
     *
     * @param IOHandlerInterface ioh An IOHandlerInterface object to get input and output
     * @return Money The amount entered by the user as a Money object
     *
     * Prompts the user to enter an amount in the format "x.xx" and returns it as a Money object.
     */
    public static Money readMoney(IOHandlerInterface ioh) {
        String input = ioh.get("Please enter the amount (in the format x.xx): ");
        int dollars, cents;
        try {
            String[] parts = input.split("\\.");
            if (parts.length == 2) {
                dollars = Integer.parseInt(parts[0]);
                cents = Integer.parseInt(parts[1]);
                if (cents >= 100) {
                    throw new NumberFormatException();
                }
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            ioh.put("Invalid input. Please try again.");
            return readMoney(ioh);
        }
        return new Money(dollars, cents);
    }

    /**
     * writeToFile:
     *
     * @param String fileName The name of the file to write to
     * @param BankInterface bank A BankInterface object to write to the file
     * @throws IOException
     *
     * Writes the contents of the given bank to a file with the given file name. The accounts in the bank must be sorted beforehand.
     */
    public static void writeToFile(String fileName, BankInterface bank) throws IOException {
        PrintWriter output = new PrintWriter(new FileWriter(fileName));

        output.printf(bank.toString());

        output.close();
    }

} // end ATM
