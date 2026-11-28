package engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/** Balance changes and spending boundaries for coins. */
@Isolated
class CurrencyManagerTest {

	private CurrencyManager balance;
	private int originalCoins;

	@BeforeEach
	void setUp() {
		balance = CurrencyManager.getInstance();
		originalCoins = balance.getCoins();
		balance.reset();
	}

	@AfterEach
	void tearDown() {
		// Restore the balance even when a test fails. These methods also save it.
		balance.reset();
		balance.addCoins(originalCoins);
	}

	@Test
	void addingAndSpendingUpdatesTheBalance() {
		balance.addCoins(100);
		balance.addCoins(20);
		assertEquals(120, balance.getCoins());

		assertTrue(balance.trySpend(30));
		assertEquals(90, balance.getCoins());
	}

	@Test
	void insufficientFundsLeaveTheBalanceUnchanged() {
		balance.addCoins(50);

		assertFalse(balance.canAfford(51));
		assertFalse(balance.trySpend(51));
		assertEquals(50, balance.getCoins());
	}

	@Test
	void spendingTheExactBalanceLeavesZero() {
		balance.addCoins(50);

		assertTrue(balance.canAfford(50));
		assertEquals(50, balance.getCoins());
		assertTrue(balance.trySpend(50));
		assertEquals(0, balance.getCoins());
	}
}
