package org.pjj.list_;

import java.util.ArrayList;
import java.util.List;

/**
 * List接口是Collection接口的子接口
 *
 * 1. List集合中元素有序、且可重复  (记住只要是实现了 java.util.List 接口的集合, 都是有序的)
 * 2. List集合中的每个元素都有其对应的顺序索引, 即支持索引 (2 与 3 意思基本一样)
 * 3. List容器中的元素都对应一个整数型的序号记载其在容器中的位置，可以根据序号存取容器中的元素
 * 4. JDK API中List接口的实现类有: AbstractList, AbstractSequentiaList, ArrayList, AttributeList, CopyOnWriteArrayList,
 *                              LinkedList, RoleList, RoleUnresolvedList, Stack, Vector
 *
 * 常用的有: ArrayList, LinkedList, Vector
 *
 * @author PengJiaJun
 * @Date 2022/07/04 00:19
 */
public class List_ {
    public static void main(String[] args) {
        // List集合中元素有序、且可重复  (记住只要是实现了 java.util.List 接口的集合, 都是有序的)
        List list = new ArrayList();
        list.add("jack");
        list.add("tom");
        list.add("mary");
        list.add("hsp");
        list.add("tom");
        System.out.println("list=" + list);


        //List集合中的每个元素都有其对应的顺序索引, 即支持索引
        System.out.println(list.get(3));//hsp
    }
}
