package net.codejava;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class MvcController {
  /**
   * A simplified JDBC client that is injected with the login credentials
   * specified in /src/main/resources/application.properties
   */
  private JdbcTemplate jdbcTemplate;
  final double OVERDRAFT_INTEREST = 1.02;
  final int DOLLAR_CENT_CONVERSION_FACTOR = 100;

  public MvcController(@Autowired JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
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
   * HTML GET request handler that serves the "login_form" page to the user.
   * An empty `User` object is also added to the Model as an Attribute to store
   * the user's login form input.
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
   * Helper method that queries the MySQL DB for the customer account info (First Name, Last Name, and Balance)
   * and adds these values to the `user` Model Attribute so that they can be displayed in the "account_info" page.
   * 
   * @param user
   */
  private void updateAccountInfo(User user) {
    String getUserNameAndBalanceSql = String.format("SELECT FirstName, LastName, Balance, OverdraftBalance FROM customers WHERE CustomerID='%s';", user.getUsername());
    List<Map<String,Object>> queryResults = jdbcTemplate.queryForList(getUserNameAndBalanceSql);
    Map<String,Object> userData = queryResults.get(0);

    String getUserOverdraftLogs = String.format("SELECT Timestamp, DepositAmt, OldOverBalance, NewOverBalance FROM OverdraftLogs WHERE CustomerID='%s' ORDER BY Timestamp DESC;", user.getUsername());
    List<Map<String,Object>> logs = jdbcTemplate.queryForList(getUserOverdraftLogs);


    user.setFirstName((String)userData.get("FirstName"));
    user.setLastName((String)userData.get("LastName"));
    user.setBalance((int)userData.get("Balance"));
    user.setOverdraftBalance((int) ((int)userData.get("OverdraftBalance") * OVERDRAFT_INTEREST));
    user.setOverdraftLogs(logs);
  }

  /**
   * HTML POST request handler that uses user input from Login Form page to determine 
   * login success or failure.
   * 
   * Queries 'passwords' table in MySQL DB for the correct password associated with the
   * username ID given by the user. Compares the user's password attempt with the correct
   * password.
   * 
   * If the password attempt is correct, the "account_info" page is served to the customer
   * with all account details retrieved from the MySQL DB.
   * 
   * If the password attempt is incorrect, the user is redirected to the "welcome" page.
   * 
   * @param user
   * @return "account_info" page if login successful. Otherwise, redirect to "welcome" page.
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
   * HTML GET request handler that serves the "deposit_form" page to the user.
   * An empty `User` object is also added to the Model as an Attribute to store
   * the user's deposit form input.
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
   * If the password attempt is correct, the balance is incremented by the amount specified
   * in the Deposit Form. The user is then served the "account_info" with an updated balance.
   * 
   * If the password attempt is incorrect, the user is redirected to the "welcome" page.
   * 
   * @param user
   * @return "account_info" page if login successful. Otherwise, redirect to "welcome" page.
   */
  @PostMapping("/deposit")
  public String submitDeposit(@ModelAttribute("user") User user) {
		System.out.println(user); // Print user's existing fields for debugging

    String userID = user.getUsername();
    String userPasswordAttempt = user.getPassword();

    // Retrieve correct password for this customer.
    String getUserPasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", userID);
    String userPassword = jdbcTemplate.queryForObject(getUserPasswordSql, String.class);

    // Retrieve current overdraft balance
    String getUserOverdraftBalance = String.format("SELECT OverdraftBalance FROM Customers WHERE CustomerID='%s';", userID);
    int currentOverdraftBalance = jdbcTemplate.queryForObject(getUserOverdraftBalance, Integer.class);


    // Returns to welcome page if password is incorrect or amount is negative
    if (userPasswordAttempt.equals(userPassword) && user.getAmountToDeposit() >= 0) {
      if (currentOverdraftBalance == 0) {
        // Execute SQL Update command that increments user's Balance by given amount from the deposit form.
        String balanceIncreaseSql = String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';", user.getAmountToDeposit()/DOLLAR_CENT_CONVERSION_FACTOR, userID);
        System.out.println(balanceIncreaseSql); // Print executed SQL update for debugging
        jdbcTemplate.update(balanceIncreaseSql);

      } else if (currentOverdraftBalance > 0){
        int overdraftWithInterest = (int) (currentOverdraftBalance * OVERDRAFT_INTEREST);
        int balanceIncreaseAmount;
        int newOverdraft;
        if (user.getAmountToDeposit() > overdraftWithInterest) {
          newOverdraft = 0;
          balanceIncreaseAmount = (user.getAmountToDeposit() - overdraftWithInterest)/DOLLAR_CENT_CONVERSION_FACTOR;

        } else {
          newOverdraft = overdraftWithInterest - user.getAmountToDeposit();
          balanceIncreaseAmount = 0;
        }
        newOverdraft = (int)(newOverdraft/OVERDRAFT_INTEREST);
        
        // ------------ Changing fields in customer table ------------------------
        String balanceIncreaseSql = String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';", balanceIncreaseAmount, userID);
        String overdraftBalanceIncreaseSql = String.format("UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", newOverdraft, userID);
        System.out.println(balanceIncreaseSql);
        System.out.println(overdraftBalanceIncreaseSql);

        // Update new balance in sql table
        jdbcTemplate.update(balanceIncreaseSql);
        // Update new overdraft balance in sql table
        jdbcTemplate.update(overdraftBalanceIncreaseSql);

        // ------------ Changing fields in overdraft logs table -------------------------
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String currentTime = sdf.format(date);

        String newLogEntrySql = String.format("INSERT INTO OverdraftLogs VALUES (%s,\'%s\',%d,%d,%d);", userID, currentTime, user.getAmountToDeposit(), currentOverdraftBalance, newOverdraft);
        System.out.println(newLogEntrySql);
        jdbcTemplate.update(newLogEntrySql);
        
      } else {
        System.out.println("You shouldn't be here!");
      }

      updateAccountInfo(user);
      return "account_info";
    } else {
      return "welcome";
    }
  }
	
  /**
   * HTML GET request handler that serves the "withdraw_form" page to the user.
   * An empty `User` object is also added to the Model as an Attribute to store
   * the user's withdraw form input.
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
   * If the password attempt is correct, the balance is decremented by the amount specified
   * in the Withdraw Form. The user is then served the "account_info" with an updated balance.
   * 
   * If the password attempt is incorrect, the user is redirected to the "welcome" page.
   * 
   * @param user
   * @return "account_info" page if login successful. Otherwise, redirect to "welcome" page.
   */
  @PostMapping("/withdraw")
  public String submitWithdraw(@ModelAttribute("user") User user) {
    String userID = user.getUsername();
    String userPasswordAttempt = user.getPassword();

    String getUserPasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", userID);

    String userPassword = jdbcTemplate.queryForObject(getUserPasswordSql, String.class);

    // Returns to welcome page if password is incorrect or amount is negative
    if (userPasswordAttempt.equals(userPassword) && user.getAmountToWithdraw() >= 0) {
      // Execute SQL Update command that decrements Balance value for
      // user's row in Customers table using user.getAmountToWithdraw()

      
      String getUserBalance = String.format("SELECT Balance FROM Customers WHERE CustomerID='%s';", userID);
      int userBalance = jdbcTemplate.queryForObject(getUserBalance, Integer.class);

      String getUserOverdraftBalance = String.format("SELECT OverdraftBalance FROM Customers WHERE CustomerID='%s';", userID);
      int currentOverdraftBalance = jdbcTemplate.queryForObject(getUserOverdraftBalance, Integer.class);

      if (userBalance >= user.getAmountToWithdraw()/DOLLAR_CENT_CONVERSION_FACTOR) {
        String balanceIncreaseSql = String.format("UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';", user.getAmountToWithdraw()/DOLLAR_CENT_CONVERSION_FACTOR, userID);
        System.out.println(balanceIncreaseSql);
        jdbcTemplate.update(balanceIncreaseSql);

      } else if ((user.getAmountToWithdraw()/DOLLAR_CENT_CONVERSION_FACTOR) > userBalance && (user.getAmountToWithdraw()/DOLLAR_CENT_CONVERSION_FACTOR) <= (userBalance + (1000 - currentOverdraftBalance/DOLLAR_CENT_CONVERSION_FACTOR))) {
        
        // ------------ Changing fields in customer table -------------------------
        int withdrawCreditAmount = (user.getAmountToWithdraw()) - userBalance*DOLLAR_CENT_CONVERSION_FACTOR; // withdraw amount in cents
        String balanceIncreaseSql = String.format("UPDATE Customers SET Balance = %d WHERE CustomerID='%s';", 0, userID);
        String overdraftBalanceIncreaseSql = String.format("UPDATE Customers SET OverdraftBalance = OverdraftBalance + %d WHERE CustomerID='%s';", withdrawCreditAmount, userID);
        System.out.println(balanceIncreaseSql);
        System.out.println(overdraftBalanceIncreaseSql);

        // Update new balance in sql table
        jdbcTemplate.update(balanceIncreaseSql);
        // Update new overdraft balance in sql table
        jdbcTemplate.update(overdraftBalanceIncreaseSql);

      } else {
        // Withdrew amount which exceeds overdraft balance limit ($1000)
        return "welcome";
      }

      updateAccountInfo(user);

      return "account_info";
    } else {
      return "welcome";
    }
  }
}