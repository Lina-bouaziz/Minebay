package minebay.test;

import static minebay.test.DataProvider.LG_STREAM;
import static minebay.test.DataProvider.adSupplier;
import static minebay.test.DataProvider.adSupplierWithNull;
import static minebay.test.DataProvider.enumSupplier;
import static minebay.test.DataProvider.enumSupplierWithNull;
import static minebay.test.DataProvider.stringSupplier;
import static minebay.test.DataProvider.randInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebay.AdCategory;
import minebay.ClassifiedAd;
import minebay.AdCategory;
import minebay.test.DataProvider;

/**
 * Test class for ClassifiedAd.
 *
 * Une petite annonce d'un utilisateur de l'application Minebay. Chaque annonce
 * est caractérisée par sa description, sa date de création, sa catégorie et son
 * prix.
 * 
 * Cette classe est non modifiable.
 */
public class ClassifiedAdTest {

	public static Stream<ClassifiedAd> adProvider() {
		return Stream.generate(() -> adSupplier()).limit(LG_STREAM);
	}

	public static Stream<Arguments> CatAndNameAndPrice() {
		return Stream
				.generate(() -> Arguments.of(enumSupplierWithNull(AdCategory.class), stringSupplier(), randInt(50) - 5))
				.limit(LG_STREAM);
	}

	public static Stream<String> stringProvider() {
		return Stream.generate(() -> stringSupplier()).limit(LG_STREAM);
	}

	public static Stream<Arguments> adAndadProvider() {
		return Stream.generate(() -> Arguments.of(adSupplier(), adSupplierWithNull())).limit(LG_STREAM);
	}

	private String description;
	private AdCategory cat;
	private int price;
	private Instant date;

	private void saveState(ClassifiedAd self) {
		// Put here the code to save the state of self:
		this.description = self.getDescription();
		this.cat = self.getCategory();
		this.price = self.getPrice();
		this.date = self.getDate();
	}

	private void assertPurity(ClassifiedAd self) {
		// Put here the code to check purity for self:
		assertEquals(description, self.getDescription());
		assertEquals(cat, self.getCategory());
		assertEquals(price, self.getPrice());
		assertEquals(date, self.getDate());
	}

	public void assertInvariant(ClassifiedAd self) {
		// Put here the code to check the invariant:
		// @invariant getDescription() != null;
		assertNotNull(self.getDescription());
		// @invariant !getDescription().isBlank();
		assertFalse(self.getDescription().isBlank());
		// @invariant getCategory() != null;
		assertNotNull(self.getCategory());
		// @invariant getPrice() > 0;
		assertTrue(self.getPrice() > 0);
		// @invariant getDate() != null;
		assertNotNull(self.getDate());
	}

	/**
	 * Test method for constructor ClassifiedAd
	 *
	 * Initialise une nouvelle annonce. La date de cette nouvelle annonce est la
	 * date courante au moment de l'exécution de ce constructeur.
	 * 
	 * @throws NullPointerException     si la catégorie ou la description sont null
	 * @throws IllegalArgumentException si le prix spécifié est inférieur ou égal à
	 *                                  0 ou la description de l'objet ne contient
	 *                                  pas de caractères
	 */
	@ParameterizedTest
	@MethodSource("CatAndNameAndPrice")
	public void testClassifiedAd(AdCategory cat, String desc, int price) {

		// Pré-conditions:
		// @requires cat != null;
		// @requires desc != null;
		// @requires !desc.isBlank();
		// @requires price > 0;
		if (cat == null || desc == null || desc.isBlank() || price <= 0) {
			Throwable e = assertThrows(Throwable.class, () -> new ClassifiedAd(cat, desc, price));
			if (e instanceof NullPointerException) {
				assertTrue(cat == null || desc == null);
				return;
			}
			if (e instanceof IllegalArgumentException) {
				assertTrue(desc.isBlank() || price <= 0);
				return;
			}
			fail("Exception inattendue");
		}

		// Oldies:
		// old in:@ensures \old(Instant.now()).isBefore(getDate());
		Instant oldNow = Instant.now();

		// Exécution:
		ClassifiedAd result = new ClassifiedAd(cat, desc, price);

		// Post-conditions:
		// @ensures getCategory().equals(cat);
		assertEquals(cat, result.getCategory());
		// @ensures getDescription().equals(desc);
		assertEquals(desc, result.getDescription());
		// @ensures getPrice() == price;
		assertEquals(price, result.getPrice());
		// @ensures getDate() != null;
		assertNotNull(result.getDate());
		// @ensures \old(Instant.now()).isBefore(getDate());
		assertTrue(oldNow.isBefore(result.getDate()));
		// @ensures getDate().isBefore(Instant.now());
		assertTrue(result.getDate().isBefore(Instant.now()));

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method getDate
	 *
	 * Renvoie la date de création de cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetDate(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Instant result = self.getDate();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getDescription
	 *
	 * Renvoie la description de cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetDescription(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getDescription();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getCategory
	 *
	 * Renvoie la catégorie de cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetCategory(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		AdCategory result = self.getCategory();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPrice
	 *
	 * Renvoie le prix de l'objet mis en vente par cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetPrice(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getPrice();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method isBefore
	 *
	 * Teste si cette annonce a été publiée avant l'annonce spécifiée.
	 * 
	 * @throws NullPointerException si l'annonce spécifiée est null
	 */
	@ParameterizedTest
	@MethodSource("adAndadProvider")
	public void testisBefore(ClassifiedAd self, ClassifiedAd ad) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires ad != null;
		if (ad == null) {
			assertThrows(NullPointerException.class, () -> self.isBefore(ad));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.isBefore(ad);

		// Post-conditions:
		// @ensures \result <==> this.getDate().isBefore(ad.getDate());
		assertEquals(self.getDate().isBefore(ad.getDate()), result);
		// @ensures !(\result && this.isAfter(ad));
		assertFalse(result && self.isAfter(ad));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method isAfter
	 *
	 * Teste si cette annonce a été publié après l'annonce spécifiée.
	 * 
	 * @throws NullPointerException si l'annonce spécifiée est null
	 */
	@ParameterizedTest
	@MethodSource("adAndadProvider")
	public void testisAfter(ClassifiedAd self, ClassifiedAd ad) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires ad != null;
		if (ad == null) {
			assertThrows(NullPointerException.class, () -> self.isAfter(ad));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.isAfter(ad);

		// Post-conditions:
		// @ensures \result <==> this.getDate().isAfter(ad.getDate());
		assertEquals(self.getDate().isAfter(ad.getDate()), result);
		// @ensures !(\result && this.isBefore(ad));
		assertFalse(result && self.isBefore(ad));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method compareTo
	 *
	 * Compare cette annonce avec l'annonce spécifiée selon l'ordre de leurs dates
	 * de création (de l'annonce la plus récente à l'annonce la plus ancienne).
	 * Renvoie un entier négatif si cette annonce est plus récente que l'annonce
	 * spécifiée, zéro si les deux annonces ont la même date et un entier positif si
	 * cette annonce est plus ancienne que l'annonce spécifiée.
	 * 
	 * @throws NullPointerException si l'annonce spécifiée est null
	 */
	@ParameterizedTest
	@MethodSource("adAndadProvider")
	public void testcompareTo(ClassifiedAd self, ClassifiedAd ad) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires ad != null;
		if (ad == null) {
			assertThrows(NullPointerException.class, () -> self.compareTo(ad));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.compareTo(ad);

		// Post-conditions:
		// @ensures \result < 0 <==> this.isAfter(ad);
		assertEquals(self.isAfter(ad), result < 0);
		// @ensures \result > 0 <==> this.isBefore(ad);
		assertEquals(self.isBefore(ad), result > 0);
		// @ensures \result == 0 <==> this.equals(ad);
		assertEquals(self.equals(ad), result == 0);
		// @ensures \result == -getDate().compareTo(ad.getDate());
		assertEquals(-self.getDate().compareTo(ad.getDate()), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method equals
	 *
	 * Renvoie true si l'objet spécifié est une ClassifiedAd ayant les mêmes
	 * caractéristiques que cette ClassifiedAd.
	 */
	@ParameterizedTest
	@MethodSource("adAndadProvider")
	public void testequals(ClassifiedAd self, Object obj) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.equals(obj);

		// Post-conditions:
		// @ensures !(obj instanceof ClassifiedAd) ==> !\result;
		if (!(obj instanceof ClassifiedAd)) {
			assertFalse(result);
		}
		// @ensures \result <==> (obj instanceof ClassifiedAd)
		// && getDate().equals(((ClassifiedAd) obj).getDate())
		// && getDescription().equals(((ClassifiedAd) obj).getDescription())
		// && getCategory().equals(((ClassifiedAd) obj).getCategory())
		// && getPrice() == ((ClassifiedAd) obj).getPrice());
		boolean equal = (obj instanceof ClassifiedAd) && self.getDate().equals(((ClassifiedAd) obj).getDate())
				&& self.getDescription().equals(((ClassifiedAd) obj).getDescription())
				&& self.getCategory().equals(((ClassifiedAd) obj).getCategory())
				&& self.getPrice() == ((ClassifiedAd) obj).getPrice();
		assertEquals(equal, result);
		// @ensures \result ==> hashCode() == obj.hashCode());
		if (result) {
			assertEquals(self.hashCode(), obj.hashCode());
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hashCode
	 *
	 * Renvoie un code de hashage pour cette ClassifiedAd.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testhashCode(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.hashCode();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method toString
	 *
	 * Renvoie une chaîne de caractères contenant les caractéristiques de cette
	 * ClassifiedAd.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testtoString(ClassifiedAd self) {
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
		// @ensures \result.contains(getCategory().toString());
		assertTrue(result.contains(self.getCategory().toString()));
		// @ensures \result.contains(getDate().toString());
		assertTrue(result.contains(self.getDate().toString()));
		// @ensures \result.contains(getDescription().toString());
		assertTrue(result.contains(self.getDescription().toString()));
		// @ensures \result.contains("" + getPrice());
		assertTrue(result.contains("" + self.getPrice()));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for ClassifiedAd
