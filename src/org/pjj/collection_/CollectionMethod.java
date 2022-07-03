package org.pjj.collection_;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection接口的方法用法
 * 由于Collection是个接口 于是采用它的子类 ArrayList 来进行讲解
 *
 * add:添加单个元素
 * remove:删除指定元素
 * contains:查找元素是否存在
 * size:获取元素个数
 * isEmpty:判断是否为空
 * clear:清空
 * addAll:添加多个元素
 * containsAll:查找多个元素是否都存在
 * removeAll:删除多个元素
 * 说明:以ArrayList实现类来演示。
 *
 * @author PengJiaJun
 * @Date 2022/07/03 21:07
 */
public class CollectionMethod {
    public static void main(String[] args) {
        List list = new ArrayList();

//        add:添加单个元素
        list.add("jack");
        list.add(10);// list.add(Integer.valueOf(10));
        list.add(true);
        System.out.println("list=" + list);//list=[jack, 10, true]


//        remove:删除指定元素
//        list.remove(0);//删除下标为 0 的元素
//        System.out.println("list=" + list);//list=[10, true]
//        list.remove(new Integer(10));//指定删除哪个元素
//        list.remove("jack");
//        System.out.println("list=" + list);//list=[true]

//        contains:查找元素是否存在
        System.out.println(list.contains("jack"));//true
        System.out.println(list.contains("jack~"));//false


//        size:获取元素个数
        System.out.println(list.size());//3


//        isEmpty:判断是否为空
        System.out.println(list.isEmpty());//false


//        clear:清空
//        list.clear();//清空集合, 相当于删除list中所有的元素
//        System.out.println(list.isEmpty());//true
//        System.out.println(list.size());//0


//        addAll:添加多个元素  boolean addAll(Collection<? extends E> c); 传入一个 Collection类型的元素, 即只要实现了Collection接口的对象都可以传进行
        List list2 = new ArrayList();
        list2.add("红楼梦");
        list2.add("三国演义");
        list.addAll(list2);//将 list2 中的每一个元素 add进 list集合中
        System.out.println("list=" + list);//list=[jack, 10, true, 红楼梦, 三国演义]


//        containsAll:查找多个元素是否都存在
        System.out.println(list.containsAll(list2));//true


//        removeAll:删除多个元素
        list.removeAll(list2);
        System.out.println("list=" + list);//list=[jack, 10, true]


    }
}
