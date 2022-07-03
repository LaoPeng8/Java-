package org.pjj.collection_;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Foreach的使用 (foreach底层使用 Iterator)
 * @author PengJiaJun
 * @Date 2022/07/03 22:58
 */
public class CollectionForeach {
    public static void main(String[] args) {
        Collection<Book> collection = new ArrayList<Book>();

        collection.add(new Book("三国演义", "罗贯中", 10.1));
        collection.add(new Book("小李飞刀", "古龙", 5.1));
        collection.add(new Book("红楼梦", "曹雪芹", 34.6));

        // foreach底层也是使用的 迭代器 Iterator
        for (Book book : collection) {
            System.out.println(book);
        }

        // foreach 也可以用用在数组
        int[] arr = {1, 8, 9, 100, -2, 0};
        for (int i : arr) {
            System.out.print(i + ",  ");
        }
    }
}
