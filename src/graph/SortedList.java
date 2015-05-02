package graph;

public class SortedList {
	SortedListNode head;

	
	/** 
	 * Konstruktor fuer die Leere Warteschlange
	 *
	 */
	public SortedList() {
		head = null;
	}

	/**
	 * Pruefen ob die Warteschlange leer ist
	 * @return true/false
	 */
	public boolean isEmpty() {
		return (head == null);
	}

	/**
	 * Einfuegen eines neuen Knotens in die sortierte Liste anhand des Gewichtsvektors und den zugehörigen
	 * Knotenindizes
	 * @param neuerKnoten
	 * @param gewichte
	 */
	public void insert(SortedListNode neuerKnoten,int[] gewichte) {
		SortedListNode tmp = head;
		while (tmp.getNext() != null && gewichte[neuerKnoten.getKnotenIndex()]<gewichte[tmp.getKnotenIndex()]){
			tmp = tmp.getNext();
		}
		neuerKnoten.setNext(tmp.getNext());
		tmp.setNext(neuerKnoten);
	}
	
	/**
	 * Enternen und zurueckgeben des letzten Elementes 
	 * Der Uebersichtlichkeit halber wird null zurueckgegeben, wenn die Schlange leer ist
	 * Im Normalfall wuerde man sowas eher mit einer Exception behandeln
	 * Der Einfachheit halber wird hier darauf verzichtet
	 * 
	 * @return null oder Knoten
	 */
	public SortedListNode removeFirst() {
		if (isEmpty()) return null; 
		SortedListNode tmp = head;
		if (head.getNext() == null)
			head = null;
		else
			head = tmp.getNext();
		return tmp;
	}
		
}
	
