package org.pjj.map1_8;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author PengJiaJun
 * @Date 2022/07/17 16:57
 */
public class HashMapSource {
    public static void main(String[] args) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("123", "2");
        String str = hashMap.get("123");


        /**
         * 很多人搞不清楚, 到底是链表长度 等于 8 才转RBTree, 还是 链表长度 等于 9 时才转RBTree, 这里具体分析一下
         *
         * 可以通过调整链表长度, 来观察 "链表转红黑树" 是否打印, 通过debug观察 binCount 的变化
         * 可以发现 当前链表长度为 7 加上当前插入节点 长度为 8, 不会触发转红黑树, 当前链表长度为8 加上 当前插入节点 长度为 9 才会触发转红黑树
         * AA  BB  CC  DD  EE  FF  GG  HH
         * 0   1   2   3   4   5   6   7
         *
         * 也就是
         */
        Node hh = newNode("HH".hashCode(), "HH", "HH", null);
        Node gg = newNode("GG".hashCode(), "GG", "GG", hh);
        Node ff = newNode("FF".hashCode(), "FF", "FF", gg);
        Node ee = newNode("EE".hashCode(), "EE", "EE", ff);
        Node dd = newNode("DD".hashCode(), "DD", "DD", ee);
        Node cc = newNode("CC".hashCode(), "CC", "CC", dd);
        Node bb = newNode("BB".hashCode(), "BB", "BB", cc);
        Node head = newNode("AA".hashCode(), "AA", "AA", bb);
        //首先有一条链表 "AA" -> "BB" -> "CC" -> "DD" -> "EE" -> "FF" -> "GG" -> "HH"

        String key = "II";
        String value = "II";
        Object k = null;
        int hash = key.hashCode();

        Node e;  Node p = head;
        for (int binCount = 0; ; ++binCount) {
            if ((e = p.next) == null) {
                p.next = newNode(hash, key, value, null);
                if (binCount >= 8 - 1) // -1 for 1st
                    System.out.println("treeifyBin(tab, hash);方法执行, 即开始 链表转红黑树" + "   binCount = " + binCount);
                break;
            }
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                break;
            p = e;
        }
    }

    /**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        HashMapSource.Node<K,V> next;

        Node(int hash, K key, V value, HashMapSource.Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    static Node newNode(int hash, String key, String value, Node next) {
        return new Node<>(hash, key, value, next);
    }
}
