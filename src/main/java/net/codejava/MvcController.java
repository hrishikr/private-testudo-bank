package net.codejava;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class MvcController {
  /**
   * A simplified JDBC client that is injected with the login credentials
   * specified in /src/main/resources/application.properties
   */
  private JdbcTemplate jdbcTemplate;
  private final static double INTEREST_RATE = 1.02;
  private final static int MAX_REVERSALS = 2;
  private final static int MAX_OVERDRAFT_IN_PENNIES = 100000;
  private final static String HTML_LINE_BREAK = "<br/>";

  public MvcController(@Autowired JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // Private method to get current date and time in a yyyy-MM-dd HH:mm:ss as a String object
  private String getCurrentDateTime() {
    java.util.Date dt = new java.util.Date();
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String sql_datetime_format = sdf.format(dt);
    return sql_datetime_format;
  }

  /**
   * HTML GET request handler that serves the "welcome" page to the user.
   * 
   * @param model
   * @return "welcome" page
   */
  @GetMapping("/")
  public String showWelcome(Model model) {
    return "welcome";
  }

  /**
   * HTML GET request handler that serves the "login_form" page to the user. An
   * empty `User` object is also added to the Model as an Attribute to store the
   * user's login form input.
   * 
   * @param model
   * @return "login_form" page
   */
  @GetMapping("/login")
  public String showLoginForm(Model model) {
    User user = new User();
    model.addAttribute("user", user);

    return "login_form";
  }

  /**
   * Helper method that queries the MySQL DB for the customer account info (First
   * Name, Last Name, and Balance) and adds these values to the `user` Model
   * Attribute so that they can be displayed in the "account_info" page.
   * 
   * @param user
   */
  private void updateAccountInfo(User user) {
    String getUserNameAndBalanceAndOverDraftBalanceSql = String.format(
        "SELECT FirstName, LastName, Balance, OverdraftBalance FROM customers WHERE CustomerID='%s';",
        user.getUsername());
    List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(getUserNameAndBalanceAndOverDraftBalanceSql);
    String getOverDraftLogsSql = String.format("SELECT * FROM OverdraftLogs WHERE CustomerID='%s';",
        user.getUsername());
    String getTransactionLogsSql = String.format(
        "SELECT * FROM TransactionHistory WHERE CustomerID='%s' ORDER BY Timestamp DESC limit 3;", user.getUsername());

    List<Map<String, Object>> queryRepaymentLogs = jdbcTemplate.queryForList(getOverDraftLogsSql);
    String repaymentLogs = HTML_LINE_BREAK;
    for (Map<String, Object> x : queryRepaymentLogs) {
      repaymentLogs += x + HTML_LINE_BREAK;
    }

    List<Map<String, Object>> queryTransactionLogs = jdbcTemplate.queryForList(getTransactionLogsSql);
    String transactionLogs = HTML_LINE_BREAK;
    for (Map<String, Object> x : queryTransactionLogs) {
      transactionLogs += x + HTML_LINE_BREAK;
    }

    Map<String, Object> userData = queryResults.get(0);

    user.setFirstName((String) userData.get("FirstName"));
    user.setLastName((String) userData.get("LastName"));
    user.setBalance((int) userData.get("Balance") / 100.0);
    double overDraftBalance = (int) userData.get("OverdraftBalance");
    user.setOverDraftBalance(overDraftBalance / 100);
    user.setRepaymentLogs(repaymentLogs);
    user.setTransactionLogs(transactionLogs);

  }

  /**
   * HTML POST request handler that uses user input from Login Form page to
   * determine login success or failure.
   * 
   * Queries 'passwords' table in MySQL DB for the correct password associated
   * with the username ID given by the user. Compares the user's password attempt
   * with the correct password.
   * 
   * If the password attempt is correct, the "account_info" page is served to the
   * customer with all account details retrieved from the MySQL DB.
   * 
   * If the password attempt is incorrect, the user is redirected to the "welcome"
   * page.
   * 
   * @param user
   * @return "account_info" page if login successful. Otherwise, redirect to
   *         "welcome" page.
   */
  @PostMapping("/login")
  public String submitLoginForm(@ModelAttribute("user") User user) {
    // Print user's existing fields for debugging
    System.out.println(user);

    String userID = user.getUsername();
    String userPasswordAttempt = user.getPassword();

    // Retrieve correct password for this customer.
    String getUserPasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", userID);
    String userPassword = jdbcTemplate.queryForObject(getUserPasswordSql, String.class);

    if (userPasswordAttempt.equals(userPassword)) {
      updateAccountInfo(user);

      return "account_info";
    } else {
      return "welcome";
    }
  }

  /**
   * HTML GET request handler that serves the "deposit_form" page to the user. An
   * empty `User` object is also added to the Model as an Attribute to store the
   * user's deposit form input.
   * 
   * @param model
   * @return "deposit_form" page
   */
  @GetMapping("/deposit")
  public String showDepositForm(Model model) {
    User user = new User();
    model.addAttribute("user", user);
    return "deposit_form";
  }

  /**
   * HTML POST request handler for the Deposit Form page.
   * 
   * The same username+password handling from the login page is used.
   * 
   * If the password attempt is correct, the balance is incremented by the amount
   * specified in the Deposit Form. The user is then served the "account_info"
   * with an updated balance.
   * 
   * If the password attempt is incorrect, the user is redirected to the "welcome"
   * page.
   * 
   * @param user
   * @return "account_info" page if login successful. Otherwise, redirect to
   *         "welcome" page.
   */
  @PostMapping("/deposit")
  public String submitDeposit(@ModelAttribute("user") User user) {
    String userID = user.getUsername();
    String userPasswordAttempt = user.getPassword();

    String getUserPasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", userID);
    String userPassword = jdbcTemplate.queryForObject(getUserPasswordSql, String.class);

    String currTime = getCurrentDateTime();

    // unsuccessful login
    if (userPasswordAttempt.equals(userPassword) == false) {
      return "welcome";
    }

    double userDepositAmt = user.getAmountToDeposit();
    int userDepositAmtInPennies = (int) (userDepositAmt * 100);

    if (userDepositAmt < 0) {
      return "welcome";
    }

    // Get number of fraud reversals
    String getUserReversalsSql = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';",
        userID);
    int numFraudReversals = jdbcTemplate.queryForObject(getUserReversalsSql, Integer.class);

    // Account frozen and no withdrawals/deposits can be made
    if (numFraudReversals >= MAX_REVERSALS) {
      return "welcome";
    }

    String getUserOverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';",
        userID);
    int userOverdraftBalanceInPennies = jdbcTemplate.queryForObject(getUserOverdraftBalanceSql, Integer.class);

    // if the overdraft balance is positive, subtract the deposit with interest
    if (userOverdraftBalanceInPennies > 0) {
      int newOverdraftBalanceInPennies = Math.max(userOverdraftBalanceInPennies - userDepositAmtInPennies, 0);

      String overdraftLogsInsertSql = String.format("INSERT INTO OverdraftLogs VALUES ('%s', '%s', %d, %d, %d);",
          userID, currTime, userDepositAmtInPennies, userOverdraftBalanceInPennies, newOverdraftBalanceInPennies);
      jdbcTemplate.update(overdraftLogsInsertSql);

      // updating customers table
      String overdraftBalanceUpdateSql = String.format(
          "UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", newOverdraftBalanceInPennies, userID);
      jdbcTemplate.update(overdraftBalanceUpdateSql);
      updateAccountInfo(user);
    }

    // if in the overdraft case and there is excess deposit, deposit the excess
    // amount.
    // otherwise, this is a non-overdraft case, so just use the userDepositAmt.
    int balanceIncreaseAmtInPennies = 0;
    if (userOverdraftBalanceInPennies > 0 && userDepositAmtInPennies > userOverdraftBalanceInPennies) {
      balanceIncreaseAmtInPennies = userDepositAmtInPennies - userOverdraftBalanceInPennies;
    } else if (userOverdraftBalanceInPennies > 0 && userDepositAmtInPennies <= userOverdraftBalanceInPennies) {
      balanceIncreaseAmtInPennies = 0; // overdraft case, but no excess deposit. don't increase balance column.
    } else {
      balanceIncreaseAmtInPennies = userDepositAmtInPennies;
    }

    String balanceIncreaseSql = String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';",
        balanceIncreaseAmtInPennies, userID);
    System.out.println(balanceIncreaseSql); // Print executed SQL update for debugging
    jdbcTemplate.update(balanceIncreaseSql);

    // Adding new transaction log
    String transactionLogInsertSQL = String.format("INSERT INTO TransactionHistory VALUES ('%s', '%s', '%s', %d);",
        userID, currTime, "Deposit", userDepositAmtInPennies);
    jdbcTemplate.update(transactionLogInsertSQL);

    updateAccountInfo(user);
    return "account_info";
  }

  /**
   * HTML GET request handler that serves the "withdraw_form" page to the user. An
   * empty `User` object is also added to the Model as an Attribute to store the
   * user's withdraw form input.
   * 
   * @param model
   * @return "withdraw_form" page
   */
  @GetMapping("/withdraw")
  public String showWithdrawForm(Model model) {
    User user = new User();
    model.addAttribute("user", user);
    return "withdraw_form";
  }

  /**
   * HTML POST request handler for the Withdraw Form page.
   * 
   * The same username+password handling from the login page is used.
   * 
   * If the password attempt is correct, the balance is decremented by the amount
   * specified in the Withdraw Form. The user is then served the "account_info"
   * with an updated balance.
   * 
   * If the password attempt is incorrect, the user is redirected to the "welcome"
   * page.
   * 
   * @param user
   * @return "account_info" page if login successful. Otherwise, redirect to
   *         "welcome" page.
   */
  @PostMapping("/withdraw")
  public String submitWithdraw(@ModelAttribute("user") User user) {
    String userID = user.getUsername();
    String userPasswordAttempt = user.getPassword();

    String getUserPasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", userID);
    String userPassword = jdbcTemplate.queryForObject(getUserPasswordSql, String.class);

    // unsuccessful login
    if (userPasswordAttempt.equals(userPassword) == false) {
      return "welcome";
    }

    double userWithdrawAmt = user.getAmountToWithdraw();
    int userWithdrawAmtInPennies = (int) (userWithdrawAmt * 100);

    if (userWithdrawAmt < 0) {
      return "welcome";
    }

    // Get number of fraud reversals
    String getUserReversalsSql = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';",
        userID);
    int numFraudReversals = jdbcTemplate.queryForObject(getUserReversalsSql, Integer.class);

    // Account frozen and no withdrawals/deposits can be made
    if (numFraudReversals >= MAX_REVERSALS) {
      return "welcome";
    }

    String getUserBalanceSql = String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", userID);
    int userBalanceInPennies = jdbcTemplate.queryForObject(getUserBalanceSql, Integer.class);

    // if the balance is not positive, withdraw with interest fee
    if (userBalanceInPennies - userWithdrawAmtInPennies < 0) {
      // subtracts the remaining balance from withdrawal amount
      int newOverdraftAmtInPennies = userWithdrawAmtInPennies - userBalanceInPennies;

      if (newOverdraftAmtInPennies > MAX_OVERDRAFT_IN_PENNIES) {
        return "welcome";
      }

      // factor in the existing overdraft balance before executing another overdraft
      String getUserOverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';",
          userID);
      int userOverdraftBalanceInPennies = jdbcTemplate.queryForObject(getUserOverdraftBalanceSql, Integer.class);
      if (newOverdraftAmtInPennies + userOverdraftBalanceInPennies > MAX_OVERDRAFT_IN_PENNIES) {
        return "welcome";
      }

      // this is a valid overdraft, so we can set Balance column to 0
      String updateBalanceSql = String.format("UPDATE Customers SET Balance = %d WHERE CustomerID='%s';", 0, userID);
      jdbcTemplate.update(updateBalanceSql);

      int newOverdraftAmtAfterInterestInPennies = (int) (newOverdraftAmtInPennies * INTEREST_RATE);
      int cumulativeOverdraftInPennies = userOverdraftBalanceInPennies + newOverdraftAmtAfterInterestInPennies;

      String overDraftBalanceUpdateSql = String.format(
          "UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", cumulativeOverdraftInPennies, userID);
      jdbcTemplate.update(overDraftBalanceUpdateSql);
      System.out.println(overDraftBalanceUpdateSql);

      // Adding new transaction log
      String transactionLogInsertSQL = String.format("INSERT INTO TransactionHistory VALUES ('%s', '%s', '%s', %d);",
          userID, getCurrentDateTime(), "Withdraw", userWithdrawAmtInPennies);
      jdbcTemplate.update(transactionLogInsertSQL);

      updateAccountInfo(user);
      return "account_info";

    }

    // non-overdraft case
    String balanceDecreaseSql = String.format("UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';",
        userWithdrawAmtInPennies, userID);
    System.out.println(balanceDecreaseSql);
    jdbcTemplate.update(balanceDecreaseSql);

    // Adding new transaction log
    String transactionLogInsertSQL = String.format("INSERT INTO TransactionHistory VALUES ('%s', '%s', '%s', %d);",
        userID, getCurrentDateTime(), "Withdraw", userWithdrawAmtInPennies);
    jdbcTemplate.update(transactionLogInsertSQL);

    updateAccountInfo(user);
    return "account_info";

  }

  /**
   * HTML GET request handler that serves the "dispute_form" page to the user. An
   * empty `User` object is also added to the Model as an Attribute to store the
   * user's dispute form input.
   * 
   * @param model
   * @return "dispute_form" page
   */
  @GetMapping("/dispute")
  public String showDisputeForm(Model model) {
    User user = new User();
    model.addAttribute("user", user);
    return "dispute_form";
  }

  /**
   * HTML GET request handler that serves the "dispute_form" page to the user. An
   * empty `User` object is also added to the Model as an Attribute to store the
   * user's dispute form input.
   * 
   * @param model
   * @return "dispute_form" page
   */
  @PostMapping("/dispute")
  public String submitDispute(@ModelAttribute("user") User user) {
    String userID = user.getUsername();
    String userPasswordAttempt = user.getPassword();
    int transactionNum = user.getNumTransactionsAgo();

    String getUserPasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", userID);
    String userPassword = jdbcTemplate.queryForObject(getUserPasswordSql, String.class);

    // Unsuccessful login
    if (userPasswordAttempt.equals(userPassword) == false) {
      return "welcome";
    }

    // Invalid arument for which transaction to reverse
    if (transactionNum <= 0 || transactionNum > 3) {
      return "welcome";
    }

    // Get number of fraud reversals
    String getUserReversalsSql = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';",
        userID);
    int numFraudReversals = jdbcTemplate.queryForObject(getUserReversalsSql, Integer.class);

    // Account frozen and no withdrawals/deposits can be made
    if (numFraudReversals >= MAX_REVERSALS) {
      return "welcome";
    }

    String getTransactionLogsSql = String
        .format("SELECT * FROM TransactionHistory WHERE CustomerID='%s' ORDER BY Timestamp DESC;", user.getUsername());

    List<Map<String, Object>> transactionLogs = jdbcTemplate.queryForList(getTransactionLogsSql);
    Map<String, Object> transaction;
    // Check if the number of transactions ago is not greater than the number of
    // transactions in the log
    if (transactionNum - 1 >= transactionLogs.size()) {
      return "welcome";
    }
    transaction = transactionLogs.get(transactionNum - 1);
    int transactionAmountInPennies = (Integer) transaction.get("Amount");
    DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String transactionTimestamp = sdf.format((java.time.LocalDateTime) transaction.get("Timestamp"));

    if (((String) transaction.get("Action")).equals("Deposit")) {
      // ------------------------------ Reversing a Deposit -----------------------------------------

      // Getting if the deposit to be reversed was used for the repayment of an
      // overdraft
      String getOverDraftLogsSql = String.format(
          "SELECT * FROM OverdraftLogs WHERE CustomerID='%s' AND DepositAmt = '%d' AND Timestamp = '%s';",
          user.getUsername(), transactionAmountInPennies, transactionTimestamp);
      List<Map<String, Object>> queryRepaymentLog = jdbcTemplate.queryForList(getOverDraftLogsSql);
      boolean overdraftRepayment;

      /*
       * If no overdraft log corresponds to the time of the deposit then the depost
       * was not used to repay an overdraft
       */
      if (queryRepaymentLog.size() == 0) {
        overdraftRepayment = false;
      } else {
        overdraftRepayment = true;
        // Delete overdraft entry from table
        String deleteOverdraftLogSql = String.format(
          "DELETE FROM OverdraftLogs WHERE CustomerID='%s' AND DepositAmt = '%d' AND Timestamp = '%s';",
          user.getUsername(), transactionAmountInPennies, transactionTimestamp);
        jdbcTemplate.update(deleteOverdraftLogSql);
      }

      // Getting user balance
      String getUserBalanceSql = String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", userID);
      int userBalanceInPennies = jdbcTemplate.queryForObject(getUserBalanceSql, Integer.class);

      // if the balance is not positive, overdraft case
      if (userBalanceInPennies - transactionAmountInPennies < 0) {
        // subtracts the remaining balance from withdrawal amount
        int newOverdraftAmtInPennies = transactionAmountInPennies - userBalanceInPennies;

        if (newOverdraftAmtInPennies > MAX_OVERDRAFT_IN_PENNIES) {
          return "welcome";
        }

        // factor in the existing overdraft balance before executing another overdraft
        String getUserOverdraftBalanceSql = String
            .format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';", userID);
        int userOverdraftBalanceInPennies = jdbcTemplate.queryForObject(getUserOverdraftBalanceSql, Integer.class);
        if (newOverdraftAmtInPennies + userOverdraftBalanceInPennies > MAX_OVERDRAFT_IN_PENNIES) {
          return "welcome";
        }

        // this is a valid overdraft, so we can set Balance column to 0
        String updateBalanceSql = String.format("UPDATE Customers SET Balance = %d WHERE CustomerID='%s';", 0, userID);
        jdbcTemplate.update(updateBalanceSql);
        int newOverdraftAmtAfterInterestInPennies;

        // If the deposit being reversed was used for overdraft repayment, no interest
        // to be charged
        if (overdraftRepayment == true) {
          newOverdraftAmtAfterInterestInPennies = (int) (newOverdraftAmtInPennies);
        } else {
          newOverdraftAmtAfterInterestInPennies = (int) (newOverdraftAmtInPennies * INTEREST_RATE);
        }
        int cumulativeOverdraftInPennies = userOverdraftBalanceInPennies + newOverdraftAmtAfterInterestInPennies;

        // Updating overdraft balance
        String overDraftBalanceUpdateSql = String.format(
            "UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", cumulativeOverdraftInPennies, userID);
        jdbcTemplate.update(overDraftBalanceUpdateSql);
        System.out.println(overDraftBalanceUpdateSql);

        // Adding new transaction log
        String transactionLogInsertSQL = String.format("INSERT INTO TransactionHistory VALUES ('%s', '%s', '%s', %d);",
                                                        userID, 
                                                        getCurrentDateTime(), 
                                                        "Withdraw", 
                                                        transactionAmountInPennies);
        jdbcTemplate.update(transactionLogInsertSQL);

      } else {
        // non-overdraft case
        String balanceDecreaseSql = String.format("UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';",
                                                    transactionAmountInPennies, 
                                                    userID);
        System.out.println(balanceDecreaseSql);
        jdbcTemplate.update(balanceDecreaseSql);

        // Adding new transaction log
        String transactionLogInsertSQL = String.format("INSERT INTO TransactionHistory VALUES ('%s', '%s', '%s', %d);",
                                                      userID, 
                                                      getCurrentDateTime(), 
                                                      "Withdraw", 
                                                      transactionAmountInPennies);
        jdbcTemplate.update(transactionLogInsertSQL);

      }

    } else {
      // ------------------------------ Reversing a withdraw --------------------------------

      String getUserOverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';", userID);
      int userOverdraftBalanceInPennies = jdbcTemplate.queryForObject(getUserOverdraftBalanceSql, Integer.class);

      String currTime = getCurrentDateTime();

      // if the overdraft balance is positive, subtract the deposit with interest
      if (userOverdraftBalanceInPennies > 0) {
        int newOverdraftBalanceInPennies = Math.max(userOverdraftBalanceInPennies - transactionAmountInPennies, 0);

        String overdraftLogsInsertSql = String.format("INSERT INTO OverdraftLogs VALUES ('%s', '%s', %d, %d, %d);",
                                                      userID, 
                                                      currTime, 
                                                      transactionAmountInPennies,
                                                      userOverdraftBalanceInPennies, 
                                                      newOverdraftBalanceInPennies);
        jdbcTemplate.update(overdraftLogsInsertSql);

        // updating customers table
        String overdraftBalanceUpdateSql = String.format(
            "UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", 
            newOverdraftBalanceInPennies, 
            userID);
        jdbcTemplate.update(overdraftBalanceUpdateSql);
        updateAccountInfo(user);
      }

      // if in the overdraft case and there is excess deposit, deposit the excess
      // amount.
      // otherwise, this is a non-overdraft case, so just use the userDepositAmt.
      int balanceIncreaseAmtInPennies = 0;
      if (userOverdraftBalanceInPennies > 0 && transactionAmountInPennies > userOverdraftBalanceInPennies) {
        balanceIncreaseAmtInPennies = transactionAmountInPennies - userOverdraftBalanceInPennies;
      } else if (userOverdraftBalanceInPennies > 0 && transactionAmountInPennies <= userOverdraftBalanceInPennies) {
        balanceIncreaseAmtInPennies = 0; // overdraft case, but no excess deposit. Don't increase balance column.
      } else {
        balanceIncreaseAmtInPennies = transactionAmountInPennies;
      }

      String balanceIncreaseSql = String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';",
                                                  balanceIncreaseAmtInPennies, 
                                                  userID);
      System.out.println(balanceIncreaseSql); // Print executed SQL update for debugging
      jdbcTemplate.update(balanceIncreaseSql);

      // Adding new transaction log
      String transactionLogInsertSQL = String.format("INSERT INTO TransactionHistory VALUES ('%s', '%s', '%s', %d);",
                                                      userID, 
                                                      currTime, 
                                                      "Deposit", 
                                                      transactionAmountInPennies);
      jdbcTemplate.update(transactionLogInsertSQL);

    }

    // Updating the number of fraud reversals done by the user
    String updateNumFraudReversals = String
        .format("UPDATE Customers SET NumFraudReversals = NumFraudReversals + %d WHERE CustomerID='%s';", 1, userID);
    System.out.println(updateNumFraudReversals); // Print executed SQL update for debugging
    jdbcTemplate.update(updateNumFraudReversals);

    updateAccountInfo(user);
    return "account_info";
  }
}