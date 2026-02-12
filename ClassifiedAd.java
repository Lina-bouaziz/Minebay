package minebay;

import java.time.Instant;

/**
 * Une petite annonce d'un utilisateur de l'application Minebay. Chaque annonce
 * est caractérisée par sa description, sa date de création, sa catégorie et son
 * prix.
 * 
 * Cette classe est non modifiable.
 * 
 * @invariant getDescription() != null;
 * @invariant !getDescription().isBlank();
 * @invariant getCategory() != null;
 * @invariant getPrice() > 0;
 * @invariant getDate() != null;
 * 
 * @author Marc Champesme
 * @since 27/09/2024
 * @version 8/12/2024
 */
public class ClassifiedAd implements Categorized<AdCategory>, Comparable<ClassifiedAd> {

	private int Price;
	private Instant Date;
	private String Description;
	private AdCategory Category;

	/**
	 * Initialise une nouvelle annonce. La date de cette nouvelle annonce est la
	 * date courante au moment de l'exécution de ce constructeur.
	 * 
	 * @param cat   la catégorie de l'objet mis en vente
	 * @param desc  la description de l'objet mis en vente
	 * @param price le prix de l'objet mis en vente par cette annonce
	 * 
	 * @requires cat != null;
	 * @requires desc != null;
	 * @requires !desc.isBlank();
	 * @requires price > 0;
	 * @ensures getCategory().equals(cat);
	 * @ensures getDescription().equals(desc);
	 * @ensures getPrice() == price;
	 * @ensures getDate() != null;
	 * @ensures \old(Instant.now()).isBefore(getDate());
	 * @ensures getDate().isBefore(Instant.now());
	 * 
	 * @throws NullPointerException     si la catégorie ou la description sont null
	 * @throws IllegalArgumentException si le prix spécifié est inférieur ou égal à
	 *                                  0 ou la description de l'objet ne contient
	 *                                  pas de caractères
	 */

	public ClassifiedAd(AdCategory cat, String desc, int price) {
		if (cat == null || desc == null)
			throw new NullPointerException("Catégorie ou description nulle");

		if (price <= 0 || desc.isBlank())
			throw new IllegalArgumentException("Prix invalide ou description vide");

		this.Date = Instant.now();
		this.Description = desc;
		this.Category = cat;
		this.Price = price;
	}

	/**
	 * Renvoie la date de création de cette annonce.
	 * 
	 * @return la date de création de cette annonce
	 * 
	 * @pure
	 */
	public Instant getDate() {
		return this.Date;
	}

	/**
	 * Renvoie la description de cette annonce.
	 * 
	 * @return la description de cette annonce
	 * 
	 * @pure
	 */
	public String getDescription() {
		return this.Description;
	}

	/**
	 * Renvoie la catégorie de cette annonce.
	 * 
	 * @return la catégorie de cette annonce
	 * 
	 * @pure
	 */
	@Override
	public AdCategory getCategory() {
		return this.Category;
	}

	/**
	 * Renvoie le prix de l'objet mis en vente par cette annonce.
	 * 
	 * @return le prix de l'objet à vendre
	 * 
	 * @pure
	 */
	public int getPrice() {
		return this.Price;
	}

	/**
	 * Teste si cette annonce a été publiée avant l'annonce spécifiée.
	 * 
	 * @param ad l'annonce à comparer avec cette annonce
	 * 
	 * @return true si et seulement si la date de cette annonce est strictement
	 *         antérieure à celle de l'annonce spécifiée
	 * 
	 * @requires ad != null;
	 * @ensures \result <==> this.getDate().isBefore(ad.getDate());
	 * @ensures !(\result && this.isAfter(ad));
	 * 
	 * @throws NullPointerException si l'annonce spécifiée est null
	 * 
	 * @pure
	 */
	public boolean isBefore(ClassifiedAd ad) {
		if (ad == null)
			throw new NullPointerException("Annonce nulle");

		return getDate().isBefore(ad.getDate());
	}

	/**
	 * Teste si cette annonce a été publié après l'annonce spécifiée.
	 * 
	 * @param ad l'annonce à comparer avec cette annonce
	 * 
	 * @return true si et seulement si la date de cette annonce est strictement
	 *         postérieure à celle de l'annonce spécifiée
	 * 
	 * @requires ad != null;
	 * @ensures \result <==> this.getDate().isAfter(ad.getDate());
	 * @ensures !(\result && this.isBefore(ad));
	 * 
	 * @throws NullPointerException si l'annonce spécifiée est null
	 * 
	 * @pure
	 */
	public boolean isAfter(ClassifiedAd ad) {
		if (ad == null)
			throw new NullPointerException("Annonce nulle");

		return getDate().isAfter(ad.getDate());
	}

	/**
	 * Compare cette annonce avec l'annonce spécifiée selon l'ordre de leurs dates
	 * de création (de l'annonce la plus récente à l'annonce la plus ancienne).
	 * Renvoie un entier négatif si cette annonce est plus récente que l'annonce
	 * spécifiée, zéro si les deux annonces ont la même date et un entier positif si
	 * cette annonce est plus ancienne que l'annonce spécifiée.
	 * 
	 * @requires ad != null;
	 * @ensures \result < 0 <==> this.isAfter(ad);
	 * @ensures \result > 0 <==> this.isBefore(ad);
	 * @ensures \result == 0 <==> this.equals(ad);
	 * @ensures \result == -getDate().compareTo(ad.getDate());
	 * 
	 * @throws NullPointerException si l'annonce spécifiée est null
	 * 
	 * @pure
	 */
	@Override
	public int compareTo(ClassifiedAd ad) {
		if (ad == null)
			throw new NullPointerException("Annonce nulle");

		return -getDate().compareTo(ad.getDate());
	}

	/**
	 * Renvoie true si l'objet spécifié est une ClassifiedAd ayant les mêmes
	 * caractéristiques que cette ClassifiedAd.
	 * 
	 * @param obj l'objet à comparer avec cette ClassifiedAd
	 * 
	 * @return true si l'objet spécifié est une ClassifiedAd ayant les mêmes
	 *         caractéristiques que cette ClassifiedAd; false sinon
	 * 
	 * @ensures !(obj instanceof ClassifiedAd) ==> !\result;
	 * @ensures \result <==> (obj instanceof ClassifiedAd) &&
	 *          getDate().equals(((ClassifiedAd) obj).getDate()) &&
	 *          getDescription().equals(((ClassifiedAd) obj).getDescription()) &&
	 *          getCategory().equals(((ClassifiedAd) obj).getCategory()) &&
	 *          getPrice() == ((ClassifiedAd) obj).getPrice());
	 * @ensures \result ==> hashCode() == obj.hashCode());
	 * 
	 * @pure
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClassifiedAd))
			return false;

		ClassifiedAd nv = (ClassifiedAd) obj;

		return getPrice() == nv.getPrice()
				&& Description.equals(nv.getDescription())
				&& getDate().equals(nv.getDate())
				&& this.Category.equals(nv.getCategory());
	}

	/**
	 * Renvoie un code de hashage pour cette ClassifiedAd.
	 * 
	 * @return un code de hashage pour cette ClassifiedAd
	 * 
	 * @pure
	 */
	@Override
	public int hashCode() {
		return java.util.Objects.hash(getDescription(), getDate(), getPrice(), getCategory());
	}

	/**
	 * Renvoie une chaîne de caractères contenant les caractéristiques de cette
	 * ClassifiedAd.
	 * 
	 * @return une chaîne de caractères contenant les caractéristiques de cette
	 *         ClassifiedAd
	 * 
	 * @ensures \result != null;
	 * @ensures \result.contains(getCategory().toString());
	 * @ensures \result.contains(getDate().toString());
	 * @ensures \result.contains(getDescription().toString());
	 * @ensures \result.contains("" + getPrice());
	 * @pure
	 */
	@Override
	public String toString() {
		return String.format("Description: %s\nCategorie: %s\nPrix: %d\nDate: %s",
				getDescription(), getCategory(), getPrice(), getDate());
	}

}
