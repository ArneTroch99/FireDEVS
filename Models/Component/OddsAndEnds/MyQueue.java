package Component.OddsAndEnds;

import java.util.NoSuchElementException;

import Component.BasicProcessor.*;

public class MyQueue<Item> {
	private int N;         // number of elements on queue
	private Node first;    // beginning of queue
	private Node last;     // end of queue

	// helper linked list class
	private class Node {
		private Item item;
		private Node next;
	}

	/**
	 * Create an empty queue.
	 */
	public MyQueue() {
		first = null;
		last  = null;
		N = 0;
	}

	/**
	 * Is the queue empty?
	 */
	public boolean isEmpty() {
		return first == null;
	}

	/**
	 * Return the number of items in the queue.
	 */
	public int size() {
		return N;     
	}

	/**
	 * Return the item least recently added to the queue.
	 * @throws java.util.NoSuchElementException if queue is empty.
	 */
	public Item peek() {
		if (isEmpty()) throw new NoSuchElementException("Queue underflow");
		return first.item;
	}

	/**
	 * Add the item to the queue.
	 */
	public void enqueue(Item item) {
		
		Node oldlast = last;
		last = new Node();
		last.item = item;
		last.next = null;
		if (isEmpty()) 
			first = last;
		else 
			oldlast.next = last;
		N++;
	}

	/**
	 * Remove and return the item on the queue least recently added.
	 * @throws java.util.NoSuchElementException if queue is empty.
	 */
	public Item dequeue() {
		if (isEmpty()) 
			throw new NoSuchElementException("Queue underflow");
		Item item = first.item;
		first = first.next;
		N--;
		if (isEmpty()) 
			last = null;   // to avoid loitering
		return item;
	}
	
	public void display(){
		Node temp = first;
		while(temp!=null){
			System.out.println(temp.item + "\t");
			temp = temp.next;
		}
		System.out.println();
	}
}
