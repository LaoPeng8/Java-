package org.pjj.map1_7.concurrent;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * concurrentHashMap 1.7 源码解读
 *
 * hashtable 与 hashmap的区别:
 *      1. 线程是否安全： HashMap 是非线程安全的，HashTable 是线程安全的,因为 HashTable 内部的方法基本都经过synchronized 修饰。
 *      2. 效率： 因为线程安全的问题，HashMap 要比 HashTable 效率高一点。另外，HashTable 基本被淘汰，不要在代码中使用它；
 *      3. 对 Null key 和 Null value 的支持：
 *          HashMap 可以存储 null 的 key 和 value，但 null 作为键只能有一个，null 作为值可以有多个；
 *          HashTable 不允许有 null 键和 null 值，否则会抛出 NullPointerException。
 *      4. 初始容量大小和每次扩充容量大小的不同 ：
 *          HashMap默认初始化长度为 16, 若指定长度, 会将该长度扩容到 2 的幂次方数(如: 17变为32), 之后每次扩容, 容量翻倍
 *          HashTable默认初始化长度为11， 若指定长度, 则直接初始化指定长度, 之后每次扩容, 容量变为原来的 2n + 1;
 *      5. 1.8的HashMap, 触发条件会转为 红黑树, HashTable不会.
 *
 * 那么此时需要一个线程安全且效率不低的HashMap, 此时就引出了 concurrentHashMap
 *
 *
 * @author PengJiaJun
 * @Date 2022/07/09 22:16
 */
public class ConcurrentHashMapSource {
    public static void main(String[] args) {
        /**
         * hashtable 与 hashmap 最大的区别就在于 一个线程安全, 一个线程不安全, 换言之就是hashtable的方法上加了synchronized, 而hashmap没有,
         * 这就导致, hashtable线程安全, 但是效率很低, 因为同时只能有一个线程操作(其他线程被synchronized挡住了)
         */
        Hashtable<String, String> hashtable = new Hashtable<>();
        new HashMap<String, String>();

        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put("name", "liukang");
        concurrentHashMap.putIfAbsent("name", "wanglei");//key值重复了, 则什么也不做, 返回该key的value
        System.out.println(concurrentHashMap.get("name"));

        System.out.println((int)(2 * 0.75));
    }
}
