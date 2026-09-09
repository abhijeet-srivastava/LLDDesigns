package org.example;

import java.util.Objects;

public class Account {

    private static final int FD_MATURE_COUNT = 5;
    private static final int FD_INTEREST_RATE = 5;
    String ownerId;
    Integer amount;

    FixedDeposit fd;

    public boolean hasSufficientBalance(int amount) {
        return amount <= this.amount;
    }

    public void createFD(int amount) {
        this.fd = new FixedDeposit(amount);
    }


    private class FixedDeposit {

        int amount;
        int txnCount;

        public FixedDeposit(int amount) {
            this.amount = amount;
            this.txnCount = 0;
        }

        public boolean incrementCount() {
            this.txnCount += 1;
            return this.txnCount == FD_MATURE_COUNT;
        }
    }

    public Account(String ownerId) {
        this.ownerId = ownerId;
        this.amount = 0;
    }

    public boolean debit(int amount) {
        if(this.amount < amount) {
            return false;
        }
        this.amount -= amount;
        if(hasActiveFd()) {
            if(this.amount < this.fd.amount) {
                breakFd();
                return true;
            }
            if(this.fd.incrementCount()) {
                creditFdInterest();
            }
        }
        return true;
    }

    private void creditFdInterest() {
        double interestAmount = (this.fd.amount * FD_INTEREST_RATE)*0.01d;
        int roundOfAmount = (int)Math.round(interestAmount);
        credit(roundOfAmount);

    }

    private void breakFd() {
        this.fd = null;
    }

    public boolean hasActiveFd() {
        return Objects.nonNull(fd);
    }

    public void credit(int amount) {
        this.amount += amount;
    }

}
