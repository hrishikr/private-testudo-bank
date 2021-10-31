package net.codejava;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class MvcControllerTest {
  @Mock
  private static JdbcTemplate jdbcTemplate;

  @Mock
  Model mockModel;

  private MvcController controller;

  private static String CUSTOMER1_USERNAME;
  private static List<Map<String, Object>> CUSTOMER1_DATA;
  private static List<Map<String, Object>> CUSTOMER1_TRANSACTIONS;

  @BeforeAll
  public static void init() {
    CUSTOMER1_USERNAME = "123456789";

    // prepare what the updateAccountInfo() helper method should return when stubbed
    CUSTOMER1_DATA = new ArrayList<>();
    CUSTOMER1_DATA.add(new HashMap<>());
    CUSTOMER1_DATA.get(0).put("FirstName", "John");
    CUSTOMER1_DATA.get(0).put("LastName", "Doe");
    CUSTOMER1_DATA.get(0).put("Balance", 10000);
    CUSTOMER1_DATA.get(0).put("OverdraftBalance", 0);

    CUSTOMER1_TRANSACTIONS = new ArrayList<>();
    CUSTOMER1_TRANSACTIONS.add(new HashMap<>());
    CUSTOMER1_TRANSACTIONS.add(new HashMap<>());
    CUSTOMER1_TRANSACTIONS.get(0).put("CustomerID", "John");
    CUSTOMER1_TRANSACTIONS.get(0).put("Timestamp", "2021-10-31 14:52:42");
    CUSTOMER1_TRANSACTIONS.get(0).put("Action", "Withdraw");
    CUSTOMER1_TRANSACTIONS.get(0).put("Amount", 1000);
    CUSTOMER1_TRANSACTIONS.get(1).put("CustomerID", "John");
    CUSTOMER1_TRANSACTIONS.get(1).put("Timestamp", "2021-10-31 14:52:42");
    CUSTOMER1_TRANSACTIONS.get(1).put("Action", "Deposit");
    CUSTOMER1_TRANSACTIONS.get(1).put("Amount", 1000);
  }

  @BeforeEach
  public void setup() {
    MockitoAnnotations.initMocks(this);
    controller = new MvcController(jdbcTemplate);
  }

  @Test
  public void testControllerIsCreated() {
    assertThat(controller, is(notNullValue()));
  }

  @Test
  public void testShowWelcomeSuccess() {
    assertEquals("welcome", controller.showWelcome(null));
  }

  @Test
  public void testShowLoginForm() {
    assertEquals("login_form", controller.showLoginForm(mockModel));
  }

  @Test
  public void testSubmitLoginFormSuccessWithCorrectPassword() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);

    // send login request
    String pageReturned = controller.submitLoginForm(customer1);

    // Verify that the SELECT SQL command executed to retrieve user's password uses the customer's ID
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));

    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testSubmitLoginFormFailureWithIncorrectPassword() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("not password");

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);

    // send login request
    String pageReturned = controller.submitLoginForm(customer1);

    // Verify that the SELECT SQL command executed to retrieve user's password uses the customer's ID
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));

    // verify that customer is re-directed to "welcome" page
    assertEquals("welcome", pageReturned);
  }

  @Test
  public void testShowDepositFormSuccess() {
    assertEquals("deposit_form", controller.showDepositForm(mockModel));
  }

  @Test
  public void testDepositSuccesswithCorrectPassword() {
    // initialize user input to the deposit form
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setAmountToDeposit(100);

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // no overdraft
    String getCustomer1OverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class))).thenReturn(0);
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(0);
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);

    // send deposit request
    String pageReturned = controller.submitDeposit(customer1);

    // Verify that the SQL Update command executed uses customer1's ID and amountToDeposit.
    int expectedDepositAmtInPennies = (int) (customer1.getAmountToDeposit() * 100);
    String balanceIncreaseSqlCustomer1=String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';",
                                                     expectedDepositAmtInPennies,
                                                     customer1.getUsername());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceIncreaseSqlCustomer1));

    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testDepositFailurewithIncorrectPassword() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("not password");
    customer1.setAmountToDeposit(100);

    // stub jdbc calls
    // unsuccessful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");

    // send deposit request
    String pageReturned = controller.submitDeposit(customer1);

    // Verify that no SQL Update commands are sent
    Mockito.verify(jdbcTemplate, Mockito.times(0)).update(anyString());

    // verify that customer is re-directed to "welcome" page
    assertEquals("welcome", pageReturned);
  }

  @Test
  public void testShowWithdrawFormSuccess() {
    assertEquals("withdraw_form", controller.showWithdrawForm(mockModel));
  }

  @Test
  public void testWithdrawSuccesswithCorrectPassword() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setAmountToWithdraw(100);

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // retrieve balance of $200 (which is stored as 20000 pennies) from DB
    String getCustomer1BalanceSql=String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class))).thenReturn(20000);
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(0);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);

    // send withdraw request
    String pageReturned = controller.submitWithdraw(customer1);

    // Verify that the SQL Update command executed uses customer1's ID and amountToWitdraw.
    int expectedWithdrawAmtInPennies = (int) (customer1.getAmountToWithdraw() * 100);
    String balanceDecreaseSqlCustomer1=String.format("UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';",
                                                     expectedWithdrawAmtInPennies,
                                                     customer1.getUsername());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceDecreaseSqlCustomer1));

    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testWithdrawFailurewithIncorrectPassword() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("not password");
    customer1.setAmountToWithdraw(100);

    // stub jdbc calls
    // unsuccessful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");

    // send withdraw request
    String pageReturned = controller.submitWithdraw(customer1);

    // Verify that no SQL Update commands are sent
    Mockito.verify(jdbcTemplate, Mockito.times(0)).update(anyString());

    // verify that customer is re-directed to "welcome" page
    assertEquals("welcome", pageReturned);
  }

  @Test
  public void testWithdrawOverDraftBalanceSuccess() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setAmountToWithdraw(10); // withdraw $10 in overdraft

    String getCustomer1PasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    String getCustomer1BalanceSql =  String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    String getCustomer1OverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';", CUSTOMER1_USERNAME);

    // stub jdbc calls
    // successful login
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(0);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);
    // start customer with balance and overdraft balance of $0
    when(jdbcTemplate.queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class))).thenReturn(0);
    when(jdbcTemplate.queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class))).thenReturn(0);

    // send withdraw request
    String pageReturned = controller.submitWithdraw(customer1);

    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class));
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class));
    // expect a new overdraft balance of $10.20 due to 2% interest rate
    String overDraftBalanceUpdateSql = String.format("UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", 1020, CUSTOMER1_USERNAME);
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(overDraftBalanceUpdateSql); 

    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testWithdrawOverDraftBalanceFailure() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setAmountToWithdraw(2000); // try to withdraw $2000 in overdraft, but the max allowed is $1000

    String getCustomer1PasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    String getCustomer1BalanceSql =  String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    
    // stub jdbc calls
    // successful login
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);
    // start customer with balance of $0
    when(jdbcTemplate.queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class))).thenReturn(0);
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(0);

    // send withdraw request
    String pageReturned = controller.submitWithdraw(customer1);
    
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class));
    // no update due to failing on customer.getAmountToWithdraw() > MAX_AMOUNT
    Mockito.verify(jdbcTemplate, Mockito.times(0)).update(anyString());

    // verify "welcome" page is returned
    assertEquals("welcome", pageReturned);
  }

  @Test
  public void testDepositOverDraftBalanceSuccess() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setAmountToDeposit(100); // deposit $100 to pay off $10 of overdraft and deposit $90 excess into main balance

    String getCustomer1PasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    String getCustomer1OverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';", CUSTOMER1_USERNAME);

    // stub jdbc calls
    // successful login
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);
    // start customer with overdraft balance of $10 (represented as 1000 pennies in the DB)
    when(jdbcTemplate.queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class))).thenReturn(1000); 
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(0);
    
    // send deposit request
    String pageReturned = controller.submitDeposit(customer1);

    // verify queries for password and overdraft balance
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class));

    // verify updating overdraft balance to $0
    String overDraftBalanceUpdateSql = String.format("UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", 0, CUSTOMER1_USERNAME);
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(overDraftBalanceUpdateSql));

    // verify updating balance to $90 due to excess deposit (represented as 9000 pennies in the DB)
    String balanceUpdateSql = String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';", 9000, CUSTOMER1_USERNAME);
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceUpdateSql));

    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testDepositOverDraftBalanceNotCleared() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setAmountToDeposit(100); // deposit $100 to pay off part of a $500 overdraft balance

    String getCustomer1PasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    String getCustomer1OverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    
    // stub jdbc calls
    // successful login
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);
    // start customer with overdraft balance of $500 (represented as 50000 pennies in the DB)
    when(jdbcTemplate.queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class))).thenReturn(50000); 
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(0);

    // send deposit request
    String pageReturned = controller.submitDeposit(customer1);

    // verify queries for password and overdraft balance
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class));

    // overdraft balance > customer deposit, so new overdraft balance must be $400 (represented as 40000 pennies in DB)
    String overDraftBalanceUpdateSql = String.format("UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", 40000, CUSTOMER1_USERNAME);
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(overDraftBalanceUpdateSql));

    // main balance should remain unchanged
    String balanceUpdateSql = String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';", 0, CUSTOMER1_USERNAME);
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceUpdateSql));

    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testDisputeDepositSuccess() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setNumTransactionsAgo(2);

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(1);
    // handles transaction query
    String getTransactionLogsSql = String.format("SELECT * FROM TransactionHistory WHERE CustomerID='%s' ORDER BY Timestamp DESC;", customer1.getUsername());
    when(jdbcTemplate.queryForList(eq(getTransactionLogsSql))).thenReturn(CUSTOMER1_TRANSACTIONS);
    // balance query
    String getCustomer1BalanceSql =  String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    when(jdbcTemplate.queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class))).thenReturn(0);

    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);
    String updateAccount = String.format("SELECT FirstName, LastName, Balance, OverdraftBalance FROM customers WHERE CustomerID='%s';", customer1.getUsername());
    //handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(eq(updateAccount))).thenReturn(CUSTOMER1_DATA);

    // send reverse request
    String pageReturned = controller.submitDispute(customer1);

    // Verify that the SQL Update command executed uses customer1's ID and amount from transaction logs.
    String balanceDecreaseSqlCustomer1=String.format("UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';",
                                                     (int)CUSTOMER1_TRANSACTIONS.get(1).get("Amount"),
                                                     customer1.getUsername());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceDecreaseSqlCustomer1));


    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testDisputeWithdrawSuccess() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setNumTransactionsAgo(1);

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(1);
    // handles transaction query
    String getTransactionLogsSql = String.format("SELECT * FROM TransactionHistory WHERE CustomerID='%s' ORDER BY Timestamp DESC;", customer1.getUsername());
    when(jdbcTemplate.queryForList(eq(getTransactionLogsSql))).thenReturn(CUSTOMER1_TRANSACTIONS);
    // balance query
    String getCustomer1BalanceSql =  String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", CUSTOMER1_USERNAME);
    when(jdbcTemplate.queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class))).thenReturn(0);

    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(1);
    String updateAccount = String.format("SELECT FirstName, LastName, Balance, OverdraftBalance FROM customers WHERE CustomerID='%s';", customer1.getUsername());
    //handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(eq(updateAccount))).thenReturn(CUSTOMER1_DATA);

    // send reverse request
    String pageReturned = controller.submitDispute(customer1);

    // Verify that the SQL Update command executed uses customer1's ID and amount from transaction logs.
    String balanceIncreaseSqlCustomer1=String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';",
                                                     (int)CUSTOMER1_TRANSACTIONS.get(0).get("Amount"),
                                                     customer1.getUsername());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceIncreaseSqlCustomer1));

    // verify "account_info" page is returned
    assertEquals("account_info", pageReturned);
  }

  @Test
  public void testDisputeFailureDueToExcessReversals() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setNumTransactionsAgo(1);

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // handles transaction query
    String getTransactionLogsSql = String.format("SELECT * FROM TransactionHistory WHERE CustomerID='%s' ORDER BY Timestamp DESC;", customer1.getUsername());
    when(jdbcTemplate.queryForList(eq(getTransactionLogsSql))).thenReturn(CUSTOMER1_TRANSACTIONS);
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(2);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(2);

    // send reverse request
    String pageReturned = controller.submitDispute(customer1);

  
    Mockito.verify(jdbcTemplate, Mockito.times(0)).update(anyString());

    // verify "account_info" page is returned
    assertEquals("welcome", pageReturned);
  }

  @Test
  public void testDisputeFailureInvalidArgument() {
    User customer1 = new User();
    customer1.setUsername(CUSTOMER1_USERNAME);
    customer1.setPassword("password");
    customer1.setNumTransactionsAgo(5); // Invalid argument

    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // handles transaction query
    String getTransactionLogsSql = String.format("SELECT * FROM TransactionHistory WHERE CustomerID='%s' ORDER BY Timestamp DESC;", customer1.getUsername());
    when(jdbcTemplate.queryForList(eq(getTransactionLogsSql))).thenReturn(CUSTOMER1_TRANSACTIONS);
    // no num fraud reversals
    String getUserNumFraudReversals = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getUserNumFraudReversals), eq(Integer.class))).thenReturn(0);
    // not working with live DB
    when(jdbcTemplate.update(anyString())).thenReturn(2);

    // send reverse request
    String pageReturned = controller.submitDispute(customer1);

    Mockito.verify(jdbcTemplate, Mockito.times(0)).update(anyString());

    // verify "account_info" page is returned
    assertEquals("welcome", pageReturned);
  }
}

