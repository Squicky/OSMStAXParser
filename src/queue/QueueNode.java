package queue;

/** Diese Klasse repraesentiert einen Knoten einer Queue
 * 
 * @author Norbert Goebel
 *
 * Quelle: Info1 Vorlesung Leuschel
 */
public class QueueNode {
	
	QueueNode next; // Referenz auf den naechsten Knoten

	/**
	 * Konstruktor fuer einen Knoten
	 *
	 */
	public QueueNode() {
	 next = null;
	}
		
	/**
	 * Setze den Nachfolgern dieses Knotens
	 * @param n = Nachfolger
	 */
	public void setNext(QueueNode n) {
		this.next = n;
	}

	/**
	 * Erfrage den Nachfolger dieses Knotens
	 * @return Nachfolger
	 */
	public QueueNode getNext() {
		return this.next;
	}
}