package minebay.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebay.AdCategory;
import minebay.ClassifiedAd;
import minebay.MultiEnumList;
import test.util.model.ListIterObserverAdapter;
import minebay.Categorized;
import static minebay.test.DataProvider.LG_STREAM;
import static minebay.test.DataProvider.BAD_ARG_PROBA;
import static minebay.test.DataProvider.MAX_LIST_SIZE;
import static minebay.test.DataProvider.randomCollection;
import static minebay.test.DataProvider.randBool;
import static minebay.test.DataProvider.randInt;
import static minebay.test.DataProvider.adSupplier;
import static minebay.test.DataProvider.catSetSupplier;

/**
 * Test class for MultiEnumList.
 *
 * Collection ordonnée, dont les éléments sont instances de classes implémentant
 * les interfaces Categorized et Comparable. Une MultiEnumList ne peut pas
 * contenir null.
 * 
 * <p>
 * Une MultiEnumList prend en compte les catégories de ses éléments en
 * définissant pour certaines méthodes des versions alternatives opérant
 * uniquement sur un ensemble de catégories donné en argument de ces méthodes.
 * Cependant les méthodes héritées de l'interface Collection s'appliquent
 * toujours sur l'ensemble des éléments quelque soit leur catégorie.
 * </p>
 * 
 * <p>
 * Dans ce but, MultiEnumList utilise une liste distincte (une LinkedList) pour
 * chaque catégorie. Les opérations utilisant un index (i.e. les deux méthodes
 * get), parcourent la liste depuis le début à l'aide d'un itérateur (renvoyé
 * par la méthode iterator) jusqu'à atteindre l'élèment à cet index, en
 * conséquence, pour des raisons d'efficacité, l'usage d'un itérateur doit être
 * préféré à chaque fois qu'un parcours de la liste doit être effecué. Les
 * méthodes iterator et listIterator construisent l'itérateur renvoyé en
 * fusionnant les itérateurs des listes des catégories sélectionnées à l'aide
 * d'un FusionSortedIterator.
 * </p>
 */
public class MultiEnumListTest extends TestCollectionContract {
	static {
		IMMUTABLE = false;
		NULL_SUPPORTED = false;
		ADD_SUPPORTED = true;
		REMOVE_SUPPORTED = true;
	}

	public static Stream<MultiEnumList<AdCategory, ClassifiedAd>> multiEnumListProvider() {
		return Stream.generate(() -> multiEnumListSupplier()).limit(LG_STREAM);
	}

	public static Stream<Arguments> classAndCollProvider() {
		return Stream.generate(() -> Arguments.of(randBool(BAD_ARG_PROBA) ? null : AdCategory.class, adCollSupplier()))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> meListAndCatSetProvider() {
		return Stream.generate(() -> Arguments.of(multiEnumListSupplier(), catSetSupplier(AdCategory.class)))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> meListAndCatSetAndIntProvider() {
		return Stream.generate(() -> Arguments.of(multiEnumListSupplier(), catSetSupplier(AdCategory.class),
				randInt((MAX_LIST_SIZE / 2) + 1) - 3)).limit(LG_STREAM);
	}

	public static Stream<Arguments> meListAndIntProvider() {
		return Stream.generate(() -> Arguments.of(multiEnumListSupplier(), randInt((MAX_LIST_SIZE / 2) + 1) - 3))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> meListAndAdProvider() {
		return Stream
				.generate(() -> Arguments.of(multiEnumListSupplier(), randBool(BAD_ARG_PROBA) ? null : adSupplier()))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> meListAndAdColProvider() {
		return Stream.generate(() -> meListAndAdColSupplier()).limit(LG_STREAM);
	}

	public static MultiEnumList<AdCategory, ClassifiedAd> multiEnumListSupplier() {
		return new MultiEnumList<>(AdCategory.class, randomCollection(MAX_LIST_SIZE, () -> adSupplier()));
	}

	public static Collection<ClassifiedAd> adCollSupplier() {
		Collection<ClassifiedAd> c = randomCollection(MAX_LIST_SIZE, () -> adSupplier());
		if (randBool(BAD_ARG_PROBA)) {
			if (randBool()) {
				return null;
			}
			c.add(null);
		}
		return c;
	}

	public static Arguments meListAndAdColSupplier() {
		MultiEnumList<AdCategory, ClassifiedAd> first = multiEnumListSupplier();
		Collection<ClassifiedAd> second = null;
		if (randBool()) {
			return Arguments.of(first, adCollSupplier());
		}
		if (randBool()) {
			second = new MultiEnumList<>(AdCategory.class, first);
			if (randBool()) {
				second.add(adSupplier());
			}
		}
		return Arguments.of(first, second);
	}

	private List<?> state;

	@Override
	public <T> void saveState(Collection<T> self) {
		super.saveState(self);
		// Put here the code to save the state of self:
		state = new LinkedList<T>(self);
	}

	@Override
	public <T> void assertPurity(Collection<T> self) {
		super.assertPurity(self);
		// Put here the code to check purity for self:
		List<T> newState = new LinkedList<T>(self);
		assertEquals(state, newState);
	}

	public <C extends Enum<C>, T extends Comparable<T> & Categorized<C>> void assertInvariant(
			MultiEnumList<C, T> self) {
		super.assertInvariant(self);
		// Put here the code to check the invariant:
		// @invariant getCatType() != null;
		assertNotNull(self.getCatType());
		// @invariant (\forall int i, j; i >= 0 && i < j && j < size();
		// get(i).compareTo(get(j)) <= 0);
		for (int i = 1; i < self.size(); i++) {
			assertTrue(self.get(i - 1).compareTo(self.get(i)) <= 0);
		}
		// @invariant (\forall Set<C> catSet;true; (\forall int i, j; i >= 0 && i < j &&
		// j < size(catSet); get(catSet, i).compareTo(get(catSet, j)) <= 0);
		int nb = 5;
		while (nb > 0) {
			Set<C> catSet = DataProvider.enumSetSupplier(self.getCatType());
			for (int i = 1; i < self.size(catSet); i++) {
				assertTrue(self.get(catSet, i - 1).compareTo(self.get(catSet, i)) <= 0);
			}
			nb--;
		}
		// @invariant !contains(null);
		assertFalse(self.contains(null));
	}

	/**
	 * Test method for constructor MultiEnumList
	 *
	 * Initialise une MultiEnumList vide dont les éléments sont catégorisés à l'aide
	 * du type spécifié.
	 * 
	 * @throws NullPointerException si l'argument spécifié est null
	 */
	// @ParameterizedTest
	// @MethodSource("dataProvider")
	public void testMultiEnumList(Class<AdCategory> catType) {

		// Pré-conditions:
		// @requires catType != null;
		if (catType == null) {
			assertThrows(NullPointerException.class, () -> new MultiEnumList<AdCategory, ClassifiedAd>(catType));
			return;
		}

		// Oldies:

		// Exécution:
		MultiEnumList<AdCategory, ClassifiedAd> result = new MultiEnumList<AdCategory, ClassifiedAd>(catType);

		// Post-conditions:
		// @ensures isEmpty();
		assertTrue(result.isEmpty());
		// @ensures getCatType().equals(catType);
		assertEquals(catType, result.getCatType());

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for constructor MultiEnumList
	 *
	 * Initialise une MultiEnumList contenant les éléments de la collection
	 * spécifiée dont les éléments sont catégorisés à l'aide du type spécifié.
	 * 
	 * @throws NullPointerException si un des arguments spécifiés est null ou si la
	 *                              collection spécifié contient null
	 */
	@ParameterizedTest
	@MethodSource("classAndCollProvider")
	public void testMultiEnumList(Class<AdCategory> catType, Collection<? extends ClassifiedAd> c) {

		// Pré-conditions:
		// @requires catType != null;
		// @requires c != null;
		// @requires !c.contains(null);
		if (catType == null || c == null || c.contains(null)) {
			assertThrows(NullPointerException.class, () -> new MultiEnumList<AdCategory, ClassifiedAd>(catType, c));
			return;
		}

		// Oldies:

		// Exécution:
		MultiEnumList<AdCategory, ClassifiedAd> result = new MultiEnumList<AdCategory, ClassifiedAd>(catType, c);

		// Post-conditions:
		// @ensures containsAll(c);
		assertTrue(result.containsAll(c));
		// @ensures size() == c.size();
		assertEquals(c.size(), result.size());
		// @ensures getCatType().equals(catType);
		assertEquals(catType, result.getCatType());

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method getCatType
	 *
	 * Renvoie le type enum catégorisant les élèments de cette collection.
	 */
	@ParameterizedTest
	@MethodSource("multiEnumListProvider")
	public void testgetCatType(MultiEnumList<AdCategory, ClassifiedAd> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Class<AdCategory> result = self.getCatType();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method size
	 *
	 * Renvoie le nombre d'élèments de cette collection appartenant à une des
	 * catégories de l'ensemble spécifié.
	 * 
	 * @throws NullPointerException si l'ensemble spécifié est null ou contient null
	 */
	@ParameterizedTest
	@MethodSource("meListAndCatSetProvider")
	public void testsize(MultiEnumList<AdCategory, ClassifiedAd> self, Set<? extends AdCategory> catSet) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires catSet != null;
		if (catSet == null || catSet.contains(null)) {
			assertThrows(NullPointerException.class, () -> self.size(catSet));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.size(catSet);

		// Post-conditions:
		// @ensures \result >= 0 && \result <= size();
		assertTrue(result >= 0);
		assertTrue(result <= self.size());
		// @ensures catSet.isEmpty() ==> \result == 0;
		if (catSet.isEmpty()) {
			assertTrue(result == 0);
		}
		// @ensures catSet.equals(EnumSet.allOf(getCatType())) ==> \result == size();
		if (catSet.equals(EnumSet.allOf(self.getCatType()))) {
			assertEquals(self.size(), result);
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method clear
	 *
	 * Retire tous les élèments de cette collection dont la catégorie appartient à
	 * une des catégories de l'ensemble spécifié. Si l'ensemble spécifié est vide
	 * cette collection n'est pas modifiée.
	 * 
	 * @throws NullPointerException si l'ensemble spécifié est null ou contient null
	 */
	@ParameterizedTest
	@MethodSource("meListAndCatSetProvider")
	public void testclear(MultiEnumList<AdCategory, ClassifiedAd> self, Set<AdCategory> catSet) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires catSet != null;
		if (catSet == null || catSet.contains(null)) {
			assertThrows(NullPointerException.class, () -> self.size(catSet));
			return;
		}

		// Oldies:
		// old in:@ensures catSet.isEmpty() ==> size() == \old(size());
		int oldSize = self.size();
		// old in:@ensures (\forall E elt; catSet.contains(elt.getCategory()) &&
		// \old(contains(elt)); !contains(elt));
		Iterator<ClassifiedAd> iter = self.listIterator(catSet);
		List<ClassifiedAd> oldContent = new LinkedList<>();
		while (iter.hasNext()) {
			oldContent.add(iter.next());
		}
		// old in:@ensures (\forall E elt; !catSet.contains(elt.getCategory()) &&
		// \old(contains(elt)); contains(elt));
		iter = self.listIterator(EnumSet.complementOf(EnumSet.copyOf(catSet)));
		List<ClassifiedAd> oldOutContent = new LinkedList<>();
		while (iter.hasNext()) {
			oldOutContent.add(iter.next());
		}

		// Exécution:
		self.clear(catSet);

		// Post-conditions:
		// @ensures size(catSet) == 0;
		assertTrue(self.size(catSet) == 0);
		// @ensures catSet.isEmpty() ==> size() == \old(size());
		if (catSet.isEmpty()) {
			assertEquals(oldSize, self.size());
		}
		// @ensures catSet.equals(EnumSet.allOf(getCatType())) ==> isEmpty();
		if (catSet.equals(EnumSet.allOf(self.getCatType()))) {
			assertTrue(self.isEmpty());
		}
		// @ensures (\forall E elt; catSet.contains(elt.getCategory()) &&
		// \old(contains(elt)); !contains(elt));
		for (ClassifiedAd ad : oldContent) {
			assertFalse(self.contains(ad));
		}
		// @ensures (\forall E elt; !catSet.contains(elt.getCategory()) &&
		// \old(contains(elt)); contains(elt));
		for (ClassifiedAd ad : oldOutContent) {
			assertTrue(self.contains(ad));
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie l'élèment situé à la position spécifiée dans cette collection.
	 * 
	 * @throws IndexOutOfBoundsException si l'index spécifié est strictement
	 *                                   inférieur à 0 ou supérieur ou égal à size()
	 */
	@ParameterizedTest
	@MethodSource("meListAndIntProvider")
	public void testget(MultiEnumList<AdCategory, ClassifiedAd> self, int i) {
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
		// @ensures contains(\result);
		assertTrue(self.contains(result));
		// @ensures (\forall int j; j >= 0 && j < i; get(j).compareTo(\result) <= 0);
		// @ensures (\forall int j; j > i && j < size(); get(j).compareTo(\result) >=
		// 0);
		int index = 0;
		for (ClassifiedAd ad : self) {
			if (index < i) {
				assertTrue(ad.compareTo(result) <= 0);
			} else if (index > i) {
				assertTrue(ad.compareTo(result) >= 0);
			}
			index++;
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie l'élèment situé à la position spécifiée parmi les élèments de cette
	 * collection dont la catégorie appartient à l'ensemble spécifié.
	 * 
	 * @throws IndexOutOfBoundsException si l'index spécifié est strictement
	 *                                   inférieur à 0 ou supérieur ou égal à size()
	 * @throws NullPointerException      si l'ensemble spécifié est null ou contient
	 *                                   null
	 */
	@ParameterizedTest
	@MethodSource("meListAndCatSetAndIntProvider")
	public void testget(MultiEnumList<AdCategory, ClassifiedAd> self, Set<? extends AdCategory> catSet, int i) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires catSet != null;
		// @requires i >= 0 && i < size(catSet);
		if (i < 0 || catSet == null || catSet.contains(null) || i >= self.size(catSet)) {
			Throwable e = assertThrows(Throwable.class, () -> self.get(catSet, i));
			if (e instanceof IndexOutOfBoundsException) {
				assertTrue(i < 0 || i >= self.size(catSet));
				return;
			}
			if (e instanceof NullPointerException) {
				assertTrue(catSet == null || catSet.contains(null));
				return;
			}
			fail("Bad exception");
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.get(catSet, i);

		// Post-conditions:
		// @ensures contains(\result);
		assertTrue(self.contains(result));
		// @ensures catSet.contains(\result.getCategory());
		assertTrue(catSet.contains(result.getCategory()));
		// @ensures (\forall int j; j >= 0 && j < i; get(catSet, j).compareTo(\result)
		// <= 0);
		// @ensures (\forall int j; j > i && j < size(catSet); get(catSet,
		// j).compareTo(\result) >= 0);
		Iterator<ClassifiedAd> iter = self.listIterator(catSet);
		int index = 0;
		while (iter.hasNext()) {
			ClassifiedAd ad = iter.next();
			if (index < i) {
				assertTrue(ad.compareTo(result) <= 0);
			} else if (index > i) {
				assertTrue(ad.compareTo(result) >= 0);
			}
			index++;
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method add
	 *
	 * Ajoute l'élèment spécifié à cette collection en préservant l'ordre des
	 * élèments.
	 * 
	 * @throws NullPointerException si l'élèment spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("meListAndAdProvider")
	public void testadd(MultiEnumList<AdCategory, ClassifiedAd> self, ClassifiedAd elt) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires elt != null;
		if (elt == null) {
			assertThrows(NullPointerException.class, () -> self.add(elt));
			return;
		}

		// Oldies:
		// old in:@ensures size() == \old(size()) + 1;
		int oldSize = self.size();
		// old in:@ensures size(EnumSet.of(elt.getCategory())) ==
		// \old(size(EnumSet.of(elt.getCategory()))) + 1;
		int oldCatSize = self.size(EnumSet.of(elt.getCategory()));

		// Exécution:
		super.testadd(self, elt);
		if (!requireOk) {
			return;
		}
		boolean result = boolResult;

		// Post-conditions:
		// @ensures \result == true;
		assertTrue(result);
		// @ensures contains(elt);
		assertTrue(self.contains(elt));
		// @ensures size() == \old(size()) + 1;
		assertEquals(oldSize + 1, self.size());
		// @ensures size(EnumSet.of(elt.getCategory())) ==
		// \old(size(EnumSet.of(elt.getCategory()))) + 1;
		assertEquals(oldCatSize + 1, self.size(EnumSet.of(elt.getCategory())));

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method listIterator
	 *
	 * Renvoie un ListIterator sur les éléments de cette collection dont la
	 * catégorie appartient à l'ensemble spécifié. Cet itérateur respect l'ordre
	 * naturel des élèments.
	 * 
	 * @throws NullPointerException si l'ensemble spécifié est null ou contient null
	 */
	@ParameterizedTest
	@MethodSource("meListAndCatSetProvider")
	public void testlistIterator(MultiEnumList<AdCategory, ClassifiedAd> self, Set<? extends AdCategory> catSet) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires catSet != null;
		if (catSet == null || catSet.contains(null)) {
			assertThrows(NullPointerException.class, () -> self.listIterator(catSet));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ListIterator<ClassifiedAd> result = self.listIterator(catSet);

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures ListIterObserverAdapter.containsAll(\result, this);
		// assertTrue(ListIterObserverAdapter.containsAll(result, self));
		// @ensures ListIterObserverAdapter.size(\result) == size(catSet);
		assertEquals(self.size(catSet), ListIterObserverAdapter.size(result));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method iterator
	 *
	 * Renvoie un ListIterator sur tous les éléments de cette collection. Cet
	 * itérateur respect l'ordre naturel des élèments.
	 */
	@ParameterizedTest
	@MethodSource("multiEnumListProvider")
	public void testiterator(MultiEnumList<AdCategory, ClassifiedAd> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		super.testiterator(self);
		@SuppressWarnings("unchecked")
		ListIterator<ClassifiedAd> result = (ListIterator<ClassifiedAd>) objResult;

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures containsAll(ListIterObserverAdapter.toList(\result));
		assertTrue(self.containsAll(ListIterObserverAdapter.toList(result)));
		// @ensures ListIterObserverAdapter.size(\result) == size();
		assertEquals(self.size(), ListIterObserverAdapter.size(result));
		// @ensures ListIterObserverAdapter.isSorted(\result);
		assertTrue(ListIterObserverAdapter.isSorted(result, (e1, e2) -> e1.compareTo(e2)));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method equals
	 *
	 * Compare l'objet spécifié avec cette collection en terme d'égalité. Renvoie
	 * true si l'objet spécifié est une MultiEnumList contenant les mêmes éléments
	 * dans le même ordre que cette collection.
	 */
	@ParameterizedTest
	@MethodSource("meListAndAdColProvider")
	public void testequals(MultiEnumList<AdCategory, ClassifiedAd> self, Object obj) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		super.testequals(self, obj);
		boolean result = boolResult;

		// Post-conditions:
		// @ensures !(obj instanceof MultiEnumList<?,?>) ==> !\result;
		if (!(obj instanceof MultiEnumList<?, ?>)) {
			assertFalse(result);
		} else {
			MultiEnumList<?, ?> l = (MultiEnumList<?, ?>) obj;
			// @ensures !getCatType().equals(((MultiEnumList<?,?>) obj).getCatType()) ==>
			// !\result;
			if (!self.getCatType().equals(l.getCatType())) {
				assertFalse(result);
			}
			if (result) {
				// @ensures \result ==> size() == ((MultiEnumList<?,?>) obj).size();
				assertEquals(self.size(), l.size());
				// @ensures \result ==> (\forall int i; i >= 0 && i < size();
				// get(i).equals(((MultiEnumList<?,?>) obj).get(i)));
				for (int i = 0; i < self.size(); i++) {
					assertEquals(self.get(i), l.get(i));
				}
			}
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hashCode
	 *
	 * Returns the hash code value for this MultiEnumList. The hash code of a
	 * MultiEnumList is defined to be the result of the following calculation:
	 * 
	 * <pre>{@code
	 * int hashCode = 1;
	 * for (E e : list)
	 * 	hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
	 * }</pre>
	 */
	@ParameterizedTest
	@MethodSource("multiEnumListProvider")
	public void testhashCode(MultiEnumList<AdCategory, ClassifiedAd> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		super.testhashCode(self);
		int result = intResult;

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method clone
	 *
	 * Renvoie un clone de cette MultiEnumList. Chacune des listes composant cette
	 * MultiEnumList est clonée.
	 */
	@ParameterizedTest
	@MethodSource("multiEnumListProvider")
	public void testclone(MultiEnumList<AdCategory, ClassifiedAd> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		MultiEnumList<AdCategory, ClassifiedAd> result = self.clone();

		// Post-conditions:
		// @ensures \result.getClass().equals(getClass());
		assertEquals(self.getClass(), result.getClass());
		// @ensures \result != this;
		assertTrue(result != self);
		// @ensures \result.equals(this);
		assertEquals(self, result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for MultiEnumList
