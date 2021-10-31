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

	@BeforeAll
	public static void init() {
		CUSTOMER1_USERNAME = "123456789";

<<<<<<< HEAD
		// prepare what the updateAccountInfo() helper method should return when stubbed
		CUSTOMER1_DATA = new ArrayList<>();
		CUSTOMER1_DATA.add(new HashMap<>());
		CUSTOMER1_DATA.get(0).put("FirstName", "John");
		CUSTOMER1_DATA.get(0).put("LastName", "Doe");
		CUSTOMER1_DATA.get(0).put("Balance", 100);
		CUSTOMER1_DATA.get(0).put("OverdraftBalance", 0);
	}
=======
    // prepare what the updateAccountInfo() helper method should return when stubbed
    CUSTOMER1_DATA = new ArrayList<>();
    CUSTOMER1_DATA.add(new HashMap<>());
    CUSTOMER1_DATA.get(0).put("FirstName", "John");
    CUSTOMER1_DATA.get(0).put("LastName", "Doe");
    CUSTOMER1_DATA.get(0).put("Balance", 10000);
	  CUSTOMER1_DATA.get(0).put("OverdraftBalance", 0);
  }
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

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

<<<<<<< HEAD
		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper
																					// method
		when(jdbcTemplate.update(anyString(), eq(Integer.class))).thenReturn(0);
=======
    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
		when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

		// send login request
		String pageReturned = controller.submitLoginForm(customer1);

<<<<<<< HEAD
		// Verify that the SELECT SQL command executed to retrieve user's password uses
		// the customer's ID
		String getCustomer1PasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';",
				customer1.getUsername());
		Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
=======
    // Verify that the SELECT SQL command executed to retrieve user's password uses the customer's ID
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

		// verify "account_info" page is returned
		assertEquals("account_info", pageReturned);
	}

	@Test
	public void testSubmitLoginFormFailureWithIncorrectPassword() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("not password");

<<<<<<< HEAD
		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper
																					// method
=======
    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
		when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

		// send login request
		String pageReturned = controller.submitLoginForm(customer1);

<<<<<<< HEAD
		// Verify that the SELECT SQL command executed to retrieve user's password uses
		// the customer's ID
		String getCustomer1PasswordSql = String.format("SELECT Password FROM passwords WHERE CustomerID='%s';",
				customer1.getUsername());
		Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
=======
    // Verify that the SELECT SQL command executed to retrieve user's password uses the customer's ID
    Mockito.verify(jdbcTemplate, Mockito.times(1)).queryForObject(eq(getCustomer1PasswordSql), eq(String.class));
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

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
		customer1.setAmountToDeposit(10000);

<<<<<<< HEAD
		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper
																					// method
=======
    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
		when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // no overdraft
    String getCustomer1OverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1OverdraftBalanceSql), eq(Integer.class))).thenReturn(0);
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // not working with live DB
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05
		when(jdbcTemplate.update(anyString())).thenReturn(1);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

		// send deposit request
		String pageReturned = controller.submitDeposit(customer1);

<<<<<<< HEAD
		// Verify that the SQL Update command executed uses customer1's ID and
		// amountToDeposit.
		String balanceIncreaseSqlCustomer1 = String.format(
				"UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';", customer1.getAmountToDeposit()/100,
				customer1.getUsername());
		Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceIncreaseSqlCustomer1));
=======
    // Verify that the SQL Update command executed uses customer1's ID and amountToDeposit.
    int expectedDepositAmtInPennies = (int) (customer1.getAmountToDeposit() * 100);
    String balanceIncreaseSqlCustomer1=String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';",
                                                     expectedDepositAmtInPennies,
                                                     customer1.getUsername());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceIncreaseSqlCustomer1));
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

		// verify "account_info" page is returned
		assertEquals("account_info", pageReturned);
	}

	@Test
	public void testDepositFailurewithIncorrectPassword() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("not password");
		customer1.setAmountToDeposit(10000);

<<<<<<< HEAD
		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper
																					// method
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
=======
    // stub jdbc calls
    // unsuccessful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
		when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

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
		customer1.setAmountToWithdraw(10000);

<<<<<<< HEAD
		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper
																					// method
=======
    // stub jdbc calls
    // successful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
		when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
    // retrieve balance of $200 (which is stored as 20000 pennies) from DB
    String getCustomer1BalanceSql=String.format("SELECT Balance FROM customers WHERE CustomerID='%s';", customer1.getUsername());
    when(jdbcTemplate.queryForObject(eq(getCustomer1BalanceSql), eq(Integer.class))).thenReturn(20000);
    // handles updateAccountInfo() helper method
    when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA);
    // not working with live DB
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05
		when(jdbcTemplate.update(anyString())).thenReturn(1);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(100);

		// send withdraw request
		String pageReturned = controller.submitWithdraw(customer1);

<<<<<<< HEAD
		// Verify that the SQL Update command executed uses customer1's ID and
		// amountToWitdraw.
		String balanceDecreaseSqlCustomer1 = String.format(
				"UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';", customer1.getAmountToWithdraw()/100,
				customer1.getUsername());
		Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceDecreaseSqlCustomer1));
=======
    // Verify that the SQL Update command executed uses customer1's ID and amountToWitdraw.
    int expectedWithdrawAmtInPennies = (int) (customer1.getAmountToWithdraw() * 100);
    String balanceDecreaseSqlCustomer1=String.format("UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';",
                                                     expectedWithdrawAmtInPennies,
                                                     customer1.getUsername());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq(balanceDecreaseSqlCustomer1));
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

		// verify "account_info" page is returned
		assertEquals("account_info", pageReturned);
	}

	@Test
	public void testWithdrawFailurewithIncorrectPassword() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("not password");
		customer1.setAmountToWithdraw(10000);

<<<<<<< HEAD
		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper
																					// method
		when(jdbcTemplate.update(anyString(), eq(Integer.class))).thenReturn(100);
=======
    // stub jdbc calls
    // unsuccessful login
    String getCustomer1PasswordSql=String.format("SELECT Password FROM passwords WHERE CustomerID='%s';", customer1.getUsername());
		when(jdbcTemplate.queryForObject(eq(getCustomer1PasswordSql), eq(String.class))).thenReturn("password");
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

		// send withdraw request
		String pageReturned = controller.submitWithdraw(customer1);

		// Verify that no SQL Update commands are sent
		Mockito.verify(jdbcTemplate, Mockito.times(0)).update(anyString());

		// verify that customer is re-directed to "welcome" page
		assertEquals("welcome", pageReturned);
	}

	@Test
	public void testWithdrawFailureAtOverdraftLimit() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("password");
		customer1.setBalance(0);
		customer1.setOverdraftBalance(0);
		customer1.setAmountToWithdraw(100100);

		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper																					// method
		when(jdbcTemplate.update(anyString(), eq(Integer.class))).thenReturn(0);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
		// send withdraw request
		String pageReturned = controller.submitWithdraw(customer1);

		// Verify that no SQL Update commands are sent
		Mockito.verify(jdbcTemplate, Mockito.times(0)).update(anyString());

		// verify that customer is re-directed to "welcome" page
		assertEquals("welcome", pageReturned);
	}

	@Test
<<<<<<< HEAD
	public void testWithdrawSuccesswithOverdraft() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("password");
		customer1.setBalance(0);
		customer1.setOverdraftBalance(0);
		customer1.setAmountToWithdraw(10000);

		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper																					// method
		when(jdbcTemplate.update(anyString(), eq(Integer.class))).thenReturn(0);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
		// send withdraw request
		String pageReturned = controller.submitWithdraw(customer1);

		// Verify that 2 SQL Update commands are sent (one for balance, one for overdraft balance)
		Mockito.verify(jdbcTemplate, Mockito.times(2)).update(anyString());

		// verify that customer is re-directed to "account_info" page
=======
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
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05
		assertEquals("account_info", pageReturned);
	}

	@Test
<<<<<<< HEAD
	public void testWithdrawSuccesswithPartialOverdraft() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("password");
		customer1.setBalance(10000);
		customer1.setOverdraftBalance(0);
		customer1.setAmountToWithdraw(20000);

		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper																					// method
		when(jdbcTemplate.update(anyString(), eq(Integer.class))).thenReturn(0);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(100);
		// send withdraw request
		String pageReturned = controller.submitWithdraw(customer1);

		// Verify that 2 SQL Update commands are sent (one for balance, one for overdraft balance)
		Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq("UPDATE Customers SET Balance = 0 WHERE CustomerID='123456789';"));

		Mockito.verify(jdbcTemplate, Mockito.times(1)).update(eq("UPDATE Customers SET OverdraftBalance = OverdraftBalance + 10000 WHERE CustomerID='123456789';"));

		// verify that customer is re-directed to "account_info" page
		assertEquals("account_info", pageReturned);
	}

	@Test
	public void testDepositRepayOverdraftOnly() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("password");
		customer1.setBalance(0);
		customer1.setOverdraftBalance(20000);
		customer1.setAmountToDeposit(10200);

		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper																					// method
		when(jdbcTemplate.update(anyString(), eq(Integer.class))).thenReturn(0);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(20000);
		// send withdraw request
		String pageReturned = controller.submitDeposit(customer1);

		// Verify that 3 SQL Update commands are sent (one for balance, one for overdraft balance, one for new log entry)
		Mockito.verify(jdbcTemplate, Mockito.times(3)).update(anyString());

		
		// verify that customer is re-directed to "account_info" page
		assertEquals("account_info", pageReturned);
	}

	
	@Test
	public void testDepositWithZeroOverdraft() {
		User customer1 = new User();
		customer1.setUsername(CUSTOMER1_USERNAME);
		customer1.setPassword("password");
		customer1.setBalance(0);
		customer1.setOverdraftBalance(10000);
		customer1.setAmountToDeposit(10200);

		// stub jdbc calls
		when(jdbcTemplate.queryForObject(anyString(), eq(String.class))).thenReturn("password");
		when(jdbcTemplate.queryForList(anyString())).thenReturn(CUSTOMER1_DATA); // handles updateAccountInfo() helper																					// method
		when(jdbcTemplate.update(anyString(), eq(Integer.class))).thenReturn(0);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
		// send withdraw request
		String pageReturned = controller.submitDeposit(customer1);

		// Verify that only 1 SQL Update commands is sent
		Mockito.verify(jdbcTemplate, Mockito.times(1)).update(anyString());

		// verify that customer is re-directed to "account_info" page
		assertEquals("account_info", pageReturned);
	}
	
=======
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
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05
}
