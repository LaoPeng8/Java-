package org.pjj.list_.vector_;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Vector 与 ArrayList 基本一样, 都继承至 java.util.List接口, 底层都是数组
 * 区别在于: 扩容机制不太一样
 * 区别在于: Vector的方法都是加了关键字 synchronized , 即线程安全的, 不过由于加了锁, 所以效率相对ArrayList较低.
 * 区别在与: 从源码也可以看出来 ArrayList调无参构造时 底层数组初始化为 空, 当调用add()方法后 才会扩容为默认长度 10
 *          而 Vector调无参构造时 是直接初始化长度为 10,   有参构造都一样 指定长度是多少就直接 new 多大的数组
 *
 *
 * @author PengJiaJun
 * @Date 2022/07/05 21:12
 */
public class VectorSource {
    public static void main(String[] args) {
        new Vector();
        new ArrayList<>();

    }
}
