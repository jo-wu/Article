ORM模型简介

ORM：对象关系映射（Object Relation Mapping）

Object

Mapping

Relation


JDBC程序设计的缺陷

- 存在信息硬编码。如，数据库的url、账户、密码
- 大量无关业务处理的编码。如，数据库连接、错误处理等等
- 扩展优化不便。如，配置数据库连接池

MyBatis概述

- 能避免硬编码
- XML配置、注解的支持
- POJO对象和数据库记录直接映射
- 完善的文档支持

Mybatis原名ibatis，ibatis=internet+abatis

功能：
数据库交互信息配置化
动态SQL处理

使用场景：
更加关注SQL优化的项目
需求改动频繁更新改动的项目

MyBatis操作流程：

1、mybatis配置
mybatis.xml 主配置文件
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <!-- 引入数据库主配置文件db.properties -->
    <properties resource="db.properties"></properties>

    <!--开发环境配置 [development:product:test] default为当前开发环境-->
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
        <environment id="product">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
        <environment id="test">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
    </environments>

    <!--引入mapper配置文件-->
    <mappers>
        <!--<mapper resource="org/mybatis/example/BlogMapper.xml"/>-->
        <mapper resource="mapper/usersmapper.xml"/>
    </mappers>
</configuration>
</configuration>
```

Mapper.xml映射配置

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.demo.mybatis.example.entity.Users">
    <select id="usersList" resultType="com.example.demo.mybatis.example.entity.Users">
        select * from users
    </select>
</mapper>
```

2、连接数据库
```
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```

3、获取数据库会话
```
SqlSession session = sqlSessionFactory.openSession();
try {
  Blog blog = (Blog) session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
} finally {
  session.close();
}
```

案例：

后台管理系统用户数据维护平台

- 所有用户数据查询
- 单个用户数据查询
- 用户数据修改
- 锁定用户账号
- 珊瑚用户账号
- 彻底删除用户账号