package net.codejava;

import java.util.List;
import java.util.Map;


import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

public class User {
  @Setter @Getter
	private String username;

  @Setter @Getter
	private String password;

  @Setter @Getter
  private String firstName;

  @Setter @Getter
  private String lastName;

<<<<<<< HEAD
  @Setter @Getter
  private List<Map<String,Object>> overdraftLogs;
=======
  @Setter  @Getter @PositiveOrZero
	private double balance;
>>>>>>> 59f3c65bdc4c2be4593bd48803a8764aed93de05

  @Setter @Getter @PositiveOrZero
	private double overDraftBalance;

  @Setter @Getter
	private String logs;

  @Setter @Getter @PositiveOrZero
	private int overdraftBalance;

  @Setter @Getter @Positive
  private double amountToDeposit;

  @Setter @Getter @Positive
  private double amountToWithdraw;

  @Setter @Getter
  private int numTransactionsAgo;

	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", balance=" + balance + "]";
	}

}
