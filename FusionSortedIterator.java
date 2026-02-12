package minebay;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import java.lang.Comparable;
import java.util.Comparator;
import java.util.Collection;

import java.util.NoSuchElementException;

/**
 * Un ListIterator fusionnant plusieurs ListIterator ordonnés en interdisant les
 * opérations de modification add et set.
 *
 * Un FusionSortedIterator garantie que, si les ListIterator fusionnés sont
 * ordonnés, alors ce FusionSortedIterator sera également ordonné.
 *
 * Par défaut, l'ordre considéré est l'ordre naturel entre les éléments,
 * cependant un ordre alternatif peut-être spécifié à la création de l'instance.
 * 
 * @param <E> le type des éléments énumérés par cet itérateur
 * @param <I> le type des itérateurs fusionnés
 *
 * @model ListIterObserver<E> iterModel = new ListIterObserverAdapter<E>(this);
 * @invariant nextIndex() == previousIndex() + 1;
 * @invariant previousIndex() >= -1 && previousIndex() < iterModel.size());
 * @invariant nextIndex() >= 0 && nextIndex() <= iterModel.size());
 * @invariant !hasPrevious() <==> previousIndex() == -1;
 * @invariant !hasNext()() <==> nextIndex() == iterModel.size();
 * @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex() ||
 *            lastIndex() == -1;
 * @invariant lastIndex() >= -1 && lastIndex() < iterModel.size());
 * @invariant !iterModel.contains(null);
 * @invariant comparator() != null;
 * @invariant iterModel.isSorted(comparator());
 * 
 * @author Marc Champesme
 * @since 2/08/2023
 * @version 8/12/2024
 *
 */
public class FusionSortedIterator<E extends Comparable<? super E>> implements ListIterator<E> {

	private List<E> iterModel;
	private Comparator<? super E> Comp;
	private List<ListIterator<E>> Liste;

	private int LastIndex;
	private int NextIndex;
	private int PreviousIndex;

	/**
	 * Initialise une instance permettant d'itérer selon l'ordre "naturel" sur tous
	 * les éléments des ListIterator de la collection spécifiée. Il s'agit donc
	 * d'une fusion de tous les ListIterator contenus dans la collection spécifiée.
	 * Les ListIterator spécifiés sont supposés ordonnés selon l'ordre "naturel" de
	 * leurs éléments.
	 *
	 * @param iters ensemble des ListIterator à fusionner
	 *
	 * @requires iters != null && !iters.contains(null);
	 * @ensures (\forall ListIterator<E> iter; iters.contains(iter);
	 *          iterModel.containsAll(toList(iter)));
	 * @ensures iterModel.size() == (\sum ListIterator<E> iter;
	 *          iters.contains(iter); size(iter));
	 * @ensures (\forall E p; iterModel.contains(p); (\exists ListIterator<E> iter;
	 *          iters.contains(iter); contains(iter, p)));
	 * @ensures !hasPrevious();
	 * @ensures lastIndex() == -1;
	 * @ensures comparator() != null;
	 *
	 * @throws NullPointerException si l'ensemble spécifié est null ou contient null
	 */

	public FusionSortedIterator(Collection<? extends ListIterator<E>> iters) {
		if (iters == null || iters.contains(null))
			throw new NullPointerException("Argument null");

		this.Liste = new ArrayList<>(iters);
		this.iterModel = new ArrayList<>();
		this.Comp = Comparator.naturalOrder();

		for (ListIterator<E> i : iters) {
			while (i.hasNext()) 
				iterModel.add(i.next());
	
			while (i.hasPrevious()) 
				i.previous();
		}

		iterModel.sort(this.Comp);

		this.NextIndex = 0;
		this.LastIndex = -1;
		this.PreviousIndex = -1;
	}

	/**
	 * Initialise une instance permettant d'itérer sur tous les éléments des
	 * ListIterator de la collection spécifiée selon l'ordre spécifié. Il s'agit
	 * donc d'une fusion de tous les ListIterator contenus dans la collection
	 * spécifiée. les ListIterator contenus dans la collection spécifiée sont
	 * supposés ordonnés selon l'ordre induit par le Comparator spécifié.
	 *
	 *
	 * @param iters      collection des ListIterator à fusionner
	 * @param comparator le comparateur à utiliser
	 *
	 * @requires iters != null && !iters.contains(null);
	 * @requires comparator != null;
	 * @ensures comparator() != null;
	 * @ensures comparator().equals(comparator);
	 * @ensures !hasPrevious();
	 * @ensures lastIndex() == -1;
	 *
	 * @throws NullPointerException si l'ensemble spécifié est null ou contient
	 *                              null, ou si le Comparator spécifié est null
	 */
	public FusionSortedIterator(Collection<? extends ListIterator<E>> iters, Comparator<? super E> comparator) {
		this(iters);

		if (comparator == null)
			throw new NullPointerException("Argument null");

		this.Comp = comparator;
		iterModel.sort(this.Comp);
	}

	/**
	 * Renvoie le comparateur selon lequel les éléments de cet itérateur sont
	 * ordonnés.
	 * 
	 * @return le comparateur selon lequel les éléments de cet itérateur sont
	 *         ordonnés
	 * 
	 * @ensures \result != null;
	 * 
	 * @pure
	 */
	public Comparator<? super E> comparator() {
		return this.Comp;
	}

	/**
	 * Renvoie true s'il reste un élément après dans l'itération.
	 * 
	 * @return true s'il reste un élément après dans l'itération; false sinon
	 * 
	 * @ensures !\result <==> nextIndex() == iterModel.size();
	 * 
	 * @pure
	 */
	@Override
	public boolean hasNext() {
		return this.NextIndex < iterModel.size();
	}

	/**
	 * Renvoie l'élèment qui sera renvoyé par le prochain appel à next().
	 * 
	 * @return l'élèment qui sera renvoyé par le prochain appel à next()
	 * 
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément suivant
	 * 
	 * @requires hasNext();
	 * @ensures \result.equals(iterModel.get(nextIndex()));
	 * 
	 * @pure
	 */
	public E getNext() {
		if (!hasNext())
			throw new NoSuchElementException("Pas de suivant");

		return iterModel.get(NextIndex);

	}

	/**
	 * Renvoie l'élément suivant et avance le curseur.
	 *
	 * @return l'élément suivant
	 *
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément suivant
	 *
	 * @requires hasNext();
	 * @ensures \result != null;
	 * @ensures \result.equals(\old(getNext()));
	 * @ensures \result.equals(getPrevious());
	 * @ensures \result.equals(iterModel.get(previousIndex()))
	 * @ensures \old(hasPrevious()) ==> comparator().compare(\old(getPrevious()),
	 *          \result) <= 0;
	 * @ensures hasNext() ==> comparator().compare(\result, getNext()) <= 0;
	 * @ensures hasPrevious();
	 * @ensures previousIndex() == \old(nextIndex());
	 * @ensures nextIndex() == \old(nextIndex() + 1);
	 * @ensures lastIndex() == \old(nextIndex());
	 * @ensures lastIndex() == previousIndex();
	 */
	@Override
	public E next() {
		if (!hasNext())
			throw new NoSuchElementException("Pas de suivant");

		PreviousIndex = NextIndex;
		LastIndex = NextIndex;

		return iterModel.get(NextIndex++);
	}

	/**
	 * Renvoie true s'il y a un élément précédent dans l'itération.
	 * 
	 * @return true s'il y a un élément précédent dans l'itération; false sinon
	 * 
	 * @ensures !\result <==> previousIndex() == -1;
	 *
	 * @pure
	 */
	@Override
	public boolean hasPrevious() {
		return this.PreviousIndex > -1;
	}

	/**
	 * Renvoie l'élèment qui sera renvoyé par le prochain appel à previous().
	 * 
	 * @return l'élèment qui sera renvoyé par le prochain appel à previous()
	 * 
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément précédent
	 * 
	 * @requires hasPrevious();
	 * @ensures \result.equals(iterModel.get(previousIndex()));
	 * 
	 * @pure
	 */
	public E getPrevious() {
		if (!hasPrevious())
			throw new NoSuchElementException("Pas de precedent");

		return iterModel.get(PreviousIndex);
	}

	/**
	 * Renvoie l'élément précédent et recule le curseur.
	 *
	 * @return l'élément précédent
	 *
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément précédent
	 *
	 * @requires hasPrevious();
	 * @ensures hasNext();
	 * @ensures \result != null;
	 * @ensures \result.equals(\old(getPrevious()));
	 * @ensures \result.equals(getNext());
	 * @ensures \result.equals(\old(iterModel.get(previousIndex())));
	 * @ensures \result.equals(iterModel.get(nextIndex()));
	 * @ensures \old(hasNext()) ==> comparator().compare(\result,
	 *          iterModel.get(\old(nextIndex())) <= 0;
	 * @ensures previousIndex() == \old(previousIndex()) - 1;
	 * @ensures nextIndex() == \old(previousIndex());
	 * @ensures lastIndex() == \old(previousIndex());
	 * @ensures lastIndex() == nextIndex();
	 */
	@Override
	public E previous() {
		if (!hasPrevious())
			throw new NoSuchElementException("Pas de precedent");

		LastIndex = PreviousIndex;
		NextIndex = PreviousIndex;

		return iterModel.get(PreviousIndex--);
	}

	/**
	 * Renvoie l'index de l'élément suivant dans l'itération. Renvoie le nombre
	 * total d'élément dans l'itération s'il n'y a pas d'élément suivant.
	 * 
	 * @return l'index de l'élément suivant dans l'itération
	 * 
	 * @ensures hasNext() <==> \result >= 0 && \result < iterModel.size();
	 * @ensures !hasNext() <==> \result == iterModel.size();
	 * 
	 * @pure
	 */
	@Override
	public int nextIndex() {
		if (!hasNext())
			return this.iterModel.size();

		return this.NextIndex;
	}

	/**
	 * Renvoie l'index de l'élément précédent dans l'itération. Renvoie -1 s'il n'y
	 * a pas d'élément précédent.
	 * 
	 * @return l'index de l'élément précédent dans l'itération
	 * 
	 * @ensures hasPrevious() ==> \result >= 0;
	 * @ensures !hasPrevious() <==> \result == -1;
	 *
	 * @pure
	 */
	@Override
	public int previousIndex() {
		if (!hasPrevious())
			return -1;

		return this.PreviousIndex;
	}

	/**
	 * Renvoie l'index de l'élément renvoyé par le dernier appel à next() ou
	 * previous(). Renvoie -1 si next() ou previous() n'ont jamais été appelés
	 * depuis la création de cet itérateur ou bien si remove a été appelée depuis le
	 * dernier appel à next ou previous.
	 * 
	 * @return l'index de l'élément renvoyé par le dernier appel à next() ou
	 *         previous()
	 * 
	 * @ensures \result >= -1 && \result < iterModel.size();
	 * 
	 * @pure
	 */
	public int lastIndex() {
		return this.LastIndex;
	}

	/**
	 * Retire de l'itération le dernier élèment renvoyé par next() ou previous().
	 * L'élément retiré est l'élèment renvoyé par le dernier appel à next() ou
	 * previous(). Ne peut être appelé qu'une fois par appel à next ou previous.
	 * 
	 * @throws IllegalStateException si next ou previous n'ont jamais été appelés,
	 *                               ou bien si remove a déjà été appelé depuis le
	 *                               dernier appel a next ou remove
	 * 
	 * @requires lastIndex() != -1;
	 * @ensures iterModel.size() == \old(iterModel.size()) - 1;
	 * @ensures (* if last move is previous: *) <br />
	 *          \old(lastIndex() == nextIndex()) ==> (previousIndex() ==
	 *          \old(previousIndex()));
	 * @ensures (* if last move is next: *) <br />
	 *          \old(lastIndex() == previousIndex()) ==> (previousIndex() ==
	 *          \old(previousIndex()) - 1);
	 * @ensures lastIndex() == -1;
	 */
	@Override
	public void remove() {
		if (LastIndex == -1)
			throw new IllegalStateException("Probleme de last index");

		E e = iterModel.get(LastIndex);

		iterModel.remove(LastIndex);

		if (LastIndex == NextIndex - 1)
			NextIndex--;
			
		LastIndex = -1;

		for (ListIterator<E> i : Liste) {
			while (i.hasNext()) {
				if (i.next().equals(e)) {
					i.remove();
					return;
				}
			}

			while (i.hasPrevious()) {
				i.previous();
			}
		}
	}

	/**
	 * Opération non supportée.
	 * 
	 * @throws UnsupportedOperationException toujours
	 */
	@Override
	public void set(E e) {
		throw new UnsupportedOperationException("set non supporté");
	}

	/**
	 * Opération non supportée.
	 * 
	 * @throws UnsupportedOperationException toujours
	 */
	@Override
	public void add(E e) {
		throw new UnsupportedOperationException("add non supporté");
	}

}
