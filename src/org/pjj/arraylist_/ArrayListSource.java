package org.pjj.arraylist_;

import java.util.ArrayList;

/**
 * ArrayList底层结构和源码分析
 * 1. ArrayList中维护了一个 Object类型的数组 elementData
 * 2. 当创建ArrayList对象时, 如果使用的是无参构造, 则初始elementData容量为 0, 第一次添加, 则扩容为 10, 如需再次扩容, 则扩容为elementData容量的 1.5倍
 * 3. 如果使用的是指定大小的构造器, 则初始elementData容量为指定大小, 如果需要扩容, 则直接扩容elementData容量的 1.5倍
 *
 * 注意: idea在debug时显示的数据是简化的(不显示为null的数据)
 * 解决: setting -> Build, Execution, Deployment -> Debugger -> Data Views -> Java
 * 取消勾选 Hide null elements in arrays and collections  与  Enable alternative view for Collections classes
 *
 * ArrayList的这个 扩容机制 感觉已经很明白了, 明天再把这个扩容的流程画一个图
 *
 * @author PengJiaJun
 * @Date 2022/07/05 00:05
 */
public class ArrayListSource {
    @SuppressWarnings({"all"})
    public static void main(String[] args) {

        ArrayList list = new ArrayList();//使用无参构造创建 ArrayList对象      (观察创建对象后 内部的 elementData 大小)
//        ArrayList list = new ArrayList(8);//使用有参构造创建 ArrayList对象   (观察创建对象后 内部的 elementData 大小)

        for(int i=1; i <= 10; i++) {//无参构造创建对象, 内部 elementData容量为 0, 第一次添加, 则扩容为 10, 注意观察扩容
            list.add(i);
        }

        for(int i=11; i <= 15; i++) {//elementData在第一次扩容为10后, 第11次add时, 会扩容 elementData容量的1.5倍 也就是扩容为 15
            list.add(i);
        }

        list.add(100);
        list.add(200);
        list.add(null);

    }
}
