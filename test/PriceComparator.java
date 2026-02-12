package minebay.test;

import java.util.Comparator;

import minebay.ClassifiedAd;


/**
 * 
 */
public class PriceComparator implements Comparator<ClassifiedAd> {
	private static final PriceComparator theInstance = new PriceComparator();
	public static PriceComparator getInstance() {
		return theInstance;
	}

	private PriceComparator() {
		
	}
	
	@Override
	public int compare(ClassifiedAd o1, ClassifiedAd o2) {
		return o1.getPrice() - o2.getPrice();
	}

}