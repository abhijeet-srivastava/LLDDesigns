package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentWalletTest {
    PaymentWallet wallet;
    @BeforeEach
    void setUp() {
        wallet = new PaymentWallet();
    }

    @Test
    void testAccountOperations() {
        wallet.registerUser("alice");
        assertThat(wallet.addMoneyToWallet("alice", 500)).isEqualTo("success");
        assertThat(wallet.spendMoney("alice", 200)).isEqualTo("success");
        assertThat(wallet.transferMoney("alice", "bob", 100)).isEqualTo("receiver does not exist");
        wallet.registerUser("bob");
        assertThat(wallet.transferMoney("alice", "bob", 100)).isEqualTo("success");
    }

    @Test
    void testFixedDeposit() {
        

    }
}