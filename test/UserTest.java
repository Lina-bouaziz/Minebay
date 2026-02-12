package minebay.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebay.AdCategory;
import minebay.AdState;
import static minebay.AdState.*;
import minebay.ClassifiedAd;
import minebay.MultiEnumList;
import minebay.User;
import static minebay.test.DataProvider.*;

/**
 * Test class for User.
 *
 * Un utilisateur de l'application MinEbay.
 * 
 * <p>
 * Un User est caractérisé par:
 * <ul>
 * <li>son nom et son mot de passe</li>
 * <li>le montant disponible sur son compte bancaire</li>
 * <li>sa date d'inscription sur MinEbay (= date de création de l'instance de
 * l'User)</li>
 * <li>la liste des annonces qu'il a posté et pour lesquelles il n'a pas trouvé
 * d'acheteur (annonces dans l'état OPEN)</li>
 * <li>la liste des annonces qu'il a posté et pour lesquelles il a vendu l'objet
 * concerné (annonces dans l'état CLOSED)</li>
 * <li>la liste des annonces (d'autres utilisateurs) pour lesquelles il a acheté
 * l'objet concerné (annonces dans l'état PURCHASE)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Chacune des trois listes d'annonces d'un User est représentée en utilisant
 * une instance de MultiEnumList. Cela permet d'effectuer des itérations portant
 * uniquement sur des annonces appartenant à un sous-ensemble de catégories,
 * sans qu'il soit besoin de créer des listes intermédiaires contenant ces
 * annonces. Comme pour les MultiEnumList, il est fortement déconseillé
 * d'utiliser les méthodes get pour effectuer un parcours de liste, l'usage d'un
 * itérateur est hautement préférable.
 * </p>
 * 
 * <p>
 * Les méthodes ne possédant pas de paramètre de type AdState ou Set<AdCategory>
 * agissent uniquement sur les annonces dont l'état et la catégorie a été
 * sélectionnée.
 * </p>
 */
public class UserTest {

	public static Stream<User> userProvider() {
		return Stream.generate(() -> userSupplier()).limit(LG_STREAM);
	}

	public static Stream<Arguments> stringAndStringProvider() {
		return Stream.generate(() -> Arguments.of(stringSupplier(), stringSupplier())).limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndStateProvider() {
		return Stream.generate(
				() -> Arguments.of(userSupplier(), randBool(BAD_ARG_PROBA) ? null : enumSupplier(AdState.class)))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndCatProvider() {
		return Stream.generate(
				() -> Arguments.of(userSupplier(), randBool(BAD_ARG_PROBA) ? null : enumSupplier(AdCategory.class)))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndUserAndAdProvider() {
		return Stream.generate(() -> userAndUserAndAdSupplier()).limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndCatAndNameAndPriceProvider() {
		return Stream.generate(() -> Arguments.of(userSupplier(), enumSupplierWithNull(AdCategory.class),
				stringSupplier(), randInt(50) - 5)).limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndStateAndCatSetProvider() {
		return Stream
				.generate(() -> Arguments.of(userSupplier(),
						randBool(BAD_ARG_PROBA) ? null : enumSupplier(AdState.class), catSetSupplier(AdCategory.class)))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndStateAndCatSetAndIntProvider() {
		return Stream.generate(
				() -> Arguments.of(userSupplier(), randBool(BAD_ARG_PROBA) ? null : enumSupplier(AdState.class),
						catSetSupplier(AdCategory.class), randInt(10) - 2))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndIntProvider() {
		return Stream.generate(() -> Arguments.of(userSupplier(), randInt(10) - 2)).limit(LG_STREAM);
	}

	public static Stream<Arguments> userAndStateAndAdProvider(){
		return Stream.generate(() -> userAndStateAndAdSupplier()).limit(LG_STREAM);
	}
	
	public static Arguments userAndUserAndAdSupplier() {
		User buyer = userSupplier();
		User vendor = userSupplier();
		ClassifiedAd ad = adSupplier();
		if (randBool(BAD_ARG_PROBA)) {
			vendor = null;
		} else if (randBool(BAD_ARG_PROBA)) {
			vendor = buyer;
		} else if (randBool(BAD_ARG_PROBA)) {
			ad = null;
		} else {
			Iterator<ClassifiedAd> iter = vendor.iterator(OPEN, EnumSet.allOf(AdCategory.class));
			int nb = randInt(10);
			while (iter.hasNext() && nb > 0) {
				ad = iter.next();
				nb--;
			}
		}
		return Arguments.of(buyer, vendor, ad);
	}

	public static Arguments userAndStateAndAdSupplier() {
		User u = userSupplier();
		AdState state = enumSupplier(AdState.class);
		ClassifiedAd ad = adSupplier();
		if (randBool(BAD_ARG_PROBA)) {
			state = null;
		}
		if (randBool(BAD_ARG_PROBA)) {
			ad = null;
		}
		if (randBool() && state != null) {
			Iterator<ClassifiedAd> iter = u.iterator(state, EnumSet.allOf(AdCategory.class));
			int nb = randInt(10);
			while (iter.hasNext() && nb > 0) {
				ad = iter.next();
				nb--;
			}
		}
		return Arguments.of(u, state, ad);
	}

	// State of a User
	private String name;
	private String password;
	private Instant registrationtionDate;
	private AdState selectedState;
	private Set<AdCategory> catSet;
	private int bankAccount;
	private LinkedList<ClassifiedAd> selectedAdList;
	private LinkedList<ClassifiedAd> openAdList;
	private LinkedList<ClassifiedAd> closedAdList;
	private LinkedList<ClassifiedAd> purchaseAdList;

	private void saveState(User self) {
		// Put here the code to save the state of self:
		this.name = self.getName();
		this.password = self.getPassword();
		this.registrationtionDate = self.getRegistrationDate();
		this.selectedState = self.getSelectedAdState();
		this.catSet = self.getSelectedCategories();
		this.bankAccount = self.getAvailableCash();
		this.selectedAdList = new LinkedList<>();
		for (ClassifiedAd ad : self) {
			this.selectedAdList.add(ad);
		}
		this.openAdList = new LinkedList<>();
		Iterator<ClassifiedAd> iter = self.iterator(OPEN, EnumSet.allOf(AdCategory.class));
		while (iter.hasNext()) {
			this.openAdList.add(iter.next());
		}
		this.closedAdList = new LinkedList<>();
		iter = self.iterator(CLOSED, EnumSet.allOf(AdCategory.class));
		while (iter.hasNext()) {
			this.closedAdList.add(iter.next());
		}
		this.purchaseAdList = new LinkedList<>();
		iter = self.iterator(PURCHASE, EnumSet.allOf(AdCategory.class));
		while (iter.hasNext()) {
			this.purchaseAdList.add(iter.next());
		}

	}

	private void assertPurity(User self) {
		// Put here the code to check purity for self:
		assertEquals(name, self.getName());
		assertEquals(password, self.getPassword());
		assertEquals(this.registrationtionDate, self.getRegistrationDate());
		assertEquals(this.selectedState, self.getSelectedAdState());
		assertEquals(this.catSet, self.getSelectedCategories());
		assertEquals(this.bankAccount, self.getAvailableCash());
		assertEquals(this.selectedAdList.size(), self.size());
		Iterator<ClassifiedAd> iter = self.iterator();
		Iterator<ClassifiedAd> iterBefore = this.selectedAdList.iterator();
		while (iter.hasNext()) {
			assertEquals(iterBefore.next(), iter.next());
		}
		assertFalse(iterBefore.hasNext());

		assertEquals(this.openAdList.size(), self.size(OPEN, EnumSet.allOf(AdCategory.class)));
		iter = self.iterator(OPEN, EnumSet.allOf(AdCategory.class));
		iterBefore = this.openAdList.iterator();
		while (iter.hasNext()) {
			assertEquals(iterBefore.next(), iter.next());
		}
		assertFalse(iterBefore.hasNext());

		assertEquals(this.closedAdList.size(), self.size(CLOSED, EnumSet.allOf(AdCategory.class)));
		iter = self.iterator(CLOSED, EnumSet.allOf(AdCategory.class));
		iterBefore = this.closedAdList.iterator();
		while (iter.hasNext()) {
			assertEquals(iterBefore.next(), iter.next());
		}
		assertFalse(iterBefore.hasNext());

		assertEquals(this.purchaseAdList.size(), self.size(PURCHASE, EnumSet.allOf(AdCategory.class)));
		iter = self.iterator(PURCHASE, EnumSet.allOf(AdCategory.class));
		iterBefore = this.purchaseAdList.iterator();
		while (iter.hasNext()) {
			assertEquals(iterBefore.next(), iter.next());
		}
		assertFalse(iterBefore.hasNext());
	}

	public void assertInvariant(User self) {
		// Put here the code to check the invariant:
		// @invariant getName() != null && !getName().isBlank();
		assertTrue(self.getName() != null && !self.getName().isBlank());
		// @invariant getPassword() != null && !getPassword().isBlank();
		assertTrue(self.getPassword() != null && !self.getPassword().isBlank());
		// @invariant getRegistrationDate() != null;
		assertNotNull(self.getRegistrationDate());
		// @invariant getAvailableCash() >= 0;
		assertTrue(self.getAvailableCash() >= 0);
		// @invariant (\forall AdState state; true; !contains(state, null));
		for (AdState state : EnumSet.allOf(AdState.class)) {
			assertFalse(self.containsInState(state, null));
		}
		// @invariant (\forall int i, j; i >= 0 && i < j && j < size();
		// get(i).isAfter(get(j)));
		for (int i = 1; i < self.size(); i++) {
			assertTrue(self.get(i - 1).isAfter(self.get(i)));
		}
		// @invariant (\forall AdState state, AdCategory cat;true; (\forall int i, j; i
		// >= 0 && i < j && j < size(state, EnumSet.of(cat)); get(state,
		// EnumSet.of(cat), i).isAfter(get(state, EnumSet.of(cat), j))));
		for (AdState state : EnumSet.allOf(AdState.class)) {
			for (AdCategory cat : EnumSet.allOf(AdCategory.class)) {
				for (int i = 1; i < self.size(state, EnumSet.of(cat)); i++) {
					assertTrue(self.get(state, EnumSet.of(cat), i - 1).isAfter(self.get(state, EnumSet.of(cat), i)));
				}

			}
		}

	}

	/**
	 * Test method for constructor User
	 *
	 * Initialise une nouvelle instance ayant les nom et mot de passe spécifiés. La
	 * date d'inscription du nouvel utilisateur est la date au moment de l'exécution
	 * de ce constructeur. L'état sélectionné est OPEN et toutes les catégories sont
	 * sélectionnées.
	 * 
	 * @throws NullPointerException     si l'un des arguments spécifiés est null
	 * @throws IllegalArgumentException si l'un des arguments spécifiés ne contient
	 *                                  que des caractères blancs ou ne contient
	 *                                  aucun caractères
	 */
	@ParameterizedTest
	@MethodSource("stringAndStringProvider")
	public void testUser(String userName, String password) {

		// Pré-conditions:
		// @requires userName != null && !userName.isBlank();
		// @requires password != null && !password.isBlank();
		if (userName == null || userName.isBlank() || password == null || password.isBlank()) {
			Throwable e = assertThrows(Throwable.class, () -> new User(userName, password));
			if (e instanceof NullPointerException) {
				assertTrue(userName == null || password == null);
				return;
			}
			if (e instanceof IllegalArgumentException) {
				assertTrue(userName.isBlank() || password.isBlank());
				return;
			}
			fail("Exception inattendue");
			return;
		}

		// Oldies:
		// old in:@ensures \old(Instant.now()).isBefore(getRegistrationDate());
		Instant oldNow = Instant.now();

		// Exécution:
		User result = new User(userName, password);

		// Post-conditions:
		// @ensures getName().equals(userName);
		assertEquals(userName, result.getName());
		// @ensures getPassword().equals(password);
		assertEquals(password, result.getPassword());
		// @ensures getRegistrationDate() != null;
		assertNotNull(result.getRegistrationDate());
		// @ensures \old(Instant.now()).isBefore(getRegistrationDate());
		assertTrue(oldNow.isBefore(result.getRegistrationDate()));
		// @ensures getRegistrationDate().isBefore(Instant.now());
		assertTrue(result.getRegistrationDate().isBefore(Instant.now()));
		// @ensures getAvailableCash() = DEFAULT_CASH_AMMOUNT;
		assertEquals(User.DEFAULT_CASH_AMMOUNT, result.getAvailableCash());
		// @ensures getSelectedAdState().equals(OPEN);
		assertEquals(OPEN, result.getSelectedAdState());
		// @ensures getSelectedCategories().equals(EnumSet.allOf(AdCategory.class));
		assertEquals(EnumSet.allOf(AdCategory.class), result.getSelectedCategories());

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method getSelectedAdState
	 *
	 * Renvoie l'état actuellement sélectionné pour les annonces.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetSelectedAdState(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		AdState result = self.getSelectedAdState();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method selectAdState
	 *
	 * Sélectionne l'état spécifié.
	 * 
	 * @throws NullPointerException si l'état spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("userAndStateProvider")
	public void testselectAdState(User self, AdState state) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		if (state == null) {
			assertThrows(NullPointerException.class, () -> self.selectAdState(state));
			return;
		}

		// Oldies:
		// old in:@ensures getSelectedCategory().equals(\old(getSelectedCategory()));
		Set<AdCategory> oldCatSet = self.getSelectedCategories();

		// Exécution:
		self.selectAdState(state);

		// Post-conditions:
		// @ensures getSelectedAdState().equals(state);
		assertEquals(state, self.getSelectedAdState());
		// @ensures getSelectedCategory().equals(\old(getSelectedCategory()));
		assertEquals(oldCatSet, self.getSelectedCategories());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method addSelectedCategory
	 *
	 * Ajoute la catégorie spécifiée à l'ensemble des catégories sélectionnées.
	 * 
	 * @throws NullPointerException si la catégorie spécifiée est null
	 */
	@ParameterizedTest
	@MethodSource("userAndCatProvider")
	public void testaddSelectedCategory(User self, AdCategory cat) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires cat != null;
		if (cat == null) {
			assertThrows(NullPointerException.class, () -> self.addSelectedCategory(cat));
			return;
		}

		// Oldies:
		// old in:@ensures \result <==> !\old(getSelectedCategories().contains(cat));
		// old in:@ensures !\result ==>
		// getSelectedCategories().equals(\old(getSelectedCategories()));
		Set<AdCategory> oldCatSet = self.getSelectedCategories();
		boolean oldContains = oldCatSet.contains(cat);

		// Exécution:
		boolean result = self.addSelectedCategory(cat);

		// Post-conditions:
		// @ensures !getSelectedCategories().isEmpty();
		assertFalse(self.getSelectedCategories().isEmpty());
		// @ensures getSelectedCategories().contains(cat);
		assertTrue(self.getSelectedCategories().contains(cat));
		// @ensures \result <==> !\old(getSelectedCategories().contains(cat));
		assertEquals(!oldContains, result);
		// @ensures !\result ==>
		// getSelectedCategories().equals(\old(getSelectedCategories()));
		if (!result) {
			assertEquals(oldCatSet, self.getSelectedCategories());
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method removeSelectedCategory
	 *
	 * Retire la catégorie spécifiée de l'ensemble des catégories sélectionnées.
	 */
	@ParameterizedTest
	@MethodSource("userAndCatProvider")
	public void testremoveSelectedCategory(User self, Object obj) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:
		// old in:@ensures \result <==> \old(getSelectedCategories().contains(obj));
		// old in:@ensures !\result ==>
		// getSelectedCategories().equals(\old(getSelectedCategories()));
		Set<AdCategory> oldCatSet = self.getSelectedCategories();
		boolean oldContains = oldCatSet.contains(obj);

		// Exécution:
		boolean result = self.removeSelectedCategory(obj);

		// Post-conditions:
		// @ensures !getSelectedCategories().contains(obj);
		assertFalse(self.getSelectedCategories().contains(obj));
		// @ensures \result <==> \old(getSelectedCategories().contains(obj));
		assertEquals(oldContains, result);
		// @ensures !\result ==>
		// getSelectedCategories().equals(\old(getSelectedCategories()));
		if (!result) {
			assertEquals(oldCatSet, self.getSelectedCategories());
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getSelectedCategories
	 *
	 * Renvoie une vue non modifiable de l'ensemble des catégories actuellement
	 * sélectionnées. Si aucune catégorie n'est sélectionnée, renvoie un ensemble
	 * vide.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetSelectedCategories(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Set<AdCategory> result = self.getSelectedCategories();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method clearSelectedCategories
	 *
	 * Déselectionne toutes les catégories précédemment sélectionnées.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testclearSelectedCategories(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		boolean result = self.clearSelectedCategories();

		// Post-conditions:
		// @ensures getSelectedCategories().isEmpty();
		assertTrue(self.getSelectedCategories().isEmpty());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method selectAllCategories
	 *
	 * Sélectionne toutes les catégories.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testselectAllCategories(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		boolean result = self.selectAllCategories();

		// Post-conditions:
		// @ensures getSelectedCategories().equals(EnumSet.allOf(AdCategory.class));
		assertEquals(EnumSet.allOf(AdCategory.class), self.getSelectedCategories());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getName
	 *
	 * Renvoie le nom de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetName(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getName();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPassword
	 *
	 * Renvoie le mot de passe de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetPassword(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getPassword();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getRegistrationDate
	 *
	 * Renvoie l'Instant représentant la date d'inscription de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetRegistrationDate(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Instant result = self.getRegistrationDate();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getAvailableCash
	 *
	 * Renvoie la somme d'argent disponible pour cet utilisateur pour acheter des
	 * objets.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetAvailableCash(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getAvailableCash();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method buy
	 *
	 * Achète à l'utilisateur spécifié l'objet présenté dans l'annonce spécifiée.
	 * Pour le vendeur l'annonce passe de la liste des annonces OPEN à la liste des
	 * annonces CLOSED et il reçoit le montant du prix de l'objet présenté dans
	 * l'annonce. Pour ce User, l'annonce est ajoutée à la liste des annonces
	 * PURCHASE et sa somme d'argent disponible est diminuée du prix de l'objet.
	 * 
	 * @throws NullPointerException     si un des deux arguments spécifié est null
	 * @throws IllegalArgumentException si l'annonce spécifiée n'est pas une annonce
	 *                                  OPEN du vendeur spécifié ou si le vendeur
	 *                                  spécifié est ce User
	 * @throws IllegalStateException    si l'argent disponible de ce User est
	 *                                  inférieur au prix de l'annonce spécifiée
	 */
	@ParameterizedTest
	@MethodSource("userAndUserAndAdProvider")
	public void testbuy(User self, User vendor, ClassifiedAd ad) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires vendor != null;
		// @requires this != vendor;
		// @requires ad != null;
		// @requires vendor.containsInState(OPEN, ad);
		// @requires getAvailableCash() >= ad.getPrice();
		boolean nullExcept = vendor == null || ad == null;
		boolean argExcept = vendor != null && (self == vendor || !vendor.containsInState(OPEN, ad));
		boolean noMoney = ad != null && (self.getAvailableCash() < ad.getPrice());
		if (nullExcept || argExcept || noMoney) {
			Throwable e = assertThrows(Throwable.class, () -> self.buy(vendor, ad));
			if (e instanceof NullPointerException) {
				assertTrue(nullExcept);
				return;
			}
			if (e instanceof IllegalArgumentException) {
				assertTrue(argExcept);
				return;
			}
			if (e instanceof IllegalStateException) {
				assertTrue(noMoney);
				return;
			}
			fail("Exception inattendue");
			return;
		}

		// Oldies:
		// old in:@ensures size(PURCHASE, EnumSet.of(ad.getCategory())) ==
		// \old(size(PURCHASE, EnumSet.of(ad.getCategory()))) + 1;
		int oldPurchaseSize = self.size(PURCHASE, EnumSet.of(ad.getCategory()));
		// old in:@ensures vendor.size(OPEN, EnumSet.of(ad.getCategory())) ==
		// \old(vendor.size(OPEN, EnumSet.of(ad.getCategory()))) - 1;
		int oldVendorOpenSize = vendor.size(OPEN, EnumSet.of(ad.getCategory()));
		// old in:@ensures vendor.size(CLOSED, EnumSet.of(ad.getCategory())) ==
		// \old(vendor.size(CLOSED, EnumSet.of(ad.getCategory()))) + 1;
		int oldVendorClosedSize = vendor.size(CLOSED, EnumSet.of(ad.getCategory()));
		// old in:@ensures getAvailableCash() == \old(getAvailableCash()) -
		// ad.getPrice();
		int oldCash = self.getAvailableCash();
		// old in:@ensures vendor.getAvailableCash() == \old(vendor.getAvailableCash())
		// + ad.getPrice();
		int oldVendorCash = vendor.getAvailableCash();

		// Exécution:
		self.buy(vendor, ad);

		// Post-conditions:
		// @ensures containsInState(PURCHASE, ad);
		assertTrue(self.containsInState(PURCHASE, ad));
		// @ensures size(PURCHASE, EnumSet.of(ad.getCategory())) == \old(size(PURCHASE,
		// EnumSet.of(ad.getCategory()))) + 1;
		assertEquals(oldPurchaseSize + 1, self.size(PURCHASE, EnumSet.of(ad.getCategory())));
		// @ensures !vendor.containsInState(OPEN, ad);
		assertFalse(vendor.containsInState(OPEN, ad));
		// @ensures vendor.size(OPEN, EnumSet.of(ad.getCategory())) ==
		// \old(vendor.size(OPEN, EnumSet.of(ad.getCategory()))) - 1;
		assertEquals(oldVendorOpenSize - 1, vendor.size(OPEN, EnumSet.of(ad.getCategory())));
		// @ensures vendor.containsInState(CLOSED, ad);
		assertTrue(vendor.containsInState(CLOSED, ad));
		// @ensures vendor.size(CLOSED, EnumSet.of(ad.getCategory())) ==
		// \old(vendor.size(CLOSED, EnumSet.of(ad.getCategory()))) + 1;
		assertEquals(oldVendorClosedSize + 1, vendor.size(CLOSED, EnumSet.of(ad.getCategory())));
		// @ensures getAvailableCash() == \old(getAvailableCash()) - ad.getPrice();
		assertEquals(oldCash - ad.getPrice(), self.getAvailableCash());
		// @ensures vendor.getAvailableCash() == \old(vendor.getAvailableCash()) +
		// ad.getPrice();
		assertEquals(oldVendorCash + ad.getPrice(), vendor.getAvailableCash());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method add
	 *
	 * Crée et renvoie une nouvelle instance de ClassifiedAd dont les
	 * caractéristiques sont les arguments spécifiés et l'ajoute à la liste des
	 * annonces ouvertes (open ads) de cet utilisateur. La date de cette annonce est
	 * la date d'exécution de cette méthode.
	 * 
	 * @throws NullPointerException     si la catégorie ou la description sont null
	 * @throws IllegalArgumentException si le prix spécifié est inférieur ou égal à
	 *                                  0 ou la description de l'objet ne contient
	 *                                  pas de caractères
	 */
	@ParameterizedTest
	@MethodSource("userAndCatAndNameAndPriceProvider")
	public void testadd(User self, AdCategory cat, String msg, int price) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires cat != null;
		// @requires msg != null;
		// @requires !msg.isBlank()
		// @requires price > 0;
		if (cat == null || msg == null || price <= 0 || msg.isBlank()) {
			Throwable e = assertThrows(Throwable.class, () -> self.add(cat, msg, price));
			if (e instanceof NullPointerException) {
				assertTrue(cat == null || msg == null);
				return;
			}
			if (e instanceof IllegalArgumentException) {
				assertTrue(price <= 0 || msg.isBlank());
				return;
			}
			fail("Exception inattendue");
			return;
		}

		// Oldies:
		// old in:@ensures size(OPEN, EnumSet.of(cat)) == \old(size(OPEN,
		// EnumSet.of(cat))) + 1;
		int oldOpenSize = self.size(OPEN, EnumSet.of(cat));
		Instant oldNow = Instant.now();

		// Exécution:
		ClassifiedAd result = self.add(cat, msg, price);

		// Post-conditions:
		// @ensures get(OPEN, EnumSet.of(cat), 0).equals(\result);
		assertEquals(result, self.get(OPEN, EnumSet.of(cat), 0));
		// @ensures \result.getCategory().equals(cat);
		assertEquals(cat, result.getCategory());
		// @ensures \result.getDescription().equals(msg);
		assertEquals(msg, result.getDescription());
		// @ensures \result.getPrice() == price;
		assertEquals(price, result.getPrice());
		// @ensures size(OPEN, EnumSet.of(cat)) == \old(size(OPEN, EnumSet.of(cat))) +
		// 1;
		assertEquals(oldOpenSize + 1, self.size(OPEN, EnumSet.of(cat)));
		// @ensures oldDate.isBefore(\result.getDate());
		assertTrue(oldNow.isBefore(result.getDate()));
		// @ensures \result.getDate().isBefore(Instant.now());
		assertTrue(result.getDate().isBefore(Instant.now()));

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method size
	 *
	 * Renvoie le nombre d'annonces de cet utilisateur dans l'état sélectionné
	 * (open, closed ou purchase) et appartenant à une des catégories sélectionnées
	 * seules les annonces de cette catégorie sont comptées.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testsize(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.size();

		// Post-conditions:
		// @ensures \result == size(getSelectedAdState(), getSelectedCategories());
		assertEquals(self.size(self.getSelectedAdState(), self.getSelectedCategories()), result);
		// @ensures getSelectedCategories().isEmpty() ==> \result == 0;
		if (self.getSelectedCategories().isEmpty()) {
			assertTrue(result == 0);
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method size
	 *
	 * Renvoie le nombre d'annonces de cet utilisateur dans l'état spécifié et
	 * appartenant à une des catégories appartenant à l'ensemble spécifié.
	 * 
	 * @throws NullPointerException si l'un des paramètres est null
	 */
	@ParameterizedTest
	@MethodSource("userAndStateAndCatSetProvider")
	public void testsize(User self, AdState state, Set<AdCategory> catSet) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		// @requires catSet != null;
		if (state == null || catSet == null || catSet.contains(null)) {
			assertThrows(NullPointerException.class, () -> self.size(state, catSet));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.size(state, catSet);

		// Post-conditions:
		// @ensures \result >= 0;
		assertTrue(result >= 0);
		// @ensures getSelectedAdState().equals(state) &&
		// getSelectedCategories().equals(catSet) ==> \result == size();
		if (self.getSelectedAdState().equals(state) && self.getSelectedCategories().equals(catSet)) {
			assertEquals(self.size(), result);
		}
		// @ensures catSet.isEmpty() ==> \result == 0;
		if (catSet.isEmpty()) {
			assertTrue(result == 0);
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie la ième plus récente annonce de ce User dans l'état sélectionné et
	 * dont la catégorie appartient à l'ensemble de catégories sélectionnées.
	 * 
	 * @throws IndexOutOfBoundsException si l'index spécifié est strictement
	 *                                   inférieur à 0 ou supérieur ou égal à size()
	 */
	@ParameterizedTest
	@MethodSource("userAndIntProvider")
	public void testget(User self, int i) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires i >= 0 && i < size();
		if (i < 0 || i >= self.size()) {
			assertThrows(IndexOutOfBoundsException.class, () -> self.get(i));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.get(i);

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.equals(get(getSelectedAdState(), getSelectedCategories(),
		// i);
		assertEquals(self.get(self.getSelectedAdState(), self.getSelectedCategories(), i), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie le ième élément de la liste des annonces étant dans l'état spécifié
	 * et dont la catégorie appartient à l'ensemble de catégories spécifié.
	 * 
	 * @throws NullPointerException      si l'état spécifié ou l'ensemble de
	 *                                   catégories spécifié est null
	 * @throws IndexOutOfBoundsException si l'index spécifié est strictement
	 *                                   inférieur à 0 ou supérieur ou égal à
	 *                                   size(state, catSet)
	 */
	@ParameterizedTest
	@MethodSource("userAndStateAndCatSetAndIntProvider")
	public void testget(User self, AdState state, Set<AdCategory> catSet, int i) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		// @requires catSet != null;
		// @requires i >= 0 && i < size(state, catSet);
		if (state == null || catSet == null || catSet.contains(null) || i < 0 || i >= self.size(state, catSet)) {
			Throwable e = assertThrows(Throwable.class, () -> self.get(state, catSet, i));
			if (e instanceof NullPointerException) {
				assertTrue(state == null || catSet == null || catSet.contains(null));
				return;
			}
			if (e instanceof IndexOutOfBoundsException) {
				assertTrue(i < 0 || i >= self.size(state, catSet));
				return;
			}
			fail("Exception inattendue");
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.get(state, catSet, i);

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures containsInState(state, \result);
		assertTrue(self.containsInState(state, result));
		// @ensures catSet.contains(\result.getCategory());
		assertTrue(catSet.contains(result.getCategory()));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method containsInState
	 *
	 * Renvoie true si ce User possède parmi les annonces dans l'état spécifié,
	 * l'objet spécifié.
	 */
	@ParameterizedTest
	@MethodSource("userAndStateAndAdProvider")
	public void testcontainsInState(User self, AdState state, Object obj) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		if (state == null) {
			assertThrows(NullPointerException.class, () -> self.containsInState(state, obj));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.containsInState(state, obj);

		// Post-conditions:
		// @ensures !(obj instanceof ClassifiedAd) ==> !\result;
		if (!(obj instanceof ClassifiedAd)) {
			assertFalse(result);
		}
		// @ensures \result <==> (\exists int i; i >= 0 && i < size(state,
		// EnumSet.allOf(AdCategory.class)); get(state, EnumSet.allOf(AdCategory.class),
		// i).equals(obj));
		boolean indexFound = false;
		for (int i = 0; i < self.size(state, EnumSet.allOf(AdCategory.class)) && !indexFound; i++) {
			indexFound = self.get(state, EnumSet.allOf(AdCategory.class), i).equals(obj);
		}
		assertEquals(indexFound, result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method iterator
	 *
	 * Renvoie un itérateur sur les annonces dans l'état sélectionné et appartenant
	 * à une des catégories sélectionnées.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testiterator(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ListIterator<ClassifiedAd> result = self.iterator();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures size() > 0 ==> \result.hasNext();
		if (self.size() > 0) {
			assertTrue(result.hasNext());
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method iterator
	 *
	 * Renvoie un itérateur sur les annonces dans l'état spécifié et dont la
	 * catégories appartient à l'ensemble spécifié.
	 * 
	 * @throws NullPointerException si l'état spécifié ou l'ensemble de catégories
	 *                              spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("userAndStateAndCatSetProvider")
	public void testiterator(User self, AdState state, Set<AdCategory> catSet) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		// @requires catSet != null && !catSet.contains(null);
		if (state == null || catSet == null || catSet.contains(null)) {
			assertThrows(NullPointerException.class, () -> self.iterator(state, catSet));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ListIterator<ClassifiedAd> result = self.iterator(state, catSet);

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures size(state, catSet) > 0 ==> \result.hasNext();
		if (self.size(state, catSet) > 0) {
			assertTrue(result.hasNext());
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method toString
	 *
	 * Renvoie une chaîne de caractères contenant le nom de ce User ainsi que le
	 * nombre d'annonces de cet utilisateur dans les trois états possibles (OPEN,
	 * CLOSED, PURCHASE).
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testtoString(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.toString();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.contains(getName());
		assertTrue(result.contains(self.getName()));
		// @ensures \result.contains("" + size(OPEN, EnumSet.allOf(AdCategory.class)));
		assertTrue(result.contains("" + self.size(OPEN, EnumSet.allOf(AdCategory.class))));
		// @ensures \result.contains("" + size(CLOSED,
		// EnumSet.allOf(AdCategory.class)));
		assertTrue(result.contains("" + self.size(CLOSED, EnumSet.allOf(AdCategory.class))));
		// @ensures \result.contains("" + size(PURCHASE,
		// EnumSet.allOf(AdCategory.class)));
		assertTrue(result.contains("" + self.size(PURCHASE, EnumSet.allOf(AdCategory.class))));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for User
