package queue;


/**
 * Diese Klasse repraesentiert eine Warteschlange aus Knoten (Node)
 * Man kann ein neues Element einfuegen (am Anfang der Schlange)
 * Oder eines entnehmen (am Ende der Schlange)
 * 
 * @author Norbert Goebel
 * 
 * Quelle: Info1 Vorlesung Leuschel
 */
public class Queue  {

	QueueNode head;

	/** 
	 * Konstruktor fuer die Leere Warteschlange
	 *
	 */
	public Queue() {
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
	 * Einfuegen eines neuen Knotens am Anfang der Schlange
	 * @param neuerKnoten
	 */
	public void enqueue(QueueNode neuerKnoten) {
		neuerKnoten.setNext(head);
		head = neuerKnoten;
	}
	
	/**
	 * Entfernen und zurueckgeben des letzten Elementes 
	 * Der Uebersichtlichkeit halber wird null zurueckgegeben, wenn die Schlange leer ist
	 * Im Normalfall wuerde man sowas eher mit einer Exception behandeln
	 * Der Einfachheit halber wird hier darauf verzichtet
	 * 
	 * @return null oder Knoten
	 */
	public QueueNode dequeue() {
		if (isEmpty()) return null; 
		QueueNode tmp = null;
		QueueNode n = head;
		while (n.getNext() != null) {
			tmp = n;
			n = n.getNext();
		}
		if (tmp != null)
			tmp.setNext(null);
		else
			head = null;
		return n;
	}
}