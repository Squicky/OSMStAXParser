package graph;

public class SortedListNode {
		
		private SortedListNode next; // Referenz auf den naechsten Knoten
		private int knotenIndex;

		/**
		 * Konstruktor fuer einen Knoten
		 *
		 */
		public SortedListNode(int index) {
		 this.next = null;
		 this.knotenIndex = index;
		}
			
		/**
		 * Setze den Nachfolgern dieses Knotens
		 * @param n = Nachfolger
		 */
		public void setNext(SortedListNode n) {
			this.next = n;
		}

		/**
		 * Erfrage den Nachfolger dieses Knotens
		 * @return Nachfolger
		 */
		public SortedListNode getNext() {
			return this.next;
		}

		/**
		 * @return the knotenIndex
		 */
		public int getKnotenIndex() {
			return knotenIndex;
		}
	}

