CREATE OR REPLACE AND COMPILE JAVA SOURCE NAMED DISTINCT_RANK_JAVA_CONTEXT
AS
package org.babyfish.hibernate.dialect.oracle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import oracle.sql.ROWID;

/*
 * Make sure JDK1.4 can compile the source code to 
 * support low version oracle embedded JVM.
 */
public final class DistinctRankContext {
    
    /*
     * Be different with generic JVM, Oracle startup an isolated JVM
     * for each session. For one isolated JVM, it's a single-thread 
     * environment, if user create some java threads, they will be executed one
     * OS thread.
     * 
     * So static variables may not be accessed by multiple threads.
     */
    private static final Map INSTANCE_MAP = new HashMap();
    
    private static int contextIdSequence;

    // Key: ROWID.stringValue(), Value: ROWIDRank
    // The key has to be String, why dose not oracle.sql.ROWID support hashCode and equals?
    private Map rankMap;
    
    private IntRedBlackTree removedRanks;
    
    private int rankSequence;
    
    private int currentRank;
    
    private DistinctRankContext() {
        this.reset();
    }
    
    public static int allocateContext() {
        int contextId = ++contextIdSequence;
        INSTANCE_MAP.put(new Integer(contextId), new DistinctRankContext());
        return contextId;
    }
    
    public static void resetContext(int contextId) {
        context(contextId).reset();
    }
    
    public static void freeContext(int contextId) {
        INSTANCE_MAP.remove(new Integer(contextId));
    }
    
    public static void iterate(int contextId, ROWID rowid) {
        context(contextId).add(rowid.stringValue(), 1);
    }
    
    public static void delete(int contextId, ROWID rowid) {
        context(contextId).delete(rowid);
    }
    
    public static void merge(int contextId1, int contextId2) {
        context(contextId1).merge(context(contextId2));
        freeContext(contextId2); //Very important to free the resource of second context.
    }
    
    public static int rank(int contextId) {
        return context(contextId).rank();
    }
    
    private static DistinctRankContext context(int contextId) {
        DistinctRankContext ctx = (DistinctRankContext)INSTANCE_MAP.get(new Integer(contextId));
        if (ctx == null) {
            throw new IllegalArgumentException("No such context " + contextId);
        }
        return ctx;
    }
    
    private void reset() {
        this.rankMap = new HashMap();
        this.removedRanks = new IntRedBlackTree();
        this.rankSequence = 0;
        this.currentRank = 0;
    }
    
    private void add(String rowidString, int repeatCount) {
        Map map = this.rankMap;
        ROWIDRank rowidRank = (ROWIDRank)map.get(rowidString);
        if (rowidRank == null) {
            rowidRank = new ROWIDRank(++this.rankSequence, repeatCount);
            map.put(rowidString, rowidRank);
        } else {
            rowidRank.repeatCount += repeatCount;
        }
        this.currentRank = rowidRank.rank;
    }
    
    private void delete(ROWID rowid) {
        Map map = this.rankMap;
        String key = rowid.stringValue();
        ROWIDRank rowidRank = (ROWIDRank)map.get(key);
        if (rowidRank != null && --rowidRank.repeatCount == 0) {
            map.remove(key);
            this.removedRanks.add(rowidRank.rank);
        }
    }
    
    private void merge(DistinctRankContext mergedCtx) {
        if (this == mergedCtx) {
            return;
        }
        Iterator entryItr = mergedCtx.rankMap.entrySet().iterator();
        while (entryItr.hasNext()) {
            Entry entry = (Entry)entryItr.next();
            this.add((String)entry.getKey(), ((ROWIDRank)entry.getValue()).repeatCount);
        }
    }
    
    private int rank() {
        return this.currentRank - this.removedRanks.headSize(this.currentRank);
    }
    
    private static class ROWIDRank {
        
        int rank;
        
        int repeatCount;
        
        public ROWIDRank(int rank, int repeatCount) {
            this.rank = rank;
            this.repeatCount = repeatCount;
        }
    };
    
    /*
     * java.util.TreeSet<E> supports headSet(toElement) view,
     * but the performance of "headSet(max).size()" is not high.
     * 
     * org.babyfish.collect.TreeSet<E> support the the "headSet(max).size()" 
     * with high performance, but this java class is used by Oracle embedded JVM 
     * whose version may be very low so that it is impossible to let this class 
     * depend on babyfish collection framework(It is not good idea too even if
     * the Oracle use high version JVM).
     * 
     * So, I create this class.
     */
    private static class IntRedBlackTree {
        
        private IntNode root;
        
        public void add(int value) {
            if (this.root == null) {
                this.root = new IntNode(value);
                return;
            }
            IntNode node = this.root;
            IntNode parent = null;
            int cmp = 0;
            do {
                parent = node;
                cmp = value - node.value;
                if (cmp < 0) {
                    node = node.left;
                } else if (cmp > 0) {
                    node = node.right;
                } else {
                    node.value = value;
                    return;
                }
            } while (node != null);
            IntNode newNode = new IntNode(parent, value);
            if (cmp < 0) {
                parent.left = newNode;
            } else {
                parent.right = newNode;
            }
            while (parent != null) {
                parent.size++;
                parent = parent.parent;
            }
            this.fixAfterInsertion(newNode);
        }
        
        public int headSize(int maxExclusive) {
            if (this.root == null) {
                return 0;
            }
            int size = this.root.size;
            IntNode high = this.lower(maxExclusive);
            if (high == null) {
                return 0;
            }
            size -= sizeOf(high.right);
            while (true) {
                IntNode hp = high.parent;
                if (hp == null) {
                    break;
                }
                if (hp.left == high) {
                    size -= sizeOf(hp.right);
                    size--;
                }
                high = hp;
            }
            return size;
        }
        
        private void fixAfterInsertion(IntNode x) {

            x.red = true;

            /*
             * We must use left parenthesis after "&&", 
             * because the annoying sqlplus is not smart enough,
             * If we don't do that, it will consider "x" as a 
             * variable whose value must be inputed by keyboard.  
             */
            while ((x != null) && (x != this.root) && (x.parent.red)) {
                if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                    IntNode y = rightOf(parentOf(parentOf(x)));
                    if (isRed(y)) {
                        setRed(parentOf(x), false);
                        setRed(y, false);
                        setRed(parentOf(parentOf(x)), true);
                        x = parentOf(parentOf(x));
                    } else {
                        if (x == rightOf(parentOf(x))) {
                            x = parentOf(x);
                            rotateLeft(x);
                        }
                        setRed(parentOf(x), false);
                        setRed(parentOf(parentOf(x)), true);
                        rotateRight(parentOf(parentOf(x)));
                    }
                } else {
                    IntNode y = leftOf(parentOf(parentOf(x)));
                    if (isRed(y)) {
                        setRed(parentOf(x), false);
                        setRed(y, false);
                        setRed(parentOf(parentOf(x)), true);
                        x = parentOf(parentOf(x));
                    } else {
                        if (x == leftOf(parentOf(x))) {
                            x = parentOf(x);
                            rotateRight(x);
                        }
                        setRed(parentOf(x), false);
                        setRed(parentOf(parentOf(x)), true);
                        rotateLeft(parentOf(parentOf(x)));
                    }
                }
            }

            this.root.red = false;
        }
        
        private IntNode lower(int value) {
            IntNode p = this.root;
            while (p != null) {
                if (value > p.value) {
                    if (p.right != null) {
                        p = p.right;
                    }
                    else {
                        return p;
                    }
                } else {
                    if (p.left != null) {
                        p = p.left;
                    } else {
                        IntNode parent = p.parent;
                        IntNode ch = p;
                        /*
                         * We must use left parenthesis after "&&", 
                         * because the annoying sqlplus is not smart enough,
                         * If we don't do that, it will consider "ch" as a 
                         * variable whose value must be inputed by keyboard.  
                         */
                        while ((parent != null) && (ch == parent.left)) {
                            ch = parent;
                            parent = parent.parent;
                        }
                        return parent;
                    }
                }
            }
            return null;
        }
        
        private static boolean isRed(IntNode p) {
            return (p == null ? false : p.red);
        }

        private static void setRed(IntNode p, boolean red) {
            if (p != null) {
                p.red = red;
            }
        }
        
        private static IntNode parentOf(IntNode p) {
            return (p == null ? null : p.parent);
        }

        private static IntNode leftOf(IntNode p) {
            return (p == null) ? null : p.left;
        }

        private static IntNode rightOf(IntNode p) {
            return (p == null) ? null : p.right;
        }
        
        private static int sizeOf(IntNode p) {
            return (p == null) ? 0 : p.size;
        }
        
        private void rotateLeft(IntNode p) {
            if (p != null) {
                IntNode r = p.right;
                p.right = r.left;
                if (r.left != null) {
                    r.left.parent = p;
                }
                r.parent = p.parent;
                if (p.parent == null) {
                    this.root = r;
                } else if (p.parent.left == p) {
                    p.parent.left = r;
                } else {
                    p.parent.right = r;
                }
                r.left = p;
                p.parent = r;
                p.size = 1 + sizeOf(p.left) + sizeOf(p.right);
                r.size = 1 + sizeOf(r.left) + sizeOf(r.right);
            }
        }

        private void rotateRight(IntNode p) {
            if (p != null) {
                IntNode l = p.left;
                p.left = l.right;
                if (l.right != null) {
                    l.right.parent = p;
                }
                l.parent = p.parent;
                if (p.parent == null) {
                    this.root = l;
                } else if (p.parent.right == p) {
                    p.parent.right = l;
                } else {
                    p.parent.left = l;
                }
                l.right = p;
                p.parent = l;
                p.size = 1 + sizeOf(p.left) + sizeOf(p.right);
                l.size = 1 + sizeOf(l.left) + sizeOf(l.right);
            }
        }
    }
    
    private static class IntNode {
        
        IntNode parent;
        
        IntNode left;
        
        IntNode right;
        
        int value;
        
        boolean red;
        
        /*
         * The node count of this sub tree
         * (Include the current node itself)
         */
        int size = 1;
        
        public IntNode(int value) {
            this.value = value;
        }
        
        public IntNode(IntNode parent, int value) {
            this.parent = parent;
            this.value = value;
        }
    }
};
