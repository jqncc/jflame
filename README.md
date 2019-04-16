# Java开发组件jflame
jflame是java开发的一些经验积累,是一个造轮子的项目,谈不上开发框架,所以暂时就叫开发组件吧.目标有两个,一个好用且常用的工具包,一个项目脚手架.


## 子项目介绍
子项目有: jflame-toolkit, jflame-web, jflame-db, jflame-mvc.

### jflame-toolkit
工具包,不依赖于具体框架或环境的工具包,对常用的一些技术做抽象和封装.依赖apache commons-lang3,二者互为补充.已有功能:
1. redis操作封装,对单机,集群,哨兵3种模式下常用的redis命令做了抽象,对不常用的事务,管道和发布订阅等没有做深入支持. 基于jedis和spring-data-redis两个实现,spring-data-redis本身已经做了很好的封装(似乎过度封装了^_^),但个人用着感觉不是太方便,特别是泛型限定

2. zookeeper操作封装,实现统一的zk操作,不必区分客户端,目前有zkclient和curator两个zookeeper最常用客户端的实现,推荐使用curator.

3. 分布式锁,基于redis和zookeeper实现,zookeeper锁的实现则使用curator已有实现.

4. excel导入导出工具,通过注解方式轻松的实现报表数据的导出,基于apache poi(这货不管怎么玩实在吃内存).

5. 参数验证加强,提供一个静态工具类实现常用验证规则,外加bean-validator的一些规则补充.

6. 加解密和编码支持,对称加密(3des,aes),非对象加密rsa,摘要算法(md5,sha/256/512,hmacmd5)均有实现,复杂加解密一个方法搞定.编码转换包括:base64,hex,urlencode,bytes->int, bytes->long等.

7. http/ftp操作简易封装

8. 配置参数的获取和类型转换抽象,servlet,filter参数,properties属性文件配置.

9. 文件操作工具,zip文件压缩,图片压缩,properties文件操作等

10. 各种helper类,StringHelper,DateHelper,JsonHelper,MapHelper,CollectionHelper ....

### jflame-web

web环境下常用功能(与mvc框架无关).跨域解决CorsFilter,csrf过滤,url模糊匹配filter等.

### jflame-db
一个基于spring-jdbc的简易ORM,通过注解和一个万能dao搞定db操作,由于mybatis在小公司还是主流所以目前暂停维护中

### jflame-mvc
脚手架项目,目前暂停中
