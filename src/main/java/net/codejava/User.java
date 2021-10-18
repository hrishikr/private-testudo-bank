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

  @Setter @Getter
  private List<Map<String,Object>> overdraftLogs;

  @Setter @Getter @PositiveOrZero
	private int balance;

  @Setter @Getter @PositiveOrZero
	private int overdraftBalance;

  @Setter @Getter @Positive
  private int amountToDeposit;

  @Setter @Getter @Positive
  private int amountToWithdraw;

	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", balance=" + balance + "]";
	}

}
