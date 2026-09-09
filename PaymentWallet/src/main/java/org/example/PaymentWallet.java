package org.example;

import java.util.HashMap;
import java.util.Map;

public class PaymentWallet {
    private final Map<String, Account> userAccounts;

    public PaymentWallet() {
        userAccounts = new HashMap<>();
    }

    /**
     *- Registers a new unique user with account balance = 0.
     * - userId will always be non blank and unique
     * @param userId
     */
    void registerUser(String userId) {
        if(userId == null || userId.trim().isBlank() || userAccounts.containsKey(userId)) {
            return;
        }
        userAccounts.put(userId, new Account(userId));
    }

    /**
     * - amount will always be a positive integer ≤ 1000
     * - if userId is not already registered then return "user does not exist"
     * - else if amount is successfully added then return "success"
     * @param userId
     * @param amount
     * @return
     */
    String addMoneyToWallet(String userId, int amount) {
        if(!userAccounts.containsKey(userId)) {
            return "user does not exist";
        }
        userAccounts.get(userId).credit(amount);
        return "success";
    }

    /**
     * - if userId is not registered then return "user does not exist"
     * - if user doesn't have enough balance then return "insufficient balance"
     * - if amount is successfully deducted then return "success"
     * @param userId
     * @param amount
     * @return
     */
    String spendMoney(String userId, int amount) {
        if(!userAccounts.containsKey(userId)) {
            return "user does not exist";
        }
        if(!this.userAccounts.get(userId).debit(amount)){
            return "insufficient balance";
        }
        return "success";

    }

    /**
     *
     * @param fromUser
     * @param toUser
     * @param amount
     * @return
     */
    String transferMoney(String fromUser, String toUser, int amount) {
        if(!userAccounts.containsKey(fromUser)) {
            return "sender does not exist";
        }
        if(!userAccounts.containsKey(toUser)) {
            return "receiver does not exist";
        }
        if(!userAccounts.get(fromUser).hasSufficientBalance(amount)){
            return "insufficient balance";
        }
        this.userAccounts.get(fromUser).debit(amount);
        this.userAccounts.get(toUser).credit(amount);
        return "success";
    }

    /**
     *
     * @param userId
     * @param amount
     * @return
     */
    String createFixedDeposit(String userId, int amount) {
        if(!userAccounts.containsKey(userId)) {
            return "user does not exist";
        } else if(!this.userAccounts.get(userId).hasSufficientBalance(amount)) {
            return "insufficient balance";
        } else if(this.userAccounts.get(userId).hasActiveFd()) {
            return "an active fixed deposit already exists";
        }
        this.userAccounts.get(userId).createFD(amount);
        return "success";
    }

    /**
     *
     * @param userId
     * @return
     */
    int getAccountBalance(String userId) {
        if(!userAccounts.containsKey(userId)) {
            return -1;
        }
        return userAccounts.get(userId).amount;
    }

}
