package minebay.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebay.AdCategory;
import minebay.ClassifiedAd;
import minebay.MultiEnumList;
import static minebay.test.MultiEnumListTest.*;
import static minebay.test.DataProvider.*;

/**
 * Test class for Collection.
 *
 * The root interface in the <i>collection hierarchy</i>. A collection
 * represents a group of objects, known as its <i>elements</i>. Some collections
 * allow duplicate elements and others do not. Some are ordered and others
 * unordered. The JDK does not provide any <i>direct</i> implementations of this
 * interface: it provides implementations of more specific subinterfaces like
 * {@code Set} and {@code List}. This interface is typically used to pass
 * collections around and manipulate them where maximum generality is desired.
 *
 * <p>
 * <i>Bags</i> or <i>multisets</i> (unordered collections that may contain
 * duplicate elements) should implement this interface directly.
 *
 * <p>
 * All general-purpose {@code Collection} implementation classes (which
 * typically implement {@code Collection} indirectly through one of its
 * subinterfaces) should provide two "standard" constructors: a void (no
 * arguments) constructor, which creates an empty collection, and a constructor
 * with a single argument of type {@code Collection}, which creates a new
 * collection with the same elements as its argument. In effect, the latter
 * constructor allows the user to copy any collection, producing an equivalent
 * collection of the desired implementation type. There is no way to enforce
 * this convention (as interfaces cannot contain constructors) but all of the
 * general-purpose {@code Collection} implementations in the Java platform
 * libraries comply.
 *
 * <p>
 * Certain methods are specified to be <i>optional</i>. If a collection
 * implementation doesn't implement a particular operation, it should define the
 * corresponding method to throw {@code UnsupportedOperationException}. Such
 * methods are marked "optional operation" in method specifications of the
 * collections interfaces.
 *
 * <p>
 * <a id="optional-restrictions"></a>Some collection implementations have
 * restrictions on the elements that they may contain. For example, some
 * implementations prohibit null elements, and some have restrictions on the
 * types of their elements. Attempting to add an ineligible element throws an
 * unchecked exception, typically {@code NullPointerException} or
 * {@code ClassCastException}. Attempting to query the presence of an ineligible
 * element may throw an exception, or it may simply return false; some
 * implementations will exhibit the former behavior and some will exhibit the
 * latter. More generally, attempting an operation on an ineligible element
 * whose completion would not result in the insertion of an ineligible element
 * into the collection may throw an exception or it may succeed, at the option
 * of the implementation. Such exceptions are marked as "optional" in the
 * specification for this interface.
 *
 * <p>
 * It is up to each collection to determine its own synchronization policy. In
 * the absence of a stronger guarantee by the implementation, undefined behavior
 * may result from the invocation of any method on a collection that is being
 * mutated by another thread; this includes direct invocations, passing the
 * collection to a method that might perform invocations, and using an existing
 * iterator to examine the collection.
 *
 * <p>
 * Many methods in Collections Framework interfaces are defined in terms of the
 * {@link Object#equals(Object) equals} method. For example, the specification
 * for the {@link #contains(Object) contains(Object o)} method says: "returns
 * {@code true} if and only if this collection contains at least one element
 * {@code e} such that {@code (o==null ? e==null : o.equals(e))}." This
 * specification should <i>not</i> be construed to imply that invoking
 * {@code Collection.contains} with a non-null argument {@code o} will cause
 * {@code o.equals(e)} to be invoked for any element {@code e}. Implementations
 * are free to implement optimizations whereby the {@code equals} invocation is
 * avoided, for example, by first comparing the hash codes of the two elements.
 * (The {@link Object#hashCode()} specification guarantees that two objects with
 * unequal hash codes cannot be equal.) More generally, implementations of the
 * various Collections Framework interfaces are free to take advantage of the
 * specified behavior of underlying {@link Object} methods wherever the
 * implementor deems it appropriate.
 *
 * <p>
 * Some collection operations which perform recursive traversal of the
 * collection may fail with an exception for self-referential instances where
 * the collection directly or indirectly contains itself. This includes the
 * {@code clone()}, {@code equals()}, {@code hashCode()} and {@code toString()}
 * methods. Implementations may optionally handle the self-referential scenario,
 * however most current implementations do not do so.
 *
 * <h2><a id="view">View Collections</a></h2>
 *
 * <p>
 * Most collections manage storage for elements they contain. By contrast,
 * <i>view collections</i> themselves do not store elements, but instead they
 * rely on a backing collection to store the actual elements. Operations that
 * are not handled by the view collection itself are delegated to the backing
 * collection. Examples of view collections include the wrapper collections
 * returned by methods such as {@link Collections#checkedCollection
 * Collections.checkedCollection}, {@link Collections#synchronizedCollection
 * Collections.synchronizedCollection}, and
 * {@link Collections#unmodifiableCollection
 * Collections.unmodifiableCollection}. Other examples of view collections
 * include collections that provide a different representation of the same
 * elements, for example, as provided by {@link List#subList List.subList},
 * {@link NavigableSet#subSet NavigableSet.subSet}, or {@link Map#entrySet
 * Map.entrySet}. Any changes made to the backing collection are visible in the
 * view collection. Correspondingly, any changes made to the view collection
 * &mdash; if changes are permitted &mdash; are written through to the backing
 * collection. Although they technically aren't collections, instances of
 * {@link Iterator} and {@link ListIterator} can also allow modifications to be
 * written through to the backing collection, and in some cases, modifications
 * to the backing collection will be visible to the Iterator during iteration.
 *
 * <h2><a id="unmodifiable">Unmodifiable Collections</a></h2>
 *
 * <p>
 * Certain methods of this interface are considered "destructive" and are called
 * "mutator" methods in that they modify the group of objects contained within
 * the collection on which they operate. They can be specified to throw
 * {@code UnsupportedOperationException} if this collection implementation does
 * not support the operation. Such methods should (but are not required to)
 * throw an {@code UnsupportedOperationException} if the invocation would have
 * no effect on the collection. For example, consider a collection that does not
 * support the {@link #add add} operation. What will happen if the
 * {@link #addAll addAll} method is invoked on this collection, with an empty
 * collection as the argument? The addition of zero elements has no effect, so
 * it is permissible for this collection simply to do nothing and not to throw
 * an exception. However, it is recommended that such cases throw an exception
 * unconditionally, as throwing only in certain cases can lead to programming
 * errors.
 *
 * <p>
 * An <i>unmodifiable collection</i> is a collection, all of whose mutator
 * methods (as defined above) are specified to throw
 * {@code UnsupportedOperationException}. Such a collection thus cannot be
 * modified by calling any methods on it. For a collection to be properly
 * unmodifiable, any view collections derived from it must also be unmodifiable.
 * For example, if a List is unmodifiable, the List returned by
 * {@link List#subList List.subList} is also unmodifiable.
 *
 * <p>
 * An unmodifiable collection is not necessarily immutable. If the contained
 * elements are mutable, the entire collection is clearly mutable, even though
 * it might be unmodifiable. For example, consider two unmodifiable lists
 * containing mutable elements. The result of calling
 * {@code list1.equals(list2)} might differ from one call to the next if the
 * elements had been mutated, even though both lists are unmodifiable. However,
 * if an unmodifiable collection contains all immutable elements, it can be
 * considered effectively immutable.
 *
 * <h2><a id="unmodview">Unmodifiable View Collections</a></h2>
 *
 * <p>
 * An <i>unmodifiable view collection</i> is a collection that is unmodifiable
 * and that is also a view onto a backing collection. Its mutator methods throw
 * {@code UnsupportedOperationException}, as described above, while reading and
 * querying methods are delegated to the backing collection. The effect is to
 * provide read-only access to the backing collection. This is useful for a
 * component to provide users with read access to an internal collection, while
 * preventing them from modifying such collections unexpectedly. Examples of
 * unmodifiable view collections are those returned by the
 * {@link Collections#unmodifiableCollection
 * Collections.unmodifiableCollection}, {@link Collections#unmodifiableList
 * Collections.unmodifiableList}, and related methods.
 *
 * <p>
 * Note that changes to the backing collection might still be possible, and if
 * they occur, they are visible through the unmodifiable view. Thus, an
 * unmodifiable view collection is not necessarily immutable. However, if the
 * backing collection of an unmodifiable view is effectively immutable, or if
 * the only reference to the backing collection is through an unmodifiable view,
 * the view can be considered effectively immutable.
 *
 * <h2><a id="serializable">Serializability of Collections</a></h2>
 *
 * <p>
 * Serializability of collections is optional. As such, none of the collections
 * interfaces are declared to implement the {@link java.io.Serializable}
 * interface. However, serializability is regarded as being generally useful, so
 * most collection implementations are serializable.
 *
 * <p>
 * The collection implementations that are public classes (such as
 * {@code ArrayList} or {@code HashMap}) are declared to implement the
 * {@code Serializable} interface if they are in fact serializable. Some
 * collections implementations are not public classes, such as the
 * <a href="#unmodifiable">unmodifiable collections.</a> In such cases, the
 * serializability of such collections is described in the specification of the
 * method that creates them, or in some other suitable place. In cases where the
 * serializability of a collection is not specified, there is no guarantee about
 * the serializability of such collections. In particular, many
 * <a href="#view">view collections</a> are not serializable.
 *
 * <p>
 * A collection implementation that implements the {@code Serializable}
 * interface cannot be guaranteed to be serializable. The reason is that in
 * general, collections contain elements of other types, and it is not possible
 * to determine statically whether instances of some element type are actually
 * serializable. For example, consider a serializable {@code Collection<E>},
 * where {@code E} does not implement the {@code Serializable} interface. The
 * collection may be serializable, if it contains only elements of some
 * serializable subtype of {@code E}, or if it is empty. Collections are thus
 * said to be <i>conditionally serializable,</i> as the serializability of the
 * collection as a whole depends on whether the collection itself is
 * serializable and on whether all contained elements are also serializable.
 *
 * <p>
 * An additional case occurs with instances of {@link SortedSet} and
 * {@link SortedMap}. These collections can be created with a {@link Comparator}
 * that imposes an ordering on the set elements or map keys. Such a collection
 * is serializable only if the provided {@code Comparator} is also serializable.
 *
 * <p>
 * This interface is a member of the <a href="
 * {@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework"> Java
 * Collections Framework</a>.
 */
public class TestCollectionContract {
	public static boolean NULL_SUPPORTED = true;
	public static boolean IMMUTABLE = false;
	public static boolean ADD_SUPPORTED = !IMMUTABLE;
	public static boolean REMOVE_SUPPORTED = !IMMUTABLE;

	public static Stream<? extends Collection<ClassifiedAd>> instanceProvider() {
		return multiEnumListProvider();
	}

	/*
	 * public static Collection<String> getRandomCollStr() { return
	 * test.BaseDataProviders.getRandomCollString(); }
	 * 
	 * public static Stream<String> stringProvider() { return
	 * test.BaseDataProviders.stringProvider(10); }
	 * 
	 * public static String getRandomString() { return
	 * test.BaseDataProviders.getRandomElt(test.BaseDataProviders.stringInstances);
	 * }
	 */
	/*
	 * public static int getRandomInt(int min, int max) { return
	 * test.BaseDataProviders.randInt(min, max); }
	 */

	/*
	 * public static Object getRandomString(Collection<?> c) { Object result =
	 * getRandomString(); if (c != null && !c.isEmpty() && getRandomInt(0,3) == 0) {
	 * result = test.BaseDataProviders.getRandomElt(c); } return result; }
	 */
	public static Collection<ClassifiedAd> getRandomColl(Collection<ClassifiedAd> c) {
		if (c == null) {
			return adCollSupplier();
		}
		Collection<ClassifiedAd> res = null;
		switch (randInt(0, 10)) {
		case 0:
			// return c;
		case 1:
			return new HashSet<ClassifiedAd>(c);
		case 2:
			return new LinkedList<ClassifiedAd>(c);
		case 3:
			// Instance of the tested class:
			Optional<? extends Collection<ClassifiedAd>> resOpt = instanceProvider().filter(col -> col != null)
					.findAny();
			if (resOpt.isEmpty()) {
				return null;
			} else {
				return resOpt.get();
			}
		case 4:
			res = new LinkedList<ClassifiedAd>(c);
			res.add(adSupplier());
			return res;
		case 5:
			if (!c.isEmpty()) {
				res = new LinkedList<ClassifiedAd>(c);
				res.remove(getRandomElt(c));
				return res;
			}
		default:
			return adCollSupplier();
		}
	}

	public static Stream<Arguments> instanceAndStringProvider() {
		return instanceProvider().map(c -> Arguments.of(c, c.isEmpty() ? null : getRandomElt(c)));
	}

	public static Stream<Arguments> instanceAndCollProvider() {
		return instanceProvider().map(c -> Arguments.of(c, getRandomColl(c)));
	}

	public static Stream<Arguments> instanceAndTabProvider() {
		return instanceProvider().map(l -> Arguments.of(l, new ClassifiedAd[randInt(0, 5)]));
	}

	public static <T> void assertIsUnmodifiable(Collection<T> c, Supplier<? extends T> s) {
		T anyElt = s.get();
		List<T> anyList = Collections.singletonList(anyElt);
		assertThrows(UnsupportedOperationException.class, () -> c.add(anyElt));
		assertThrows(UnsupportedOperationException.class, () -> c.addAll(anyList));
		assertThrows(UnsupportedOperationException.class, () -> c.clear());

		assertThrows(UnsupportedOperationException.class, () -> c.remove(anyElt));
		assertThrows(UnsupportedOperationException.class, () -> c.removeAll(anyList));
		assertThrows(UnsupportedOperationException.class, () -> c.removeIf((e) -> true));
		assertThrows(UnsupportedOperationException.class, () -> c.retainAll(anyList));
		assertIsUnmodifiable(c.iterator());
		if (c instanceof List<?>) {
			List<T> l = (List<T>) c;
			assertThrows(UnsupportedOperationException.class, () -> l.add(0, anyElt));
			assertThrows(UnsupportedOperationException.class, () -> l.addAll(0, anyList));
			assertThrows(UnsupportedOperationException.class, () -> l.replaceAll((e) -> anyElt));
			assertIsUnmodifiable(l.listIterator());
			assertIsUnmodifiable(l.listIterator(0));
			if (!l.isEmpty()) {
				assertThrows(UnsupportedOperationException.class, () -> l.remove(0));
				assertThrows(UnsupportedOperationException.class, () -> l.set(0, anyElt));
			}
		}
	}

	public static <T> void assertIsUnmodifiable(Iterator<T> iter) {
		if (iter.hasNext()) {
			T elt = iter.next();
			assertThrows(UnsupportedOperationException.class, () -> iter.remove());
			if (iter instanceof ListIterator<?>) {
				ListIterator<T> listIter = (ListIterator<T>) iter;
				assertThrows(UnsupportedOperationException.class, () -> listIter.set(elt));
			}
		}
		if (iter instanceof ListIterator<?>) {
			ListIterator<T> listIter = (ListIterator<T>) iter;
			assertThrows(UnsupportedOperationException.class, () -> listIter.add(null));
		}
	}

	private Collection<?> state;
	public int intResult;
	public boolean boolResult;
	public Object objResult;
	public boolean requireOk;

	public <T> void saveState(Collection<T> self) {
		// Put here the code to save the state of self:
		state = new LinkedList<T>(self);
	}

	public <T> void assertPurity(Collection<T> self) {
		// Put here the code to check purity for self:
		assertEquals(state.size(), self.size());
		LinkedList<?> oldState = new LinkedList<Object>(state);
		LinkedList<T> newState = new LinkedList<T>(self);
		while (!newState.isEmpty()) {
			assertTrue(oldState.remove(newState.removeFirst()));
		}
		assertTrue(oldState.isEmpty());
	}

	public <T> void assertInvariant(Collection<T> self) {
		// Put here the code to check the invariant:
		// @invariant !NULL_SUPPORTED ==> !contains(null);
		if (!NULL_SUPPORTED) {
			assertFalse(self.contains(null));
		}
	}

	/**
	 * Test method for method size
	 *
	 * Returns the number of elements in this collection. If this collection
	 * contains more than {@code Integer.MAX_VALUE} elements, returns
	 * {@code Integer.MAX_VALUE}.
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testsize(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.size();
		intResult = result;

		// Post-conditions:
		// @ensures \result >= 0;
		assertTrue(result >= 0);
		// @ensures isEmpty() ==> \result == 0;
		if (self.isEmpty()) {
			assertTrue(result == 0);
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method isEmpty
	 *
	 * Returns {@code true} if this collection contains no elements.
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testisEmpty(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.isEmpty();
		boolResult = result;

		// Post-conditions:
		// @ensures \result <==> size() == 0;
		assertEquals(self.size() == 0, result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method contains
	 *
	 * Returns {@code true} if this collection contains the specified element. More
	 * formally, returns {@code true} if and only if this collection contains at
	 * least one element {@code e} such that {@code Objects.equals(o, e)}.
	 * 
	 * @throws ClassCastException   if the type of the specified element is
	 *                              incompatible with this collection (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this
	 *                              collection does not permit null elements
	 *                              (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	@ParameterizedTest
	@MethodSource("instanceAndStringProvider")
	public <T> void testcontains(Collection<T> self, Object o) {
		assumeTrue(self != null);
		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.contains(o);
		this.boolResult = result;

		// Post-conditions:
		// @ensures isEmpty() ==> !\result;
		if (self.isEmpty()) {
			assertFalse(result);
		}
		// @ensures o == null ==> !\result;
		if (!NULL_SUPPORTED && o == null) {
			assertFalse(result);
		}
		// @ensures \result <==> (\exists int i; i >= 0 && i < size();
		// get(i).equals(o));
		boolean found = false;
		Iterator<T> iter = self.iterator();
		while (!found && iter.hasNext()) {
			T elt = iter.next();
			if (elt == null) {
				if (o == null) {
					found = true;
				}
			} else {
				if (elt.equals(o)) {
					found = true;
				}
			}
		}
		assertEquals(found, result);

		// Assert purity:
		assertPurity(self);
		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method iterator
	 *
	 * Returns an iterator over the elements in this collection. There are no
	 * guarantees concerning the order in which the elements are returned (unless
	 * this collection is an instance of some class that provides a guarantee).
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testiterator(Collection<T> self) {
		assumeTrue(self != null);
		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Iterator<T> result = self.iterator();
		this.objResult = result;

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures isEmpty() <==> !\result.hasNext();
		assertEquals(self.isEmpty(), !result.hasNext());
		if (!self.isEmpty()) {
			assertTrue(result.hasNext());
			// assertEquals(self.getFirst(), result.next());
			if (!REMOVE_SUPPORTED) {
				assertThrows(UnsupportedOperationException.class, () -> result.remove());
			}
		}

		// Assert purity:
		assertPurity(self);
		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method toArray
	 *
	 * Returns an array containing all of the elements in this collection. If this
	 * collection makes any guarantees as to what order its elements are returned by
	 * its iterator, this method must return the elements in the same order. The
	 * returned array's {@linkplain Class#getComponentType runtime component type}
	 * is {@code Object}.
	 *
	 * <p>
	 * The returned array will be "safe" in that no references to it are maintained
	 * by this collection. (In other words, this method must allocate a new array
	 * even if this collection is backed by an array). The caller is thus free to
	 * modify the returned array.
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testtoArray(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Object[] result = self.toArray();
		objResult = result;

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.getClass().getComponentType() == Object.class;
		assertEquals(result.getClass().getComponentType(), Object.class);
		// @ensures \result.length >= size();
		assertTrue(result.length >= self.size());
		// @ensures (\forall int i; i >= 0 && i < size(); contains(\result[i]));
		for (int i = 0; i < self.size(); i++) {
			assertTrue(self.contains(result[i]));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method toArray
	 *
	 * Returns an array containing all of the elements in this collection; the
	 * runtime type of the returned array is that of the specified array. If the
	 * collection fits in the specified array, it is returned therein. Otherwise, a
	 * new array is allocated with the runtime type of the specified array and the
	 * size of this collection.
	 *
	 * <p>
	 * If this collection fits in the specified array with room to spare (i.e., the
	 * array has more elements than this collection), the element in the array
	 * immediately following the end of the collection is set to {@code null}. (This
	 * is useful in determining the length of this collection <i>only</i> if the
	 * caller knows that this collection does not contain any {@code null}
	 * elements.)
	 *
	 * <p>
	 * If this collection makes any guarantees as to what order its elements are
	 * returned by its iterator, this method must return the elements in the same
	 * order.
	 * 
	 * @throws ArrayStoreException  if the runtime type of any element in this
	 *                              collection is not assignable to the
	 *                              {@linkplain Class#getComponentType runtime
	 *                              component type} of the specified array
	 * @throws NullPointerException if the specified array is null
	 */
	@ParameterizedTest
	@MethodSource("instanceAndTabProvider")
	public <T1, T2> void testtoArray(Collection<T1> self, T2[] a) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires a != null;
		// @requires (\forall E obj; contains(obj); obj == null || obj instanceof T2);
		requireOk = true;
		boolean typeOk = true;
		if (a != null) {
			for (T1 elt : self) {
				if (elt != null && !a.getClass().getComponentType().isInstance(elt)) {
					typeOk = false;
				}
			}
		}
		if (a == null || (!typeOk)) {
			Throwable ex = assertThrows(Throwable.class, () -> self.toArray(a));
			if (ex instanceof NullPointerException) {
				assertNull(a);
			} else {
				if (ex instanceof ArrayStoreException) {
					assertFalse(typeOk);
				} else {
					fail("Exception inattendue:" + ex);
				}
			}
			requireOk = false;
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		T2[] result = self.toArray(a);
		objResult = result;

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.length >= size();
		assertTrue(result.length >= self.size());
		if (a.length < self.size()) {
			// @ensures a.length < size() ==> \result != a && \result.length == size();
			assertTrue(result != a);
			assertEquals(self.size(), result.length);
		} else {
			// @ensures a.length >= size() ==> \result == a;
			assertTrue(result == a);
		}
		// @ensures \result.getClass().getComponentType() == a.getComponentType();
		assertEquals(result.getClass().getComponentType(), a.getClass().getComponentType());
		// @ensures (\forall int i; i >= 0 && i < size(); contains(\result[i]));
		for (int i = 0; i < self.size(); i++) {
			assertTrue(self.contains(result[i]));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method toArray
	 *
	 * Returns an array containing all of the elements in this collection, using the
	 * provided {@code generator} function to allocate the returned array.
	 *
	 * <p>
	 * If this collection makes any guarantees as to what order its elements are
	 * returned by its iterator, this method must return the elements in the same
	 * order.
	 * 
	 * @throws ArrayStoreException  if the runtime type of any element in this
	 *                              collection is not assignable to the
	 *                              {@linkplain Class#getComponentType runtime
	 *                              component type} of the generated array
	 * @throws NullPointerException if the generator function is null
	 */
	// @ParameterizedTest
	@MethodSource("dataProvider")
	public <T, R> void testtoArray(Collection<R> self, IntFunction<T[]> generator) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		T[] result = self.toArray(generator);

		// Post-conditions:

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method add
	 *
	 * Ensures that this collection contains the specified element (optional
	 * operation). Returns {@code true} if this collection changed as a result of
	 * the call. (Returns {@code false} if this collection does not permit
	 * duplicates and already contains the specified element.)
	 * <p>
	 *
	 * Collections that support this operation may place limitations on what
	 * elements may be added to this collection. In particular, some collections
	 * will refuse to add {@code null} elements, and others will impose restrictions
	 * on the type of elements that may be added. Collection classes should clearly
	 * specify in their documentation any restrictions on what elements may be
	 * added.
	 * <p>
	 *
	 * If a collection refuses to add a particular element for any reason other than
	 * that it already contains the element, it <i>must</i> throw an exception
	 * (rather than returning {@code false}). This preserves the invariant that a
	 * collection always contains the specified element after this call returns.
	 * 
	 * @throws UnsupportedOperationException if the {@code add} operation is not
	 *                                       supported by this collection
	 * @throws ClassCastException            if the class of the specified element
	 *                                       prevents it from being added to this
	 *                                       collection
	 * @throws NullPointerException          if the specified element is null and
	 *                                       this collection does not permit null
	 *                                       elements
	 * @throws IllegalArgumentException      if some property of the element
	 *                                       prevents it from being added to this
	 *                                       collection
	 * @throws IllegalStateException         if the element cannot be added at this
	 *                                       time due to insertion restrictions
	 */
	@ParameterizedTest
	@MethodSource("instanceAndStringProvider")
	public <T> void testadd(Collection<T> self, T e) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires ADD_SUPPORTED;
		// @requires !NULL_SUPPORTED ==> e != null;
		requireOk = true;
		if ((!NULL_SUPPORTED && e == null) || (!ADD_SUPPORTED)) {
			Throwable ex = assertThrows(Throwable.class, () -> self.add(e));
			if (ex instanceof NullPointerException) {
				assertNull(e);
			} else {
				if (ex instanceof UnsupportedOperationException) {
					assertFalse(ADD_SUPPORTED);
				} else {
					fail("Exception inattendue" + ex);
				}
			}
			requireOk = false;
			return;
		}
		// Oldies:
		// old in:@ensures !\old(contains(e)) ==> \result;
		boolean oldContains = self.contains(e);
		// old in:@ensures \result <==> size()== \old(size()) + 1;
		int oldSize = self.size();
		// old in:@ensures \result.containsAll(\old(this));
		Collection<T> oldThis = new ArrayList<T>(self);

		// Exécution:
		boolean result = self.add(e);
		boolResult = result;

		// Post-conditions:
		// @ensures contains(e);
		assertTrue(self.contains(e));
		// @ensures !isEmpty();
		assertFalse(self.isEmpty());
		// @ensures !\old(contains(e)) ==> \result;
		if (!oldContains) {
			assertTrue(result);
		}
		// @ensures \result <==> size()== \old(size()) + 1;
		assertEquals(result, self.size() == (oldSize + 1));
		// @ensures self.containsAll(\old(this));
		assertTrue(self.containsAll(oldThis));

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method remove
	 *
	 * Removes a single instance of the specified element from this collection, if
	 * it is present (optional operation). More formally, removes an element
	 * {@code e} such that {@code Objects.equals(o, e)}, if this collection contains
	 * one or more such elements. Returns {@code true} if this collection contained
	 * the specified element (or equivalently, if this collection changed as a
	 * result of the call).
	 * 
	 * @throws ClassCastException            if the type of the specified element is
	 *                                       incompatible with this collection
	 *                                       (<a href=
	 *                                       "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException          if the specified element is null and
	 *                                       this collection does not permit null
	 *                                       elements (<a href=
	 *                                       "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws UnsupportedOperationException if the {@code remove} operation is not
	 *                                       supported by this collection
	 */
	@ParameterizedTest
	@MethodSource("instanceAndStringProvider")
	public <T> void testremove(Collection<T> self, Object o) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires REMOVE_SUPPORTED;
		requireOk = true;
		if (!REMOVE_SUPPORTED) {
			assertThrows(UnsupportedOperationException.class, () -> self.remove(o));
			requireOk = false;
			return;
		}

		// Oldies:
		// old in:@ensures \result <==> \old(contains(o));
		// old in:@ensures \result ==> size() == \old(size()) - 1;
		// old in:@ensures \result ==> Collections.frequency(this, o) ==
		// \old(Collections.frequency(this, o)) - 1;
		// old in:@ensures (\forall Object obj; \old(contains(obj)) && !obj.equals(o);
		// contains(obj));
		boolean oldContains = self.contains(o);
		int oldSize = self.size();
		int oldFrequency = Collections.frequency(self, o);
		Collection<T> oldSelf = new LinkedList<T>(self);

		// Exécution:
		boolean result = self.remove(o);
		boolResult = result;

		// Post-conditions:
		// @ensures \result <==> \old(contains(o));
		assertEquals(oldContains, result);
		if (result) {
			// @ensures \result ==> size() == \old(size()) - 1;
			assertEquals(oldSize - 1, self.size());
			// @ensures \old(contains(o)) ==> Collections.frequency(this, o) ==
			// \old(Collections.frequency(this, o)) - 1;
			assertEquals(oldFrequency - 1, Collections.frequency(self, o));
		}
		// @ensures (\forall Object obj; \old(contains(obj)) && obj != null &&
		// !obj.equals(o); contains(obj));
		for (T elt : oldSelf) {
			if ((elt != null && !elt.equals(o)) || (elt == null && elt != o)) {
				assertTrue(self.contains(elt));
			}
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method containsAll
	 *
	 * Returns {@code true} if this collection contains all of the elements in the
	 * specified collection.
	 * 
	 * @throws ClassCastException   if the types of one or more elements in the
	 *                              specified collection are incompatible with this
	 *                              collection (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified collection contains one or more
	 *                              null elements and this collection does not
	 *                              permit null elements (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>),
	 *                              or if the specified collection is null.
	 */
	@ParameterizedTest
	@MethodSource("instanceAndCollProvider")
	public <T> void testcontainsAll(Collection<T> self, Collection<?> c) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires c != null;
		requireOk = true;
		if (c == null) {
			assertThrows(NullPointerException.class, () -> self.containsAll(c));
			requireOk = false;
			return;
		}
		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.containsAll(c);
		boolResult = result;

		// Post-conditions:
		// @ensures \result <==> (\forall Object o; c.contains(o); contains(o));
		boolean allContains = true;
		Iterator<?> iter = c.iterator();
		while (iter.hasNext() && allContains) {
			if (!self.contains(iter.next())) {
				allContains = false;
			}
		}
		assertEquals(result, allContains);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method addAll
	 *
	 * Adds all of the elements in the specified collection to this collection
	 * (optional operation). The behavior of this operation is undefined if the
	 * specified collection is modified while the operation is in progress. (This
	 * implies that the behavior of this call is undefined if the specified
	 * collection is this collection, and this collection is nonempty.)
	 * 
	 * @throws UnsupportedOperationException if the {@code addAll} operation is not
	 *                                       supported by this collection
	 * @throws ClassCastException            if the class of an element of the
	 *                                       specified collection prevents it from
	 *                                       being added to this collection
	 * @throws NullPointerException          if the specified collection contains a
	 *                                       null element and this collection does
	 *                                       not permit null elements, or if the
	 *                                       specified collection is null
	 * @throws IllegalArgumentException      if some property of an element of the
	 *                                       specified collection prevents it from
	 *                                       being added to this collection
	 * @throws IllegalStateException         if not all the elements can be added at
	 *                                       this time due to insertion restrictions
	 */
	@ParameterizedTest
	@MethodSource("instanceAndCollProvider")
	public <T> void testaddAll(Collection<T> self, Collection<? extends T> c) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires ADD_SUPPORTED;
		// @requires c != null;
		// @requires !NULL_SUPPORTED ==> !c.contains(null);
		requireOk = true;
		if (c == null || (!NULL_SUPPORTED && c.contains(null)) || !ADD_SUPPORTED) {
			Throwable ex = assertThrows(Throwable.class, () -> self.addAll(c));
			if (ex instanceof UnsupportedOperationException) {
				assertFalse(ADD_SUPPORTED);
			} else {
				if (ex instanceof NullPointerException) {
					assertTrue(c == null || (!NULL_SUPPORTED && c.contains(null)));
				} else {
					fail("Exception inattendue: " + ex);
				}
			}
			requireOk = false;
			return;
		}

		// Oldies:
		// old in:@ensures size() <= \old(size()) + c.size();
		// old in:@ensures size() >= \old(size());
		// old in:@ensures \result <==> size() > \old(size());
		int oldSize = self.size();
		// old in:@ensures (\forall E obj; \old(contains(obj)); contains(obj));
		Collection<T> oldSelf = new LinkedList<T>(self);

		// Exécution:
		boolean result = self.addAll(c);
		boolResult = result;

		// Post-conditions:
		// @ensures containsAll(c);
		assertTrue(self.containsAll(c));
		// @ensures size() <= \old(size()) + c.size();
		assertTrue(self.size() <= (oldSize + c.size()));
		// @ensures size() >= \old(size());
		assertTrue(self.size() >= oldSize);
		// @ensures \result <==> size() > \old(size());
		assertEquals(result, self.size() > oldSize);
		// @ensures (\forall E obj; \old(contains(obj)); contains(obj));
		for (T elt : oldSelf) {
			assertTrue(self.contains(elt));
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method removeAll
	 *
	 * Removes all of this collection's elements that are also contained in the
	 * specified collection (optional operation). After this call returns, this
	 * collection will contain no elements in common with the specified collection.
	 * 
	 * @throws UnsupportedOperationException if the {@code removeAll} method is not
	 *                                       supported by this collection
	 * @throws ClassCastException            if the types of one or more elements in
	 *                                       this collection are incompatible with
	 *                                       the specified collection (<a href=
	 *                                       "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException          if this collection contains one or more
	 *                                       null elements and the specified
	 *                                       collection does not support null
	 *                                       elements (<a href=
	 *                                       "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>),
	 *                                       or if the specified collection is null
	 */
	@ParameterizedTest
	@MethodSource("instanceAndCollProvider")
	public <T> void testremoveAll(Collection<T> self, Collection<?> c) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires REMOVE_SUPPORTED;
		// @requires c != null;
		requireOk = true;
		if (c == null || !REMOVE_SUPPORTED) {
			Throwable ex = assertThrows(Throwable.class, () -> self.removeAll(c));
			if (ex instanceof UnsupportedOperationException) {
				assertFalse(REMOVE_SUPPORTED);
			} else {
				assertTrue(ex instanceof NullPointerException);
			}
			requireOk = false;
			return;
		}
		// Oldies:
		// old in:@ensures \result <==> size() < \old(size());
		// old in:@ensures !\result <==> size() == \old(size());
		// old in:@ensures \old(isEmpty()) || c.isEmpty() ==> !\result;
		boolean oldEmpty = self.isEmpty();
		// old in:@ensures (\forall Object o; \old(contains(o)) && !c.contains(o);
		// contains(o));
		// old in:@ensures \old(c.containsAll(this)) ==> isEmpty();
		// old in:@ensures !\result ==> this.containsAll(\old(this));
		Collection<T> oldSelf = new ArrayList<T>(self);
		// old in:@ensures size() <= \old(size());
		int oldSize = self.size();
		// old in:@ensures \old(c.containsAll(this)) ==> isEmpty();
		boolean oldCContains = c.containsAll(self);
		// old in:@ensures c.isEmpty() ==> this.equals(\old(this));

		// Exécution:
		boolean result = self.removeAll(c);
		boolResult = result;

		// Post-conditions:
		// @ensures \result <==> size() < \old(size());
		assertEquals(self.size() < oldSize, result);
		// @ensures !\result <==> size() == \old(size());
		assertEquals(self.size() == oldSize, !result);
		// @ensures (\forall Object o; c.contains(o); !contains(o));
		for (Object o : c) {
			assertFalse(self.contains(o));
		}
		for (T elt : oldSelf) {
			// @ensures (\forall Object o; \old(contains(o)) && !c.contains(o);
			// contains(o));
			if (!c.contains(elt)) {
				assertTrue(self.contains(elt));
			}
		}
		// @ensures size() <= \old(size());
		assertTrue(self.size() <= oldSize);
		// @ensures \old(c.containsAll(this)) ==> isEmpty();
		if (oldCContains) {
			assertTrue(self.isEmpty());
		}
		// @ensures \old(isEmpty()) || c.isEmpty() ==> !\result;
		if (oldEmpty || c.isEmpty()) {
			assertFalse(result);
		}
		// @ensures !\result ==> this.containsAll(\old(this));
		if (!result) {
			assertTrue(self.containsAll(oldSelf));
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method removeIf
	 *
	 * Removes all of the elements of this collection that satisfy the given
	 * predicate. Errors or runtime exceptions thrown during iteration or by the
	 * predicate are relayed to the caller.
	 * 
	 * @throws NullPointerException          if the specified filter is null
	 * @throws UnsupportedOperationException if elements cannot be removed from this
	 *                                       collection. Implementations may throw
	 *                                       this exception if a matching element
	 *                                       cannot be removed or if, in general,
	 *                                       removal is not supported.
	 */
	// @ParameterizedTest
	@MethodSource("dataProvider")
	public <T> void testremoveIf(Collection<T> self, Predicate<? super T> filter) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires REMOVE_SUPPORTED;
		// @requires filter != null;

		// Oldies:
		// old in:@ensures (\forall E obj; \old(contains(obj)) && filter.test(obj);
		// !contains(obj));
		// old in:@ensures (\forall E obj; \old(contains(obj)) && !filter.test(obj);
		// contains(obj));
		// old in:@ensures \result <==> (\exists E obj; \old(contains(obj));
		// filter.test(obj));
		// old in:@ensures \result <==> size() < \old(size());
		// old in:@ensures !\result <==> size() == \old(size());

		// Exécution:
		boolean result = self.removeIf(filter);

		// Post-conditions:
		// @ensures (\forall E obj; \old(contains(obj)) && filter.test(obj);
		// !contains(obj));
		// @ensures (\forall E obj; \old(contains(obj)) && !filter.test(obj);
		// contains(obj));
		// @ensures \result <==> (\exists E obj; \old(contains(obj)); filter.test(obj));
		// @ensures \result <==> size() < \old(size());
		// @ensures !\result <==> size() == \old(size());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method retainAll
	 *
	 * Retains only the elements in this collection that are contained in the
	 * specified collection (optional operation). In other words, removes from this
	 * collection all of its elements that are not contained in the specified
	 * collection.
	 * 
	 * @throws UnsupportedOperationException if the {@code retainAll} operation is
	 *                                       not supported by this collection
	 * @throws ClassCastException            if the types of one or more elements in
	 *                                       this collection are incompatible with
	 *                                       the specified collection (<a href=
	 *                                       "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException          if this collection contains one or more
	 *                                       null elements and the specified
	 *                                       collection does not permit null
	 *                                       elements (<a href=
	 *                                       "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>),
	 *                                       or if the specified collection is null
	 */
	@ParameterizedTest
	@MethodSource("instanceAndCollProvider")
	public <T> void testretainAll(Collection<T> self, Collection<?> c) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires REMOVE_SUPPORTED;
		// @requires c != null;
		requireOk = true;
		if (!REMOVE_SUPPORTED || c == null) {
			Throwable ex = assertThrows(Throwable.class, () -> self.retainAll(c));
			if (ex instanceof UnsupportedOperationException) {
				assertFalse(REMOVE_SUPPORTED);
			} else {
				assertTrue(ex instanceof NullPointerException);
			}
			requireOk = false;
			return;
		}

		// Oldies:
		// old in:@ensures (\forall E obj; \old(contains(obj)); contains(obj) <==>
		// c.contains(obj));
		// old in:@ensures \result <==> (\exists E obj; \old(contains(obj));
		// !c.contains(obj));
		// old in:@ensures \result <==> size() < \old(size());
		// old in:@ensures !\result <==> size() == \old(size());
		List<T> oldSelf = new ArrayList<T>(self);
		int oldSize = self.size();
		boolean alienExists = false;
		for (T elt : self) {
			if (!c.contains(elt)) {
				alienExists = true;
			}
		}

		// Exécution:
		boolean result = self.retainAll(c);
		boolResult = result;

		// Post-conditions:
		// @ensures \result <==> size() < \old(size());
		assertEquals(self.size() < oldSelf.size(), result);
		// @ensures !\result <==> size() == \old(size());
		assertEquals(self.size() == oldSelf.size(), !result);
		// @ensures size() <= \old(size());
		assertTrue(self.size() <= oldSize);
		// @ensures (\forall Object o; contains(o); c.contains(o));
		for (T elt : self) {
			assertTrue(c.contains(elt));
		}
		// @ensures (\forall Object o; \old(contains(o)) && !c.contains(o);
		// !contains(o));
		for (T elt : oldSelf) {
			if (!c.contains(elt)) {
				assertFalse(self.contains(elt));
			}
		}

		// @ensures \result <==> (\exists E obj; \old(contains(obj)); !c.contains(obj));
		assertEquals(alienExists, result);

		// @ensures c.isEmpty() ==> this.isEmpty();
		if (c.isEmpty()) {
			assertTrue(self.isEmpty());
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method clear
	 *
	 * Removes all of the elements from this collection (optional operation). The
	 * collection will be empty after this method returns.
	 * 
	 * @throws UnsupportedOperationException if the {@code clear} operation is not
	 *                                       supported by this collection
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testclear(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires REMOVE_SUPPORTED;
		requireOk = true;
		if (!REMOVE_SUPPORTED) {
			assertThrows(UnsupportedOperationException.class, () -> self.clear());
			requireOk = false;
			return;
		}

		// Oldies:

		// Exécution:
		self.clear();

		// Post-conditions:
		// @ensures isEmpty();
		assertTrue(self.isEmpty());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method equals
	 *
	 * Compares the specified object with this collection for equality.
	 * <p>
	 *
	 * While the {@code Collection} interface adds no stipulations to the general
	 * contract for the {@code Object.equals}, programmers who implement the
	 * {@code Collection} interface "directly" (in other words, create a class that
	 * is a {@code Collection} but is not a {@code Set} or a {@code List}) must
	 * exercise care if they choose to override the {@code Object.equals}. It is not
	 * necessary to do so, and the simplest course of action is to rely on
	 * {@code Object}'s implementation, but the implementor may wish to implement a
	 * "value comparison" in place of the default "reference comparison." (The
	 * {@code List} and {@code Set} interfaces mandate such value comparisons.)
	 * <p>
	 *
	 * The general contract for the {@code Object.equals} method states that equals
	 * must be symmetric (in other words, {@code a.equals(b)} if and only if
	 * {@code b.equals(a)}). The contracts for {@code List.equals} and
	 * {@code Set.equals} state that lists are only equal to other lists, and sets
	 * to other sets. Thus, a custom {@code equals} method for a collection class
	 * that implements neither the {@code List} nor {@code Set} interface must
	 * return {@code false} when this collection is compared to any list or set. (By
	 * the same logic, it is not possible to write a class that correctly implements
	 * both the {@code Set} and {@code List} interfaces.)
	 */
	@ParameterizedTest
	@MethodSource("instanceAndCollProvider")
	public <T> void testequals(Collection<T> self, Object o) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.equals(o);
		boolResult = result;

		// Post-conditions:
		// @ensures o == null ==> !\result;
		if (o == null) {
			assertFalse(result);
		}
		// @ensures this == o ==> \result;
		if (self == o) {
			assertTrue(result);
		}
		if (o != null) {
			// @ensures (o != null) ==> (\result <==> o.equals(this));
			assertEquals(result, o.equals(self));
			if (self.hashCode() != o.hashCode()) {
				// @ensures (o != null) && (hashCode() != o.hashCode()) ==> !\result;
				assertFalse(result);
			}
		}
		// @ensures \result ==> hashCode() == o.hashCode();
		if (result) {
			assertEquals(self.hashCode(), o.hashCode());
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hashCode
	 *
	 * Returns the hash code value for this collection. While the {@code Collection}
	 * interface adds no stipulations to the general contract for the
	 * {@code Object.hashCode} method, programmers should take note that any class
	 * that overrides the {@code Object.equals} method must also override the
	 * {@code Object.hashCode} method in order to satisfy the general contract for
	 * the {@code Object.hashCode} method. In particular, {@code c1.equals(c2)}
	 * implies that {@code c1.hashCode()==c2.hashCode()}.
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testhashCode(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.hashCode();
		intResult = result;

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method spliterator
	 *
	 * Creates a {@link Spliterator} over the elements in this collection.
	 *
	 * Implementations should document characteristic values reported by the
	 * spliterator. Such characteristic values are not required to be reported if
	 * the spliterator reports {@link Spliterator#SIZED} and this collection
	 * contains no elements.
	 *
	 * <p>
	 * The default implementation should be overridden by subclasses that can return
	 * a more efficient spliterator. In order to preserve expected laziness behavior
	 * for the {@link #stream()} and {@link #parallelStream()} methods, spliterators
	 * should either have the characteristic of {@code IMMUTABLE} or
	 * {@code CONCURRENT}, or be
	 * <em><a href="Spliterator.html#binding">late-binding</a></em>. If none of
	 * these is practical, the overriding class should describe the spliterator's
	 * documented policy of binding and structural interference, and should override
	 * the {@link #stream()} and {@link #parallelStream()} methods to create streams
	 * using a {@code Supplier} of the spliterator, as in:
	 * 
	 * <pre>{@code
	 *     Stream<E> s = StreamSupport.stream(() -> spliterator(), spliteratorCharacteristics)
	 * }</pre>
	 * <p>
	 * These requirements ensure that streams produced by the {@link #stream()} and
	 * {@link #parallelStream()} methods will reflect the contents of the collection
	 * as of initiation of the terminal stream operation.
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testspliterator(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		Spliterator<T> result = self.spliterator();
		objResult = result;

		// Post-conditions:
		assertNotNull(result);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method stream
	 *
	 * Returns a sequential {@code Stream} with this collection as its source.
	 *
	 * <p>
	 * This method should be overridden when the {@link #spliterator()} method
	 * cannot return a spliterator that is {@code IMMUTABLE}, {@code CONCURRENT}, or
	 * <em>late-binding</em>. (See {@link #spliterator()} for details.)
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void teststream(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		Stream<T> result = self.stream();
		objResult = result;

		// Post-conditions:
		assertNotNull(result);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method parallelStream
	 *
	 * Returns a possibly parallel {@code Stream} with this collection as its
	 * source. It is allowable for this method to return a sequential stream.
	 *
	 * <p>
	 * This method should be overridden when the {@link #spliterator()} method
	 * cannot return a spliterator that is {@code IMMUTABLE}, {@code CONCURRENT}, or
	 * <em>late-binding</em>. (See {@link #spliterator()} for details.)
	 */
	@ParameterizedTest
	@MethodSource("instanceProvider")
	public <T> void testparallelStream(Collection<T> self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		Stream<T> result = self.parallelStream();
		objResult = result;

		// Post-conditions:
		assertNotNull(result);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for Collection
