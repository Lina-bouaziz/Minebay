package minebay;

/**
 * Caractérise une classe dont chaque instance appartient à une catégorie 
 * représentée par un type enum.
 * @param <C> le type enum utilisé pour caractériser les instances de cette classe
 * 
 * @invariant getCategory() != null;
 * 
 * @author Marc Champesme
 * @since 27/09/2024
 * @version 8/12/2024
 */
public interface Categorized<C extends Enum<C>> {
	/**
	 * Renvoie la catégorie de cette instance.
	 * 
	 * @return la catégorie de cette instance
	 * 
	 * @pure
	 */
	public C getCategory();
}
