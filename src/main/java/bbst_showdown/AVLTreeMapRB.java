package bbst_showdown;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * A rank-balanced AVL tree implementation with only one extra bit per node -
 * (delta rank is one or two by definition of the rank-balanced AVL tree).
 * 
 * This code is based on the paper "Rank Balanced Trees".
 * 
 * At the time I wrote it I found no other similar implementations.
 * 
 * TODO
 * I didn't code the delete method yet.  If you need it email me, minimum 1 day of work needed.
 * 
 * @author David McManamon
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class AVLTreeMapRB<K, V> extends AbstractMap<K, V> {
    // every node except the root must have a delta r of 1 or 2
    protected static final boolean ONE = true;
    protected static final boolean TWO = false;
    
    protected transient Entry<K, V> root = null;

    /**
     * The number of entries in the tree
     */
    protected transient int size = 0;
    
    /**
     * The comparator used to maintain order in this tree map, or
     * null if it uses the natural ordering of its keys.
     *
     * @serial
     */
    protected final Comparator<? super K> comparator;
    
    /**
     * The number of structural modifications to the tree.
     */
    protected transient int modCount = 0;
    
    protected transient int rotations = 0;
    
    /**
     * Constructs a new, empty tree map, using the natural ordering of its
     * keys.  All keys inserted into the map must implement the {@link
     * Comparable} interface.  Furthermore, all such keys must be
     * <em>mutually comparable</em>: {@code k1.compareTo(k2)} must not throw
     * a {@code ClassCastException} for any keys {@code k1} and
     * {@code k2} in the map.  If the user attempts to put a key into the
     * map that violates this constraint (for example, the user attempts to
     * put a string key into a map whose keys are integers), the
     * {@code put(Object key, Object value)} call will throw a
     * {@code ClassCastException}.
     */
    public AVLTreeMapRB() {
	comparator = null;
    }
    
    /**
     * Constructs a new tree map containing the same mappings as the given
     * map, ordered according to the <em>natural ordering</em> of its keys.
     * All keys inserted into the new map must implement the {@link
     * Comparable} interface.  Furthermore, all such keys must be
     * <em>mutually comparable</em>: {@code k1.compareTo(k2)} must not throw
     * a {@code ClassCastException} for any keys {@code k1} and
     * {@code k2} in the map.  This method runs in n*log(n) time.
     *
     * @param  m the map whose mappings are to be placed in this map
     * @throws ClassCastException if the keys in m are not {@link Comparable},
     *         or are not mutually comparable
     * @throws NullPointerException if the specified map is null
     */
    public AVLTreeMapRB(Map<? extends K, ? extends V> m) {
        comparator = null;
        putAll(m);
    }
    
    public int treeHeight() {
	return treeHeight(root) - 1;
    }

    protected int treeHeight(Entry<K, V> node) {
	if (node == null)
	    return 0;
	return (1 + Math.max(treeHeight(node.left), treeHeight(node.right)));
    }
    
    public int rotations() {
	return rotations;
    }

    public String toString() {
	return "Rank balanced AVL tree of size: " + size + ", height: " + treeHeight() + ", rotations " + rotations;
    }
    
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }
    
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code key} compares
     * equal to {@code k} according to the map's ordering, then this
     * method returns {@code v}; otherwise it returns {@code null}.
     * (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <em>necessarily</em>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    public V get(Object key) {
        Entry<K,V> p = getEntry(key);
        return (p==null ? null : p.value);
    }
	
	/**
     * Node in the Tree.  
     * Doubles as a means to pass key-value pairs back to
     * user (see Map.Entry).
     */
    static final class Entry<K,V> implements Map.Entry<K,V> {
        K key;
        V value;
        Entry<K,V> left = null;
        Entry<K,V> right = null;
        Entry<K,V> parent = null;
        boolean deltaR = ONE;

        /**
         * Make a new cell with given key, value, and parent, and with
         * {@code null} child links, and delta r of 1
         */
        Entry(K key, V value, Entry<K,V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        /**
         * Returns the key.
         *
         * @return the key
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value associated with the key.
         *
         * @return the value associated with the key
         */
        public V getValue() {
            return value;
        }

        /**
         * Replaces the value currently associated with the key with the given
         * value.
         *
         * @return the value associated with the key before this method was
         *         called
         */
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

            return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
        }

        public int hashCode() {
            int keyHash = (key==null ? 0 : key.hashCode());
            int valueHash = (value==null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public String toString() {
            return key + "=" + value + "," + deltaR;
        }
    }
    
    /**
     * Returns this map's entry for the given key, or {@code null} if the map
     * does not contain an entry for the key.
     *
     * @return this map's entry for the given key, or {@code null} if the map
     *         does not contain an entry for the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    final Entry<K,V> getEntry(Object key) {
    		// Offload comparator-based version for sake of performance
        if (comparator != null)
            return getEntryUsingComparator(key);
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked")
            Comparable<? super K> k = (Comparable<? super K>) key;
        Entry<K,V> p = root;
        while (p != null) {
            int cmp = k.compareTo(p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
    }
    
    /**
     * Version of getEntry using comparator. Split off from getEntry
     * for performance. (This is not worth doing for most methods,
     * that are less dependent on comparator performance, but is
     * worthwhile here.)
     */
    final Entry<K,V> getEntryUsingComparator(Object key) {
        @SuppressWarnings("unchecked")
            K k = (K) key;
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            Entry<K,V> p = root;
            while (p != null) {
                int cmp = cpr.compare(k, p.key);
                if (cmp < 0)
                    p = p.left;
                else if (cmp > 0)
                    p = p.right;
                else
                    return p;
            }
        }
        return null;
    }
    
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     *
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    public V put(K key, V value) {
	Entry<K, V> t = root;
	if (t == null) {
	    compare(key, key); // type (and possibly null) check

	    root = new Entry<>(key, value, null);
	    size = 1;
	    modCount++;
	    return null;
	}
	int cmp;
	Entry<K, V> parent;
	// split comparator and comparable paths
	Comparator<? super K> cpr = comparator;
	if (cpr != null) {
	    do {
		parent = t;
		cmp = cpr.compare(key, t.key);
		if (cmp < 0)
		    t = t.left;
		else if (cmp > 0)
		    t = t.right;
		else
		    return t.setValue(value);
	    } while (t != null);
	} else {
	    if (key == null)
		throw new NullPointerException();
	    @SuppressWarnings("unchecked")
	    Comparable<? super K> k = (Comparable<? super K>) key;
	    do {
		parent = t;
		cmp = k.compareTo(t.key);
		if (cmp < 0)
		    t = t.left;
		else if (cmp > 0)
		    t = t.right;
		else
		    return t.setValue(value);
	    } while (t != null);
	}

	Entry<K, V> e = new Entry<>(key, value, parent);
	Entry<K, V> sibling;
	if (cmp < 0) {
	    parent.left = e;
	    sibling = parent.right;
	} else {
	    parent.right = e;
	    sibling = parent.left;
	}

	if (sibling == null)
	    fixAfterInsertion(parent);

	size++;
	modCount++;
	return null;
    }
    
    /**
If the path of incremented ranks reaches the root of the tree, then the rebalancing procedure stops.
If the path of incremented ranks reaches a node whose parent's rank previously differed by two, the rebalancing procedure stops.
If the procedure increases the rank of a node x, so that it becomes equal to the rank of the parent y of x, 
	but the other child of y has a rank that is smaller by two (so that the rank of y cannot be increased) 
	then again the rebalancing procedure stops after performing rotations necessary.
     */
    private void fixAfterInsertion(Entry<K, V> x) {
	while (x.deltaR != TWO && x.parent != null) { 
	    Entry<K, V> p = x.parent;
	    if (p.left == x) { // node was added on left so check if left side is unbalanced
		Entry<K, V> sibling = p.right;
		if (sibling == null || sibling.deltaR == TWO) { // need to rebalance
		    if (sibling != null)
			sibling.deltaR = ONE;
		    
		    if (x.right != null) {
			 if(x.right.deltaR == ONE) {
			     if (x.left != null) x.left.deltaR = ONE;
			     rotateLeft(x);
			 } else
			     x.right.deltaR = ONE;
		    } 
		    
		    if (p.deltaR == TWO) {  // maintain delta 2 at parent
			p.deltaR = ONE;
			p.left.deltaR = TWO;
		    }
		    rotateRight(p);
		    return;
		} else if (sibling.deltaR == ONE) {
		    sibling.deltaR = TWO;
		} 
	    } else { // checking right side heavy
		Entry<K, V> sibling = p.left;
		if (sibling == null || sibling.deltaR == TWO) { // need to rebalance
		    if (sibling != null)
			sibling.deltaR = ONE;
		    
		    if (x.left != null) {
			if (x.left.deltaR == ONE) {
			    if (x.right != null) x.right.deltaR = ONE;
			    rotateRight(x);
			} else
			    x.left.deltaR = ONE;
		    }
		    
		    if (p.deltaR == TWO) {  // maintain delta 2 at parent
			p.deltaR = ONE;
			p.right.deltaR = TWO;
		    }
		    rotateLeft(p);
		    return;
		} else if (sibling.deltaR == ONE) {
		    sibling.deltaR = TWO;
		} 
	    }
	    
	    x = x.parent;
	}
	if (x.deltaR == TWO)
	    x.deltaR=ONE;
    }

    
    /** From CLR */
    private void rotateLeft(Entry<K, V> p) {
	Entry<K, V> r = p.right;
	p.right = r.left;
	if (r.left != null)
	    r.left.parent = p;
	r.parent = p.parent;
	if (p.parent == null)
	    root = r;
	else if (p.parent.left == p)
	    p.parent.left = r;
	else
	    p.parent.right = r;
	r.left = p;
	p.parent = r;
	rotations++;
    }

    /** From CLR */
    private void rotateRight(Entry<K, V> p) {
	Entry<K, V> l = p.left;
	p.left = l.right;
	if (l.right != null)
	    l.right.parent = p;
	l.parent = p.parent;
	if (p.parent == null)
	    root = l;
	else if (p.parent.right == p)
	    p.parent.right = l;
	else
	    p.parent.left = l;
	l.right = p;
	p.parent = l;
	rotations++;
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     *
     * @param  key key for which mapping should be removed
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    public V remove(Object key) {
	Entry<K, V> p = getEntry(key);
	if (p == null)
	    return null;

	V oldValue = p.value;
	deleteEntry(p);
	return oldValue;
    }
    
    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(Entry<K,V> p) {
        modCount++;
        size--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
            Entry<K,V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        Entry<K,V> replacement = (p.left != null ? p.left : p.right);

        if (replacement != null) {
            // Link replacement to parent
	    replacement.parent = p.parent;
	    Entry<K, V> mirror = null;
	    if (p.parent == null) {
		root = replacement;
		return;
	    } else if (p == p.parent.left) {
		p.parent.left = replacement;
		mirror = p.parent.right;
	    } else {
		p.parent.right = replacement;
		mirror = p.parent.left;
	    }

	    // Null out links so they are OK to use by fixAfterDeletion.
	    p.left = p.right = p.parent = null;
	    //TODO
	    fixAfterDeletion(replacement.parent, mirror);
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
            // TODO check if null check necessary?
            Entry<K, V> fixPoint = p.parent;
            @SuppressWarnings("unused")
	    Entry<K, V> sibling = null;

	    if (p == p.parent.left) {
		p.parent.left = null;
		sibling = fixPoint.right;
	    } else if (p == p.parent.right) {
		p.parent.right = null;
		sibling = fixPoint.left;
	    }
	    p.parent = null;

	    // TODO
//	    if (mirror == null || (fixPoint.rank - p.rank >= 2) || (fixPoint.rank - mirror.rank) != 1 ) {
		fixAfterDeletion(fixPoint, null);
//	    }
        }
    }
    
    private void fixAfterDeletion(Entry<K, V> p, Entry<K,V> mirror) {
	throw new RuntimeException();
    }
    
    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
    	modCount++;
        size = 0;
        root = null;
        rotations = 0;
    }
    
    /**
     * Test two values for equality.  Differs from o1.equals(o2) only in
     * that it copes with {@code null} o1 properly.
     */
    static final boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }

    /**
     * Compares two keys using the correct comparison method for this TreeMap.
     */
    @SuppressWarnings("unchecked")
    final int compare(Object k1, Object k2) {
	return comparator == null ? ((Comparable<? super K>) k1).compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
    }

    /**
     * Returns the key corresponding to the specified Entry.
     * 
     * @throws NoSuchElementException
     *             if the Entry is null
     */
    static <K> K key(Entry<K,?> e) {
        if (e==null)
            throw new NoSuchElementException();
        return e.key;
    }
	
	/**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    final Entry<K,V> getFirstEntry() {
        Entry<K,V> p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }

    /**
     * Returns the last Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    final Entry<K,V> getLastEntry() {
        Entry<K,V> p = root;
        if (p != null)
            while (p.right != null)
                p = p.right;
        return p;
    }
    
    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    static <K,V> Entry<K,V> successor(Entry<K,V> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            Entry<K,V> p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            Entry<K,V> p = t.parent;
            Entry<K,V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Returns the predecessor of the specified Entry, or null if no such.
     */
    static <K,V> Entry<K,V> predecessor(Entry<K,V> t) {
        if (t == null)
            return null;
        else if (t.left != null) {
            Entry<K,V> p = t.left;
            while (p.right != null)
                p = p.right;
            return p;
        } else {
            Entry<K,V> p = t.parent;
            Entry<K,V> ch = t;
            while (p != null && ch == p.left) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
	// TODO Auto-generated method stub
	return null;
    }
    
    public void inOrderTraversal(Entry<K, V> x) {
	if (x == null)
	    return;
	inOrderTraversal(x.left);
	System.out.println(x.value + ", " + x.deltaR);
	inOrderTraversal(x.right);
    }
    
    public boolean identicalTrees(Entry<K, V> a, AVLTreeMap.Entry<K, V> b) {
	/* 1. both empty */
	if (a == null && b == null)
	    return true;

	/* 2. both non-empty -> compare them */
	if (a != null && b != null)
	    return (a.value == b.value && identicalTrees(a.left, b.left) && identicalTrees(a.right, b.right));

	/* 3. one empty, one not -> false */
	return false;
    }
}