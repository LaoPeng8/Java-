package org.pjj.list_;

import java.util.ArrayList;
import java.util.List;

/**
 * List 集合里添加了一些根据索引来操作集合元素的方法
 * 可以发现很多方法 和 List 的父接口 Collection 中提供的方法类似,可以说就是相当于 List 在 Collection的基础上重载了很多有着自己特色的增删查改方法,(特色指 下标)
 * 即 在各种方法上加入 index 来操作List.
 * (有一点比较好奇 LinkedList是基于双向链表实现的, LinkedList显然也是实现了List接口, 链表没有下标 那么它会画大力气来实现这些有索引的方法吗?)(显然是有这些方法的...)
 *
 * List接口的常用方法:
 * 1) void add(int index, Object ele): 在index位置插入ele元素
 * 2) boolean addAll(int index,Collection eles): 从index位置开始将eles中的所有元素添加进来
 * 3) Object get(int index): 获取指定index位置的元素
 * 4) int indexOf(Object obj): 返回obj在集合中首次出现的位置
 * 5) int lastIndexOf(Object obj):返回obj在当前集合中未次出现的位置
 * 6) Object remove(int index):移除指定index位置的元素，并返回此元素
 * 7) Object set(int index,Object ele):设置指定index位置的元素为ele ,相当于是替换.  返回先前位于指定位置的元素
 * 8) List subList(int fromIndex, int toIndex):返回从fromIndex到toIndex位置的子集合
 *
 * @author PengJiaJun
 * @Date 2022/07/04 00:28
 */
public class ListMethod {
    @SuppressWarnings({"all"})
    public static void main(String[] args) {
        List list = new ArrayList();
        list.add("张三丰");
        list.add("贾宝玉");

//        1) void add(int index, Object ele): 在index位置插入ele元素
        list.add(1, "诸葛亮");
        System.out.println(list);// [张三丰, 诸葛亮, 贾宝玉]


//        2) boolean addAll(int index,Collection eles): 从index位置开始将eles中的所有元素添加进来
        List list2 = new ArrayList();
        list2.add("可口可乐");
        list2.add("百事可乐");
        list.addAll(1, list2);
        System.out.println(list);// [张三丰, 可口可乐, 百事可乐, 诸葛亮, 贾宝玉]


//        3) Object get(int index): 获取指定index位置的元素
        System.out.println(list.get(1));//可口可乐


//        4) int indexOf(Object obj): 返回obj在集合中首次出现的位置
        list.add(3, "张三丰");
        System.out.println(list);//[张三丰, 可口可乐, 百事可乐, 张三丰, 诸葛亮, 贾宝玉]
        System.out.println(list.indexOf("张三丰"));// 0


//        5) int lastIndexOf(Object obj):返回obj在当前集合中未次出现的位置
        System.out.println(list.lastIndexOf("张三丰"));// 3


//        6) Object remove(int index):移除指定index位置的元素，并返回此元素
        System.out.println(list.remove(4));//诸葛亮


//        7) Object set(int index,Object ele):设置指定index位置的元素为ele ,相当于是替换.   返回 先前位于指定位置的元素
        Object del = list.set(1, "可口可乐yyds");
        System.out.println(del);//可口可乐
        System.out.println(list);//[张三丰, 可口可乐yyds, 百事可乐, 张三丰, 贾宝玉]


//        8) List subList(int fromIndex, int toIndex):返回从fromIndex到toIndex位置的子集合
        List res = list.subList(1, 3);// 左边右开 [1, 3) 表示 1 ~ 3  包含 1 不包括 3   所以实际是 1, 2
        System.out.println(res);//[可口可乐yyds, 百事可乐]
    }
}
