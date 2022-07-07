//package org.pjj.list_;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * list 排序
// * @author PengJiaJun
// * @Date 2022/07/04 12:10
// */
//public class ListSort {
//    public static void main(String[] args) {
//        List<Book> list = new ArrayList<>();
//        list.add(new Book("红楼梦", "曹雪芹", 100.0));
//        list.add(new Book("西游记", "吴承恩", 10.0));
//        list.add(new Book("水浒传", "施耐庵", 9.0));
//        list.add(new Book("三国演义", "罗贯中", 80.0));
//        list.add(new Book("西游记", "吴承恩", 10.0));
//
//        //排序 1   list.sort是在list的基础上直接排序 (会改变原有list的值)
////        list.sort((b1,b2) -> Double.compare(b1.getPrice(), b2.getPrice()));
////        list.forEach(System.out::println);
//
//        //排序 2   list.stream().sorted 通过stream流的方式进行排序 不会对原有list产生影响, 排序后的结果生成一个新的list
////        List<Book> collect = list.stream().sorted((b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice())).collect(Collectors.toList());
////        collect.forEach(System.out::println);
//
//        //排序 3 冒泡
//        ListSort.maoPaoSort(list);
////        list.forEach(System.out::println);
//
//
//
//    }
//
//    public static void maoPaoSort(List<Book> list) {
//        boolean flag = false;
//        for(int i=0; i < list.size() - 1; i++) {
//            for(int j=0; j < list.size() - 1 - i; j++) {
//                if(list.get(j).getPrice() > list.get(j + 1).getPrice()) {
//                    Book temp = list.get(j);
//                    list.set(j, list.get(j + 1));
//                    list.set(j + 1, temp);
//
//                    flag = true;//如果发生过两两交换, 则标志位置为true;
//                }
//            }
//            if(flag) {
//                // 说明该轮冒泡进行过两两交换, 所以list顺序还是乱的, 还需要进行排序, 则将 flag重置为false;
//                flag = false;
//            }else {
//                //flag = false 说明该轮冒泡没有进行过两两交换, 说明已经排好序了, 之后的排序就不用再进行了
//                return;
//            }
//        }
//    }
//
//}
//
//class Book{
//    private String name;
//    private String author;
//    private Double price;
//
//    public Book() {
//    }
//    public Book(String name, String author, Double price) {
//        this.name = name;
//        this.author = author;
//        this.price = price;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public Double getPrice() {
//        return price;
//    }
//
//    public void setPrice(Double price) {
//        this.price = price;
//    }
//
//    public String getAuthor() {
//        return author;
//    }
//
//    public void setAuthor(String author) {
//        this.author = author;
//    }
//
//    @Override
//    public String toString() {
//        return "book{" +
//                "name='" + name + '\'' +
//                ", price=" + price +
//                ", author='" + author + '\'' +
//                '}';
//    }
//}
