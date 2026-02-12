package minebay.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebay.ClassifiedAd;
import minebay.FusionSortedIterator;
import static minebay.test.DataProvider.LG_STREAM;
import static minebay.test.DataProvider.listOfListIterSupplier;
import static minebay.test.DataProvider.listOfListIterSupplierWithNull;
import static minebay.test.DataProvider.listOfListIterSupplierWithCmp;
import static minebay.test.DataProvider.adSupplier;
import static minebay.test.DataProvider.randInt;
import test.util.model.ListIterObserver;
import test.util.model.ListIterObserverAdapter;

/**
 * Test class for FusionSortedIterator.
 *
 * Un ListIterator fusionnant plusieurs ListIterator ordonnés en interdisant les
 * opérations de modification add et set.
 *
 * Un FusionSortedIterator garantie que, si les ListIterator fusionnés sont
 * ordonnés, alors ce FusionSortedIterator sera également ordonné.
 *
 * Par défaut, l'ordre considéré est l'ordre naturel entre les éléments,
 * cependant un ordre alternatif peut-être spécifié à la création de l'instance.
 */
public class FusionSortedIteratorTest {

	public static Stream<FusionSortedIterator<ClassifiedAd>> fusionSortedIteratorProvider() {
		return Stream.generate(() -> fusionSortedIteratorSupplier()).limit(LG_STREAM);
	}
	
	public static FusionSortedIterator<ClassifiedAd> fusionSortedIteratorSupplier() {
		FusionSortedIterator<ClassifiedAd> iter = new FusionSortedIterator<ClassifiedAd>(listOfListIterSupplier());
		int nbMove = randInt(5);
		while (nbMove > 0 && iter.hasNext()) {
			iter.next();
			nbMove--;
		}
		if (iter.hasPrevious()) {
			iter.previous();
		}
		return iter;
	}
	
	public static Stream<? extends Collection<? extends ListIterator<ClassifiedAd>>> listOfListIterProvider() {
		return Stream.generate(() -> listOfListIterSupplierWithNull()).limit(LG_STREAM);
	}
	public static Stream<Arguments> listOfListIterProviderWithComp() {
		return Stream.generate(() -> Arguments.of(listOfListIterSupplierWithCmp(), PriceComparator.getInstance())).limit(LG_STREAM);
	}

	// State of a FusionSortedIterator<ClassifiedAd>:
	private int previousIndex, nextIndex, lastIndex;
	private boolean hasNext, hasPrevious;
	private ClassifiedAd nextElt, prevElt;
	private List<ClassifiedAd> content;
	private ListIterObserver<ClassifiedAd> iterModel;
	private Comparator<? super ClassifiedAd> comparator;

	private void setModel(FusionSortedIterator<ClassifiedAd> self) {
		iterModel = new ListIterObserverAdapter<ClassifiedAd>(self);
	}

	private void saveState(FusionSortedIterator<ClassifiedAd> self) {
		// Put here the code to save the state of self:
		content = iterModel.toList();
		this.previousIndex = self.previousIndex();
		this.nextIndex = self.nextIndex();
		this.lastIndex = self.lastIndex();
		this.hasNext = self.hasNext();
		this.hasPrevious = self.hasPrevious();
		if (self.hasNext()) {
			this.nextElt = self.getNext();
		} else {
			this.nextElt = null;
		}
		if (self.hasPrevious()) {
			this.prevElt = self.getPrevious();
		} else {
			this.prevElt = null;
		}
		comparator = self.comparator();
	}

	private void assertPurity(FusionSortedIterator<ClassifiedAd> self) {
		// Put here the code to check purity for self:
		assertEquals(content, iterModel.toList());
		assertEquals(this.previousIndex, self.previousIndex());
		assertEquals(this.nextIndex, self.nextIndex());
		assertEquals(this.lastIndex, self.lastIndex());
		assertEquals(this.hasNext, self.hasNext());
		assertEquals(this.hasPrevious, self.hasPrevious());
		if (self.hasNext()) {
			assertEquals(this.nextElt, self.getNext());
		} else {
			assertNull(this.nextElt);
		}
		if (self.hasPrevious()) {
			assertEquals(this.prevElt, self.getPrevious());
		} else {
			assertNull(this.prevElt);
		}
		assertEquals(comparator, self.comparator());
	}

	public void assertInvariant(FusionSortedIterator<ClassifiedAd> self) {
		// Put here the code to check the invariant:
		// @invariant nextIndex() == previousIndex() + 1;
		assertEquals(self.nextIndex(), self.previousIndex() + 1);
		// @invariant previousIndex() >= -1 && previousIndex() < iterModel.size());
		assertTrue(self.previousIndex() >= -1);
		assertTrue(self.previousIndex() < iterModel.size());
		// @invariant nextIndex() >= 0 && nextIndex() <= iterModel.size());
		assertTrue(self.nextIndex() >= 0);
		assertTrue(self.nextIndex() <= iterModel.size());
		// @invariant !hasPrevious() <==> previousIndex() == -1;
		assertEquals(!self.hasPrevious(), self.previousIndex() == -1);
		// @invariant !hasNext()() <==> nextIndex() == iterModel.size();
		assertEquals(!self.hasNext(), self.nextIndex() == iterModel.size());
		// @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex() ||
		// lastIndex() == -1;
		assertTrue(self.lastIndex() == self.nextIndex() || self.lastIndex() == self.previousIndex()
				|| self.lastIndex() == -1);
		// @invariant lastIndex() >= -1 && lastIndex() < iterModel.size());
		assertTrue(self.lastIndex() >= -1 && self.lastIndex() < iterModel.size());
		// @invariant !iterModel.contains(null);
		// assertFalse(iterModel.contains​(null));
		// @invariant comparator() != null;
		assertNotNull(self.comparator());
		// @invariant iterModel.isSorted(comparator());
		assertTrue(iterModel.isSorted(self.comparator()));
	}
	public void assertInvariantWithoutModel(FusionSortedIterator<ClassifiedAd> self) {
		// Put here the code to check the invariant:
		// @invariant nextIndex() == previousIndex() + 1;
		assertEquals(self.nextIndex(), self.previousIndex() + 1);
		// @invariant previousIndex() >= -1 && previousIndex() < iterModel.size());
		assertTrue(self.previousIndex() >= -1);
		//assertTrue(self.previousIndex() < iterModel.size());
		// @invariant nextIndex() >= 0 && nextIndex() <= iterModel.size());
		assertTrue(self.nextIndex() >= 0);
		//assertTrue(self.nextIndex() <= iterModel.size());
		// @invariant !hasPrevious() <==> previousIndex() == -1;
		assertEquals(!self.hasPrevious(), self.previousIndex() == -1);
		// @invariant !hasNext()() <==> nextIndex() == iterModel.size();
		//assertEquals(!self.hasNext(), self.nextIndex() == iterModel.size());
		// @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex() ||
		// lastIndex() == -1;
		assertTrue(self.lastIndex() == self.nextIndex() || self.lastIndex() == self.previousIndex()
				|| self.lastIndex() == -1);
		// @invariant lastIndex() >= -1 && lastIndex() < iterModel.size());
		//assertTrue(self.lastIndex() >= -1 && self.lastIndex() < iterModel.size());
		// @invariant !iterModel.contains(null);
		// assertFalse(iterModel.contains​(null));
		// @invariant comparator() != null;
		assertNotNull(self.comparator());
		// @invariant iterModel.isSorted(comparator());
		//assertTrue(iterModel.isSorted(self.comparator()));
	}
	/**
	 * Test method for constructor FusionSortedIterator
	 *
	 * Initialise une instance permettant d'itérer selon l'ordre "naturel" sur tous
	 * les éléments des ListIterator de la collection spécifiée. Il s'agit donc
	 * d'une fusion de tous les ListIterator contenus dans la collection spécifiée.
	 * Les ListIterator spécifiés sont supposés ordonnés selon l'ordre "naturel" de
	 * leurs éléments.
	 * 
	 * @throws NullPointerException si l'ensemble spécifié est null ou contient null
	 */
	@ParameterizedTest
	@MethodSource("listOfListIterProvider")
	public void testFusionSortedIterator(Collection<? extends ListIterator<ClassifiedAd>> iters) {

		// Pré-conditions:
		// @requires iters != null && !iters.contains(null);
		if (iters == null || iters.contains(null)) {
			assertThrows(NullPointerException.class, () -> new FusionSortedIterator<ClassifiedAd>(iters));
			return;
		}

		// Oldies:

		// Exécution:
		FusionSortedIterator<ClassifiedAd> result = new FusionSortedIterator<ClassifiedAd>(iters);

		// Post-conditions:
		setModel(result);
		// @ensures lastIndex() == -1;
		assertEquals(-1, result.lastIndex());
		// @ensures (\forall ListIterator<E> iter; iters.contains(iter);
		// iterModel.containsAll(toList(iter)));
		for (ListIterator<ClassifiedAd> iter : iters) {
			assertTrue(iterModel.containsAll(ListIterObserverAdapter.toList(iter)));
		}
		// @ensures iterModel.size() == (\sum ListIterator<E> iter;
		// iters.contains(iter); size(iter));
		int sum = 0;
		for (ListIterator<ClassifiedAd> iter : iters) {
			sum += ListIterObserverAdapter.size(iter);
		}
		assertEquals(sum, iterModel.size());
		// @ensures (\forall E p; iterModel.contains(p); (\exists ListIterator<E> iter;
		// iters.contains(iter); contains(iter, p)));
		List<ClassifiedAd> list = iterModel.toList();
		for (ClassifiedAd ad : list) {
			boolean iterExists = false;
			Iterator<? extends ListIterator<ClassifiedAd>> liter = iters.iterator();
			while (liter.hasNext() && !iterExists) {
				iterExists = ListIterObserverAdapter.contains(liter.next(), ad);
			}
			assertTrue(iterExists);
		}
		// @ensures !hasPrevious();
		assertFalse(result.hasPrevious());
		// @ensures comparator() != null;
		assertNotNull(result.comparator());

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for constructor FusionSortedIterator
	 *
	 * Initialise une instance permettant d'itérer sur tous les éléments des
	 * ListIterator de la collection spécifiée selon l'ordre spécifié. Il s'agit
	 * donc d'une fusion de tous les ListIterator contenus dans la collection
	 * spécifiée. les ListIterator contenus dans la collection spécifiée sont
	 * supposés ordonnés selon l'ordre induit par le Comparator spécifié.
	 * 
	 * @throws NullPointerException si l'ensemble spécifié est null ou contient
	 *                              null, ou si le Comparator spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("listOfListIterProviderWithComp")
	public void testFusionSortedIterator(Collection<? extends ListIterator<ClassifiedAd>> iters,
			Comparator<? super ClassifiedAd> comparator) {

		// Pré-conditions:
		// @requires iters != null && !iters.contains(null);
		// @requires comparator != null;
		if (iters == null || iters.contains(null) || comparator == null) {
			assertThrows(NullPointerException.class, () -> new FusionSortedIterator<ClassifiedAd>(iters, comparator));
			return;
		}

		// Oldies:

		// Exécution:
		FusionSortedIterator<ClassifiedAd> result = new FusionSortedIterator<ClassifiedAd>(iters, comparator);

		// Post-conditions:
		setModel(result);
		// @ensures comparator() != null;
		assertNotNull(result.comparator());
		// @ensures comparator().equals(comparator);
		assertEquals(comparator, result.comparator());
		// @ensures !hasPrevious();
		assertFalse(result.hasPrevious());
		// @ensures lastIndex() == -1;
		assertEquals(-1, result.lastIndex());

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method comparator
	 *
	 * Renvoie le comparateur selon lequel les éléments de cet itérateur sont
	 * ordonnés.
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testcomparator(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Comparator<? super ClassifiedAd> result = self.comparator();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasNext
	 *
	 * Renvoie true s'il reste un élément après dans l'itération.
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testhasNext(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasNext();

		// Post-conditions:
		// @ensures !\result <==> nextIndex() == iterModel.size();
		assertEquals(self.nextIndex() == iterModel.size(), !result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getNext
	 *
	 * Renvoie l'élèment qui sera renvoyé par le prochain appel à next().
	 * 
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément suivant
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testgetNext(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasNext();
		if (!self.hasNext()) {
			assertThrows(NoSuchElementException.class, () -> self.getNext());
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.getNext();

		// Post-conditions:
		// @ensures \result.equals(iterModel.get(nextIndex()));
		assertEquals(iterModel.get(self.nextIndex()), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method next
	 *
	 * Renvoie l'élément suivant et avance le curseur.
	 * 
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément suivant
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testnext(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasNext();
		if (!self.hasNext()) {
			assertThrows(NoSuchElementException.class, () -> self.next());
			return;
		}

		// Oldies:
		// old in:@ensures \result.equals(\old(getNext()));
		ClassifiedAd oldNext = self.getNext();
		// old in:@ensures \old(hasPrevious()) ==>
		// comparator().compare(\old(getPrevious()), \result) <= 0;
		boolean oldHasPrevious = self.hasPrevious();
		ClassifiedAd oldPrevious = null;
		if (self.hasPrevious()) {
			oldPrevious = self.getPrevious();
		}
		// old in:@ensures previousIndex() == \old(nextIndex());
		// old in:@ensures nextIndex() == \old(nextIndex() + 1);
		// old in:@ensures lastIndex() == \old(nextIndex());
		int oldNextIndex = self.nextIndex();

		// Exécution:
		ClassifiedAd result = self.next();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.equals(\old(getNext()));
		assertEquals(oldNext, result);
		// @ensures \result.equals(getPrevious());
		assertEquals(self.getPrevious(), result);
		// @ensures \result.equals(iterModel.get(previousIndex()))
		assertEquals(iterModel.get(self.previousIndex()), result);
		// @ensures \old(hasPrevious()) ==> comparator().compare(\old(getPrevious()),
		// \result) <= 0;
		if (oldHasPrevious) {
			assertTrue(self.comparator().compare(oldPrevious, result) <= 0);
		}
		// @ensures hasNext() ==> comparator().compare(\result, getNext()) <= 0;
		if (self.hasNext()) {
			assertTrue(self.comparator().compare(result, self.getNext()) <= 0);
		}
		// @ensures hasPrevious();
		assertTrue(self.hasPrevious());
		// @ensures previousIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.previousIndex());
		// @ensures nextIndex() == \old(nextIndex() + 1);
		assertEquals(oldNextIndex + 1, self.nextIndex());
		// @ensures lastIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.lastIndex());
		// @ensures lastIndex() == previousIndex();
		assertEquals(self.lastIndex(), self.previousIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasPrevious
	 *
	 * Renvoie true s'il y a un élément précédent dans l'itération.
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testhasPrevious(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:dataProvider

		// Exécution:
		boolean result = self.hasPrevious();

		// Post-conditions:
		// @ensures !\result <==> previousIndex() == -1;
		assertEquals(self.previousIndex() == -1, !result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPrevious
	 *
	 * Renvoie l'élèment qui sera renvoyé par le prochain appel à previous().
	 * 
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément précédent
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testgetPrevious(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasPrevious();
		if (!self.hasPrevious()) {
			assertThrows(NoSuchElementException.class, () -> self.getPrevious());
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.getPrevious();

		// Post-conditions:
		// @ensures \result.equals(iterModel.get(previousIndex()));
		assertEquals(iterModel.get(self.previousIndex()), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previous
	 *
	 * Renvoie l'élément précédent et recule le curseur.
	 * 
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément précédent
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testprevious(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasPrevious();
		if (!self.hasPrevious()) {
			assertThrows(NoSuchElementException.class, () -> self.previous());
			return;
		}

		// Oldies:
		// old in:@ensures \result.equals(\old(getPrevious()));
		ClassifiedAd oldPrevious = self.getPrevious();
		// old in:@ensures \result.equals(\old(iterModel.get(previousIndex())));
		ClassifiedAd oldModelPrevious = iterModel.get(self.previousIndex());
		// old in:@ensures \old(hasNext()) ==> comparator().compare(\result,
		// iterModel.get(\old(nextIndex())) <= 0;
		boolean oldHasNext = self.hasNext();
		int oldNextIndex = -1;
		if (self.hasNext()) {
			oldNextIndex = self.nextIndex();
		}
		// old in:@ensures previousIndex() == \old(previousIndex()) - 1;
		// old in:@ensures nextIndex() == \old(previousIndex());
		// old in:@ensures lastIndex() == \old(previousIndex());
		int oldPreviousIndex = self.previousIndex();

		// Exécution:
		ClassifiedAd result = self.previous();

		// Post-conditions:
		// @ensures hasNext();
		assertTrue(self.hasNext());
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.equals(\old(getPrevious()));
		assertEquals(oldPrevious, result);
		// @ensures \result.equals(getNext());
		assertEquals(self.getNext(), result);
		// @ensures \result.equals(\old(iterModel.get(previousIndex())));
		assertEquals(oldModelPrevious, result);
		// @ensures \result.equals(iterModel.get(nextIndex()));
		assertEquals(iterModel.get(self.nextIndex()), result);
		// @ensures \old(hasNext()) ==> comparator().compare(\result,
		// iterModel.get(\old(nextIndex())) <= 0;
		if (oldHasNext) {
			assertTrue(self.comparator().compare(result, iterModel.get(oldNextIndex)) <= 0);
		}
		// @ensures previousIndex() == \old(previousIndex()) - 1;
		assertEquals(oldPreviousIndex - 1, self.previousIndex());
		// @ensures nextIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.nextIndex());
		// @ensures lastIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.lastIndex());
		// @ensures lastIndex() == nextIndex();
		assertEquals(self.lastIndex(), self.nextIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method nextIndex
	 *
	 * Renvoie l'index de l'élément suivant dans l'itération. Renvoie le nombre
	 * total d'élément dans l'itération s'il n'y a pas d'élément suivant.
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testnextIndex(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.nextIndex();

		// Post-conditions:
		// @ensures hasNext() <==> \result >= 0 && \result < iterModel.size();
		assertEquals(self.hasNext(), result >= 0 && result < iterModel.size());
		// @ensures !hasNext() <==> \result == iterModel.size();
		assertEquals(!self.hasNext(), result == iterModel.size());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previousIndex
	 *
	 * Renvoie l'index de l'élément précédent dans l'itération. Renvoie -1 s'il n'y
	 * a pas d'élément précédent.
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testpreviousIndex(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.previousIndex();

		// Post-conditions:
		// @ensures hasPrevious() ==> \result >= 0;
		if (self.hasPrevious()) {
			assertTrue(result >= 0);
		}
		// @ensures !hasPrevious() <==> \result == -1;
		assertEquals(!self.hasPrevious(), result == -1);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method lastIndex
	 *
	 * Renvoie l'index de l'élément renvoyé par le dernier appel à next() ou
	 * previous(). Renvoie -1 si next() ou previous() n'ont jamais été appelés
	 * depuis la création de cet itérateur ou bien si remove a été appelée depuis le
	 * dernier appel à next ou previous.
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testlastIndex(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.lastIndex();

		// Post-conditions:
		// @ensures \result >= -1 && \result < iterModel.size();
		assertTrue(result >= -1);
		assertTrue(result < iterModel.size());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method remove
	 *
	 * Retire de l'itération le dernier élèment renvoyé par next() ou previous().
	 * L'élément retiré est l'élèment renvoyé par le dernier appel à next() ou
	 * previous(). Ne peut être appelé qu'une fois par appel à next ou previous.
	 * 
	 * @throws IllegalStateException si next ou previous n'ont jamais été appelés,
	 *                               ou bien si remove a déjà été appelé depuis le
	 *                               dernier appel a next ou remove
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testremove(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariantWithoutModel(self);

		// Pré-conditions:
		// @requires lastIndex() != -1;
		if (self.lastIndex() == -1) {
			assertThrows(IllegalStateException.class, () -> self.remove());
			return;
		}

		// Oldies:
		// old in:@ensures iterModel.size() == \old(iterModel.size()) - 1;
		//int oldModelSize = iterModel.size();
		// old in:@ensures (* if last move is previous: *) \old(lastIndex() ==
		// nextIndex()) ==> (previousIndex() == \old(previousIndex()));
		// old in:@ensures (* if last move is next: *) \old(lastIndex() ==
		// previousIndex()) ==> (previousIndex() == \old(previousIndex()) - 1);
		boolean lastMoveIsPrevious = self.lastIndex() == self.nextIndex();
		boolean lastMoveIsNext = self.lastIndex() == self.previousIndex();
		int oldPreviousIndex = self.previousIndex();

		// Exécution:
		self.remove();

		// Post-conditions:
		// @ensures lastIndex() == -1;
		assertTrue(self.lastIndex() == -1);
		// @ensures iterModel.size() == \old(iterModel.size()) - 1;
		//assertEquals(oldModelSize - 1, iterModel.size());
		// @ensures (* if last move is previous: *) \old(lastIndex() == nextIndex()) ==>
		// (previousIndex() == \old(previousIndex()));
		if (lastMoveIsPrevious) {
			assertEquals(oldPreviousIndex, self.previousIndex());
		}
		// @ensures (* if last move is next: *) \old(lastIndex() == previousIndex()) ==>
		// (previousIndex() == \old(previousIndex()) - 1);
		if (lastMoveIsNext) {
			assertEquals(oldPreviousIndex - 1, self.previousIndex());
		}

		// Invariant:
		assertInvariantWithoutModel(self);
	}

	/**
	 * Test method for method set
	 *
	 * Opération non supportée.
	 * 
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testset(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);
		ClassifiedAd e = adSupplier();
		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		assertThrows(UnsupportedOperationException.class, () -> self.set(e));

		// Post-conditions:

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method add
	 *
	 * Opération non supportée.
	 * 
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("fusionSortedIteratorProvider")
	public void testadd(FusionSortedIterator<ClassifiedAd> self) {
		assumeTrue(self != null);
		setModel(self);
		ClassifiedAd e = adSupplier();

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		assertThrows(UnsupportedOperationException.class, () -> self.add(e));

		// Post-conditions:

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for FusionSortedIterator
