package org.pjj.collection_;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 1. 集合主要分为两组: 单列集合, 双列集合
 * 2. Collection 接口有两个重要的子接口 List Set , 他们的实现子类都是单列集合
 * 3. Map 接口的实现子类是双列集合, 存放的 KV对
 * 4. 两张图必须得记住 (Collection 与 Map 两个接口下 重要得继承关系) (本项目 img/Collection.png 与 Map.png)
 *
 * @author PengJiaJun
 * @Date 2022/07/03 17:53
 */
public class Collection_ {
    public static void main(String[] args) {

        Collection collection = new ArrayList();
        Map map = new HashMap();
    }
}
