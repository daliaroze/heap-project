/**
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min=null;
    public HeapNode head=null;
    public int size=0;
    public int numTrees=0;
    public int numMarkedNodes=0;
    public int links=0;
    public int cuts=0;
    public int numHeapifyUp=0;
    
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     * Time complexity (Worst Case): O(log n) if lazyMelds=false, 
     * O(1) if lazyMelds=true.
     */
    public HeapItem insert(int key, String info) 
    {   
    	// Create a temporary heap for the new node
    	HeapNode newNode = new HeapNode(key, info);
    	Heap tempHeap = new Heap(this.lazyMelds,this.lazyDecreaseKeys);
		tempHeap.head = newNode;
		tempHeap.min = newNode.item;
		tempHeap.numTrees = 1;
		tempHeap.size = 1;
		this.meld(tempHeap);
		return newNode.item;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     * Time complexity: O(1)
     */
    public HeapItem findMin()
    {
    	return this.min;
    }

    /**
     * 
     * Delete the minimal item.
     *Time complexity:
     * If lazyMelds = true: O(n). 
     * If lazyMelds = false: O(log n).
     */
    public void deleteMin()
    {
        if (this.min == null) {  // empty heap
            return;
        }

        if (this.size == 1) { // tree has one node
            this.head = null;
            this.min = null;
            this.size = 0;
            this.numTrees = 0;
            this.numMarkedNodes = 0;
            return;
        }
        HeapNode minNode = this.min.node;
        minNode.prev.next = minNode.next;  //remove min from tree list
        minNode.next.prev = minNode.prev;
        if (this.head == minNode) {
            this.head = minNode.next;}
        this.size--;        
        this.numTrees--;
        if (this.numTrees == 0) {
            this.head = null;
            this.min = null;
        }
        if (minNode.child != null) { //Add all children of min to root list
            Heap childrenHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
            HeapNode firstChild = minNode.child;
            HeapNode curr = firstChild;
            int childCount = 0;
            
            while (true) {
                curr.parent = null;
                if (curr.marked) {
                    curr.marked = false;
                    this.numMarkedNodes--;
                }
                childCount++;
                curr = curr.next;

                if (curr == firstChild) {
                    break;
                }
            }
            childrenHeap.head = firstChild;
            childrenHeap.min = firstChild.item;   // temp
            childrenHeap.numTrees = childCount;
            minNode.child = null;   
            this.meld(childrenHeap);
        }
       
        successive_link();
        
    }
    
    /**
     * Helper: Links two trees of the same rank.
     * The node with the smaller key becomes the parent.
     * Time complexity: O(1)
     */
    public HeapNode Link(HeapNode x,HeapNode y) {
        if (x.item.key > y.item.key) { // x always the smallest
            HeapNode tmp = x;
            x = y;
            y = tmp;
        }
        y.parent = x; //y becomes x's child
        if (y.marked) {
            y.marked = false;
            numMarkedNodes--;
        }

        if (x.child == null) { //x doesnt have kids
            y.next = y;
            y.prev = y;
            x.child = y;
        } else {
            HeapNode c = x.child;
            HeapNode last = c.prev;
            last.next = y;
            y.prev = last;
            y.next = c;
            c.prev = y;
        }
        x.rank++;
        this.links++;
        return x;
    }
    
    /**
     * Helper: Consolidates the root list so no two trees have the same rank.
     * * Time complexity:
     * If lazyMelds = true: O(n). 
     * If lazyMelds = false: O(log n).
     */
    public void successive_link() {
    	double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
    	int maxRank = (int) (Math.log(this.size) / Math.log(phi)) + 2;
    	HeapNode[] buckets = new HeapNode[maxRank];
    	for (int i = 0; i < maxRank; i++) {
    	    buckets[i] = null;
    	}
        //to-buckets 
        HeapNode start = this.head;
        start.prev.next = null;
        HeapNode curr = start;
        while (curr != null) {
            HeapNode x = curr;
            curr = curr.next;   
            x.parent = null;    
            int r = x.rank;
            while (buckets[r] != null) {
                HeapNode y = buckets[r];
                buckets[r] = null;
                x = Link(x, y);   // same rank
                r = x.rank;
            }

            buckets[r] = x;
        }
        // from-buckets 
        this.head = null;
        this.min = null;
        this.numTrees = 0;
        for (int i = 0; i < maxRank; i++) {
            HeapNode x = buckets[i];
            if (x != null) {
                if (this.head == null) {
                    x.next = x;
                    x.prev = x;
                    this.head = x;
                    this.min = x.item;
                } else {
                    HeapNode last = this.head.prev;
                    last.next = x;
                    x.prev = last;
                    x.next = this.head;
                    this.head.prev = x;
        
                    if (x.item.key < this.min.key) {  // Update the minimum pointer during the scan
                        this.min = x.item;
                    }
                }

                this.numTrees++;
            }
        }
    }
    
    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * Time complexity: 
     * If lazyDecreaseKeys = false (Binomial/Lazy Binomial): O(log n).
     * If lazyDecreaseKeys = true AND lazyMelds = true (Fibonacci): O(n).
     * If lazyDecreaseKeys = true AND lazyMelds = false (With Cuts): O(n log n).
     * 
     */
    public void decreaseKey(HeapItem x, int diff) 
    {    
    	x.key-= diff;	
    	HeapNode node=x.node;
    	if (this.min == null || x.key < this.min.key) {
    	    this.min = x;
    	}
    	// Check if heap property is violated
    	if (node.parent == null || (node.parent!=null && x.key >= node.parent.item.key)) {
    		return;
    	}
    	if (this.lazyDecreaseKeys) {
    		cascadingCut(node,node.parent);
    	}
    	else {
    		heapifyUp(node);
    	}
    	

        
    }
    /**
     * Helper: Performs cascading cut operation for Fibonacci logic.
     * Time complexity:
     * if lazyMelds=true: O(n).
     * if lazyMelds=false: O(n log n).
     */
    public void cascadingCut(HeapNode x, HeapNode parent) {
        cut(x, parent);
        if (parent.parent != null) {
            if (!parent.marked) {
                parent.marked = true;
                this.numMarkedNodes++;
            } else {
                cascadingCut(parent, parent.parent);
            }
        }
    }
    /**
     * Helper: Cuts node x from parent y and adds x to root list.
     * Time complexity: 
     * if lazyMelds = true: O(1).
     * if lazyMelds = false: O(log n).
     */
    public void cut(HeapNode x, HeapNode y) {   
        x.parent = null; // x becomes a root
        if (x.marked) {
            x.marked = false;
            this.numMarkedNodes--;
        }
        // remove x from y's child list
        if (x.next == x) {
            y.child = null;
        } else {
            x.prev.next = x.next;
            x.next.prev = x.prev;
            if (y.child == x) {
                y.child = x.next;
            }
        }
        y.rank--;
        x.next = x;
        x.prev = x;
        Heap singleHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        singleHeap.head = x;
        singleHeap.min = x.item;
        singleHeap.numTrees = 1;
        this.meld(singleHeap);
        this.cuts++;
    }
    /**
     * Helper: Bubbles up the node to fix heap property (Binomial logic).
     * Time complexity: O(log n) (Tree height).
     */
    public void heapifyUp(HeapNode x) {
    	int counter=0;
    	while (x.parent != null && x.item.key < x.parent.item.key) {
    		counter++;
    		swap(x,x.parent);
    		x=x.parent; // Move up pointer
    		
    	}
    	this.numHeapifyUp+= counter;
    }
    /**
     * Helper: Swaps the Items of two nodes.
     * Time complexity: O(1).
     */
     public void swap(HeapNode x,HeapNode y) {
    	    HeapItem tmp = x.item;
    	    x.item = y.item;
    	    y.item = tmp;

    	    x.item.node = x;
    	    y.item.node = y;
  
     }

    /**
     * 
     * Delete the x from the heap.
     *Time complexity:
     * O(nlogn). 
     * Reason: Calls decreaseKey (O(nlogn)or O(n) or O(log n)) and deleteMin (O(n) or O(log n))
     */
    public void delete(HeapItem x) 
    {    
    	if (x == null) {
			return; 
		}
        decreaseKey(x, Integer.MAX_VALUE);
		deleteMin();
		return;
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *Time complexity:
     * If lazyMelds = true: O(1).
     * If lazyMelds = false: O(log n).
     */
    public void meld(Heap heap2)
    {
    	if (heap2 == null || heap2.head ==null) {
			return;
		}
		boolean heap1_empty = this.head == null;
		if (heap1_empty) {
			this.head = heap2.head;
			this.min = heap2.min;
		}
		this.size += heap2.size;
		this.numTrees += heap2.numTrees;
		this.numMarkedNodes += heap2.numMarkedNodes;
		this.links += heap2.links;
		this.cuts += heap2.cuts;
		this.numHeapifyUp += heap2.numHeapifyUp;
		
		if (!heap1_empty) {
			HeapNode last1 = this.head.prev;
			HeapNode last2 = heap2.head.prev;

			last1.next = heap2.head;
			heap2.head.prev = last1;

			last2.next = this.head;
			this.head.prev = last2;
		}

		if (!heap1_empty && heap2.min.key < this.min.key) {
			this.min = heap2.min;
		}

		if (!lazyMelds) {
			successive_link();
		}         
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     * Time complexity: O(1)
     */
    public int size()
    {
        return size; 
    }


    /**
     * 
     * Return the number of trees in the heap.
     * Time complexity: O(1)
     */
    public int numTrees()
    {
        return numTrees; 
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * Time complexity: O(1)
     */
    public int numMarkedNodes()
    {
        return numMarkedNodes; 
    }
    
    
    /**
     * 
     * Return the total number of links.
     * Time complexity: O(1)
     */
    public int totalLinks()
    {
        return links; 
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * Time complexity: O(1)
     */
    public int totalCuts()
    {
        return cuts; 
    }
    

    /**
     * 
     * Return the total heapify costs.
     * Time complexity: O(1)
     */
    public int totalHeapifyCosts()
    {
       return numHeapifyUp;
    }
 
    

    
    /**
     * Class implementing a node in a ExtendedFibonacci Heap.
     *  
     */
    public static class HeapNode{
    	public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public boolean marked;
        
        /**
         * Constructor to initialize a new node.
         */
        public HeapNode(int key, String info) {
    		this.item = new HeapItem(key,info,this);
    		this.next = this;
    		this.prev = this;
    		}
    	
    }
    /**
     * Class implementing the Item (Key-Value) in the Heap.
     */
    public static class HeapItem{
        public HeapNode node;
        public int key;
        public String info;
        
        /**
         * Constructor to initialize a new item.
         */
        public HeapItem(int key, String info, HeapNode node) {
            this.key = key;
            this.info = info;
            this.node = node;
        }
    }

}
