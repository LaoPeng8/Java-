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

