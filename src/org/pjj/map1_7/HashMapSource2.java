package org.pjj.map1_7;

import java.util.HashMap;
import java.util.Iterator;

/**
 * HashMap中的属性 modCount 与 java.util.ConcurrentModificationException 异常 出现的原因
 *
 * @author PengJiaJun
 * @Date 2022/07/08 20:58
 */
public class HashMapSource2 {
    public static void main(String[] args) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("1", "1");
        hashMap.put("2", "2");

        // 出现异常 java.util.ConcurrentModificationException
//        for(String key : hashMap.keySet()) {
//            if(key.equals("2")) {
//                hashMap.remove(key);
//            }
//        }


        /**
         * 上面这段代码编译后是这样的 (foreach底层也是 iterator 实现的, 所以说编译后是这样的)
         *
         * 为什么会出现异常 java.util.ConcurrentModificationException ?
         * 首先先看 这个 Iterator 迭代器对象是哪里来的
         * hashMap.keySet().iterator() 看看keySet()对象哪里来的, 点进去看看就是这样的
         * keySet = new KeySet() 可以看到实质是new了一个 KeySet() 构造器, 点进去看看
         * iterator() return newKeyIterator(); 可以看到 这个 KeySet()对象的 iterator()方法返回的迭代器是调用 newKeyIterator() 获取的, 点进去看看
         * new KeyIterator() 发现内部是 new的一个 KeyIterator() 迭代器, 点进去看看
         *     private final class KeyIterator extends HashIterator<K> {
         *         public K next() {
         *             return nextEntry().getKey();
         *         }
         *     }
         *      发现继承自 HashIterator 迭代器, 由于初始化该类, 会先调用父类的构造方法, 所以点进去看看
         *      expectedModCount = modCount; 发现HashIterator迭代器的构造方法中将  HashMap中的 modCount属性赋值给了 expectedModCount
         *      那么此时 HashMap是调用了两次 put() 方法 所以 modCount = 2; (modCount表示被修改的次数, put, remove 都会导致modCount++)
         *      那么此时 modCount = 2; expectedModCount = 2;
         *
         *      发现重写了 next()方法, next方式实质先需要调用 nextEntry()方法, 点进去看看
         *          if (modCount != expectedModCount)
         *              throw new ConcurrentModificationException();
         *      发现 nextEntry()方法中 会先判断 mouCount != expectedModCount就会抛出异常.
         * 那么到这里, 基本上也就解谜了, 在迭代器构造方法中 expectedModCount被赋值为 2, 当调用 next()方法时 2 == 2; 没有问题
         * 然后执行 hashMap.remove(key) 当, 该元素被删除后, HashMap的remove()方法中 会mouCount++,
         * 然后继续循环, 执行 next()方法, 然后啪 2 != 3 (modCount = 3; expectedModCount = 2;), 那么就是抛出了 java.util.ConcurrentModificationException
         *
         * 解决就是使用 这个迭代器提供的 remove() 方法删除当前元素
         * remove()方法中有一行 expectedModCount = modCount; 那么就是 在删除完当前元素后,modCount不是会++嘛, 所以 expectedModCount = modCount;
         * 就不会出现 modCount = 3; expectedModCount = 2; 的情况了, 而是 modCount = 3; expectedModCount = 3;
         *
         * 对应的解决方法还有第二种, 就是使用 concurrentHashMap(), 多线程环境下的 HashMap
         *
         */
        Iterator i$ = hashMap.keySet().iterator();//该迭代器真正的类型 KeyIterator extends HashIterator<K>

        while(i$.hasNext()) {
            String key = (String)i$.next();
            if (key.equals("2")) {
//                hashMap.remove(key);
                i$.remove();
            }
        }

    }
}
