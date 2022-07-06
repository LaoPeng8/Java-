package org.pjj.list_.linkedlist;

import java.util.LinkedList;

/**
 * LinkedList与ArrayList
 * ArrayList底层采用 数组，优点就是数组的优点，随机查找快，修改快。
 * LinkedList底层采用 双向链表，优点就是链表的优点，增加 删除快，查询，修改慢
 *
 * @author PengJiaJun
 * @Date 2022/07/06 14:39
 */
public class LinkedListSource {
    public static void main(String[] args) {
        LinkedList<String> list = new LinkedList<>();

        list.add("A");
        list.add("B");
        list.add("C");


        list.remove("A");
        list.remove();
        System.out.println(list.isEmpty());
//        list.set(1, "");
//        list.get(0);
    }
}
