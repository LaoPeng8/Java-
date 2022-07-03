package org.pjj.collection_;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * 使用 Iterator 遍历 Collection集合
 *
 * 1. Iterator对象称为迭代器, 主要用于遍历Collection集合中的元素
 * 2. 所有实现了Collection接口的集合类都有一个 iterator()方法, 用于返回一个实现了Iterator接口的对象, 即返回一个迭代器
 * 3. Iterator仅用于遍历集合, Iterator本身并不存放对象
 *
 * @author PengJiaJun
 * @Date 2022/07/03 22:28
 */
public class CollectionIterator {
    @SuppressWarnings({"all"})
    public static void main(String[] args) {

        Collection collection = new ArrayList();

        collection.add(new Book("三国演义", "罗贯中", 10.1));
        collection.add(new Book("小李飞刀", "古龙", 5.1));
        collection.add(new Book("红楼梦", "曹雪芹", 34.6));

//        System.out.println("collection=" + collection);
        Iterator iterator = collection.iterator();//所有实现了Collection接口的集合都有iterator()方法, 用于返回一个 Iterator对象
        while (iterator.hasNext()) {//hasNext() 判断当前游标 下一个元素是否存在
            Book book = (Book) iterator.next();//将游标向下移动一位, 并获取移动后游标指向的对象
            System.out.println(book);
        }
        //while循环退出后, Iterator迭代器游标已经指向了最后一个元素
        //若继续 .next() 即会抛出 java.util.NoSuchElementException
        //如果需要再次遍历, 则需要再次获取迭代器对象 iterator = collection.iterator();

    }
}

class Book{
    private String name;
    private String author;
    private Double price;

    public Book() {
    }
    public Book(String name, String author, Double price) {
        this.name = name;
        this.author = author;
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                '}';
    }
}
