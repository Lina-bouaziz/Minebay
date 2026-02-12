package minebay.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import minebay.AdState;
import minebay.AdCategory;
import minebay.ClassifiedAd;
import minebay.MultiEnumList;
import minebay.User;

/**
 * 
 */
public class DataProvider {
	public static final int LG_STREAM = 200;
	public static final int BAD_ARG_PROBA = 50;
	public static final int MAX_LIST_SIZE = 30;

	private static List<String> goodUserNames = Arrays.asList("Marcel", "Adam", "Sonia", "Idir", "Mohamed", "Marc",
			"Ali", "Ziad", "Lyes", "Ayman", "Mounir", "Pierre", "Chanez", "Lamia", "Yanis", "Faycal", "Boris", "Imam",
			"Naila", "Zahra", "Rosa", "Lisa", "Sheraz", "Nima", "Aliou", "Issa", "Mamadou", "Ismael");
	private static List<String> badNames = Arrays.asList("", " ", "  ", "\n \t", null);
	private static List<User> allUsers;
	private static List<ClassifiedAd> allAds;
	private static Random randGen = new Random();

	static {
		//System.out.println("Début init User");
		initUsers(goodUserNames);
		//System.out.println("Fin init User");
	}

	private static void initUsers(List<String> userNames) {
		allUsers = new ArrayList<User>(userNames.size());
		for (String name : userNames) {
			allUsers.add(new User(name, "pass" + name));
		}
		addAdsToUsers(allUsers);
		addPurchaseToUsers(allUsers);
		someSelection(allUsers);
	}

	private static void addAdsToUsers(List<User> userList) {
		int adNb = 1500;
		allAds = new ArrayList<ClassifiedAd>(adNb);
		while (adNb > 0) {
			User u = getRandomElt(userList);
			allAds.add(u.add(enumSupplier(AdCategory.class), "Annonce n°" + adNb + " from " + u.getName(),
					randInt(100) + 1));
			adNb--;
		}
	}

	private static void addPurchaseToUsers(List<User> userList) {
		int nbPurchase = 400;
		while (nbPurchase > 0) {
			User buyer = getRandomElt(userList);
			User vendor = getRandomElt(userList);
			if (buyer != vendor && buyer.getAvailableCash() > 100 && vendor.size() > 0) {
				ClassifiedAd ad = vendor.get(randInt(vendor.size()));
				buyer.buy(vendor, ad);
				nbPurchase -= 3;
			} else {
				nbPurchase--;
			}
		}
	}

	private static void someSelection(List<User> userList) {
		int nbUsers = userList.size();
		while (nbUsers > 0) {
			User user = getRandomElt(userList);
			user.selectAdState(enumSupplier(AdState.class));

			int nbSteps = randInt(5);
			while (nbSteps > 0) {
				user.addSelectedCategory(enumSupplier(AdCategory.class));
				nbSteps--;
			}
			nbUsers--;
		}
	}

	public static List<User> allUser() {
		return Collections.unmodifiableList(allUsers);
	}

	public static ClassifiedAd adSupplier() {
		return getRandomElt(allAds);
	}

	public static ClassifiedAd adSupplierWithNull() {
		if (randBool(BAD_ARG_PROBA)) {
			return null;
		}
		return getRandomElt(allAds);
	}

	public static MultiEnumList<AdCategory, ClassifiedAd> multiEnumListSupplier() {
		Collection<ClassifiedAd> c = randomCollection(randInt(allAds.size() / 2), () -> adSupplier());
		return new MultiEnumList<AdCategory, ClassifiedAd>(AdCategory.class, c);
	}

	public static <T extends Enum<T>> T enumSupplier(Class<T> enumClass) {
		T[] values = enumClass.getEnumConstants();
		return values[randInt(values.length)];
	}

	public static <T extends Enum<T>> T enumSupplierWithNull(Class<T> enumClass) {
		if (randBool(BAD_ARG_PROBA)) {
			return null;
		}
		T[] values = enumClass.getEnumConstants();
		return values[randInt(values.length)];
	}

	public static <C extends Enum<C>> Set<C> enumSetSupplier(Class<C> enumClass) {
		EnumSet<C> allCats = EnumSet.allOf(enumClass);
		int size = allCats.size();
		int nb = randInt(size + 2);
		while (nb > 0) {
			allCats.remove(enumSupplier(enumClass));
			nb--;
		}
		return allCats;
	}
	
	public static <C extends Enum<C>> Set<C> catSetSupplier(Class<C> enumClass) {
		Set<C> catSet = null;
		if (randBool(BAD_ARG_PROBA)) {
			if (randBool()) {
				return null;
			}
			catSet = new HashSet<>(enumSetSupplier(enumClass));
			catSet.add(null);
			return catSet;
		}
		return enumSetSupplier(enumClass);
	}

	public static User userSupplier() {
		return getRandomElt(allUsers);
	}

	public static ListIterator<ClassifiedAd> listIterSupplier() {
		if (randBool()) {
			List<ClassifiedAd> l = randomList(30, () -> adSupplier());
			l.sort(null);
			return l.listIterator();
		} else {
			User u = userSupplier();
			return u.iterator(AdState.OPEN, enumSetSupplier(AdCategory.class));
		}
	}
	
	public static ListIterator<ClassifiedAd> listIterSupplier(Comparator<? super ClassifiedAd> cmp) {
		List<ClassifiedAd> l = randomList(30, () -> adSupplier());
		l.sort(cmp);
		return l.listIterator();
	}
	
	public static List<? extends ListIterator<ClassifiedAd>> listOfListIterSupplier() {
		return randomList(15, () -> listIterSupplier());
	}
	public static List<? extends ListIterator<ClassifiedAd>> listOfListIterSupplierWithCmp() {
		return randomList(15, () -> listIterSupplier(PriceComparator.getInstance()));
	}
	public static List<? extends ListIterator<ClassifiedAd>> listOfListIterSupplierWithNull() {
		if (randBool(BAD_ARG_PROBA)) {
			return null;
		}
		List<? extends ListIterator<ClassifiedAd>> l = randomList(15, () -> listIterSupplier());
		if (randBool(BAD_ARG_PROBA)) {
			l.add(randInt(l.size() + 1), null);
		}
		return l;
	}

	public static String stringSupplier() {
		if (randBool(BAD_ARG_PROBA)) {
			return getRandomElt(badNames);
		}
		return getRandomElt(goodUserNames);
	}

	/**
	 * Renvoie un élément tiré aléatoirement parmi les éléments de la collection
	 * spécifiée.
	 *
	 * @requires c != null;
	 * @requires !c.isEmpty();
	 * @ensures c.contains(\result);
	 *
	 * @param <T> Type des éléments de la collection spécifiée
	 * @param c   collection dans laquelle est choisi l'élément retourné
	 *
	 * @return un élément tiré aléatoirement parmi les éléments de la collection
	 *         spécifiée
	 * 
	 * @throws NullPointerException     si l'argument spécifié est null
	 * @throws IllegalArgumentException si l'argument spécifié est vide
	 */
	public static <T> T getRandomElt(Collection<T> c) {
		int index = randInt(c.size());
		if (c instanceof List<?>) {
			return ((List<T>) c).get(index);
		}
		int i = 0;
		for (T elt : c) {
			if (i == index) {
				return elt;
			}
			i++;
		}
		throw new NoSuchElementException(); // Ne peut pas arriver
	}

	/**
	 * Renvoie une Collection dont la taille maximale est l'entier spécifié et dont
	 * les éléments sont obtenus à l'aide du Supplier spécifié.
	 * 
	 * @param <T>         le type des éléments de la Collection
	 * 
	 * @param maxLength   taille maximale de la Collection générée
	 * @param eltSupplier le Supplier utilisé pour générer les éléments de la
	 *                    Collection
	 * @return une Collection dont la taille maximale est l'entier spécifié
	 *         spécifiée et dont les éléments sont obtenus à l'aide du Supplier
	 *         spécifié
	 * 
	 * @throws NullPointerException     si le Supplier spécifié est null
	 * @throws IllegalArgumentException si maxLength < 0
	 * 
	 * @requires maxLength >= 0;
	 * @requires eltSupplier != null;
	 * @ensures \result != null;
	 * @ensures \result.size() <= maxLength;
	 * @ensures (\result instanceof Set<?>) || (\result instanceof List<?>);
	 */
	public static <T> Collection<T> randomCollection(int maxLength, Supplier<? extends T> eltSupplier) {
		int realLength = randInt(maxLength + 1);
		return Stream.generate(eltSupplier).limit(realLength)
				.collect(Collectors.toCollection(randBool() ? LinkedList<T>::new : HashSet<T>::new));
	}

	public static <T> List<T> randomList(int maxLength, Supplier<? extends T> eltSupplier) {
		int realLength = randInt(maxLength + 1);
		return Stream.generate(eltSupplier).limit(realLength).collect(Collectors.toList());
	}

	/**
	 * Renvoie un int obtenue par un générateur pseudo-aléatoire.
	 *
	 * @param max la valeur maximale du nombre aléatoire attendu
	 *
	 * @return un entier >= 0 et < max
	 *
	 * @throws IllegalArgumentException si max <= 0
	 *
	 * @requires max > 0;
	 * @ensures \result >= 0;
	 * @ensures \result < max;
	 */
	public static int randInt(int max) {
		return randGen.nextInt(max);
	}
	
	/**
	 * Renvoie un int obtenue par un générateur pseudo-aléatoire.
	 * 
	 * @param min la valeur minimale du nombre aléatoire attendu
	 * @param max la valeur maximale du nombre aléatoire attendu
	 * 
	 * @return un entier >= min et < max
	 * 
	 * @throws IllegalArgumentException si max <= min
	 * 
	 * @requires max > min;
	 * @ensures \result >= min;
	 * @ensures \result < max;
	 */
	public static int randInt(int min, int max) {
		return randInt(max - min) + min;
	}
	
	/**
	 * Renvoie une valeur booléenne obtenue par un générateur pseudo-aléatoire. La
	 * valeur renvoyée a une probabilité d'être true similaire à la probabilité que
	 * randInt(max) renvoie la valeur 0.
	 *
	 * @return une valeur booléenne aléatoire
	 * 
	 * @throws IllegalArgumentException si max <= 0
	 * 
	 * @requires max > 0;
	 */
	public static boolean randBool(int max) {
		return randGen.nextInt(max) == 0;
	}

	/**
	 * Renvoie une valeur booléenne obtenue par un générateur pseudo-aléatoire.
	 *
	 * @return une valeur booléenne aléatoire
	 */
	public static boolean randBool() {
		return randGen.nextBoolean();
	}
}
