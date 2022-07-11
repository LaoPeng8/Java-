# Java集合专题

> 作者: LaoPeng
>
> 2022/7/3 17:29 始



# 集合体系图

这两个图是需要记住得。(虚线代表实现) (实线代表继承)               (idea还是不太智能, 关系多了线就很乱, 我调整了好久，强迫症哈哈哈)

![1.jpg](./img/Collection.png)

![2.jpg](./img/Map.png)

# ArrayList扩容机制
![3.jpg](./img/ArrayList扩容机制.png)

# Vector扩容机制
![4.jpg](./img/Vector扩容机制.png)

# LinkedList
![5.jpg](./img/LinkedList.png)

想要找到好的教程不难，找到适合自己的还真不好找，韩老师讲的挺不错，就是太啰嗦了，就相当于是给小白讲的Java基础中 第一次学集合的小伙伴讲的
不太适合我，一个HashSet源码讲解，前置4个视频铺垫，将HashSet的方法，HashSet底层的HashMap是kv对，无序不能重复。是数组 + 链表的形式
我感觉讲HashSet前应该先讲HashMap好些吧。溜了我直接去找讲解HashMap源码的了.

# HashMap 1.7
大家都知道 HashMap是由数组 + 链表实现。
老师问了一个问题为什么 HashMap的数组部分 不像 ArrayList一样，顺序存放数据 即 0 ~ 8, 然后数组满了之后 继续 0 ~ 8,  相同下标的根据链表next存储？

我刚想到这个问题的时候，感觉这种方法也挺好的，不用计算hash值，而且非常散列。直到老师说这种方法插入快的一B。那么实际这种方式 查询效率 是非常低的， 
因为HashMap正常计算Hash值然后存入数组，这样**查询的时候可以直接计算出Hash值，然后去对应的链表中查询**(相当于只用了 1/8 的时间，这不比二分查找快多了)
然而如果采用问题的方式存储，**那么就只能 8 个链表挨个遍历**，可想而知非常慢(假设数组长度为 8)

HashMap1.7插入链表的方式是采用**头插法**这样效率高些，免得还需要遍历到链表尾部再插入(但是实际上是，put时不管头插还是尾插都需要遍历链表来检测是否key会重复，
如果key没有重复，那么也是遍历到链表尾部了，如果key重复了，则都是遍历一半就替换掉key重复了的节点中的value值，然后返回)

以前只知道put方法插入相同的key，会把value值替换掉，没想到还会返回旧的value值，如果返回null则说明key没有重复没有进行替换，所以**可以使用put的返回值验证当前key是否存在**

为什么hashMap数组的容量一定得是 2得幂次方数呢?  (简单说就是为了保证下标 在 0 ~ 数组长度-1 之间不越界, 如16 则保证下标在 0 - 15)  (在数组扩容的时候也有好处到时候再说)
```java
private void inflateTable(int toSize) { //传入toSize得是 threshold, 而threshold在构造方法中被赋为数组长度
    // Find a power of 2 >= toSize
    int capacity = roundUpToPowerOf2(toSize);//获取 >= toSize得 2得幂次方数 (传入 5返回8, 传入10返回16，传入17返回32)
        
    // 这行就是计算出threshold(阈值), 阈值=数组长度 * 加载因子(默认0.75) 后面的话估计就是根据该阈值
    threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
    table = new Entry[capacity];//初始化指定长度得数组
    initHashSeedAsNeeded(capacity);
}

/**
 * 方法得作用 是 传入 key得hash值 与 数组得长度 计算出 该key在数组中对应得下标
 * 那么这个 index 是有条件得:
 *      1. 得在数组长度内 比如数组长度 16, 则index: 0~15
 *      2. 平均
 * 之前 我们模拟的这个hashmap计算出下标是通过 取余的方式 h % 16; 这样也只会产生 0-15 的数, 但是可能不够平均(不够散列)
 * 那么此处就是使用  & 的方式  h & (length - 1); 也只会产生 0-15的值. 为什么?
 * 假设: 数组长度为 16 => 0001 0000;   16 - 1 = 15 => 0000 1111;   随机的一个hash值 h => 0101 0101
 * 16: 0001 0000                32: 0010 0000
 * 
 * 15: 0000 1111                31: 0001 1111
 * h:  0101 0101                h:  0101 0101
 * &                            &
 *     0000 0101                    0001 0101
 * 
 * 有没有发现什么 为什么数组的容量一定得是 2得幂次方数呢?
 * 先说上面的一个结论就是 不管hash值是多少 与 数组长度 - 1 进行 & 运算后, 得出的结果都是 hash值的 低几位,
 * 如数组长度 16 则取hash值的低4位, 就算4位全部是1, 那也只是 1111 => 15 不会超过数组长度 16 刚好满足 0-15
 * 如数组长度 32 则取hash值的低5位, 就算5位全部是1, 那也只是 11111 => 31 不会超过数组长度 32 刚好满足 0-31
 * 
 * 2得整次幂转为 二进制有一个特征即: 只有最高位是1, 其他位都是0,        比如: 1, 10, 100, 1000, 10000 分别对应 1, 2, 4, 8, 16
 * 那么 这些数 - 1 后也是存在一个特征的: 原本的最高位变为0, 其他位都是1, 比如: 0, 01, 011, 0111, 01111 分别对应 0, 1, 3, 7, 15
 * 而 & 操作是  全1为1, 所以 hash值 与 2的幂次方 - 1 进行 & 运算 实际是获取了 hash值低位的数值, 而且不会超出数组长度
 * 
 * 没想到短短几行代码 蕴含着 如此奥秘的逻辑 (感觉说的可能不是很清楚, 虽然已经明白了, 但是不太好说出来)
 * 
 * 
 */
static int indexFor(int h, int length) {
    // assert Integer.bitCount(length) == 1 : "length must be a non-zero power of 2";
    return h & (length-1);
}


/**
 * Retrieve object hash code and applies a supplemental hash function to the
 * result hash, which defends against poor quality hash functions.  This is
 * critical because HashMap uses power-of-two length hash tables, that
 * otherwise encounter collisions for hashCodes that do not differ
 * in lower bits. Note: Null keys always map to hash 0, thus index 0.
 * 
 * 检索对象哈希代码，并对结果哈希应用一个补充哈希函数，以防止低质量的哈希函数。这是至关重要的，
 * 因为HashMap使用两次方长度的哈希表，否则会遇到hashcode在较低位没有差异的冲突。注意:空键总是映射到散列0，因此索引0。
 * 
 * 用自己的话说就是: 如果使用原生的hashcode() 那么在低位的冲突会比较严重, 因为一个hashcode()产生的hash值 是int型的, 显然有 32bit
 * 但是 如果数组长度为 16 那么最终根据 hash计算数组下标时
 * 16: 0001 0000                32: 0010 0000     (这里需要结合上面的 indexFor() 方法进行理解)
 * 
 * 15: 0000 1111                31: 0001 1111
 * h:  0101 0101                h:  0101 0101
 * &                            &
 *     0000 0101                    0001 0101
 * 
 * 是 这样的, 就是说不管 hash值有多长 最后与 15 的二进制进行 & 运算时, 只需要hash值后4位的值, 即此处的 0101, 因为15是1111 那么hash值高位的值不管是 0 和 1
 * 和 15 进行 & 运算, 显然结果都是 0 (因为 15 高位都是 0), 所以说 很容易发生冲入 32bit的一个hash值, 在数组长度16的情况下, 只用到了后4bit, 那能不冲突才怪呢.
 * 比如说 key1的哈希值 1111 1001 0101, key2的哈希值 0000 0110 0101, 那么由于 后4位一致, 显然在数组中的下标是5, 前面那么多值不一样... 所以说很容易冲突
 * 
 * 那么怎么样才能减少冲突? 显然只要把 高位数 参与到 最终计算数组下标的运算中去就ok了. 也就是这两行
 *     h ^= (h >>> 20) ^ (h >>> 12);
 *     return h ^ (h >>> 7) ^ (h >>> 4);
 * 一顿操作 疯狂右移, 异或 虽然看不懂, 但是 效果就是 根据 hashcode()(整个hash值 包括高位与低位)生成一个 新的hash值, 用这个hash值去进行计算 数组下标, 就是说 结果非常散列
 * 封装了一个HashMap内部的 生成hash值的方法 hash()
 * 
 */
final int hash(Object k) {
    int h = hashSeed;
    if (0 != h && k instanceof String) {
        return sun.misc.Hashing.stringHash32((String) k);
    }

    h ^= k.hashCode();
    
    // This function ensures that hashCodes that differ only by
    // constant multiples at each bit position have a bounded
    // number of collisions (approximately 8 at default load factor).
    // 这个函数确保hashcode在每个位位置上的差异仅为常数倍，冲突的次数是有限制的(默认负载因子约为8)。
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
```

**扩容**
```java
// 在put方法中 是调用该方法来进行添加元素的
void addEntry(int hash, K key, V value, int bucketIndex) {
    // 扩容条件: 满足 size >= 阈值 同时 本次插入的数组下标处 != null 才会触发扩容
    if ((size >= threshold) && (null != table[bucketIndex])) {
        resize(2 * table.length);// 调用该方法进行扩容, 扩容大小为 原数组长度的两倍
        
        hash = (null != key) ? hash(key) : 0;// 扩容后, 使用之前的hash值

        /**
         * 扩容后使用之前根据 (hash值 & table.length - 1) 计算出来的数组下标 就不太好使用了. 因为数组长度发生了改变, 那么计算出来的值就不一样了,
         * 必须的使用新的值, 要不然 下次get("key")时 根据key的hash值 + 数组长度计算出来的 下标, 然后去该下标对应的元素链表中去遍历可就找不到该key了哈哈哈
         */
        bucketIndex = indexFor(hash, table.length);
    }

    createEntry(hash, key, value, bucketIndex);
}


void resize(int newCapacity) {// 传入的新数组的长度  (原数组长度 * 2)
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;

    //此处判断的是 旧数组的长度如果 == 数组最大值, 则不进行扩容, 将阈值调整为Integer.MAX_VALUE;
    // (因为已经达到最大值了不能扩容了, 调整阈值为Integer最大值,相当于间接扩容, 相当于暂时不会触发扩容, 再触发扩容也阔不了了)
    if (oldCapacity == MAXIMUM_CAPACITY) {
        threshold = Integer.MAX_VALUE;
        return;
    }

    Entry[] newTable = new Entry[newCapacity];//根据新的数组长度, 创建出新数组
    transfer(newTable, initHashSeedAsNeeded(newCapacity));//将旧数组中的所有数据 copy 到新数组
    table = newTable;// 将新数组赋值给 table, 那么就是完成了一次扩容 table已经变为了扩容后的数组了
    threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);//由于数组扩容了, 所以 新的数组长度 * 加载因子, 重新计算 阈值
}

// 单线程下该方法 重新计算旧数组+链表中元素的新的下标, 然后存储到新数组中是没有问题的
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K,V> e : table) {//遍历旧的数组
        while(null != e) {//如果数组中不为null, 则继续遍历该链表 (为扩容前老数组+链表中的元素 找到在新数组中的位置并存放)
            Entry<K,V> next = e.next;
            
            /**
             * 这个 rehash 大部分情况为 fasle, 那么这个rehash怎么来的,
             * 就得看 谁调用本方法传入的什么了 那么此处显然是 resize()方法(就在上面)调用的本方法 transfer(newTable, initHashSeedAsNeeded(newCapacity));
             * 那么主要就是看 initHashSeedAsNeeded(newCapacity); 方法 返回的 true 还是 false, 则此处的 rehash 就是 true 还是 false
             * initHashSeedAsNeeded(newCapacity);的介绍方法下面了.
             * initHashSeedAsNeeded(newCapacity);返回true或者false是取决于哈希种子是否发生改变, 改变则为true, 没改变就是false
             * hash()方法在计算hash值时 最核心的一句是 hashSeed ^= k.hashCode();
             * 所以如果 hashSeed 发生改变, 那么元素的 hash值显然也是发生变化了
             * 所以此处就是 判断哈希种子是否被修改, 如修改了 则需要根据元素的key的hashcode 与 新的hashSeed 计算出 新的hash值
             */
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }

            /**
             * 扩容后使用之前根据 (hash值 & table.length - 1) 计算出来的数组下标 就不太好使用了. 因为数组长度发生了改变, 那么计算出来的值就不一样了,
             * 必须的使用新的值, 要不然 下次get("key")时 根据key的hash值 + 数组长度计算出来的 下标, 然后去该下标对应的元素链表中去遍历可就找不到该key了哈哈哈
             * 可以发现 有两种情况：
             *      一种就是 扩容前下标为 5 扩容后 下标变为 21, 这是因为 数组长度发生改变 进行 & 运算后值自然不一样, 数组长度只会是2的整数幂, 扩容也只是长度 * 2
             *          15的二进制 与 31的二进制  实际也就是 左边最高位多了一位 一个是 1111 一个是11111 多了一个最高位 在此处为 16 (也就是oldTable.length)
             *          那么如果 hash值的 这一位 也是 1 那么扩容就是为 0101 => 5 扩容后为 10101 => 21
             *          这种情况的实质就是 原下标 + 老数组.length = 新下标 (因为扩容是翻倍(原数组.length * 2), 二进制数进一位实质也是翻倍 1111是15, 11111是31, 最高位是16 也是原数组长度)
             *      第二种情况就是 hahs值的 这一位 是 0, 那么扩容就是为 0101 => 5 扩容后为 0101 => 5
             *      这就是第二种情况 扩容前下标是多少 扩容后下标也是多少
             *      
             *      
             * 16: 0001 0000                32: 0010 0000                           16: 0001 0000                32: 0010 0000
             *
             * 15: 0000 1111                31: 0001 1111                           15: 0000 1111                31: 0001 1111
             * h:  0101 0101                h:  0101 0101                           h:  0100 0101                h:  0100 0101
             * &                            &                                       &                            &
             *     0000 0101 => 5               0001 0101 => 21                         0000 0101 => 5               0000 0101 => 5
             *                                  (实际是 此处 + 的是 旧数组.length 也就是 16)
             */
            int i = indexFor(e.hash, newCapacity);//根据新的数组长度 使用 (hash值 & table.length - 1) 计算出新的数组下标
            
            // 这两步就相当于完成一个链表的头插法, 将e的下一个元素 指向 新数组中e对应下标的元素, 
            // 然后将 新数组中e对应下标的地方赋值为 e, 即完成一次头插法, 即 将待插入节点e的下一个指向 头节点newTable[i], 然后头节点再指向e, 这样e就成为头节点了而且e.next指向旧的头节点
            // 使用头插法插入, 从老数组 到 新数组后 有一个特点, 就是 链表顺序会发生反转, 老数组中存储的 1->2->3->null 到新数组就变成了 3->2->1->null
            e.next = newTable[i];
            newTable[i] = e;
            
            e = next;//将 e 指向的节点存储到 新数组后, e 被赋值为 老数组中的下一个元素, 进行遍历, 直到将老数组中的所有值全部转存到新数组
        }
    }
}

/**
 * 该方法呢, 就是传入一个数组长度, 然后判断是否要 修改 hashSeed 哈希种子, 将是否修改的boolean值返回扩容方法,
 * 扩容方法决定是否要 根据新的hashSeed, 重新计算新的hash值 (hashSeed如果被修改了, 肯定要重新计算hash值
 * 
 * 该方法不仅在 扩容中被调用了, 在初始化化数组时, 也是调用了的, 作用一致, 计算出hashSeed
 * 
 * hashSeed大部分情况默认为 0, 如果设置了JVM参数 jdk.map.althashing.threshold, 才有可能会触发 hashSeed被修改, JVM参数不变的情况下, 数组长度发生改变也可能会触发 hashSeed被修改
 * 那么修改 hashSeed的的作用就是 使得 hash()函数在计算 元素hash值时, 计算出来的hash值存放到数组中时会更加 散列
 */
final boolean initHashSeedAsNeeded(int capacity) {
    boolean currentAltHashing = hashSeed != 0; // hashSeed 默认为 0, 那么意思就是此处默认是 currentAltHashing = false
    boolean useAltHashing = sun.misc.VM.isBooted() && // 判断jvm是否启动，开始运行为true, (相当于永远为true)
        (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);//此处 就是根据 数组长度 是否大于等于 Holder.ALTERNATIVE_HASHING_THRESHOLD 
        // 该 Holder.ALTERNATIVE_HASHING_THRESHOLD 是通过 JVM参数 jdk.map.althashing.threshold 来判断的, 如果手动设置JVM参数 jdk.map.althashing.threshold,
        // 那么 Holder.ALTERNATIVE_HASHING_THRESHOLD 的值就是 jdk.map.althashing.threshold的值, 如果没有设置 jdk.map.althashing.threshold 的值
        // 则 Holder.ALTERNATIVE_HASHING_THRESHOLD 的默认值为 Integer.MAX_VALUE
        // 所以如果设置了JVM参数值, 数组长度有可能 >= JVM参数, 有可能为true, 有可能会修改hashSeed
        
    boolean switching = currentAltHashing ^ useAltHashing;//异或操作 不同为1, 相同为0, 意思就是必须 一true, 一fasle才会返回true
    if (switching) {
        // hashSeed 没有在其他地方操作的, 只有这里会对hashSeed进行赋值
        // 只有当 switching 为true了, 才会修改hashSeed种子的值
        // 修改哈希种子值的目的就是 觉得HashMap默认的算法不太好, 不够散列, 所以修改hashSeed, 在计算hash值时 生成更加散列的hash值
        // (具体得看hash()方法, hash()方法中计算hash值 是根据 hashSeed ^= k.hashCode(); 然后经过一 右移 与 异或操作 得到的hash值)
        // 所以修改了 hashSeed, hash值会发生改变
        // 所以在数组扩容时, 数组的长度发生了改变, 所以hashSeed也有可能发生改变, hashSeed是否改变将boolean返回, 告诉扩容方法
        // 扩容方法决定是否要 重新计算元素的hash(根据新的哈希种子 计算新的hash值)
        hashSeed = useAltHashing
            ? sun.misc.Hashing.randomHashSeed(this)
            : 0;
    }
    return switching;
}
```

**1.7多线程环境下: 扩容时会发生死锁**
那么我认为最主要的原因就是 扩容前链表的顺序是 1->2->3->null, 扩容后 3->2->1->null 就导致会产生一个循环链表, 导致死循环
![HashMap1.7扩容死锁问题.jpg](./img/HashMap1.7扩容死锁问题.png)

**如何避免扩容时, hashmap产生死锁**
只有一个办法, 防止hashmap产生扩容: 比如说确定了这个hashmap使用过程中不会put进入超过30个元素, 那么new HashMap得时候就可以根据有参构造 
`new HashMap(64, 0.75)`, 传入得数组长度 与 加载因子 来控制使 **阈值**不要超过 30(阈值=数组长度*加载因子), 那么就永远不会扩容 因为扩容得条件是 size >= 阈值,
另一种不算方法得方法就是, 避免在多线程环境下使用HashMap 或者说 在使用HashMap前对上层 进行加锁等控制, 保证同时只有一个线程能操作HashMap

**get()方法**
```java
/**
 * get方法比较简单
 */
public V get(Object key) {
    // 若 key == null 则调用 专门获取null得方法获取值 (存储key=null得元素时 也是专门得方法)
    if (key == null)
        return getForNullKey();
    Entry<K,V> entry = getEntry(key);//获取key存储得元素得值 (这里返回得entry是一个节点, 真正得数据是 entry.getValue())

    return null == entry ? null : entry.getValue();//entry != null 才会返回 真正得value值
}

/**
 * 获取 key = null 得元素得值
 */
private V getForNullKey() {
    if (size == 0) { // 如果元素个数 == 0则直接返回null, 就是一个简单得安全判断
        return null;
    }
    //在存储key=null得方法中, 由于key=null无法计算hash值, 所以 key=null得元素是直接存储在 table[0] 中得, 所以取值也是直接遍历 table[0]
    for (Entry<K,V> e = table[0]; e != null; e = e.next) {
        if (e.key == null) // 在table[0] 中挨个比对 key == null 找到了则 将对应得元素返回
            return e.value;
    }
    return null;//找不到则返回null
}

/**
 * 
 */
final Entry<K,V> getEntry(Object key) {
    if (size == 0) { // 如果元素个数 == 0则直接返回null, 就是一个简单得安全判断
        return null;
    }

    int hash = (key == null) ? 0 : hash(key);//计算出 key 得hash值.
    
    // 根据 hash值找到该key对应得数组下标 indexFor(hash, table.length)
    // 然后遍历该 数组元素处得链表, 挨个比对 满足 (元素中存储得hash值 与 key计算出来得hash值一致) 同时 (key == key 或者 (key != null 同时 key.equals(k)) 则返回e返回
    // 据我分析 e.hash == hash 是必须满足得, 也就是说 key 得hash值必须一致
    // (k = e.key) == key 这段是比较地址, 那么应该就是 判断 key值是一个对象得情况
    // (key != null && key.equals(k)) 这段 使用equals 应该是 判断 key值是一个 字符串得情况
    // 除了 e.hash == hash hash值必须一致外, 还需要满足 上述了两个条件之一, 即key值是对象需满足 (k = e.key) == key, key值是String需满足 (key != null && key.equals(k))
    for (Entry<K,V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
        Object k;
        if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
            return e;
    }
    return null;
}
```

**HashMap中的属性 modCount 与 java.util.ConcurrentModificationException 异常 出现的原因**
```java
package org.pjj.map1_7;

import java.util.HashMap;
import java.util.Iterator;

/**
 * HashMap中的属性 modCount 与 java.util.ConcurrentModificationException 异常 出现的原因
 * 
 * @author PengJiaJun
 * @Date 2022/07/08 20:58
 */
public class HashMapSource2 {
    public static void main(String[] args) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("1", "1");
        hashMap.put("2", "2");

        // 出现异常 java.util.ConcurrentModificationException
//        for(String key : hashMap.keySet()) {
//            if(key.equals("2")) {
//                hashMap.remove(key);
//            }
//        }


        /**
         * 上面这段代码编译后是这样的 (foreach底层也是 iterator 实现的, 所以说编译后是这样的)
         *
         * 为什么会出现异常 java.util.ConcurrentModificationException ?
         * 首先先看 这个 Iterator 迭代器对象是哪里来的
         * hashMap.keySet().iterator() 看看keySet()对象哪里来的, 点进去看看就是这样的
         * keySet = new KeySet() 可以看到实质是new了一个 KeySet() 构造器, 点进去看看
         * iterator() return newKeyIterator(); 可以看到 这个 KeySet()对象的 iterator()方法返回的迭代器是调用 newKeyIterator() 获取的, 点进去看看
         * new KeyIterator() 发现内部是 new的一个 KeyIterator() 迭代器, 点进去看看
         *     private final class KeyIterator extends HashIterator<K> {
         *         public K next() {
         *             return nextEntry().getKey();
         *         }
         *     }
         *      发现继承自 HashIterator 迭代器, 由于初始化该类, 会先调用父类的构造方法, 所以点进去看看
         *      expectedModCount = modCount; 发现HashIterator迭代器的构造方法中将  HashMap中的 modCount属性赋值给了 expectedModCount
         *      那么此时 HashMap是调用了两次 put() 方法 所以 modCount = 2; (modCount表示被修改的次数, put, remove 都会导致modCount++)
         *      那么此时 modCount = 2; expectedModCount = 2;
         *
         *      发现重写了 next()方法, next方式实质先需要调用 nextEntry()方法, 点进去看看
         *          if (modCount != expectedModCount)
         *              throw new ConcurrentModificationException();
         *      发现 nextEntry()方法中 会先判断 mouCount != expectedModCount就会抛出异常.
         * 那么到这里, 基本上也就解谜了, 在迭代器构造方法中 expectedModCount被赋值为 2, 当调用 next()方法时 2 == 2; 没有问题
         * 然后执行 hashMap.remove(key) 当, 该元素被删除后, HashMap的remove()方法中 会mouCount++,
         * 然后继续循环, 执行 next()方法, 然后啪 2 != 3 (modCount = 3; expectedModCount = 2;), 那么就是抛出了 java.util.ConcurrentModificationException
         *
         * 解决就是使用 这个迭代器提供的 remove() 方法删除当前元素
         * remove()方法中有一行 expectedModCount = modCount; 那么就是 在删除完当前元素后,modCount不是会++嘛, 所以 expectedModCount = modCount;
         * 就不会出现 modCount = 3; expectedModCount = 2; 的情况了, 而是 modCount = 3; expectedModCount = 3;
         *
         * 对应的解决方法还有第二种, 就是使用 concurrentHashMap(), 多线程环境下的 HashMap
         *
         */
        Iterator i$ = hashMap.keySet().iterator();//该迭代器真正的类型 KeyIterator extends HashIterator<K>

        while(i$.hasNext()) {
            String key = (String)i$.next();
            if (key.equals("2")) {
//                hashMap.remove(key);
                i$.remove();
            }
        }

    }
}
```
**HashMap1.7完结**
原本准备想像ArrayList一样将代码的介绍都直接画在图上的, 但是感觉位置不够大, 要说的东西太多了, 画图不方便, 所以这个图画了一半还有一半以代码的形式贴在该文件上了
![HashMap1.7.jpg](./img/HashMap1.7.png)

# ConcurrentHashMap 1.7
```java
//先理解下 concurrentHashMap 底层的一个结构

final Segment<K,V>[] segments;// 这个有点类似 HashMap中的 table[] 但是不是, 这个只是用来存储 segment 的
transient volatile HashEntry<K,V>[] table;// Segment内部的一个属性, 用来存储HashEntry, 也就用来真正存储节点

static final class HashEntry<K,V> {// 该hashEntry是真正存储数据的, 与 HashMap中的 Entry一样
    final int hash;//hash值
    final K key;//key
    volatile V value;//value
    volatile HashEntry<K, V> next;//next
    //可以说与 HashMap中的entry对象一模一样
}

static final int DEFAULT_INITIAL_CAPACITY = 16;
static final float DEFAULT_LOAD_FACTOR = 0.75f;
static final int DEFAULT_CONCURRENCY_LEVEL = 16;
// 默认构造器
public ConcurrentHashMap() {
    //三个参数分别为: 数组的长度, 加载因子, 隔离级别 (前两个与HashMap的构造器一样, 隔离级别是此处新有的)
    // 数组的长度默认16, 加载因子默认16, 隔离级别默认16
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
}
```
![concurrentHashMap1.7内部结构.png](./img/concurrentHashMap1.7内部结构.png)

**先简单的看一下构造方法**
```java
public ConcurrentHashMap() {
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
}
public ConcurrentHashMap(int initialCapacity,float loadFactor, int concurrencyLevel) {
    if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
        throw new IllegalArgumentException();
    if (concurrencyLevel > MAX_SEGMENTS)
        concurrencyLevel = MAX_SEGMENTS;
    // Find power-of-two sizes best matching arguments
    int sshift = 0;
    
    /**
     * 看看该ssize的作用是什么, 可以看到该构造器的最后几行有一行 Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
     * 显然 Segment数组的长度是 ssize 决定的, 该while循环对ssize的值进行了赋值, 看看该while循环的作用是什么
     * ssize = 1, concurrencyLevel默认等于 16, 先不用管++sshift;
     * ssize等于1 小于 16, 进入循环 ssize等于2 小于16, 进入循环 ssize等于4 小于16, 进入循环 ssize等于8 小于16, 进入循环 ssize等于16 不小于16退出循环
     * 可以看到 ssize=16时退出了循环, 那么意味则 Segment数组大小为 16,
     * 可以发现 最终ssize等于 ssize >= 隔离级别 的2的幂次方数
     * (与HashMap中inflateTable()方法中 求大于等于输入的数组长度的2的幂次方数, 以求的真正的数组长度, 就是指定数组长度时 若指定的长度不为2的幂次方数, 则扩容为2的幂次方数)
     * 此处是采用 while + 移位的方式实现的, HashMap中是调用 Integer的highestOneBit()方法实现的(不需要循环, 通过多次移位)
     * 
     * 为什么 ssize 要是 2的幂次方数? 或者说 为什么 segment[]数组长度 要是 2的幂次方数?
     * 与HashMap一样, 是根据 key的hash值 与 segment[]数组长度 - 1, 进行 & 运算, 得到数组下标.
     * 
     */
    int ssize = 1;
    while (ssize < concurrencyLevel) {
        ++sshift;
        ssize <<= 1;
    }
    this.segmentShift = 32 - sshift;
    this.segmentMask = ssize - 1;//保存segment数组长度 - 1; 方面后面 (key.hashcode & segemnt.length - 1) 就变成了 (key.hashcode & segmentMask)
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    
    /**
     * 可以看到 cap 最终是 Segemnt对象下的 table[]属性 的长度, 也就是每个 Segment对象下保存几条链表
     * cap 又是根据 c 得到的, 所以现在从 c 开始分析
     * 假设 该构造器传入的都是默认值 int initialCapacity = 16,float loadFactor = 0.75, int concurrencyLevel = 16
     * 那么 c = 16 / ssize(经过上面的while ssize最终的是是 >= 隔离级别的2的幂次方数 所以ssize = 16), c = 16 / 16, c = 1
     * if (c * ssize < initialCapacity) = if( 1 * 16 < 16) 不满足 所以 c还是 1;
     * cap 默认 = 2, cap最小值就为 2
     * while(cap < c) cap = cap << 1;//如果 cap < c 即翻倍, 直到不小于 c
     * while(2 < 1);// 不成立 所以 cap还是 2
     * 后面 根据 cap new出了 Segment元素内 table[cap]数组, 相当于 cap 指定了 Segment元素内数组的长度
     * Segment<K,V> s0 = new Segment<K,V>(loadFactor, (int)(cap * loadFactor),(HashEntry<K,V>[])new HashEntry[cap]);
     * 所以 Segment内 table[]数组的长度是有最小值的 即为 2
     * 不会出现 Segment[]数组长16, Segment元素内的数组长1, 组成的数组长度为16的结构
     * 
     * 假设 int initialCapacity = 17, float loadFactor = 0.75, int concurrencyLevel = 16
     * initialCapacity = 17 就表示说 指定 HashEntry 17 个, 即 Segemnt[]数组长16(根据ssize=16得来),
     * 那么每个 Segment元素内部 存储几个 HashEntry? 也就是 Segment元素内部 table[]数组 长度?
     * 难道每个Segment元素存储 1 个 HashEntry, 最后一个Segment元素存储 2 个 HashEntry?
     * 不管是带入 initialCapacity = 17, ssize = 16 来进行运算, 还是说 根据默认值 16, 16, 0.75f
     * 得到的结果都是 cap = 2;//默认的最小值
     * 即 每给 Segment元素存储 2 个 HashEntry, 即 Segment元素内部数组 table[] 长度为 2
     * 虽然指定长度为 17, 但是最少是保存 32 个元素
     * 
     * 如果说 initialCapacity = 33, 其他值默认, 即 ssize = 16
     * 那么带入值可以得到 cap = 4, 即 16 * 4 = 64, 相当于数组长度为 64, 每个Segment元素内部存储 4 个 HashEntry
     * 有点像 HashMap 指定长度为 17 扩容到 32, 指定 33 扩容到 64的那个味道了.
     * 
     */
    int c = initialCapacity / ssize;
    if (c * ssize < initialCapacity)
        ++c;
    int cap = MIN_SEGMENT_TABLE_CAPACITY;//static final int MIN_SEGMENT_TABLE_CAPACITY = 2
    while (cap < c)
        cap <<= 1;
    // create segments and segments[0]
    /**
     * Segment<K,V> s0 = new Segment<K,V>(loadFactor, (int)(cap * loadFactor),(HashEntry<K,V>[])new HashEntry[cap]);
     * 这里先提一点点扩容, 可以发现 new Segment()时将, 是将 阈值传过去了的 (int)(cap * loadFactor), cap是数组长度
     * 扩容: put一个kv对时, 先计算hash值属于哪个Segment下, 然后判断是否需要扩容, 如果需要扩容, 假设当前数组长度为2, 则new一个长度为4的数组,
     * 将老数组中的元素copy过去, 然后将 Segment下的 table[]属性指向 新数组, 即完成扩容, 其他Segment下table长度为2, 扩容后Segemnt下table长度为4
     * 
     * 
     * 提一下为什么此处要 new Segment[ssize]; 与 Segment<K,V> s0 = new Segment<K,V>(loadFactor, (int)(cap * loadFactor),(HashEntry<K,V>[])new HashEntry[cap]);
     * 为什么要 new Segment[ssize]; 比较好理解, 构造方法嘛, 确定了 Segment[]数组的长度, 那么就直接初始化嘛
     * 主要是为什么要new 一个 Segment0, 这就需要结合put方法讲一讲了, put时首先要根据 key的hash值计算出 在Segments中的下标, 假设下标为 7
     * 然后将 该HashEntry对象放入 Segment对象中的table[]数组, 在进行此操作前需要先判断 该Segments[7]是否为null,
     * (Segments数组初始化后 每个元素都是 null, 除了 Segments[0], S0已经new出来了而且UNSAFE.putOrderedObject(ss, SBASE, s0);已经给Segments赋值了)
     * 如果Segments[7]为null, 则需要根据 加载因子, 阈值, 内部数组的长度 new出一个  Segment对象, 那么这些参数都是要重新获取并计算的,
     * 不过由于保存了 Segments[0], 所以只需从 Segment[0] 获取即可, 这就是为什么要在初始化 Segments[]时给 S0 赋值
     * 
     */
    Segment<K,V> s0 = new Segment<K,V>(loadFactor, (int)(cap * loadFactor),(HashEntry<K,V>[])new HashEntry[cap]);
    Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
    UNSAFE.putOrderedObject(ss, SBASE, s0); // ordered write of segments[0]
    this.segments = ss;
}
```

**put()**
```java
public V put(K key, V value) {
    Segment<K,V> s;
    if (value == null) //首先判断 value == null 直接抛异常, 这也说明了 CHashMap不能加入value为null的kv对
        throw new NullPointerException();
    int hash = hash(key);// 根据key计算出hash值 (主要就是 hashcoe ^ hashSeed 得到的hash值, 然后再各种左移右移异或)
    /**
     * segmentMask = Segengs[].length - 1; 数组长度 - 1
     * hash & 数组长度 - 1, 这种方式获取数组下标是正常的, HashMap中也正是这种方式
     * 
     * 为什么要将 hash >>> segmentShift ?
     * 是这样的, 首先要知道 segmentShift的值, 该值是在构造方法中赋值的, 即 this.segmentShift = 32 - sshift;
     * sshift是, 假如数组长度ssize为16, 则sshift为4, 因为2的4次方为16, 假如数组长度为32, 则sshift为5, 因为2的5次方为32
     * 则 数组长度为16, segmentShift = 32 - 4 = 28, 数组长度为32, segmentShift = 32 - 5 = 27
     * 
     * 假设 一个key哈希值为 (hash值是int型, int为4个字节, 即32位)
     * 01010101 01010101 01010101 01010101
     * 00000000 00000000 00000000 00000101        那么这是 hash值 右移 28位后的结果
     * 00000000 00000000 00000000 00001111        这是 数组长度 - 1
     * 
     * 看到这个就有点感觉了, 相当于是HashMap使用 hash值直接与数组长度 - 1, 进行&运算, 实际是取 hash值的后 几位(数组长度为16, 就是取后4位)
     * ConcurrentHashMap 相当于就是 取 hash值的 前几位(数组长度为16, 就是取前4位) (4位都是 1, 也只能是15, 满足了数组长度16, 下标 0 ~ 15)
     * 
     * 之后在Segment元素内部存储的时候, 就是使用 正常的hashMap的方式 数组长度 -1 & hash值, 实际就是取 低几位
     * 那么就相当于 确定Segments数组下标的过程是取hash值的高几位, 然后在Segment元素内部确实 HashEntry[] table 的下标时, 是取的低几位
     * 
     * 至于为什么确定Segments[]数组下标时采用hash值高位, 确定HashEntry[] table的下标时采用hash值的低位?
     * 我估计是这样的, 如果都取低位或者高位, 那么可能导致,  元素全部偏向一边 (元素都存在一起, 不够散列)
     * 即假如都取低位 0101, 则 元素在 Segments[]数组中存储的下标为5, 那么在Segment元素中的 HashEntry[] table 也存储在下标为 5 (假设长度有这么长)
     * 那么岂不是, Segments[]中下标为几, 则该Segment元素内部的table下标也为几, 就是说其他位置就浪费了, 不够散列
     * 
     * 为什么确定Segments[]数组下标时采用hash值高位, 确定HashEntry[] table的下标时采用hash值的低位?
     * 答案就是: 为了散列.
     * 
     */
    int j = (hash >>> segmentShift) & segmentMask;
    
    /**
     * 此处判断就是 根据 j 下标, 从Segments[j] 取出该元素, 如果为null, 则 s = ensureSegment(j), 为此处new一个Segment放进去, 然后返回Segment对象
     * 最后调用 Segment对象的 put方法, 将kv对, 放入Segment对象的 table[]数组中
     * 
     * UNSAFE.getObject(segments, (j << SSHIFT) + SBASE)) 通过这种方式取出数组元素是线程安全的, 这个UNSAFE我也不了解, 反正就是说底层不只使用了CAS算法,
     * 也有其他算法, 反正就是保证一个线程的安全, 我现在对这个 并发, 锁, 这一块还不太了解, 暂时就向上面那样理解就行了.
     * 
     * (j << SSHIFT) + SBASE) 以后会有很多这样的写法, SSHIFT 与 SBASE是怎么来的我也不太清楚, 反正挺复杂的.
     * 效果就是 取出 j 处下标的元素. UNSAFE.getObject(segments, (j << SSHIFT) + SBASE))
     * 
     */
    if ((s = (Segment<K,V>)UNSAFE.getObject          // nonvolatile; recheck
        (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
        s = ensureSegment(j);//为此下标处new一个Segment放进去, 然后返回Segment对象
    return s.put(key, hash, value, false);
}

/**
 * 生成一个Segment对象, 放入指定下标处, 并返回
 * 那么该方法就是会发生并发问题的, 假如此时两个线程同时put两个不同的kv对, 但是下标都一样需要放入同一个 Segments 中, 
 * 此时两个线程都发现 Segments[k] 为空, 则会调用该方法创建 Segments对象,
 * 那么最终只有一个线程会创建Segment对象, 另一个线程则是拿到已经被创建好了的这个Segment对象, 所以该方法是安全的, 不会产生并发问题
 */
private Segment<K,V> ensureSegment(int k) {
    final Segment<K,V>[] ss = this.segments;
    long u = (k << SSHIFT) + SBASE; // raw offset
    Segment<K,V> seg;
    
    //这里就是判断一下, Segmnet[k] 处是否为null, == null 才继续生成Segment对象, 不为null则直接返回该 Segment对象(说明被其他线程已经创建了)
    if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) {
        /**
         * 这里就是根据 Segments[0]的内部数组长度 cap, 加载因子 lf, 阈值 threshold 来new出一个 Segment对象
         * (这也是为什么之前构造方法中为什么要先初始化一个 Segments[0], 就是为了之后new Segment对象时方便取值)
         * 但是 实际没有 new Segment对象, 只是将 new Segment对象的参数都准备好了, 
         * */
        Segment<K,V> proto = ss[0]; // use segment 0 as prototype
        int cap = proto.table.length;
        float lf = proto.loadFactor;
        int threshold = (int)(cap * lf);
        HashEntry<K,V>[] tab = (HashEntry<K,V>[])new HashEntry[cap];
        /**
         * 继续判断, 在经过了上面几步的操作后, 再次获取Segment[k] 处是否为null,
         * == null 才继续生成Segment对象, 不为null则直接返回该 Segment对象(说明被其他线程已经创建了)
         * */
        if ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) { // recheck
            Segment<K,V> s = new Segment<K,V>(lf, threshold, tab);//new出 Segment对象
            while ((seg = (Segment<K,V>)UNSAFE.getObjectVolatile(ss, u)) == null) {//再次判断 Segment[k] 处是否为null
                /**
                 * 使用cas来真正将 Segment放入 Segment[k] 操作成功则返回
                 * 如果操作失败(操作失败大概率是其他线程将 Segment[k]处赋值了, 所以Segment[k] != null, 则操作失败), 则继续while 判断 Segment[k] 处是否为null,
                 * 如果为null, 则再次使用cas来真正将 Segment放入 Segment[k] 操作成功则返回
                 * 如果不为null, 则说明其他线程已经将 Segment[k] 处创建了 Segment对象并放入了数组, 则结束while循环并将 其他线程创建的Segment对象返回
                 * */
                if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s))
                    break;
            }
        }
    }
    return seg;
}

/**
 * 该方法是 Segment 内部的 put() 方法
 * 也就是确定了 Segments[] 中的位置, 即哪个 Segment, 之后继续put, 将kv对存放在 Segment内部 HashEntry[] table; 中的哪个位置.
 */
final V put(K key, int hash, V value, boolean onlyIfAbsent) {
    /*
     * 该方法的目的就是, 在等待锁的时间内, 随便完成一些其他事情以节省时间, 比如 new HashEntry<K,V>(hash, key, value, null);
     * */
    HashEntry<K,V> node = tryLock() ? null : scanAndLockForPut(key, hash, value);
    V oldValue;
    try {
        // 这里是已经确定了 Segment 所以这里是 Segment内部, table 是一个属性 表示Segment内的一个数组
        HashEntry<K,V>[] tab = table;
        int index = (tab.length - 1) & hash;//数组长度 - 1 & hash值, 常规操作, 计算出下标
        /*
         * entryAt(tab, index)方法内部就一句话
         * return (tab == null) ? null : (HashEntry<K,V>) UNSAFE.getObjectVolatile(tab, ((long)i << TSHIFT) + TBASE);
         * 也就是使用 UNSAFE 安全的获取 table 中下标为 index 的元素
         * 之后的操作就是, 如果该元素为空, 则将本次put的HashEntry直接放到该table[index], 如果不为空, 则遍历判断是否有重复, 然后替换值
         * 如果没有重复, 则使用头插法, 将元素插入链表.
         * */
        HashEntry<K,V> first = entryAt(tab, index);
        for (HashEntry<K,V> e = first;;) {//遍历该元素, 即 table[index]; 该处的 HashEntry以及HashEntry.next
            if (e != null) {
                K k;
                // 如果 key值重复, 则将value值替换, 然后将老value值返回, 退出循环, 结束方法
                // 如果 找不到key值重复, 则继续遍历, 直至遍历到链表最后一个元素的下一个即为null, 然后交由 else 处理, 使用头插法, 将其插入链表
                if ((k = e.key) == key || (e.hash == hash && key.equals(k))) {
                    oldValue = e.value;
                    if (!onlyIfAbsent) {
                        // 如果该 onlyIfAbsent = true(那么此处!true, 即为false不会进入该if), 
                        // 则 key值重复也不会替换value值, 修改次数也不会++, 返回值直接为该value
                        e.value = value;
                        ++modCount;
                    }
                    break;
                }
                e = e.next;
            }
            else {
                if (node != null)
                    node.setNext(first);
                else
                    /*
                     * 如果说, table[index] == null, 则 new 出一个 HashEntry, HashEntry.next指向旧的头节点first, 不过是为null的, 然后将table[index] = HashEntry;
                     * 
                     * 或者是 table[index].next 下的某一个, 因为该for循环以及for循环内第一个if, 会遍历整个链表, 
                     * 也就是说当遍历到链表尾部时, 说明 没有重复的key, 那么该kv对就应该插入链表, 怎么插入?
                     * 先 new HashEntry(); 且该HashEntry.next指向 链表的头节点,
                     * 然后 table[index] = 该HashEntry; 这样就完成了一次头插法, 将元素插入链表
                     * 不过不是直接 table[index] = hashentry; 这样写的, 这样不安全, 文中实际的写法是
                     * setEntryAt(tab, index, node); 内部是 UNSAFE.putOrderedObject(tab, ((long)i << TSHIFT) + TBASE, e);
                     * */
                    node = new HashEntry<K,V>(hash, key, value, first);
                int c = count + 1;//先将 count + 1 的保存起来, 也就是 该 Segment存储元素的个数 先+1, 注意: 并没有将这个值直接修改count
                if (c > threshold && tab.length < MAXIMUM_CAPACITY) //这里判断 是否需要扩容
                    rehash(node);//扩容, 等下看
                else
                    // 如果不需要扩容, 则将 HashEntry保存到 table[index], 但是不能这样写, 这样写只是保存到了当前线程的工作空间
                    // 没有同步到内存, 所以需要使用 setEntryAt(tab, index, node); 将值同步到内存
                    // setEntryAt()内部就一句话 UNSAFE.putOrderedObject(tab, ((long)i << TSHIFT) + TBASE, e);
                    // 通过 UNSAFE 保证安全, 即通过该对象存储到 table[index], 是可以保证其他线程同步的
                    setEntryAt(tab, index, node);
                
                // 添加完成后就是, ++modCount; 修改次数+1, count = c; 存储数据个数真正 + 1;
                ++modCount;
                count = c;
                oldValue = null;//这个返回值, put相同的key会将老的value返回, 这里并没有重复, 所以返回值为null
                break;
            }
        }
    } finally {
        unlock();
    }
    return oldValue;
}

/**
 * tryLock(): 当前这把锁能不能获取到, 如果能直接返回 true 否则 直接返回 false (不会阻塞)
 * lock():    当前这把锁能不能获取到, 如果能直接返回 true 否则 阻塞 直到能获取到这把锁
 * 
 * 该方法的目的就是, 在等待锁的时间内, 随便完成一些其他事情以节省时间, 比如 new HashEntry<K,V>(hash, key, value, null);
 *
 * 说实话啊, 我也不太理解, 锁来锁去的, 并发, 锁, 这块还不太了解, 可能也是我没有认真听吧, 反正ConcurrentHashMap1.8我是不打算看了, 太勾八难了感觉
 * 虽然明白了这个构造器到这个put方法的一个流程, 也感觉明白了concurrentHashMap的一个结构, 但是感觉还是不是很明白, 就好像上数学课老师将的这个题目明白了
 * 但是换另外一题, 还是不会一样, 感觉HashMap1.7我就领悟的挺透彻的,
 */
private HashEntry<K,V> scanAndLockForPut(K key, int hash, V value) {
    HashEntry<K,V> first = entryForHash(this, hash);
    HashEntry<K,V> e = first;
    HashEntry<K,V> node = null;
    int retries = -1; // negative while locating node
    while (!tryLock()) {
        HashEntry<K,V> f; // to recheck first below
        if (retries < 0) {
            if (e == null) {
                if (node == null) // speculatively create node
                    node = new HashEntry<K,V>(hash, key, value, null);
                retries = 0;
            }
            else if (key.equals(e.key))
                retries = 0;
            else
                e = e.next;
        }
        else if (++retries > MAX_SCAN_RETRIES) {
            lock();
            break;
        }
        else if ((retries & 1) == 0 && (f = entryForHash(this, hash)) != first) {
            e = first = f; // re-traverse if entry changed
            retries = -1;
        }
    }
    return node;
}
```

**扩容**
```java
/**
 * 先看看在put中调用该扩容方法的时候
 * int c = count + 1;//相当于 put后, 该Segment存储的元素个数
 * if (c > threshold && tab.length < MAXIMUM_CAPACITY) //这里判断 是否需要扩容, (元素个数 > 阈值 且 数组长度 < 最大数组长度) 即进行扩容
 *      rehash(node);//扩容, 扩容时将 node 节点传入 node = new HashEntry<K,V>(hash, key, value, first);
 *      // 传入的时候 node.next 就已经指向了 头节点了, 但是还没有插入链表
 */
private void rehash(HashEntry<K,V> node) {
    HashEntry<K,V>[] oldTable = table;//先保存老数组
    int oldCapacity = oldTable.length;//保存老数组长度
    int newCapacity = oldCapacity << 1;//新数组长度 = 老数组 << 1; 即新数组长度 = 老数组长度 * 2;
    threshold = (int)(newCapacity * loadFactor);//根据新数组长度 * 加载因子 得到新的阈值
    HashEntry<K,V>[] newTable = (HashEntry<K,V>[]) new HashEntry[newCapacity];//根据新数组长度 new 出新的 数组
    int sizeMask = newCapacity - 1;//数组长度 - 1; 方便计算下标  hash & sizeMask = 下标
        
    // 此处即是遍历老数组, 将元素转移到新数组
    for (int i = 0; i < oldCapacity ; i++) {
        HashEntry<K,V> e = oldTable[i];
        if (e != null) {// e != null 才需要转移嘛, 如果说 e == null 则说明该 oldTable[i]处 为null, 则不需要转移此处的元素到新数组
            HashEntry<K,V> next = e.next;//保存 e 的下一个节点
            int idx = e.hash & sizeMask;//idx表示 e对象在 新数组中的下标
            if (next == null)   //  Single node on list
                // 如果 e 的下一个节点为null, 说明当前 oldTable[i]处 只有 e 一个节点, 则将 e 存储到对应的新数组中即可 newTable[idx] = e;
                // 而 e 没有下一个节点, 就相当于 oldTable[i] 该处的链表已经处理完了, 可以处理数组的下一个元素了即 oldTable[i + 1]
                newTable[idx] = e;
            else { // Reuse consecutive sequence at same slot
                HashEntry<K,V> lastRun = e;// e == oldTable[i] 相当于 头节点
                int lastIdx = idx;// lastIdx 相当于 e 的新下标(根据e.hash & sizeMask计算出来的) 
                /**
                 * 该 for循环的作用就是:
                 * 遍历该链表, 同时记录链表中的元素, 转移到新数组中后的下标(lastIdx) 以及 记录该元素(lastRun)
                 * 只要 当前遍历元素的新下标 != 当前遍历元素的上一个元素的新下标   if (k != lastIdx)
                 *      则 将 lastIdx 赋值为 当前遍历元素的新下标, 同时将 lastRun 赋值为当前记录元素
                 * 如果 当前遍历元素的新下标 == 当前遍历元素的上一个元素的新下标
                 *      则 什么都不操作
                 * 那么最后这样做的效果就是 lastRun代表的节点 往后的节点 在新数组中下标都是一样的,
                 * 因为 如果不一样, 则 lastRun就被置为了这个不一样下标的元素, 然后继续比较 与下一个元素的 新节点是一致
                 * 
                 * 那么该for循环结束时就会出线一种情况, 当链表遍历完之后, lastRun所指向的元素 往后的元素(需要满足是连续的) 在新数组中下标都是一样的
                 * 所以 该for循环结束后 newTable[lastIdx] = lastRun; 即将 lastRun直接移动到新数组的属于它自己的下标处
                 * 那么链在lastRun后面的节点, 也是直接跟这lastRun一起被移动到了 新数组新下标处, 由于lastRun后面的节点的新下标
                 * 都是与LastRun一致的, 所以一起被移动到新数组的新下标 是没有问题的.
                 * 
                 * 但是我感觉这样其实没有优化多少, 还不如直接遍历, 一个一个往新数组放, 这样lastRun也只是会记录链表最尾部新下标一样的节点,
                 * 因为 就算链表中间有一大段节点的新下标都一样, 比如有四五十个, 但是只要往后有一个节点新下标不一样, 则lastRun被置为这个新下标
                 * 和前面不一样的节点, 然后重新计算该lastRun是否与后面的节点新下标一致, 说不定到最后, lastRun和后面节点新下标一致的 节点一共都没几个
                 * 为了这几个节点可以一起被 转移到新数组 感觉不值得
                 * 
                 */
                for (HashEntry<K,V> last = next; last != null; last = last.next) {
                    int k = last.hash & sizeMask;//计算当前遍历元素 转移到新数组中的下标
                    if (k != lastIdx) {
                        lastIdx = k;
                        lastRun = last;
                    }
                }
                newTable[lastIdx] = lastRun;
                
                /*
                 * 由于之前已经把 链表最尾部的 几个连续的节点已经转移到了新节点, 所以这里不需要遍历整个链表, 而是只用遍历到 p != lastRun 即可
                 * 遍历的过程就是 将元素一个一个 计算中新下标, 然后 使用头插法, 插入到新数组
                 * */
                for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {// Clone remaining nodes
                    V v = p.value;//value值
                    int h = p.hash;//hash值
                    int k = h & sizeMask;//根据 hash值 & (新数组长度 - 1) 计算出 该元素在新数组中的下标
                    HashEntry<K,V> n = newTable[k];//将 newTable[k] 赋值给 n, 相当于将新数组中的 头节点 赋值 n
        
                    // 头插法核心: 将new HashEntry<K,V>(h, p.key, v, n)当前节点的下一个节点指向 头节点
                    //            将 新的头节点 指向 当前节点
                    newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                }
            }
        }
    }
    
    // 到这里 说明 for 循环结束了, 即将oldTable老数组中的元素, 全部转移到了新数组
    // 由于该扩容方法在参数处 将 本次put的节点 传入了, 所以还需要将本次put的节点 也插入新节点
    // add the new node
    int nodeIndex = node.hash & sizeMask;//计算出下标
    node.setNext(newTable[nodeIndex]);//将当前节点的下一个节点指向 数组下标处的头节点
    newTable[nodeIndex] = node;//将头节点 置为 当前节点, 完成头插法
    table = newTable;//将新数组赋值给 table 至此 当前Segment对象 扩容结束.
}
```

**get()**
```java
/**
 * get方法没什么好讲的, 就是先根据hash计算出下标, 然后确定出Segment, 然后根据hash计算出在HashEntry中的下标, 然后遍历链表 比对key, 一致后将 value返回
 */
public V get(Object key) {
    Segment<K,V> s; // manually integrate access methods to reduce overhead
    HashEntry<K,V>[] tab;
    int h = hash(key);
    long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
    if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null && (tab = s.table) != null) {
        for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile(tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE); 
            e != null; e = e.next) {
                K k;
                if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                    return e.value;
            }
        }
        return null;
    }
```

哎, 今天又是躺平的一天, 啥也没干, 主要就是思想滑坡了(主要就是抖音刷多了: 什么郑州银行, 小镇做题家, 谭Sir, 感觉有点道心破碎, 其实这些跟我们也没什么关系, 也不是说没关系吧, 主要就是太遥远了 就像 美国 乌克兰 俄罗斯 一样)

本来准备是直接学HashMap1.8的, 但是考虑到HashMap1.8中有很多红黑树的操作, 为了避免出现concurrentHashMap1.7中出现的对并发,锁不熟悉,导致看的比较懵逼的问题出现,
在学习HashMap1.8之前我决定还是系统的学习一下红黑树(据说很难, 不指望得心应手, 至少得明白个大概), 之前学习数据结构时, 只学习到了 平衡二叉数, 那么平衡二叉树得下一级就是红黑树,
找了几个视频讲红黑树得都是两个多小时, 有一个6个多小时得, 感觉这个全面一些, 综合考虑决定学这个6个小时得, 红黑树学完了回头再来看这个HashMap1.8得源码

(红黑树学习得代码, 就不放在该项目了毕竟这是个讲集合源码得项目, 就放在数据结构得项目中了) (日后以此为鉴, 不要重蹈覆辙)

