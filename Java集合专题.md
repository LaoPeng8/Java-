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
     * 就是在 等待获取锁的期间 利用这个时间 创建一下 本次put的HashEntry对象
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
            if (e != null) { // 链表中有节点, 遍历所有节点看看是否有重复
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
            else { // 到这里链表就遍历完了, 没有重复
                if (node != null) //判断 node 是否为null, 如果不为了null, 说明之前在等待锁的时候就已经创建好了, 则 node.next = first (头插法)
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
                    //
                    // 就是 通过CAS乐观锁或者说无锁算法, 将node保存到 table[index] 也就是保存到头节点处, node.next之前已经指向了table[index] (头插入)
                    setEntryAt(tab, index, node);
                
                // 添加完成后就是, ++modCount; 修改次数+1, count = c; 存储数据个数真正 + 1;
                ++modCount;
                count = c;
                oldValue = null;//这个返回值, put相同的key会将老的value返回, 这里并没有重复, 所以返回值为null
                break;
            }
        }
    } finally {
        unlock(); //操作完成, 释放锁
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
 * 
 * 这个方法最关键的理解就是, tryLock()非阻塞式获取锁, 获取到了返回true, 获取不到返回false
 * 在 while (!tryLock()) { 中就是, 如果没有获取到锁 即!false 就会进入循环  通过一系列操作生成 本次需要put的 HashEntry 对象, kv对在形参中获取
 * 直到获取 到 锁, 即 !true 就会退出循环, 返回 HashEntry对象, 有两种可能 1.创建好了 2.没有创建好 就是null, 外面会判断是否为null的
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

# HashMap 1.8
**红黑树插入**
红黑树的插入跟平衡二叉树的插入基本一样, 只是多了个插入后判断是否平衡然后进行颜色的转换 与 左旋右旋
```java
/**
 * 向红黑树中插入节点后, 调整红黑树平衡的方法 (该方法不是插入方法)
 * 
 * 可以看我在 DataStructures 中写的红黑树调整平衡的方法, 注释很多, 比较好理解
 * 这里这个红黑树调整平衡的方法, 和我之前写的调整平衡的方法 基本一致 (红黑树调整平衡就是那么几种情况)
 * 
 * @param root 根节点
 * @param x 当前插入节点
 */
static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root, TreeNode<K,V> x) {
    x.red = true;//当前插入节点, 都是红色
    
    /*
     * 由于会出现  父节点为红色, 叔叔节点存在并且为红色(父-叔 双红) 这种情况需要将父-叔-爷节点颜色翻转, 即父黑,叔黑,爷红, 然后爷爷节点是红色,
     * 可能爷爷节点的父节点也是红色, 那么就形成了双红, 所以需要以爷爷节点为当前节点继续递归向上修复平衡
     * 
     * 所以此处需要有一个for循环
     * xp表示当前节点的父节点, xpp表示当前节点的爷爷节点, xppl表示当前节点的爷爷节点的左子节点, xppr表示当前节点的爷爷节点的右子节点
     */
    for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
        if ((xp = x.parent) == null) {//给xp赋值的的同时, 判断当前节点的父节点是否为null
            // 为null则说明当前插入节点为根节点, 根节点颜色必须为黑色, 所以将当前节点颜色设置为黑色, 然后返回, 即红黑色调整平衡结束
            x.red = false;
            return x;
        }
        //如果当前节点的父节点是黑色 或者 当前节点的爷爷节点为null 则直接return 根节点; (不需要调整平衡)
        //父节点为黑色的情况, 是不需要处理的, 因为插入节点为红色, 不会影响平衡. 爷爷节点为null 说明父节点为root节点, root节点必是黑色, 插入红色节点也不需要处理
        else if (!xp.red || (xpp = xp.parent) == null)
            return root;
        if (xp == (xppl = xpp.left)) {//判断父节点 在爷爷节点中是否为左子节点
            if ((xppr = xpp.right) != null && xppr.red) {//判断叔叔节点是否存在 且为红色. (父节点为左子节点, 叔叔肯定是右子节点嘛)
                // 父节点为红色, 叔叔节点存在并且为红色(父-叔 双红) 这种情况需要将父-叔-爷节点颜色翻转, 即父黑,叔黑,爷红, 然后爷爷节点是红色,
                //可能爷爷节点的父节点也是红色, 那么就形成了双红, 所以需要以爷爷节点为当前节点继续递归向上修复平衡
                xppr.red = false;//叔叔节点置为黑色
                xp.red = false;//父节点置为黑色
                xpp.red = true;//爷爷节点置为红色
                x = xpp;//将爷爷节点置为当前节点, 继续递归向上修复平衡
            }
            else {//叔叔节点不存在，或者为黑色
                /**
                 *  |—--情况4.2:叔叔节点不存在，或者为黑色，父节点为爷爷节点的左子树
                 *     |—--情况4.2.1:插入节点为其父节点的左子节点(LL情况)               这种情况需要将父-爷节点颜色翻转, 即父黑,爷红, 然后根据爷爷节点 进行右旋即可
                 *     |---情况4.2.2:插入节点为其父节点的右子节点(LR情况)               这种情况需要以父节点为当前节点进行左旋, 这样即得到了 4.2.1的情况
                 * */
                if (x == xp.right) {//如果 当前节点 是父节点的右子节点
                    // 这种情况需要以父节点为当前节点进行左旋, 左旋完成后实际是从 LR情况 变成了 LL情况
                    root = rotateLeft(root, x = xp);// x = xp; 显然是根据父节点进行左旋的
                    xpp = (xp = x.parent) == null ? null : xp.parent;
                }
                
                // 这种情况需要将父-爷节点颜色翻转, 即父黑,爷红, 然后根据爷爷节点 进行右旋即可 (处理 LL情况)
                if (xp != null) {
                    xp.red = false;//父节点置为黑色
                    if (xpp != null) {
                        xpp.red = true;//爷爷节点置为红色
                        root = rotateRight(root, xpp);//根据爷爷节点进行右旋
                    }
                }
            }
        }
        else {//父节点为右子节点 即 xp == xp.parent.right       这个else中的内容 相当于是 与 if中的内容 方向相反, 即处理方向相反的情况
            if (xppl != null && xppl.red) {//判断叔叔节点是否存在 且为红色. (父节点为右子节点, 叔叔肯定是左子节点嘛)
                // 父节点为红色, 叔叔节点存在并且为红色(父-叔 双红) 这种情况需要将父-叔-爷节点颜色翻转, 即父黑,叔黑,爷红, 然后爷爷节点是红色,
                //可能爷爷节点的父节点也是红色, 那么就形成了双红, 所以需要以爷爷节点为当前节点继续递归向上修复平衡
                xppl.red = false;//叔叔 黑
                xp.red = false;//父亲 黑
                xpp.red = true;//爷爷 红
                x = xpp;//以爷爷节点为当前节点继续递归向上修复平衡
            }
            else {//叔叔节点不存在，或者为黑色
                if (x == xp.left) {// 如果 当前节点 是父节点的左子节点
                    // 这种情况需要以父节点为当前节点进行左旋, 左旋完成后实际是从 RL情况 变成了 RR情况
                    root = rotateRight(root, x = xp);
                    xpp = (xp = x.parent) == null ? null : xp.parent;
                }
                
                // 这种情况需要将父-爷节点颜色翻转, 即父黑,爷红, 然后根据爷爷节点 进行左旋即可 (处理 RR情况)
                if (xp != null) {
                    xp.red = false;//父黑
                    if (xpp != null) {
                        xpp.red = true;//爷红
                        root = rotateLeft(root, xpp);//根据爷爷节点进行 左旋
                    }
                }
            }
        }
    }
}

/**
 * HashMap中对 红黑树进行 左旋的方法 (右旋就不介绍了)
 * 可以看一下总体思路 和 我们正常写的左旋一样, 就是很多赋值它都写在if里面 就感觉很乱, 仔细对着我们自己的代码看 发现差不多, 或者对着图看
 * 
 * 感觉说的不是很清楚, 需要了解左旋右旋 建议去看我在 DataStructures 中写的红黑树中的左旋右旋注释很多, 比较好理解
 */
static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,TreeNode<K,V> p) {
    TreeNode<K,V> r, pp, rl;// p 为当前节点(即旋转的节点), r 为p的右子节点, pp 为p的父节点, rl 为p的右子节点的左子节点
    if (p != null && (r = p.right) != null) {//如果 p 为null 或 p的右子节点为null, 则不用进行左旋了, 右子节点都没有怎么左旋,旋个鸡毛
        if ((rl = p.right = r.left) != null)//将 p的右子节点的左子节点 赋值给 p的右子节点(这是关键), 随便给 rl 赋下值(rl为 p的右子节点的左子节点)
            // 如果 rl == null 则说明 p的右子节点的左子节点为null, 将null赋值给p.right 也是没毛病的, 但是如果null.parent 就会报空指针了, 所以这是该if的作用
            rl.parent = p;// rl都指向p.right了, 那么 将rl.parent指向 p 也没毛病
        if ((pp = r.parent = p.parent) == null)//将 p 的右子节点的父节点指向 p的parent, 即将 p的右子节点往上提, 即成为新的根节点
            //如果 p.parent == null 说明p是根节点, 则将 r赋值给root, 即将 p的右子节点往上提, 即成为新的根节点
            (root = r).red = false;
        else if (pp.left == p)//判断 父节点是爷爷节点的 左子节点还是右子节点 (pp不为null才会到这里嘛, 如果将r往上提, 替代p则pp需要指向r嘛, 但是不知道是left指向还是right指向)
            pp.left = r;//pp.left == p, 说明p是pp的左子节点, 那么此时 r 替代 p, 则pp的左子节点指向 r 嘛
        else
            pp.right = r;//反之亦然
        r.left = p;//新的根节点的左子节点 指向 当前节点 (因为新的根节点的左子节点在第1步中实际是赋值给了 p的右子节点)
        p.parent = r;//p的parent也指向 r
    }
    return root;
}
```
**put()**
```java
// 首先是 调用hash()方法将 key的hash值计算出来, 并传入putVal()方法
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}


/**
 * 1.8中的 hash方法 没有1.7的那么复杂
 * 目的都是一样根据key的hashCode()方法的hash值 计算出一个 新的hash值(该hash值需要 在计算数组下标时 计算出的下标更加散列)
 */
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}


/**
 * put方法中的核心插入方法
 * 
 * 
 * @param onlyIfAbsent 该参数在 put()方法调用时都为false, 只有在putIfAbsent()方法种调用时都为true
 *                     分别表示: put()方法 在遇见重复key时, 会用本次put时的新value值替换旧的value值, 并返回旧的value值
 *                             putIfAbsent()方法 在遇见重复key时, 不会替换(遇见重复key啥也不干), 直接返回旧的value值
 * @param evict 该参数HashMap中没有用到, LinkedHashMap中才用到了
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    
    /**
     * 判断数组是否为空
     * 如果为空, 则调用 resize() 方法 来进行初始化数组, 并赋值给tab, n=数组长度.  等下单独讲 resize()方法, 该方法不仅包括初始化也包括扩容
     * */
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    
    /**
     * 常规的根据 (数组长度-1) & hash 计算出, 该key在数组中的下标 并赋值给 i, 然后根据下标在tab[i]数组中取出对应值赋值给 p
     * 判断 p 是否 == null, 如果 == null 说明该数组下标处链表没有元素, 则tab[i] = 插入节点, 即将新节点直接放入数组, 都不用遍历链表
     * */
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {// key对应下标处不为null, 需要遍历链表将 新节点 存放到链表尾部
        Node<K,V> e; K k;

        /**
         * 首先得知道 p 在上方已经被赋值为了当前 key下标处得 数组元素 即 tab[key下标], 而且 tab[key下标] != null
         * 另外就是 if, else if, else 这三种情况 只会指向其中一个
         * 
         * 此处if就是先粗略得判断 key值 是否重复 (后面得 else if, else 再详细判断是否重复)
         * 判断当前插入key 与 它下标对应得数组元素处(或者说 它应该存放处得链表头节点)得key是否重复
         * 如果重复, 则 将 e 指向 p; (相当于 e 就是 重复得Node节点)
         * 后面会有判断, if (e != null) 如果 == null, 则说明没有重复节点, 那么该if就不用处理了,
         * 如果有重复节点, 则 oldValue = e.value; 会保存旧的value值并返回. 返回之前会用本次put的value 替换旧的Node的value值 e.value = value;
         * 
         * 
         * 此处的 else if 就是判断 p 的类型是否为 TreeNode, 即判断 p 是否为红黑树的根节点 (不知道 p 是何物 就看本注释第一行)
         * 如果 p 就是红黑树的根节点, 即在红黑树中操作: 是否在红黑树中插入新节点 或者 是否有重复key (有重复key会将该节点返回并赋值给 e)
         * 具体的红黑树逻辑在 putTreeVal() 方法中, 等下单独讲 putTreeVal()方法.
         *
         */
        if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            /**
             * 此处的 else 就是相当于说 p 是一个链表的头节点, 且 p的key 与 当前插入key 不重复 (不知道 p 是何物, 上面的注释介绍过)
             * 遍历该链表, 遍历到链表的最后一个元素即 p.next = null (每遍历一个元素, binCount++, 用来记录链表长度) (实际上p已经在上面的if判断了是否重复了, 所以这里遍历是从p.next开始的)
             *      如果没有发现链表中有key与当前key重复, 即将当前key new成一个节点,赋值给p.next, 即尾插法
             *      然后会判断 链表长度 >= 8 - 1, 即调用 treeifyBin(tab, hash) 将链表转为红黑树
             *      结论是 可以发现 当前链表长度为 7 加上当前插入节点 长度为 8, 不会触发转红黑树, 当前链表长度为8 加上 当前插入节点 长度为 9 才会触发转红黑树
             *      (具体 什么时候转红黑树可以看 org.pjj.map1_8.HashMapSource 该类中进行了详细的debug)
             *      不管转没转红黑树, 将新节点插入链表尾部后 会执行 break 跳出循环, 此时 e == null, 所以后面也不会对e的value值进行替换
             * 
             *      如果发现了重复key, 那么直接break; 那么此时 e != null 且 e指向重复key的节点, 那么后面就会对e的value值进行替换
             * */
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);//尾插法
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);//转红黑树
                    break;
                }
                if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;//首先要知道 e = p.next; 那么此处相当于 p = p.next; 遍历链表关键之所在
            }
        }
        
        /**
         * 如果上面的 if, else if, else 判断找到了重复key, 则 e 指向的 重复key的Node节点
         * 如果 e == null 说明没有找到重复key, 那么此处也不用处理
         * 
         * onlyIfAbsent 该参数在 put()方法调用时都为false, 只有在putIfAbsent()方法种调用时都为true
         * 即  !false 就是true, 即put()方法调用时, 不管旧的value是否==null 都会替换key值重复的Node的value值
         *    !true 就是false, 则putIfAbsent()方法调用时, 只有 旧的value==null时, 才会替换为新的value值, 否则不会替换(啥也不干)
         * if (!onlyIfAbsent || oldValue == null)
         *      e.value = value;
         *
         * */
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);//该方法在HashMap中没有用到, 是空实现, 在LinkedHashMap中才用到了
            return oldValue;//返回旧的value值
        }
    }
    
    /**
     * 修改次数 + 1 (插入重复key时需要替换value值, 替换value值后都是直接return, 不会来到这, 所以插入重复key不会导致修改次数 + 1)
     * 加入当前新节点后 ++size 长度+1, 判断size是否大于 阈值, 如果大于则进行扩容 (扩容方法后面单独讲解)
     * 注意 size 表示当前 HashMap中元素的个数, 即数组中所有的元素包括链表包括红黑树中的元素的 个数
     * */
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);//该方法在HashMap中没有用到, 是空实现, 在LinkedHashMap中才用到了
    return null;
}


/**
 * 当前链表长度为8 加上 当前插入节点 长度为 9 时, 即插入该链表第九个元素时会触发 将链表转为红黑树, 即本方法
 * (看了本方法才知道 实际除了满足链表长度为8,且正在插入第9个元素 外, 还需要 数组长度大于64才会 正在转红黑树, 否则会扩容数组)
 * 
 * 实际本方法就是根据单向链表(Node) 生成了一个双向链表(TreeNode), 然后调用 hd.treeify(tab);//真正转红黑树的方法
 * 
 * @param tab HashMap中的数组
 * @param hash 当前插入key的 hash值
 */
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    
    /**
     * 只有满足链表长度为8, 且正在插入第9个元素时, 才会触发该方法 将 链表转为红黑树
     * 但是 这里有个判断 如果 数组为空 或者 数组长度 小于 64, 则实际是将数组扩容(不会将链表转为红黑树)
     * 那么将链表转为红黑树实际是因为链表太长了, 查询效率很低.
     * 那么扩容数组, 也可以达到 变短链表的效果 (显然 jdk 认为 数组长度 < 64 链表转红黑树的效率 不如 直接扩容数组来的提升大, 所有 数组长度 < 64 jdk选择扩容数组)
     *
     * */
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {//判断 该hash计算出的 数组下标处不为空, 才会执行转转红黑树 (能到这里 该链表长度最少是 9, 应该不存在为null的情况, 不过这里也是为了安全)
        TreeNode<K,V> hd = null, tl = null;
        
        /**
         * 使用do-while遍历该链表
         * 根据 以Node为节点的单向链表 生成一个 以TreeNode为节点的双向链表
         * */
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);// 该方法就一句话 return new TreeNode<>(p.hash, p.key, p.value, next);
            if (tl == null) //第一次遍历 tl肯定==null, 则单向链表的头节点p赋值给 hd
                hd = p;//hd表示双向链表的头节点
            else {//每次遍历完 tl = p; 所以 第二次遍历 tl就不为空了, (tl表示当前节点的前一个节点, p表示当前节点)
                // 将当前节点的前一个节点 指向 tl, tl的下一个节点指向 p
                p.prev = tl;
                tl.next = p;
            }
            tl = p;//保存当前节点, 作为下一个当前节点的前一个节点
        } while ((e = e.next) != null);
        if ((tab[index] = hd) != null) // 将hash值下标处的元素置为 hd, 即将此处的单向链表 改为了 双向链表存在此处
            // 然后根据该节点(hd, 也就是双向链表的头节点)进行 转红黑树
            hd.treeify(tab);//真正转红黑树的方法
    }
}


/**
 * 真正 转红黑树的方法 (就是把链表中的每个元素, 一个一个插入普通的二叉树(左小右大), 然后再调用红黑树平衡的方法 通过左旋右旋颜色翻转等 调整平衡)
 *                  root = balanceInsertion(root, x); 该方法就是调整红黑树平衡的方法, 具体如何平衡昨天已经讲了, 该方法在HashMap1.8标题下就有详细讲解
 */
final void treeify(Node<K,V>[] tab) {
    TreeNode<K,V> root = null;
    
    /**
     * this为根节点, 遍历当前链表, 然后将元素一个一个往红黑树里面插入
     * /
    for (TreeNode<K,V> x = this, next; x != null; x = next) {
        next = (TreeNode<K,V>)x.next;
        x.left = x.right = null;
        if (root == null) {//如果root节点为空, 则根节点 = x; 且根节点.parent为空, 根节点颜色为黑色 (第一次遍历链表 x == this == 链表头节点, 而且第一次root肯定为空)
            x.parent = null;
            x.red = false;
            root = x;
        }
        else {
           /**
            * 这里就是 遍历二叉树, 然后插入新节点, 也不能说是遍历二叉树吧, 就是正常插入节点到二叉搜索树的过程
            * 判断当前节点 与 遍历节点的大小, 当前节点大 就往右遍历, 当前节点小 就往左遍历 (左小右大), 直到找到null, 那么null的父节点就是当前节点的父节点
            * 
            * 
            * 
            * x 表示当前插入节点, k表示x的key, h表示 x的hash
            * p 表示 红黑树中正在遍历的节点, ph 表示 p的hash值, pk 表示p的key
            * dir 就是一个标志位 -1 表示 当前节点 < 红黑树中遍历的节点 也就是 x < p 为 -1,  x > p 为 1。  -1表示左  1表示右
            */
            K k = x.key;
            int h = x.hash;
            Class<?> kc = null;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph;
                K pk = p.key;
                if ((ph = p.hash) > h) // 如果 h < p 说明 h 应该在 p的左边, 则将标志位 dir = -1; 表示等下往左边遍历 即 p = p.left
                    dir = -1;
                else if (ph < h) // 如果 h > p 说明 h 应该在 p 的右边, 即将标志位 dir = 1; 表示等下往右边遍历 即 p = p.right
                    dir = 1;
                // 这种情况就是 hash值重复了, 即 ph == h, 那么就先comparableClassFor()方法判断一下, k 是否实现了 Comparable 接口
                // 如果实现了 则调用 compareComparables()方法 对 k 与 pk 继续比较 实际就是调用 Comparable的compareTo方法比较的, 然后将值赋值给dir
                // 如果 经过 compareComparables()方法 dir 还是 == 0, 则进入该 else if 调用tieBreakOrder()来比较 k 与 pk 的大小
                // tieBreakOrder()方法内部会通过 getClass().getName().compareTo 来比较, 如果还是 == 0,
                // 则调用 (System.identityHashCode(a) 来最终的到 -1 或 1
                // System.identityHashCode(a) 与 a.hashcode() 一样都是用来获取hash值的, 只不过如果a对象重写了hashcode方法, 即返回重写后的值, 而System.identityHashCode(a) 不管你重没重写hashcode方法都是返回真正的hash值
                // 其实我对这里并不是很了解, 返回就是 如果p.hash == h, 即hash冲突了, 是通过该一系列方法来计算出 p.hash 与 h的大小关系最终得到 -1 或 1
                else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0)
                    dir = tieBreakOrder(k, pk);//如果最终dir还是 == 0, 则最终是放在放在左边

                /**
                 * 在 p 指向 p.next 之前, 先用 xp = p; xp来保存下一个 p, 的父节点
                 * 为什么叫 xp? 是因为 如果 p 的下一个 == null, 说明 当前插入节点 x 找到了插入的地方即这个null处, 那么 xp 实际就是 x 的父节点
                 * */
                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {//dir <= 0 就是 -1 或 0,往左遍历 dir不 <= 0 就是 1,往右遍历, 然后将值赋值给 p
                                                                  //同时判断 p 是否 == null, 如果==null 说明找到 x 的插入地方了.
                    x.parent = xp;//x的父节点指向 xp
                    if (dir <= 0)//判断 xp 的左子节点 是x, 还是右子节点是x, dir上面已经判断了知道是左是右
                        xp.left = x;//dir = -1; xp.left = x;
                    else
                        xp.right = x;//dir = 1; xp.right = x;
                    //到此 一个节点已经插入到 一个普通的二叉搜索树. (即插入已经完成)
        
                    //该方法就是 插入节点到红黑树后, 进行平衡红黑树的方法, 方法具体如何平衡昨天已经讲了, 该方法在HashMap1.8标题下就有详细讲解
                    root = balanceInsertion(root, x);//左旋右选过程中, 根节点可能发生改变, 所以将 root 节点返回
                    break;
                }
            }
        }
    }
    /**
     * 该方法就是将 root 节点, 移动到数组下标处
     * 原本数组下标处 存放的是 链表头节点, 但是经过转为红黑树后, 该"链表头节点"都不知道是红黑树中哪个节点了,
     * 既然它都不是头节点了, 自然不能继续存放在数组下标处, 所以需要将 root 节点存放到数组下标处.
     */
    moveRootToFront(tab, root);
}

/**
 * 该方法就是将 root 节点, 移动到数组下标处
 * 原本数组下标处 存放的是 链表头节点, 但是经过转为红黑树后, 该"链表头节点"都不知道是红黑树中哪个节点了,
 * 既然它都不是头节点了, 自然不能继续存放在数组下标处, 所以需要将 root 节点存放到数组下标处.
 * 
 * 首先要理解的是, 之前根据单向链表构建了一个 双向链表, 然后根据双向链表 创建的红黑树,
 * 但是要注意, 创建完红黑树后, 红黑树中的节点, 同时还是双向链表中的节点 即TreeNode同时保存这红黑树的parent, left, right等节点, 还保存这双向链表的 prev, next等节点
 * 所以创建红黑树后 红黑树的root节点, 在双向链表中大概率不是在头节点的位置, 那么本方法就是要将 root节点移动到 头节点的位置上, 且 tab[index] = root;
 */
static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
    int n;
    if (root != null && tab != null && (n = tab.length) > 0) { //root节点 与 数组不为空才进行处理
        int index = (n - 1) & root.hash;//根据hash值计算出下标
        TreeNode<K,V> first = (TreeNode<K,V>)tab[index];//用first保存 链表头节点
        if (root != first) {//如果 root != 链表头节点才处理
            Node<K,V> rn;
            tab[index] = root;//将root直接放在数组下标处 (链表头节点已经用 first保存了)
            TreeNode<K,V> rp = root.prev;//rp = root节点的前一个节点 (之前说了root节点在双向链表中并不是第一个元素)
            if ((rn = root.next) != null)//rn = root节点的后一个节点, 如果root节点的后一个节点不为空
                ((TreeNode<K,V>)rn).prev = rp;//则将root节点的前一个节点 指向 root节点的后一个节点 (相当于将root从链表原有位置删除)
            if (rp != null)//root节点前一个节点不为空
                rp.next = rn;//则root节点的前一个节点的后一个节点 指向 root节点的后一个节点 (相当于将root从链表原有位置删除)
        
            // 经过上面的步骤, root节点已经从 头节点为 first的链表中删除了, 此时root节点只是单独一个节点 存放在数组下标处
            if (first != null)//如果 链表头节点 不为空
                first.prev = root;//则链表头节点的前一个指向 root
            root.next = first;//root的后一个指向 链表头节点
            root.prev = null;//root的前一个指向null
            // 即root成为新的头节点, 并存放在数组下标处, root的下一个为 原有链表的头节点(此时为链表的第二个节点)
            // 这样即完成了 root从链表的其他地方移动到链表头节点处
        }
        
        // 表示判断经过上面一系列对链表的操作后, 红黑树是否还符合红黑树的条件
        // 有点向简化半版的 throw 即,assert不满足条件会抛出异常
        assert checkInvariants(root);
    }
}

/**
 * 该方法就是 putVal()中处理 将一个节点插入红黑树得情况
 * else if (p instanceof TreeNode)
 *      e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
 * 此方法实际就是 将一个节点插入 红黑树, 与之前讲得 treeify()方法中得 else 情况一样 (不懂就本方法就对着 treeify()方法 对着看)
 * 简单介绍一下流程:
 * 先 根据 当前节点得hash值 与 遍历节点得hash比较. 当前节点hash < 遍历节点 则 dir = -1, 之后就往左继续遍历. 当前节点hash > 遍历节点 则 dir = 1, 之后就往右继续遍历.
 * 如果 当前节点hash值 既不 > 遍历节点 也不 < 遍历节点, 则判断 key值是否重复, 然后重复则 返回 重复节点 然后本方法结束, 调用本方法得putVal中会根据本方法返回得重复节点, 选择是否要替换重复值
 * 如果 当前节点hash值 既不 > 遍历节点 也不 < 遍历节点 而且 key值还不重复, 则进行一系列计算 计算出 dir 得值, 然后根据dir得值选择 是往左遍历还是往右遍历
 * dir <= 0; 往左遍历, dir 不 <= 0; 往右遍历 直到遍历到 null, 则找到了当前插入节点得位置, 则进行插入
 * moveRootToFront(tab, balanceInsertion(root, x)); 最后通过balanceInsertion()调整红黑树平衡, 通过moveRootToFront()将根节点root放入数组下标处
 * 
 * 流程是这个流程, 但是需要注意的是 在直接插入元素至红黑树中时, 也就是本方法
 * 在将元素插入至红黑树中时, 同时还维护了双向链表, 即将元素插入红黑树中的同时也将该元素插入了双向链表,
 * 因为这里的节点既有 left, right等红黑树属性 也有 prev,next等双向链表属性 即 这里的TreeNode节点 是红黑树节点的同时也是双线链表节点 (一个人打两份工了属于是)
 * 
 * 总结一下 (两者都在一起 看的比较混乱, 这里给拎出来看)
 *      处理红黑树关系的代码
 *      xp.left = x; 或 xp.right = x; //父节点 指向 待插入节点
 *      x.parent = xp;//待插入节点的父节点 指向 父节点
 *      
 *      处理双向链表关系的代码
 *      Node<K,V> xpn = xp.next;//保存 父节点的 下一个节点
 *      TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn); //相当于是 x.next = xpn; 待插入节点的下一个节点 指向 父节点的下一个
 *      xp.next = x;//父节点 的下一个指向 待插入节点 (父节点原本的下一个 已经由 x的下一个保持了, 不用担心链表会断)
 *      x.prev = xp;// 当前节点的前一个节点 指向 父节点 (此时 当前节点的父节点已经指向当前节点, 当前节点的前一个节点也指向了父节点)
 *      ((TreeNode<K,V>)xpn).prev = x;// 当前节点的下一个节点的前一个节点指向当前节点 (此时 当前节点的下一个节点指向了 原本父节点的下一个节点, 原本父节点的前一个节点指向了当前节点)
 *      //至此 完成了维护双向链表关系 (也就是一个 正常的双向链表插入节点的操作)
 *      
 */
final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v) {
    Class<?> kc = null;
    boolean searched = false;
    TreeNode<K,V> root = (parent != null) ? root() : this;
    for (TreeNode<K,V> p = root;;) {
        int dir, ph; K pk;
        if ((ph = p.hash) > h)
            dir = -1;
        else if (ph < h)
            dir = 1;
        else if ((pk = p.key) == k || (k != null && k.equals(pk)))
            return p;
        else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0) {
            // 当 待插入节点的hash值 与 红黑树中以有节点的hash值重复了, 则是这个 else if 处理的情况
            
            if (!searched) {
                // 这个 if 还真是看不太懂啊, 当本次putTreeVal的节点hash值重复, 时 serched = false; !serched = true, 肯定会进入该if的
                // 之后就是判断 如果红黑树当前遍历hash值重复的元素 的左子节点不为空, 则以左子节点为根节点 递归查找 本次putTreeVal的key是否重复 (find方法的内容)
                // 而查找方式 也是 比较hash值 往左往右查询, 找到了key值重复的节点 则一层一层返回 找不到 则返回null, 由底部的 dir = tieBreakOrder(k, pk); 计算出到底往左还是往右    
                // 感觉完全是多此一举, 完全不知道该 if (!searched)存在的意义, 感觉就算不要 也是可以正常运行的...
                TreeNode<K,V> q, ch;
                searched = true;
                if (((ch = p.left) != null && (q = ch.find(h, k, kc)) != null) || ((ch = p.right) != null && (q = ch.find(h, k, kc)) != null))
                    return q;
            }
            
            dir = tieBreakOrder(k, pk);// 计算处 hash值重复了, 应该是往左还是往右
        }

        TreeNode<K,V> xp = p;// 保存 p, 之后 p = left 或者 p = right; 就相当于 xp 是 p的父节点 (此时 p == null, p相当于是待插入节点, xp相当于待插入节点的父节点)
        if ((p = (dir <= 0) ? p.left : p.right) == null) {//dir <= 0 就是 -1 或 0,往左遍历 dir不 <= 0 就是 1,往右遍历, 然后将值赋值给 p
                                                            //同时判断 p 是否 == null, 如果==null 说明找到 x 的插入地方了.
                                                            //如果 != null, 则说明没有遍历到红黑树 叶子节点, 则继续for循环,往左往右 直到找到插入地方
            Node<K,V> xpn = xp.next;// xpn 保存 待插入节点的父节点 的next指针 (以在插入红黑树节点的同时 维护 双线链表)
            TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);// x 就是待插入节点, 同时 x.next = xpn (即 待插入节点的下一个节点 指向 待插入节点的父节点的下一个节点)
            if (dir <= 0)
                xp.left = x; //将带插入节点插入红黑树
            else
                xp.right = x;//将带插入节点插入红黑树
            xp.next = x;// 之前两步已经将 x.next 指向了 xp.next 此时将 xp.next = x; 相当于将 x 插入双向链表中
            x.parent = x.prev = xp;// 将 xp 赋值给 x.parent (处理红黑树关系), 将 xp 赋值给 x.prev (处理双向链表关系)
            if (xpn != null)
                ((TreeNode<K,V>)xpn).prev = x;//如果 xpn不为null, 则将 xpn.prev 指向 x
            moveRootToFront(tab, balanceInsertion(root, x));//节点插入红黑树, 红黑树旋转保持平衡, 旋转后root节点可能移动, 该方法将root赋值到数组下标处
            return null;
        }
    }
}
```

**扩容**
```java
/**
 * 首先要知道 在putVal()方法中, 有三种情况会调用本方法进行扩容
 * 1. 数组为空 或 数组长度 == 0 会调用本方法进行 对数组进行初始化
 * 2. 当 链表长度为8, 正在插入第9个元素时, 会对该链表进行转为红黑树操作, 在实际进行转红黑树之前 会先判断数组长度是否 < 64, 如果小于64则不进行转红黑树, 而且 调用本方法对数组进行扩容
 * 3. HashMap元素个数 > 阈值; ++size > threshold 则调用本方法进行扩容
 * 
 * 
 * 
 * 
 * 
 * 
 */
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    
    //老数组长度 > 0 的情况就是, 正常扩容的情况.
    if (oldCap > 0) { //首先判断 老数组长度 > 0
        if (oldCap >= MAXIMUM_CAPACITY) {//如果老数组长度 >= 数组最大值(1 << 30), 则数组实质扩不了容了, 已经最大了
            threshold = Integer.MAX_VALUE;//则将阈值 赋值为 Integer的最大值, 相当于变相扩容了
            return oldTab;//返回老数组(在此处 即是扩容后的数组(虽然并没有真正意义上的扩容))
        }
        // 如果 老数组长度不 >= 数组最大值, 则 新数组长度为 老数组 << 1;(即长度翻倍)
        // 且判断 新数组长度是否 < 数组最大值 同时 老数组长度 >= 16, 如果满足 则新阈值 = 老阈值 << 1;(翻倍)
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold  新阈值 = 老阈值 * 2; 和 新阈值 = 新数组长度 * 加载因子; 实际是一样的
    }
    // 这里的 else if 与 else 就是处理 数组初始化的情况
    // 旧阈值 = threshold = 数组长度 * 0.75; 数组长度为0 时 threshold = 0; 则旧阈值 = 0; 说明数组还为初始化
    else if (oldThr > 0) // initial capacity was placed in threshold
        // oldThr > 0, 不知道 为什么 oldThr > 0 则 新数组长度为 老阈值, oldThr > 0 只能说明 原数组肯定是初始化过了的, 不知道为什么要 newCap = oldThr
        //明白了, 这种情况是处理 有参构造器 来new HashMap() 之后, 进行初始化的情况
        //public HashMap(int initialCapacity) { 这是有参构造器, 它接收一个 数组长度
        //    this(initialCapacity, DEFAULT_LOAD_FACTOR);//然后调用无参构造, 传入指定数组长度, 与默认加载因子 0.75
        //}
        //this.threshold = tableSizeFor(initialCapacity);//无参构造中 第一次阈值是被赋值为 数组长度的 (tableSizeFor的作用是返回2的幂次方数, 如传入10返回16, 传入17返回32)
        //所以, 如果 oldCap <= 0 才会进入 该else if(oldThr > 0)判断
        //相当于说进入该 else if 是同时满足了, oldCap <= 0 && oldThr > 0
        //那么什么时候会出现这种情况呢, 就是有参构造传入数组长度为 负数 或者 0, 此时数组长度为 0, 阈值为 1 (tableSizeFor(数组长度)导致 阈值最少为 1)
        //所以这里初始化就是处理这种情况, 处理有参构造器传入 数组长度 < 0的情况, 此时则初始化数组长度为 1
        newCap = oldThr; // 新数组长度为 老阈值
    else {               // zero initial threshold signifies using defaults
        // 如果 oldThr不 >0 说明 <= 0, 说明数组没有初始化, 那么执行初始化的逻辑  (这里就处理的 无参构造的情况)
        newCap = DEFAULT_INITIAL_CAPACITY;//newCap = 16
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);//newThr = 0.75 * 16 = 12
    }
    //这里判断 如果newThr == 0, 则newThr = 新数组长度 < 数组长度最大值 && (新数组长度 * 0.75) < 最大数组长度 ? (新数组长度 * 0.75) : Integer最大值
    //不太明白这里是干什么, 如果 newCap = 36; ft = 24; 则最后返回newThr为24, 可能是做一些最小最大值判断
    //这里是处理 该方法开头 if中的 else if没有满足条件, 则没有进入设置 newThr的值, 则newThr的值到这里还是为 0, 于是到这里处理
    //然后判断 ft 和 新数组长度是否 都 < 数组最大值, 如小于则阈值 = ft, 否则 阈值 = Integer最大值    
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ? (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;//将新阈值赋值给 threshold; (newThr = oldThr << 1)
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];//根据新的数组长度new一个新数组, 并赋值给table; (newCap = oldCap << 1)
    table = newTab;
    if (oldTab != null) {//如果老数组不为空, 则继续遍历
        for (int j = 0; j < oldCap; ++j) {//遍历老数组
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {//遍历老数组种的每一个数组下标处元素
                oldTab[j] = null;
                if (e.next == null) //如果 e.next == null 说明 e 只有一个元素, 则根据e.hash & 新数组长度 - 1; 计算出在新数组的下标
                    newTab[e.hash & (newCap - 1)] = e;//将 e 放入新数组
                else if (e instanceof TreeNode)//判断 e 是否为一个 TreeNode 即判断 e 是否为一个 红黑树的节点, 如是红黑树, 则进入split()方法处理 红黑树情况
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order 最后这种情况就是 e 是链表头节点, 且不止一个节点, 所以接下来需要遍历该链表, 将节点一个一个转移到新数组
                    Node<K,V> loHead = null, loTail = null;// low 指不需要修改下标的链表
                    Node<K,V> hiHead = null, hiTail = null;// high 指需要将下标修改为 原数组下标 + 旧数组长度 的链表
                    Node<K,V> next;
                    do {
                        /**
                         * 这里先用 next 保存 e.next 节点, 防止修改了 e.next指向的节点后 链表丢失
                         * if ((e.hash & oldCap) == 0) 就是在判断 e 在新数组中的下标是在 老数组中同样的位置, 或者是 在另一个位置
                         * 一个节点的下标 在扩容前 与 扩容后只有两种情况:
                         * 1. 扩容前下标为 5, 扩容后还是5
                         * 2. 扩容前下标为 5, 扩容后为 21 (原数组长 16) (扩容后节点下标 = 原下标 + 原数组长度)
                         * 为什么? 在HashMap1.7的扩容方法中介绍过, 这里就不多解释了
                         * 只需要知道 假设原数组长16 即 10000, 原数组计算下标时 hash & (16 - 1) = 15 即 hash & 1111
                         * 那么扩容后 数组长 32 即 100000, 32 - 1 = 31 即 11111
                         * 那么可以发现 15 的 01111 与 32 的 11111 实际就是最高位一个是 0, 一个是 1, 这个最高位1十进制表示16
                         * 那么可以知道 hash & 15 或 hash & 31 实际差就差在这个 16, 如果 hash & 15 = 5, 那么 & 31 只会是 5 或者 5 + 16 = 31
                         * 
                         * 所以 if((e.hash & oldCap) == 0) 实际就是 hash & 10000 也就是判断 最高位 即第5位 即 16 是否发生改变
                         * == 0 说明 hash值的 第5位 是 0, 说明 不管是 & 15 还是 & 31 值都一样, 所以在新数组中下标没有发生该表
                         * == 1 说明 hash值的 第5位 是 1, 说明 & 15时 假设下标为5, &31时 下标就是 5 + 16 = 21
                         * 
                         * 先说一下这里大概的逻辑:
                         * 就是 一个节点 它在新数组中的下标 只有两种, 要么是 原下标, 要么是原下标 + 原数组长度
                         * 这里就是把 正在遍历的这个链表中的所有 原下标的节点 串成一个链表, 然后把所有 原下标 + 原数组长度 的节点 串成一个链表
                         * 然后将 两个链表的头节点 分别放入对应的在 新数组中的对应下标处, 即完成了一个链表的转移, 其他链表照葫芦画瓢都转移过去
                         * (不得不说 确实比 1.7的一个节点一个节点往新数组中放要好很多, 就是代码复杂了好多哈哈哈)
                         * 
                         * */
                        next = e.next;//这里先用 next 保存 e.next 节点, 防止修改了 e.next指向的节点后 链表丢失
                        if ((e.hash & oldCap) == 0) {//不需要修改下标的链表
                            if (loTail == null) //第一次进来 loTail 肯定 == null
                                loHead = e;//将 当前节点 赋值给 loHead, 然后 loTail = e; 即此时 e 既是头节点 也是尾节点, 之后插入该链表的元素 就直接插入尾节点的next; 然后尾节点重新指向最后的节点 即 loTail = e;
                            else
                                // loTail尾节点不为null, 说明尾节点已存在, 则将尾节点的下一个指向 e (尾插法)
                                loTail.next = e;
                            loTail = e;// e表示插入链表的 最后一个元素, loTail = e; 则表示将 loTail重新指向链表真正的最后一个节点
                        }
                        else {//需要修改下标的链表
                            //过程与 上面if中的过程一样, 不过这里存储的节点都是 需要修改下标的节点 组成的链表
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);//遍历老链表完成
                    
                    // 如果 loTail != null 说明不需要修改下标的链表 不为null, 则将最后一个节点的下一个指向null
                    // 由于不需要修改下标 所以直接使用原数组下标 j, 将链表头节点放入数组下标处 newTab[j] = loHead;
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }

                    // 如果 hiTail != null 说明需要修改下标的链表 不为null, 则将最后一个节点的下一个指向null
                    // 由于需要修改下标 所以直接使用原数组下标 j + 老数组长度 oldCap, 将链表头节点放入数组下标处 newTab[j + oldCap] = hiHead;
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                    
                    //至此 已经将老数组中的一个下标处的元素(链表 或 红黑树) 转移到了 新数组
                    //之后继续遍历, 直至老数组中的所有元素都被转移到 新数组
                }
            }
        }
    }
    return newTab;
}


/**
 * 本方法是 resize() 方法中, 处理数组元素中存储的是 红黑树节点的情况
 * 即老数组的某个元素存储的红黑树根节点, 此时调用本方法将红黑树中的节点 移动到 新数组中去
 * 
 * 通过观察本方法发现 本方法 和 扩容方法resize()中 处理 将链表 转移到新数组中的代码 几乎一致
 * 也就是说 将红黑树 转移到新数组 和 将链表转移到新数组 思路是一样的, 链表是只有一个next, 而红黑树有left,right 那么它是如何和链表一样的处理的?
 * 首先我们要知道 红黑树节点TreeNode 不仅包含 left,right,parent 还包含链表所需的 prev, next
 * 实际是 在将链表转为红黑树时, 是先转为了双向链表, 然后根据双向链表 生成的 红黑树, 同时 并没有破坏双向链表的结构, 相当于维护一个红黑树的同时, 它还是一个双向链表
 * 所以可以使用 遍历链表的方式遍历红黑树.
 * 
 * 
 * 红黑树中的元素转移到新数组中, 实际是分为两个下标的, 即两条链表
 * 如果某一链表长度 <= 6, 则从红黑树退化为链表 (节点由 TreeNode 转为 Node 即没有了 left,right等属性, 只剩下 next)
 * 如果某一链表长度 > 6 同时另外一个链表头节点 != null
 * 则根据该链表重新生成红黑树(虽然该链表同时也是红黑树, 但是它已经残缺了, 相当于不是红黑树了, 所以重新生成红黑树)(treeify()就是之前putVal中讲的的链表转红黑树的方法)
 * 如果 另外一个链表头节点 == null, 说明没有残缺, 则将 该链表的头节点直接赋值给新数组 (相当于整棵树都移动到新数组了)
 * 
 * 看懂了 扩容方法resize()中 处理 将链表 转移到新数组中的逻辑, 就可以看懂该方法, 不说一点不差 只能说一模一样
 */
final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
    TreeNode<K,V> b = this;//调用处 ((TreeNode<K,V>)e).split(this, newTab, j, oldCap); 所以 this为红黑树根节点 同时也是 链表头节点
    // Relink into lo and hi lists, preserving order
    TreeNode<K,V> loHead = null, loTail = null;// low 指不需要修改下标的链表
    TreeNode<K,V> hiHead = null, hiTail = null;// high 指需要将下标修改为 原数组下标 + 旧数组长度 的链表
    int lc = 0, hc = 0;//分别用来记录 low链表的长度 与 high链表的长度
    for (TreeNode<K,V> e = b, next; e != null; e = next) {//以链表的形式遍历该红黑树
        next = (TreeNode<K,V>)e.next;//这里先用 next 保存 e.next 节点, 防止修改了 e.next指向的节点后 链表丢失
        e.next = null;
        if ((e.hash & bit) == 0) {//不需要修改下标的节点 low
            //将 loTail 赋值给 e.prev 同时判断 是否为null, 如果第一次进来 loTail 肯定为null嘛, 则头节点的前一个节点指向null
            //如果 (e.prev = loTail) != null 说明最后一个节点指向当前节点的一个, 然后将e赋值给最后一个节点的下一个, 然后将loTail = e;
            //说白了 e.prev = loTail 就是维护了双向链表的 prev指针, 如果这里不赋值给e.prev 那么双向链表实则退化为单向链表
            if ((e.prev = loTail) == null)
                loHead = e;//所以 e 赋值 给头节点
            else//loTail不为null, 则说明链表至少有一个节点
                loTail.next = e;//尾节点的下一个指向 e; 同时 loTail = e; 即将尾节点指向真正的尾节点
            loTail = e;//然后loTail指向 e (loHead == e时说明 e即使头节点也是尾节点 即链表中只有一个节点) (loHead != e 那么就是 loTail = e; 那么将尾节点指向真正的尾节点)
            ++lc;//记录low链表长度
        }
        else {//需要修改下标的节点 high          (看懂了 if, else一模一样的)
            if ((e.prev = hiTail) == null)//将 hiTail 赋值给 e.prev 同时判断 是否为null, 如果第一次进来 hiTail 肯定为null嘛
                hiHead = e;//所以 e 赋值 给头节点
            else//hiTail不为null, 则说明链表至少有一个节点
                hiTail.next = e;//尾节点的下一个指向 e; 同时 hiTail = e; 即将尾节点指向真正的尾节点
            hiTail = e;//尾节点 指向真正的尾节点
            ++hc;//记录 high链表长度
        }
    }

    // 如果 low 链表不为空
    if (loHead != null) {
        if (lc <= UNTREEIFY_THRESHOLD) // 判断 low 链表长度 <= 6
            tab[index] = loHead.untreeify(map);//则将 low链表 从红黑树退化为单链表(节点从 TreeNode 退化为 Node) 并将头节点赋值到新数组中
        else {// low 链表长度 > 6
            tab[index] = loHead;//则将 low链表头节点赋值到新数组中
            if (hiHead != null) // (else is already treeified)
                //如果 hiHead != null 说明 整个红黑树的节点 一部分在 loHead链表 一部分在loHead链表, 所以loHead相当于不是一个红黑树了或者说不完整了, 所以要重新树化
                //如果 hiHead == null 则不用重新树化, 说明整个红黑树的节点 都在loHead链表, 相当于整棵树赋值到了 loHead 所以上面已经将 loHead 赋值给了 tab[index]
                loHead.treeify(tab);
        }
    }
    // 如果 high 链表不为空
    if (hiHead != null) {
        if (hc <= UNTREEIFY_THRESHOLD)
            tab[index + bit] = hiHead.untreeify(map);
        else {
            tab[index + bit] = hiHead;
            if (loHead != null)
                hiHead.treeify(tab);
        }
    }
    
    //至此 将红黑树转移到 新数组中完成
}
```
**1.7 与 1.8扩容数组 区别**

1.7除了超过阈值外, 还需要满足 当前插入key得数组下标出不为null, 才扩容, 否则就算size超过阈值也不扩容 (即插入数组下标处至少有一个元素)
1.8则是 只需要满足 size 超过阈值 则进行扩容

1.7 是先扩容 然后 再插入新节点, 所以是 size >= threshold
1.8 是先插入新节点 然后扩容, 所以是 ++size > threshold
```java
// 这是 1.7 在判断 size >= 阈值得同时 还需要满足 当前插入key得数组下标出不为null, 才扩容, 否则就算size超过阈值也不扩容
// 扩容条件: 满足 size >= 阈值 同时 本次插入的数组下标处 != null 才会触发扩容
if ((size >= threshold) && (null != table[bucketIndex])) {
    resize(2 * table.length);// 调用该方法进行扩容, 扩容大小为 原数组长度的两倍

// 这是 1.8 只需要满足 size > 阈值
if (++size > threshold)
    resize();
```


# ConcurrentHashMap 1.8

**构造方法**
相比于1.7, 1.8的无参构造就简单很多, 一个空的.  那么1.7中的无参构造是调用了 有参构造(数组长度, 加载因子, 隔离级别) 该有参构造中计算出了Segment数组长度 与 HashEntry数组长度
```java
public ConcurrentHashMap() {
}
```

**put()**
提一下, hashmap1.7的put方法没有 boolean onlyIfAbsent 参数, 所有它没有 putIfxxx的方法 意味着它遇到重复key就会替换, 而hashmap1.8, cHashmap1.7 1.8 都是可以 遇到重复key选择 不替换的(即啥也不干)

提一下, hashmap1.7允许插入kv对为null(key为null时有专门的方法插入, key为null,直接放入数组[0]) hashmap1.8是计算hash值时 key==null,则hash值为0, 同样也是 数组[0]  
chashmap1.7不允许插入k或v为null 直接if了 value==null抛异常, 没有判断key==null,但是在key.hashcode()时相当于null.hashcode()所以还是抛出异常  
chashmap1.8不允许插入k或v为null 直接if了 key == null || value == null, 抛出异常
```java
public V put(K key, V value) {
    return putVal(key, value, false); // fasle 表示 遇到重复key 会替换 value
}

final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException(); // 不允许 key 为 null, 或者 value 为 null
    int hash = spread(key.hashCode());//此处 spread()的作用就是 传入一个hash值 返回一个更加散列的 hash值
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        if (tab == null || (n = tab.length) == 0) //如果 数组==null 或 数组长度==0 则 初始化数组 initTable()
            //需要注意的是, 该for循环中的 if, else if, else if, else 只会执行一个
            // 在某个线程 第一次put时 tab 必然为null, 则进入该分支 进行初始化数组
            // 初始化数组之后, 会进行下一次for循环, 然后此时数组已经不为null了 所以不会再进入该分支了, 所以进行其他分支, 将本次put的kv对 插入数组
            tab = initTable();
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            // 根据 (数组长度 - 1) & hash值计算出 该key所在得 数组下标
            // 并使用 tabAt()原子性的安全的取出该 数组下标处得元素, 然后判断是否为null, 如果为null, 说明该处为空, 没有链表或红黑树
            // 则使用 cas 将该 key的节点 插入 数组下标处, 如果成功则break, 失败则进行下一次for循环 (for循环中的 if, else if, else if, else 只会执行一个)
            // 如果 数组下标处还是为null, 则再次使用 cas 将该 key的节点 插入 数组下标处, 然后一直重复 直致成功, 或者
            // 或者 cas 插入失败后, 其他线程在该 数组下标处插入元素成功了, 则进行下一次for循环再次来到这里时 该数组下标处 就不会为null了, 就不会走该分支了
            
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        else if ((fh = f.hash) == MOVED)
            // 首先要知道 f 为 当前需要put的kv对的 数组下标处元素(即链表头节点, 且不为null)
            // 如果 该数组下标处元素的hash值 == MOVED(-1) 则表示当前  table数组正在扩容 (一个线程对table扩容 或者 多个)
            // helpTransfer(tab, f) 就是帮助 对该table进行扩容的线程, 一起扩容
            // 扩容结束后, 就再次循环, 将kv对插入table, 不过此时得table就是扩容后得table了
            
            tab = helpTransfer(tab, f);
        else {
            /**
             * 进入该分支 就说明 数组不为null, 插入key值的 数组下标处不为null, 数组不在扩容. (即该情况为 正常的插入元素流程)
             * synchronized (f) 锁的是 插入key值的 数组下标处元素
             * */
            V oldVal = null;
            synchronized (f) {
                // 加锁之后, 再次使用 tabAt()原子性的安全的取出该 数组下标处得元素, 看看是否还是 == f
                // 如果等于, 说明 f 还是数组下标处元素, 则可以正常的进行 插入元素了
                // 如果 不等于, 说明 数组下标处元素 在本线程 加锁之前, 被其他线程 删除了, 或者 被改为了其他节点
                // 那么本 同步块的内容就不会执行, 然后就释放锁, 进行下一次for循环, 重新获取 数组下标处元素 然后加锁
                // (虽然 不太理解 为什么 数组下标处元素不为 f 了之后, 就不能操作了, 就算链表或红黑树 头节点, 被修改了 不是 f 了,
                //  那么应该不影响 正常的插入吧, 重新获取 链表或红黑树 头节点, 然后操作应该也是可以的吧,
                //  我估计的话, 如果 数组下标处元素不为 f 了之后, 应该就是 synchronize (f) 相当于说 锁错对象了, 可能起不到 锁的效果, 所以重新循环重新加锁)
                if (tabAt(tab, i) == f) {
                    // 判断 fh 是否 >= 0,  fh之前被赋值为 f.hash 并且执行到此处  fh肯定 != -1 (-1表示数组在扩容, -2表示该节点是 红黑树根节点)
                    // 如果 >=0 则进行 在链表中插入元素
                    // 如果 < 0 同时 节点类型为 TreeBin, 则进行 在红黑树中插入元素
                    if (fh >= 0) {
                        binCount = 1;//记录链表长度, 方便转红黑树 (binCount从1开始, 8表示链表长度为8, 8个节点)
                        for (Node<K,V> e = f;; ++binCount) { //遍历链表, 同时每遍历一个节点 ++binCount (记录链表长度)
                            K ek;
                            if (e.hash == hash && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {// 如果key值重复
                                oldVal = e.val;//保留 旧的 value值
                                if (!onlyIfAbsent)// false则替换value值, true则不做任何操作
                                    e.val = value;
                                break;//key值重复后, 替换value值, 然后break; 结束循环, 释放锁, 返回重复值, 方法结束, 完成插入
                                // 注意 break的是 synchronized里面的for循环, 而不是是外层的for循环
                            }
                            Node<K,V> pred = e;//记录 当前遍历节点, 则下次遍历时 作为 e 的 前一个节点
                            if ((e = e.next) == null) {//遍历到链表尾部 (遍历至链表最后一个节点)
                                pred.next = new Node<K,V>(hash, key, value, null);//尾插法
                                break;//结束循环, 释放锁, return null, 方法结束, 完成插入
                                // 注意 break的是 synchronized里面的for循环, 而不是是外层的for循环
                            }
                        }
                    }
                    else if (f instanceof TreeBin) {// f < 0 同时 节点类型为 TreeBin, 则进行 在红黑树中插入元素
                        Node<K,V> p;
                        binCount = 2;// binCount == 2; 表示进入了该分支 插入节点至红黑树, 这样在外面那个 if (binCount != 0) 就会进入该if, 而且不会触发转红黑树
                        // f为 链表头节点 也 表示红黑树头节点 (在此处表示 红黑树头节点)
                        // 调用 红黑树头节点.putTreeVal进行插入节点至 红黑树, 没有重复则返回null, 如有重复 则返回重复节点, 并判断是否需要替换value值
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key, value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)// 链表长度为 8, 正在插入第 9 个元素时, binCount = 8, 8 >= 8 触发链表转红黑树
                    treeifyBin(tab, i);//转红黑树
                if (oldVal != null)// oldVal != null 则说明有重复节点, 返回旧的value值
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);
    return null;
}


/**
 * 该方法 就是 在 putVal 中处理 将 链表转为红黑树的方法, 传入数组 与 需要转红黑树的数组元素下标, 然后将该下标出的链表 转为 红黑树
 * 做法很简单, 和 hashmap1.8 中的差不多, 都是将 链表转为双向链表 然后再调用方法将 双向链表 转为 红黑树
 * 与hashmap1.8的区别在与 hashmap1.8红黑树所有节点都是 TreeNode, 而chashmap1.8红黑树根节点是 TreeBin, 以及是否有 synchronized 的区别
 * 
 * 本方法加了synchronized(数组下标处元素)即, 即同一时刻只能有一个线程能 将该数组下标处元素 从链表转为红黑树
 * (本方法锁的是 table[数组长度 - 1 & key.hash] 即锁住的这个 数组下标处, 也就是链表头节点, 当某个线程想要操作某个链表 或 链表中的元素时,
 * 只能通过 链表头节点 遍历来完成, 所以锁住了链表头节点 就相当于锁住了 整个链表,  每次只是锁该数组下标处, 不影响其他线程操作其他 数组下标处)
 */
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    if (tab != null) {// 数组不为null才会操作转红黑树
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY) // 和hashmap1.8一样 如果链表长度 > 8了, 但是数组长度 < 64 则实际是扩容数组, 不会转红黑树
            tryPresize(n << 1);//扩容数组, 长度翻倍
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {//tabAt()原子性的安全的取出该 数组下标处得元素
            // 数组下标处元素不能为空 同时 hash值要 >= 0, 不满足则说明 在putVal方法中释放锁后, 到这里的期间 被其他线程操作了 或被删除了(==null),或正在扩容(b.hash = -1),或已经是红黑树头节点了(b.hash = -2)
            // 如果不满足, 则不进行操作, 直接结束方法            

            // 对数组下标处元素加锁, 之前在putVal方法中 的锁已经释放了, 之所以 一会锁一会不锁的 肯定是为了效率, 这也是同步块比同步方法的优势, 锁的范围小
            synchronized (b) {
                // 加锁之后, 再次使用 tabAt()原子性的安全的取出该 数组下标处得元素, 看看是否还是 == b
                // 如果等于, 说明 b 还是数组下标处元素, 则可以正常的进行 插入元素了
                // 如果 不等于, 说明 数组下标处元素 在本线程 加锁之前, 被其他线程 删除了, 或者 被改为了其他节点
                if (tabAt(tab, index) == b) {
                    
                    // 这个操作就是, 将单链表转为 双向链表(从Node转为TreeNode)
                    // 该操作 在hashmap1.8 中的treeifyBin()方法 中详细讲解了的  (本md文档 1119行)
                    TreeNode<K,V> hd = null, tl = null;
                    for (Node<K,V> e = b; e != null; e = e.next) { //遍历原始单向链表
                        TreeNode<K,V> p = new TreeNode<K,V>(e.hash, e.key, e.val, null, null);// 根据 当前遍历单向链表节点Node, new出一个 双向链表节点 TreeNode
                        // 第一次遍历 t1 肯定为null, 则单向链表的头节点 赋值给 hd, 同时 hd的前一个为 null
                        // 不是第一次遍历 tl 就肯定不为null, 且 tl 表示上一次循环的 "当前节点" (同时也表示 最后一个节点)
                        // 即 tl 表示 当前节点的 前一个节点, 然后将tl赋值给 当前节点的前一个节点 p.prev = tl (即 待插入节点的prev 指向 它该指向的元素)
                        // (这里需要注意的是 即使条件不满足  条件也是会执行的, 即无论如何 p.prev = tl 都是会执行的)
                        if ((p.prev = tl) == null)
                            hd = p;// hd就表示 双向链表头节点
                        else
                            tl.next = p; // 当前节点的前一个节点的下一个节点指向 当前节点 (即 链表尾部节点.next 指向 带插入元素)
                                         // p.prev = tl; tl.next = p; 则两步完成了将一个 元素插入 双向链表尾部的一个操作
                        tl = p;//保存当前节点, 作为下一个当前节点的前一个节点
                    }
                    
                    // 指向到这里之后, 就是 已经将 单向链表转为 双向链表了
                    // new TreeBin<K,V>(hd) 则是将 双线链表头节点 传入, 然后返回一棵红黑树头节点 (等下单独讲 new TreeBin)
                    // 通过 setTabAt, 原子性的安全的将 红黑树root节点 放入 tab数组的 index下标处, 完成 链表转红黑树操作 (就是通过Unsafe完成的, 没什么好讲)
                    setTabAt(tab, index, new TreeBin<K,V>(hd));//真正的红黑树节点不是 new TreeBin() 而是new出的该对象中的 root变量 表示真正的红黑树根节点
                }
            }
        }
    }
}


/**
 * 该方法 就是将一个 双向链表 转为 红黑树, 然后返回一个红黑树root节点 (真正的红黑树根节点 不是该构造器生成的 TreeBin 对象, 而是该对象内部的 TreeNode<K,V> root;)
 * 其实本方法就是一个 将普通的节点插入 二查树的方法 (将链表中每每一个元素 都依次插入 二叉树)
 * 只是每插入一个节点就会调用 balanceInsertion(r, x); 来保持红黑树平衡
 * balanceInsertion(r, x); 在此处就不讲了, 和 hashmap1.8 中的 balanceInsertion(r, x); 一模一样
 * 需要了解的可以去 1160行, 881行 都有讲解, 具体在881行.
 * 
 * 至于锁的话, 本方法是在 treeifyBin() 方法中调用的, 该方法在调用本方法之前 已经加了 synchronized(链表头节点),
 * 而 调用本方法的几个地方, 在调用本方法前 也都是需要锁的, 也就是说虽然本方法 没有加synchronized 可以被多个线程同时访问,
 * 但是 调用本方法的 几个方法都是加了锁的, 相当于想调用本方法 就得先调用 调用本方法的那两三个方法, 而那几个方法都是加了锁的, 同一时刻只能访问一个线程,
 * 所以间接的 本方法 也是同一时刻只能访问一个. 相当于加了synchronized
 * 
 */
TreeBin(TreeNode<K,V> b) {
    super(TREEBIN, null, null, null);//调用父类构造, TREEBIN = -2, 表示该节点为 红黑树根节点
    this.first = b;//链表头节点
    TreeNode<K,V> r = null;// r表示 红黑树 root节点
    for (TreeNode<K,V> x = b, next; x != null; x = next) { // 遍历链表
        next = (TreeNode<K,V>)x.next;//保存 当前节点的下一个节点, 以防止丢失
        x.left = x.right = null;
        
        // 如果根节点为 null, 则 当前待插入节点 赋值给红黑树根节点, 将根节点的parent置为null, 颜色置为黑色
        if (r == null) {
            x.parent = null;
            x.red = false;
            r = x;
        }
        else {// 说明 根节点不为null
            K k = x.key;// 当前遍历链表节点, 待插入节点 的key
            int h = x.hash;// 当前遍历链表节点, 待插入节点 的hash值
            Class<?> kc = null;
            for (TreeNode<K,V> p = r;;) { //遍历红黑树, 在该红黑树中找到 待插入节点 的位置
                int dir, ph;
                K pk = p.key;
                if ((ph = p.hash) > h) // 待插入节点 hash值 < 红黑树遍历节点 则 dir = -1 表示 待节点应该插入在当前遍历红黑树节点的 左边 (同时为 ph赋值)
                    dir = -1;
                else if (ph < h) // 待插入节点 hash值 > 红黑树遍历节点 则 dir = 1 表示 待节点应该插入在当前遍历红黑树节点的 右边
                    dir = 1;
                else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0)
                    // 这种情况就是, hash值重复了的情况, 也就是说 不知道 待插入节点 应该 是在红黑树当前元素的 左边还是右边了
                    dir = tieBreakOrder(k, pk);//则通过该方法 计算出 左边还是右边 即 -1 或者 1
                
                // 到这里 dir 就已经确定下来了, 即 已经确定了 待插入节点 是在 当前节点的左边还是右边
                // 使用 xp = p; 来保存红黑树当前遍历节点, 然后 p = p.left 或 p = p.right; 这时 xp 就表示为 p的父节点
                // (p = (dir <= 0) ? p.left : p.right) == null) 根据dir确定左右, 然后判断是否为null
                // != null 说明 此时的 p 还只是叶节点, 还没有到叶子节点, 则继续循环 往下一层找 直至叶子节点
                // == null 说明 此时 p 以及是叶子节点 的 left 或 right, p 就相当于 是当前待插入节点应该插入的位置, xp就相当于叶子节点也就是当前插入节点的父节点
                // 然后 调用 balanceInsertion(r, x) 来对 红黑树进行 保持平衡的操作
                // 之后break; 完成一次 从链表中插入一个节点 至 红黑树中, break跳出红黑树的遍历, 再次执行外层的链表遍历, 再次取出链表下一个值, 然后插入红黑树
                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {//满足条件 找到了待插入节点的位置 即 p 就是待插入节点的位置
                    x.parent = xp;//待插入节点的父节点为 xp
                    if (dir <= 0) // 分清左右
                        xp.left = x;// xp的左子节点 原为 p, 也就是 null, 现在为 待插入节点
                    else
                        xp.right = x;// xp的右子节点 原为 p, 也就是 null, 现在为 待插入节点
                    r = balanceInsertion(r, x);//保持红黑树平衡
                    break;// 完成一次 从链表中插入一个节点 至 红黑树中, break跳出红黑树的遍历, 再次执行外层的链表遍历, 再次取出链表下一个值, 然后插入红黑树
                }
            }
        }
    }
    this.root = r; // 经过红黑树的各种平衡, 根节点 r 各种变动, 到此处 已经将链表节点全部插入到红黑树中了, root节点确定了, 则赋值给 this.root
    assert checkInvariants(root);// 表示判断经过上面一系列对链表的操作后, 红黑树是否还符合红黑树的条件
                                 // 有点向简化半版的 throw 即,assert不满足条件会抛出异常
}


/**
 * 该方法 就是 在 putVal 中处理 将 节点插入红黑树的方法, 如果本次插入的key重复, 则返回那个重复的节点, 在putVal中判断 是替换value值还是 啥也不干, 如果本次插入key值没有重复, 则返回null
 * 该方法 和 将双向链表转为红黑树 的方法差不多 (因为双向链表转红黑树是 遍历双向链表 然后将链表中的元素 一个一个插入红黑树)
 * 该方法与 hashmap1.8中的 putTreeVal 也是差不多的, 在本文档 1300行 左右, 看不懂本方法 可以去看一下
 * 
 * 我感觉该putTreeVal比hashmap1.8中的putTreeVal是简单一些的
 * hashmap1.8中的putTreeVal 在维护双向链表时 是将待插入节点 插入 待插入节点的父节点.next; (相当于从红黑树中找到父节点, 然后这个父节点同时也是链表元素 以这个节点的下一个节点为插入地方, 将待插入节点插入)
 * 而此处的 chashmap1.8中的putTreeVal 维护双向链表是通过 头插入 直接插入 (不管红黑树是怎么插入的, 反正链表就使用头插入 直接插入在头部 多简单)
 *
 */
final TreeNode<K,V> putTreeVal(int h, K k, V v) {
    Class<?> kc = null;
    boolean searched = false;
    for (TreeNode<K,V> p = root;;) {//遍历红黑树, 以找到待插入节点的位置 (要找到叶子节点才会找到 即 节点.left == null || 节点.right == null 时)
        int dir, ph; K pk;
        if (p == null) { //如果
            first = root = new TreeNode<K,V>(h, k, v, null, null);
            break;
        }
        else if ((ph = p.hash) > h) // 往左
            dir = -1;
        else if (ph < h) // 往右
            dir = 1;
        else if ((pk = p.key) == k || (pk != null && k.equals(pk))) // 重复key, 直接返回该节点
            return p;
        else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0) {
            // 当 待插入节点的hash值 与 红黑树中以有节点的hash值重复了, 则是这个 else if 处理的情况

            // 这个 if 还真是看不太懂啊, 当本次putTreeVal的节点hash值重复, 时 serched = false; !serched = true, 肯定会进入该if的
            // 之后就是判断 如果红黑树当前遍历hash值重复的元素 的左子节点不为空, 则以左子节点为根节点 递归查找 本次putTreeVal的key是否重复 (find方法的内容)
            // 而查找方式 也是 比较hash值 往左往右查询, 找到了key值重复的节点 则一层一层返回 找不到 则返回null, 由底部的 dir = tieBreakOrder(k, pk); 计算出到底往左还是往右    
            // 感觉完全是多此一举, 完全不知道该 if (!searched)存在的意义, 感觉就算不要 也是可以正常运行的...
            if (!searched) {
                TreeNode<K,V> q, ch;
                searched = true;
                if (((ch = p.left) != null && (q = ch.findTreeNode(h, k, kc)) != null) || ((ch = p.right) != null && (q = ch.findTreeNode(h, k, kc)) != null))
                    return q;
            }
            
            dir = tieBreakOrder(k, pk);//计算出hash值重复了 应该是 往左 还是 往右
        }

        TreeNode<K,V> xp = p;// 保存 当前遍历节点, 当 p = p.left 或 p = right 后, xp 就表示 p的父节点, p==null时 p表示待插入节点, 则 xp 为待插入节点的父节点
        if ((p = (dir <= 0) ? p.left : p.right) == null) {
            TreeNode<K,V> x, f = first;//用 f 保存 链表头节点

            // new 操作等价于 x.next = f; first = x;  x.parent = xp;
            // x.next = f; first = x; 相当于是 使用头插法 完成链表的插入(只是完成了 待插入节点的下一个 指向 头节点, 待插入节点为头节点,  还有待插入节点的prev, 与 头节点的prev没处理)
            // x.parent = xp; 相当于是 待插入节点的父节点 指向 父节点 (之后就 父节点的左 或 右子节点 指向 待插入节点 完成一个红黑树插入)
            first = x = new TreeNode<K,V>(h, k, v, f, xp);
            if (f != null) // 如果 f != null, 则f.prev = x; 一般情况下, 旧的头节点肯定不为null
                f.prev = x;// f之前是头节点 f.prev 肯定是null, 现在f不是头节点了(已经是头节点的下一个了), 新的头节点是x, 所以需要 f的上一个指向 x;
            if (dir <= 0)
                xp.left = x;//红黑树的维护, 父节点的左子节点 或 右子节点 指向 待插入节点
            else
                xp.right = x;//红黑树的维护, 父节点的左子节点 或 右子节点 指向 待插入节点
            if (!xp.red) // 这里是 如果待插入节点的父节点是黑色, 则待插入节点为红色 (一般情况下 在红黑树中插入的节点都是红色, 后面的修复平衡会改变这些颜色使其平衡)
                x.red = true;
            else {
                // 这里是 对根节点 加锁
                // 保持红黑树平衡 (期间 root可能会发生改变) 返回平衡后的红黑树根节点
                // 解锁
        
                //这是 我是这样想的, 虽然该方法没有加 synchronized 但是 调用本方法的 put 方法是对 链表头节点同时也是红黑树根节点 加了锁的
                //虽然本线程不是同步方法, 即可以由多个线程同时调用, 但是调用本方法的几个方法都是加了锁的, 即相当于同时只会有一个线程调用本方法的父方法然后再调用本方法
                //间接的相当于本方法 同时也只会有一个线程操作, 那么为什么这里又要加锁呢
                // lockRoot();发现是锁的this, 可能是觉得 root 在保持红黑树平衡中 root节点经常变动 锁root锁不住
                // 我知道了, 虽然 间接的相当于本方法 同时也只会有一个线程操作, 但是那是针对上面那部分 即 锁链表头节点锁的住
                // 而 balanceInsertion 会变动root节点的值, 导致锁不住 (草, 我也不知道为什么了....)
                lockRoot();
                try {
                    root = balanceInsertion(root, x);
                } finally {
                    unlockRoot();
                }
            }
            break; // 跳出循环
        }
    }
    assert checkInvariants(root);
    return null;
}
```

<br/>

initTable(); // 初始化数组方法, 在 put -> putVal -> initTable() 可以看到是 在putVal中判断数组为空才进行初始化 调用 initTable()方法
```java
/**
 * 初始化数组
 * 在putVal方法中判断 table 数组为空时 会 进行调用本方法 对数组进行初始化
 * if (tab == null || (n = tab.length) == 0)
 *      tab = initTable();
 * 
 * sizeCtl 这个变量比较重要, 这里需要知道的是 无参构造器是个空构造 也是就是说 sizeCtl 默认为 0
 * 方法中的局部变量 sc == sizeCtl
 */
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) { //判断 数组是否为空, 为空则一直循环
        if ((sc = sizeCtl) < 0) // 将 sizeCtl 赋值给 sc, 然后判断 是否 < 0, 默认情况下 sizeCtl == 0,  0 !< 0 所以不会执行
            Thread.yield(); // lost initialization race; just spin
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            // cas的方法给 sc - 1, 变相的加锁, 总所周知 cas 为乐观锁, 即 多个线程同时对 sc - 1, 实际只有一个线程会成功
            // 打个比喻: A线程 与 B线程 同时调用 initTAble();本方法对 数组进行初始化
            // A线程 sc - 1; 成功, B线程 sc - 1就失败
            // 之后 A线程就再次判断, table是否为空(安全), 然后 sc > 0 则 数组长度n 就为 sc
            // sc !> 0, 则 数组长度n 为 默认长度16, 之后就是 new出 长度为n的数组了 new Node<?,?>[n];
            // 将 new好的数组 nt; 赋值给 table, tab;
            // 然后 sc = n - (n >>> 2); 先说结论 sc 此时相当于 阈值, 且最后 sc 也会赋值给 sizeCtl (sc就是sizeCtl, sizeCtl就是sc)
            // 假设 数组长度 n = 16; 那么 n >>> 2 就是 4, 右移一位就是除2 相当于右移一位就是 n就变成了 原n的二分之一, 再右移一位 就是 原n的二分之一的二分之一
            // 相当于 右移2位 结果就算 原n的四分之一, 四分之一也相当于默认的加载因子0.75剩下的0.25
            // 所以最后 sc = n - (n >>> 2); 相当于 n - n的四分之一, 相当于结果为 n的四分之三
            // 相当于 在数组长度16的情况下 sc = 12, 相当于 在数组长度16的情况下 16 * 0.75 = 12, sc = 12
            // 所以说 在此处 sc 相当于阈值, sc最后是赋值给了 sizeCtrl
        
            // 此时 A线程初始化数组完成, 在此期间 B线程 sc = -1, tab == null
            // 所以一直会进入 if ((sc = sizeCtl) < 0), 然后一直执行 yield() 让出cpu调度, 回到就绪状态, 等待cpu调度
            // 直到 A线程初始化数组完成, sc != -1了, 主要是tab != null了, 然后 B线程再次会cpu调度时 while循环条件就不满足 tab == null了
            // 就会跳出循环, 然后返回 tab (该tab就是其他线程 扩容成功后的数组)
        
            try {
                if ((tab = table) == null || tab.length == 0) {
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    sc = n - (n >>> 2);
                }
            } finally {
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```

<br/>

addCount();
```java
/**
 * 首先说一下这个方法的调用背景, 该方法是在 putVal中 被调用的 addCount(1L, binCount); 目的应该是 相当于 size + 1
 * (实际是chashmap没有size属性, binCount在插入链表时 指链表长度, 在插入红黑树时值为2, 其他情况binCount都是默认值 0)
 * 该方法实现两个功能: 1. 相当于 size + 1,   2. 扩容;  最外层的两个大if, 第一个是 size + 1; 第二个是 扩容
 * 
 * 说一下该方法的思路, 首先如果是单线程的情况下 使用size 来记录链表元素显然是没有问题的, 但是在多线程环境下 size 就不太好使了,
 * 如果多个线程同时对 size + 1, 尽管使用了 CAS乐观锁, 但是实际还是一次只能成功一个线程, 其他线程不断重试, 效率还是比较低的,
 * 所有就出现了 chashmap中的 baseCount 与 counterCells数组, 当多个线程 对 size + 1, 实际也就是对 baseCount + 1,
 * 但是成功的线程, 同一时间只有一个, 也就是 baseCount = baseCount + 1 , 那么其他失败的线程, 就会获取 一个随机数 然后 & counterCells.length - 1,
 * 就像根据hash值计算出数组下标一样, 目的就是得到的 下标一定会在 数组合法下标内, 然后 这个 counterCell 对象中有一个属性 value,
 * 那么这些失败的线程, 就会随机找到一个下标处 使用CAS将其 value + 1 (就算多个线程同时随机到了一个下标出, 那么也是分散了 baseCount 的压力),
 * 假设 counterCells数组长度为 4, 那么就是给 baseCount 分走了 五分之四的压力,
 * 最后计算 chashmap长度时, 实际上是 baseCount + counterCells数组中的所有counterCell对象的value值 = 真正的长度
 * (我估计等系统空闲时, 应该会有地方将 counterCells 中的值, 一次性使用 CAS 加到 baseCount 上)
 * (实际上 上面提到的失败的线程, 在代码中是 判断 counterCells 不为null, 则 直接走失败的逻辑, 如果counterCells为null, 才会去 baseCount + 1 判断成功与否, 然后执行上面提到的逻辑)
 * 
 * 
 * 说一下该方法中扩容的思路, 大概就是 线程A 扩容时会设置一个 步长假设 = 2, 老数组长度假设为4, 那么就会new出一个新数组长度为8,
 * 线程A根据步长计算出 它应该转移的数组部分(此处是从数组右边开始转移的, 即数组尾部开始转移), 假设转移的是数组尾部下标为 [2, 3] 转移到新数组后
 * 再转移 [0, 1] 全部转移到新数组后, 再次判断 新数组是否需要扩容
 * 所以需要再次判断 新数组是否需要扩容, 所以这也是代码中判断是否需要扩容的地方不是一个if, 而是while
 * 当 线程A 与 线程B 同时扩容时, 假设步长为2, 线程A 与 B 根据步长计算出各种 需要处理的地方, 然后将数组从老数组转移到新数组
 * 
 *
 */
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    
    // counterCells != null 则 || 后面的内容都不用判断了, 直接 给 counterCells数组中的某个 counterCell 的 value + 1
    // counterCells == null, 则继续判断 使用CAS给baseCount + 1, 加成功了 则不会走 给 counterCell 的value + 1的逻辑, 只会加失败了才会走
    if ((as = counterCells) != null || !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
        CounterCell a; long v; int m;
        boolean uncontended = true;
        
        // 先判断 counterCells == null, 如果 == null, 则直接进入if 调用 fullAddCount 来对 counterCells进行初始化 并且给其中的某一个元素 counterCell 的 value + 1
        // 继续判断 (m = as.length - 1) < 0, 如果满足条件 则 as数组为空, 调用 fullAddCount 来对 counterCells进行初始化 并且给其中的某一个元素 counterCell 的 value + 1
        // 继续判断 (a = as[ThreadLocalRandom.getProbe() & m]) == null, 如果 == null, 说明 counterCells虽然不为空, 但是 counterCells[下标] 处的 counterCell 为空, 所有还是调用 fullAddCount
        // 如果前面的都不满足, 则说明 counterCells不为空, counterCell不为空, 则用cas对 counterCell对象中的 value + 1; 成功了则 不会执行 fullAddCount, 否则只能调用 fullAddCount来对 counterCell对象中的 value + 1;
        // 注意  || 是短路的, 即前面的条件有一个满足, 则后面的条件不会判断  直接进入if
        if (as == null || (m = as.length - 1) < 0 || (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
        !(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
            // 也就是说 当 counterCells数组为null 或者 当前线程hash计算出来的下标出为null 或者 对当前线程hash计算出的下标处counterCell对象value + 1 失败
            // 就会调用该方法 对 counterCells数组初始化 或 对当前线程hash计算出来的下标处赋值一个counterCell
            // 或 对当前线程hash计算出来的下标处counterCell对象value + 1 或 扩容 counterCells数组
            fullAddCount(x, uncontended);
            return;
        }
        if (check <= 1)
            return;
        s = sumCount();//计算出 chashmap元素个数 赋值给 s
    }
    
    // 该if处理的就是 table扩容的情况, 即hash表 数组扩容
    // check 是通过参数传递的 在 putVal中该方法被调用 传入的check值有两种情况
    // 1. 插入链表时 check参数传递的值为 链表长度. 2.插入红黑树时 check参数的值为2,  3. 没有具体插入节点 那么 check参数的值 是默认值 0
    // (也就是说 putVal 方法执行完后 都会走到这里 且都满足 check >= 0, 都会判断是否需要扩容)
    //
    // check == -1 的情况 这里就不满足 check >= 0 就不需要判断 是否需要扩容
    // check == -1 的情况 就是 remove方法调用 本方法addCount();时传入的check == -1, 就不需要判断扩容
    if (check >= 0) {
        Node<K,V>[] tab, nt; int n, sc;
        // 当 数组长度 >= 阈值 s >= sizeCtl (s 表示数组长度 在上面由 sumCount()计算得来) (sizeCtl在初始化hash数组时被赋初始值 为 阈值)
        // 同时 数组不为空, 数组长度 < 数组最大值, 才会对数组进行扩容
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null && (n = tab.length) < MAXIMUM_CAPACITY) {
            int rs = resizeStamp(n);//该方法返回一个数字, 该数组 左移 RESIZE_STAMP_SHIFT 那么得到的一定是一个负数 而且还是一个很大的负数
            if (sc < 0) {// sc 默认为阈值, 所以 sc 一开始肯定是 > 0的 即会走下面的俄 else if
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS || (nt = nextTable) == null || transferIndex <= 0)
                    break;
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            
            // 该 else if 会将 sc 赋值为 (rs << RESIZE_STAMP_SHIFT) + 2 得到的值一定是一个负数
            // 如果 CAS成功, 则 sc = 负数, 且调用 transfer(tab, null)
            // transfer(tab, null) 得作用就是  生成新数组 (transfer方法中判断 如果参数二 为 null, 则是生成扩容后得新数组)
            // 因为多个线程进入该方法时 只有一个线程会CAS成功, 即由该线程 生成扩容后得新数组,
            // 另一个失败得线程, 由于 sc 已经被CAS改为 负数了, 则会执行上面那个 if(sc < 0) 了 在该if中转移老数组元素到新数组
            else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
            s = sumCount();
        }
    }
}
```

<br/>

fullAddCount(x, uncontended);
```java
/**
 * 先说一下该方法的调用背景, 该方法是在 addCount()方法中 被调用的
 * 目的是为了: 初始化counterCells数组 或 counterCell对象 或 给 counterCell对象的value值 + 1
 * 或者就是 扩容 counterCells数组
 * 
 * 该方法 的实现逻辑 是与 LongAdder 类的实现逻辑是一样的.
 *
 */
private final void fullAddCount(long x, boolean wasUncontended) {
    int h;// 表示一个当前线程的随机值 或者说 hash值,  根据该值 & length - 1, 得到数组下标
    
    // ThreadLocalRandom.getProbe() 每个线程, 这个方法获取的 随机值都是一样的, 无论调用几次
    // 如果 ThreadLocalRandom.getProbe() 获取的值 == 0
    // 则会 ThreadLocalRandom.localInit(); 估计该方法执行后, 会让 h = ThreadLocalRandom.getProbe(); 获取到的随机值 和 之前不一样, (然后将 值赋值给 h)
    // wasUncontended = true; 意思是, 已经改变一次 随机值了(或者说 已经改变了一次 该线程的hash值了)
    // 因为 后面也有独立的判断 wasUncontended == fasle 的话 会改变一次 hash 值 
    if ((h = ThreadLocalRandom.getProbe()) == 0) {
        ThreadLocalRandom.localInit();      // force initialization
        h = ThreadLocalRandom.getProbe();
        wasUncontended = true;
    }
    boolean collide = false; // True if last slot nonempty
    for (;;) {
        CounterCell[] as; CounterCell a; int n; long v;
        
        // 判断情况是处理 counterCells 数组, 不为空的情况
        if ((as = counterCells) != null && (n = as.length) > 0) {
            
            // 处理的是 该线程随机数 h, 在CounterCells数组中 取值 时 遇到 CounterCell元素为null的情况
            // 换一句话就是 该 if 是 初始化 CounterCell对象的
            if ((a = as[(n - 1) & h]) == null) {
                if (cellsBusy == 0) {            // Try to attach new Cell
                    CounterCell r = new CounterCell(x); // Optimistic create
                    if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                        boolean created = false;
                        try {               // Recheck under lock
                            CounterCell[] rs; int m, j;
                            if ((rs = counterCells) != null && (m = rs.length) > 0 && rs[j = (m - 1) & h] == null) {
                                rs[j] = r;
                                created = true;
                            }
                        } finally {
                            cellsBusy = 0;
                        }
                        if (created)
                            break;
                        continue;           // Slot is now non-empty
                    }
                }
                collide = false;
            }
            // 当 counterCells 不为空时 同时 本线程取值下标处 不为null, 则会继续判断 wasUncontended == false, 则将 wasUncontended 置为 true
            // 然后 执行底部的 h = ThreadLocalRandom.advanceProbe(h); 再次获取一个 随机值 赋值给 h, 然后再次取下标 if ((a = as[(n - 1) & h]) == null)
            // 如果还是 != null 则还是会走到此处 else if, 但是此时 wasUncontended为true不满足条件, 即会执行下一个 else if 即将此时元素value + 1
            // 需要注意的是 else if 只会执行一个, 执行了一个其他的就不执行了
            else if (!wasUncontended)       // CAS already known to fail
                wasUncontended = true;      // Continue after rehash
            
            // 该情况就是 counterCells 不为空时 同时 本线程取值下标处 不为null, 然后 wasUncontended == true, 即 h 的值已经换过一次的情况下
            // 即将 本线程对应下标处的counterCell对象的 value + 1, 成功了即 break; 相当于return了, 失败则继续循环 
            else if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))
                break;
            
            // 单看这两个 else if 很难看懂在干嘛, 要结合 else if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) 来看
            // 这两个 else if 都是在为 它做判断, 因为只有 这两个 else if不执行, 才会判断到 最后那个 else if
            // 那么相当于就是是 counterCells数组 不是无限制扩容的, 是由 这两个 else if 控制的
            //
            // else if (counterCells != as || n >= NCPU), counterCells != as 说明数组被其他线程扩容了, 则本线程本次循环不会进行扩容
            // n >= NCPU, n表示counterCells数组的长度, NCPU表示本机CPU核心数, 当 n >= NCPU 时, 是不可能执行 counterCells扩容的了
            // 因为 每次都是将 collide = false; 而这些 else if只会执行一个, 所以不会执行扩容了
            //
            // else if (!collide) 由于collide默认为false(扩容完也是false)
            // 而 !collide 为 true, 也就是说 当 collide == false 时 是会执行该if 将 collide = true;
            // 而 collide == true; 之后才会 执行到扩容的判断, 也就是说想要执行扩容的判断 最少需要循环两次
            // 所以 else if (!collide) 的作用, 控制扩容的次数
            // 所以 else if (counterCells != as || n >= NCPU) 的作用, 控制 counterCells数组的最大长度
            else if (counterCells != as || n >= NCPU)
                collide = false;            // At max size or stale
            else if (!collide)
                collide = true;
            
            // 什么时候 counterCells数组 会进行扩容, 也就是说 该情况执行
            // 首先 前面的if都不满足, 才会执行该 else if
            // 即 a = as[(n - 1) & h]) != null, 然后该 as[(n - 1) & h]) + 1 失败, counterCells == as(即counterCells数组没有发生改变), collide == true
            // 才会将 cellsBusy + 1, 如果成功了 则对 counterCells数组 进行扩容,
            // 如果 cellsBusy + 1 失败, 则换一个hash值 即 h = ThreadLocalRandom.advanceProbe(h); 然后重复上面操作
            // 直至扩容成功, 或者 a = as[(n - 1) & h]) == null, 则对该处counterCell初始化, 或a = as[(n - 1) & h]) != null 则对counterCell + 1 成功则, 结束
            // 总结: 当counterCells数组不为空, counterCell[(n - 1) & h]不为空 且 + 1一直失败(失败2次), 则会尝试进行扩容(争抢 counterCells数组的使用权 即 CELLSBUSY改为1)
            else if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {//相当于就扩容 counterCells数组的情况
                // 如果此时没有线程操作 即 cellsBusy == 0, 则本线程将 cellsBusy改为 1, 如果成功了 则进入该if
                try {
                    // 如果 counterCells 数组没有发生改变, 即 counterCells == as 然后将 countercell数组扩容 大小翻倍 [n << 1]
                    // 然后遍历 没有扩容之前的counterCells数组 即 as, 然后将 as数组的内容赋到 扩容后的counterCells数组中 即 rs
                    // 如果 counterCells 数组发生改变了, 即 counterCells != as, 说明在 本线程将 cellsBusy 改为 1 之前,
                    // counterCells 数组已经被 其他线程扩容了, 即本线程该if不执行, 然后continue;跳出本次循环, 再次将 as = counterCells, 然后处理新的 counterCells数组
                    if (counterCells == as) {// Expand table unless stale
                        CounterCell[] rs = new CounterCell[n << 1];
                        for (int i = 0; i < n; ++i)// 遍历老counterCells数组, 然后将值赋值到 新counterCells数组中
                            rs[i] = as[i];
                        counterCells = rs;//扩容完成后, 将 扩容后的新数组rs; 赋值给 counterCells 完成扩容
                    }
                } finally {
                    cellsBusy = 0;//处理完成后, 将counterCells数组改为没有线程使用
                }
                collide = false; // 每次扩容后, 会都将 collide 置为 false;
                continue;                   // Retry with expanded table   (扩容成功与否, 都会continue; 下次重新循环处理 counterCells数组)
            }
            h = ThreadLocalRandom.advanceProbe(h);
        }
        
        // 该情况是处理 数组 == 空的情况 (as = counterCells) 因为上文已经将 数组赋值给了 as 且为空, 才会走到该if 所以 as == null
        // 同时还需要满足, cellBusy == 0, 这个条件表示 该数组此时没有线程操作 (如果有线程操作 会将 cellBusy 置为 1)
        // 同时还需要满足, U.compareAndSwapInt(this, CELLSBUSY, 0, 1) 将 sellsbusy 从 0 改为 1, 如果成功才执行该if, 否则不会执行
        else if (cellsBusy == 0 && counterCells == as && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
            boolean init = false;
            try {                           // Initialize table
                // 再次判断 counterCells 是否等于 null, 如果此时不等于了, 说明有线程在本线程 修改 cellsbuys 值成功之前将 counterCells 已经进行初始化了
                //  new 出CounterCell[2] 数组, 长度为 2 (初始化 counterCells 数组)
                // 然后根据本线程的hash值, 计算出数组下标, 然后将初始化的CounterCell对象放进去, 且 CounterCell内部的value值为 x, 也就是为 1 (相当于 value + 1)
                // 将 rs 赋值给 counterCells 完成 counterCells的初始化, init = true; 表示直接break, 相当于该方法结束
                // 因为该方法的目的已经达到了 初始化counterCells数组 或 counterCell对象 或 给 counterCell对象的value值 + 1
                if (counterCells == as) {
                    CounterCell[] rs = new CounterCell[2];
                    rs[h & 1] = new CounterCell(x);
                    counterCells = rs;
                    init = true;
                }
            } finally {
                cellsBusy = 0;//操作完之后, cellsBusy = 0, 表示没有线程对 counterCells数组进行操作了
            }
            if (init)
                break;
        }
        
        // 这种情况就是 直接对 baseCount + 1, 如果成功就break, 否则一直走for循环, 然后执行各种情况
        // 假设有两个线程同时调用本方法, 那么同时只能有一个线程执行 上面那个 else if 对 counterCells数组初始化并对其中一个counterCell对象的valeu + 1
        // 那么另一个线程 就会执行到该 if, 尝试对 baseCount + 1, 成功则 break; 相当于return, fasle 则一直循环重试, 或 counterCells对象不被其他线程使用了之后, 会走到其他if
        // 简单来说, 该if就是为了提高效率, 在counterCells被使用期间, 为了防止其他线程空闲下来, 让其尝试对 baseCount + 1
        else if (U.compareAndSwapLong(this, BASECOUNT, v = baseCount, v + x))
            break;                          // Fall back on using base
    }
}
```

<br/>

transfer(Node<K,V>[] tab, Node<K,V>[] nextTab)  
这个扩容是真的不太理解, 太难了, 就是扩容这个过程, 多个线程每个线程将老数组得元素转移一部分到新数组 合起来就是全转了  
只理解 转移得过程, concurrentHashMap1.8扩容真的是比 1.7难好多, 本来觉得1.7就挺难得了
```java
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;
    
    // 根据 CPU 核数, 计算出步长, 步长最小为 MIN_TRANSFER_STRIDE 即 16
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE; // subdivide range
    
    // 如果传入得 nextTab == null, 则要初始化 nextTab 大小为 老数组得2倍, 如果nextTab太大了 导致抛出异常 OOM 则 将阈值 sizeCtl 置为最大值 (变相扩容)
    // 在addCount()方法中调用本方法时 本身就是有两种情况 一种是想初始化新数组 则 传入参数 nextTab == null, 一种是想转移老数组元素到新数组 则会传入新数组 nextTab
    if (nextTab == null) {            // initiating
        try {
            @SuppressWarnings("unchecked")
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            nextTab = nt;
        } catch (Throwable ex) {      // try to cope with OOME
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        nextTable = nextTab;// 将初始化得新数组 赋值给 成员变量 nextTable
        transferIndex = n;// transferIndex = 老数组长度
    }
    int nextn = nextTab.length;// nextn 表示新数组长度
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);//就是一个Node节点,该节点得hash值为 MOVED(-1) 表示该处正在扩容
    boolean advance = true;//advance表示当前线程 数组下标处转移元素到新数组完成了, 要不要继续将老数组[下标-1]处转移元素到新数组
                           // 如果其他地方都有线程在转移, 则当前线程会将 finishing = true;表示当前线程转移元素结束
    boolean finishing = false; // to ensure sweep before committing nextTab
        
    // i 和 bound 表示 一个线程需要 转移老数组[下标] 和 新数组[下标] 得范围
    // 假如 老数组长度为 4, 新数组长度为 8, 步长为 2, 则 最后算出来 i = 3; bound = 2;
    // 那么表示 这个线程 只需要将老数组末尾两个元素(链表 或 红黑树) 即 老数组[3], 老数组[2], 转移到新数组中, 其他下标处[0],[1] 由其他线程处理
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;
        
        // 该 while 就是计算出 i 与 bound 得值
        while (advance) {
            int nextIndex, nextBound;
            if (--i >= bound || finishing) // 将 --i (这两个判断就是一些 安全判断 不要让 i 越界了)
                advance = false;
            else if ((nextIndex = transferIndex) <= 0) {// 将老数组长度赋值给 nextIndex, 同时判断不能 <= 0
                i = -1;
                advance = false;
            }
            
            // 假设 老数组长为 4, 新数组长为 8, 步长为 2
            // 首先要知道 transferindex 在之前新数组初始化时就被赋值为 老数组长度  == 4
            // nextIndex 在上一个 else if中被赋值为 transferindex 即 nextIndex = transferIndex == 4
            // nextBound 为 nextIndex > stride ? nextIndex - stride 否则为 0,  4 > 2  4 - 2 = 2; nextBound == 2
            // bound = 2; i = 4-1 = 3;   相当于该线程转移 [2] [3] 得数据 到 新数组
            // 然后 advance = false; 该线程结束 此while循环结束, 往下走 转移 [2] [3] 得数据到新数组
            // 此时如果 又进来一个线程
            // 那么该线程得 nextIndex = transferIndex = 2; 因为之前得线程已经给 transferIndex - 2 了
            // 那么 nextBound = 2 - 2 = 0, 那么 bound = 0, i = 2-1 = 1  相当于这个线程转移 [0] [1] 得数据 到 新数组
            // 然后 advance = false; 这个线程结束 此while循环结束, 往下走 转移 [0] [1] 得数据到新数组
            else if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex, nextBound = (nextIndex > stride ? nextIndex - stride : 0))) {
                bound = nextBound;
                i = nextIndex - 1;
                advance = false;
            }
        }
        
        // 该判断是 结束扩容得判断, 即满足条件时 扩容完成 将 nextTab 赋值给 table, 并修改阈值
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            if (finishing) {
                nextTable = null;
                table = nextTab;
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    return;
                finishing = advance = true;
                i = n; // recheck before commit
            }
        }
        
        else if ((f = tabAt(tab, i)) == null) // 如果 当前线程处理得数组下标处为null
            advance = casTabAt(tab, i, null, fwd);// 则通过 cas 将当前线程处理得数组下标处赋为 fwd, fwd得hash值为 MOVED(-1) 表示数组正在扩容
        else if ((fh = f.hash) == MOVED)
            advance = true; // already processed
        else {
            // 走到这里 就是正常进行 转移了, 将老数组元素转移到新数组
            // 对于 链表 这里采取得方式是 concurrentHashMap1.7使用得方式 将老数组元素转移到新数组
            // 对于 红黑树而言, 这里采取得方式是 HashMap1.8所采取得方式 将老数组元素转移到新数组
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;
                    if (fh >= 0) {
                        int runBit = fh & n;
                        Node<K,V> lastRun = f;
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                    else if (f instanceof TreeBin) {
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>(h, e.key, e.val, null, null);
                            if ((h & n) == 0) {
                                if ((p.prev = loTail) == null)
                                    lo = p;
                                else
                                    loTail.next = p;
                                loTail = p;
                                ++lc;
                            }
                            else {
                                if ((p.prev = hiTail) == null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :(hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) : (lc != 0) ? new TreeBin<K,V>(hi) : t;
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                }
            }
        }
    }
}
```

# HashMap 与 ConcurrentHashMap 面试要点
虽然感觉这些问题我都能交流交流 但是 感觉还是记下来比较好, 如对这些问题或回答有疑问 还是看之前得源码比较好

## HashMap
**HashMap底层数据结构**

JDK7:数组+链表

JDK8: 数组+链表+红黑树（看过源码的同学应该知道JDK8中即使用了单向链表，也使用了双向链表，双向链表主要是为了链表操作方便，应该在插入，扩容，链表转红黑树，红黑树转链表的过程中都要操作链表)

<br/>

**JDK8中的HashMap为什么要使用红黑树?**

当元素个数小于一个阈值时，链表整体的插入查询效率要高于红黑树，当元素个数大于此阈值时，链表整体的插入查询效率要低于红黑树。此阈值在HashMap中为8

<br/>

**JDK8中的HashMap什么时候将链表转化为红黑树?**

这个题很容易答错，大部分答案就是:当链表中的元素个数大于8时就会把链表转化为红黑树。但是其实还有另外一个限制:当发现链表中的元素个数大于8之后，还会判断一下当前数组的长度，如果数组长度小于64时，此时并不会转化为红黑树，而是进行扩容。只有当链表中的元素个数大于8，并且数组的长度大于等于64时才会将链表转为红黑树。

上面扩容的原因是，如果数组长度还比较小,就先利用披容来缩小链表的长度。

<br/>

**JDK8中HashMap的put方法的实现过程?**
1. 根据key生成hashcode
2. 判断当前HashMap对象中的数组是否为空，如果为空则初始化该数组
3. 根据逻辑与运算，算出hashcode基于当前数组对应的数组下标i
4. 判断数组的第i个位置的元素(tab[i])是否为空
   1. 如果为空，则将key,value封装为Node对象赋值给tab[i]
   2. 如果不为空:
        1. 如果put方法传入进来的key等于tab[i].key，那么证明存在相同的key
        2. 如果不等于tab[i].key，则:
            1. 如果tab[i]的类型是TreeNode，则表示数组的第i位置上是一颗红黑树，那么将key和value插入到红黑树中，并且在插入之前会判断在红黑树中是否存在相同的key
            2. 如果tab[i]的类型不是TreeNode，则表示数组的第i位置上是一个链表，那么遍历链表寻找是否存
               在相同的key，并且在遍历的过程中会对链表中的结点数进行计数，当遍历到最后一个结点时，会将key,value封装为Node插入到链表的尾部，同时判断在插入新结点之前的链表结点个数是不是大于等于8，如果是，则将链表改为红黑树。
        3. 如果上述步骤中发现存在相同的key，则根据onlyIfAbsent标记来判断是否需要更新value值，然后返回oldValue
5. modCount++
6. HashMap得元素个数size加1
7. 如果size大于扩容的阈值，则进行扩容

<br/>

**JDK8中HashMap的get方法的实现过程**
1. 根据key生成hashcode
2. 如果数组为空，则直接返回空
3. 如果数组不为空，则利用hashcode和数组长度通过迩辑与操作算出key所对应的数组下标i
4. 如果数组的第i个位置上没有元素,则直接返回空
5. 如果数组的第1个位上的元素的key等于get方法所传进来的key，则返回该元素，并获取该元素的value
6. 如果不等于则判断该元素还有没有下一个元素，如果没有，返回空
7. 如果有则判断该元素的类型是链表结点还是红黑树结点
    1. 如果是链表则遍历链表
    2. 如果是红黑树则遍历红黑树
8. 找到即返回元素,没找到的则返回空

<br/>

**JDK7与JDK8中HashMap的不同点**
1. JDK8中使用了红黑树
2. JDK7中链表的插入使用的头插法（扩容转移元素的时候也是使用的头插法，头插法速度更快，无需遍历链表， 但是在多线程扩容的情况下使用头插法会出现循环链表的问题，导致CPU飙升)，JDK8中链表使用的尾插法(JDK8中反正要去计算链表当前结点的个数，反正要遍历的链表的，所以直接使用尾插法)
3. JDK7的Hash算法比JDK8中的更复杂,Hash算法越复杂，生成的hashcode则更散列，那么hashmap中的元 素则更散列，更散列则hashmap的查询性能更好，JDK7中没有红黑树，所以只能优化Hash算法使得元素更散列，而JDK8中增加了红黑树，查询性能得到了保障，所以可以简化一下Hash算法，毕竟Hash算法越复杂就越消耗CPU
4. 扩容的过程中JDK7中有可能会重新对key进行哈希（重新Hash跟哈希种子有关系)，而JDK8中没有这部分逻辑
5. JDK8中扩容的条件和JDK7中不一样，除开判断size是否大于阈值之外，JDK7中还判断了tab[i]是否为空，不 为空的时候才会进行扩容,而JDK8中则没有该条件了
6. JDK8中还多了一个API: putlfAbsent(key,value)
7. JDK7和JDK8扩容过程中转移元素的逻辑不一样,JDK7是每次转移一个元素，JDK8是先算出来当前位置上哪些元素在新数组的低位上，哪些在新数组的高位上，然后在一次性转移

## ConcurrentHashMap
**JDK7中的ConcurrentHashMap是怎么保证并发安全的?**
主要利用Unsafe操作+ReentrantLock+分段思想。

主要使用了Unsafe操作中的:
1. compareAndSwapObject:通过cas的方式修改对象的属性
2. putOrderedObject:并发安全的给数组的某个位置赋值
3. getObjectVolatile:并发安全的获取数组某个位置的元素

分段思想是为了提高ConcurrentHashMap的并发量，分段数越高则支 持的最大并发量越高，程序员可以通过concurrencyLevel参数来指定并发量。ConcurrentHashMap的内部类Segment就是用来表示某一个段的。

每个Segment就是一个小型的HashMap的，当调用ConcurrentHashMap的put方法是，最终会调用到Segment的put方法，而Segment类继承了ReentrantLock,所以Segment自带可重入锁，当调用到Segment的put方法时，会先利用可重入锁加锁，加锁成功后再将待插入的key,value插入到小型HashMap中，插入完成后解锁。

<br/>

**JDK7中的ConcurrentHashMap的底层原理**

ConcurrentHashMap底层是由两层嵌套数组来实现的:
1. ConcurrentHashMap对象中有一个属性segments，类型为Segment[];
2. Segment对象中有一个属性table，类型为HashEntry[];

当调用ConcurrentHashMap的put方法时，先根据key计算出对应的Segment[]的数组下标j，确定好当前key,value应该插入到哪个Segment对象中，如果segments[j]为空，则利用自旋锁的方式在j位置生成一个Segment对象。

然后调用Segment对象的put方法。

Segment对象的put方法会先加锁，然后也根据key计算出对应的HashEntry[]的数组下标i，然后将key,value封装为HashEntry对象放入该位置,此过程和JDK7的HashMap的put方法一样，然后解锁。

在加锁的过程中逻辑比较复杂，先通过自旋加锁，如果超过一定次数就会直接阻塞等等加锁。

<br/>

**JDK8中的ConcurrentHashMap是怎么保证并发安全的?**

主要利用Unsafe操作+synchronized关键字。

Unsafe操作的使用仍然和JDK7中的类似，主要负责并发安全的修改对象的属性或数组某个位置的值。

synchronized主要负责在需要操作某个位置时进行加锁(该位置不为空)，比如向某个位置的链表进行插入结点， 向某个位置的红黑树插入结点。

JDK8中其实仍然有分段锁的思想，只不过JDK7中段数是可以控制的，而JDK8中是数组的每一个位置都有一把锁。

当向ConcurrentHashMap中put一个key,value时，
1. 首先根据key计算对应的数组下标i，如果该位置没有元素，则通过自旋的方法去向该位置赋值。
2. 如果该位置有元素，则synchronized会加锁
3. 加锁成功之后，在判断该元素的类型
    1. 如果是链表节点则进行添加节点到链表中
    2. 如果是红黑树则添加节点到红黑树
4. 添加成功后，判断是否需要进行树化
5. addCount，这个方法的意思是ConcurrentHashMap的元素个数加1，但是这个操作也是需要并发安全的，并且元素个数加1成功后，会继续判断是否要进行扩容，如果需要，则会进行扩容，所以这个方法很重要。
6. 同时一个线程在put时如果发现当前ConcurrentHashMap正在进行扩容则会去帮助扩容。

<br/>

**JDK7和JDK8中的ConcurrentHashMap的不同点**

这两个的不同点太多了....，既包括了HashMap中的不同点，也有其他不同点，比如:
1. JDK8中没有分段锁了，而是使用synchronized来进行控制
2. JDK8中的扩容性能更高，支持多线程同时扩容，实际上JDK7中也支持多线程扩容，因为JDK7中的扩容是针对每个Segment的，所以也可能多线程扩容，但是性能没有JDK8高，因为JDK8中对于任意一个线程都可以去帮助扩容
3. JDK8中的元素个数统计的实现也不一样了，JDK8中增加了CounterCell来帮助计数，而JDK7中没有，JDK7 中是put的时候每个Segment内部计数，统计的时候是遍历每个Segment对象加锁统计