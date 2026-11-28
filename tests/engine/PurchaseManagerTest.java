package engine;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import shop.Currency;
import shop.ShopCatalog;
import shop.ShopCategory;
import shop.ShopItem;

/** Coin purchases grant ownership only when the balance covers the price. */
@Isolated
class PurchaseManagerTest {

	private CurrencyManager coins;
	private PurchaseManager purchases;
	private ShopItem item;
	private int originalCoins;
	private Map<String, Integer> ownedItems;
	private Integer originalQuantity;
	private Path shopFile;
	private byte[] originalShopFile;
	private boolean ready;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() throws Exception {
		coins = CurrencyManager.getInstance();
		purchases = PurchaseManager.getInstance();
		item = ShopCatalog.findByRef(ShopCategory.SKIN, "CRIMSON_HULL");
		originalCoins = coins.getCoins();

		// There is no public method to undo a purchase, so access the ownership map here.
		Field field = PurchaseManager.class.getDeclaredField("owned");
		field.setAccessible(true);
		ownedItems = (Map<String, Integer>) field.get(purchases);
		originalQuantity = ownedItems.get(item.getId());

		Path directory = Path.of(PurchaseManager.class.getProtectionDomain()
				.getCodeSource().getLocation().toURI()).getParent();
		shopFile = directory.resolve("shop-owned");
		if (Files.exists(shopFile))
			originalShopFile = Files.readAllBytes(shopFile);

		ready = true;
		coins.reset();
		ownedItems.remove(item.getId());
	}

	@AfterEach
	void tearDown() throws Exception {
		if (!ready)
			return;

		coins.reset();
		coins.addCoins(originalCoins);

		if (originalQuantity == null)
			ownedItems.remove(item.getId());
		else
			ownedItems.put(item.getId(), originalQuantity);

		// Restore the saved purchases too, not just the values in memory.
		if (originalShopFile == null)
			Files.deleteIfExists(shopFile);
		else
			Files.write(shopFile, originalShopFile);
	}

	@Test
	void buyingWithCoinsDeductsThePriceAndGrantsOwnership() {
		coins.addCoins(100);

		assertEquals(PurchaseManager.PurchaseResult.SUCCESS, purchases.purchase(item, Currency.COINS));
		assertEquals(70, coins.getCoins());
		assertTrue(purchases.isOwned(item));
		assertEquals(1, purchases.getQuantity(item));
	}

	@Test
	void insufficientCoinsLeaveBalanceAndOwnershipUnchanged() {
		coins.addCoins(29);

		assertEquals(PurchaseManager.PurchaseResult.INSUFFICIENT_FUNDS,
				purchases.purchase(item, Currency.COINS));
		assertEquals(29, coins.getCoins());
		assertFalse(purchases.isOwned(item));
	}

	@Test
	void buyingWithTheExactPriceLeavesZero() {
		coins.addCoins(30);

		assertEquals(PurchaseManager.PurchaseResult.SUCCESS, purchases.purchase(item, Currency.COINS));
		assertEquals(0, coins.getCoins());
		assertTrue(purchases.isOwned(item));
	}
}
