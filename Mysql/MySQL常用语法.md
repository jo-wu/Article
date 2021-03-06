# 准备工作

```
DROP DATABASE IF EXISTS student;
CREATE DATABASE IF NOT EXISTS student character set utf8;
USE student;
SHOW TABLES;
CREATE TABLE IF NOT EXISTS student(
  name VARCHAR(8),
  id VARCHAR(8),
  school VARCHAR(8),
  class VARCHAR(8)
);
CREATE TABLE IF NOT EXISTS studentb(
  name VARCHAR(8),
  id VARCHAR(8),
  school VARCHAR(8),
  class VARCHAR(8)
)DEFAULT CHARSET utf8;
CREATE TABLE IF NOT EXISTS teacher(
  name VARCHAR(8),
  id VARCHAR(8),
  school VARCHAR(8)
);
CREATE TABLE IF NOT EXISTS teacherb(
  name VARCHAR(8),
  id VARCHAR(8),
  school VARCHAR(8)
);
CREATE TABLE IF NOT EXISTS course(
  name VARCHAR(8),
  id VARCHAR(8),
  teacher VARCHAR(8),
  time VARCHAR(8),
  student VARCHAR(8)
);
INSERT INTO student VALUES ('张三','001','信息学院','计算机班');
INSERT INTO student VALUES ('李四','002','信息学院','计算机班');
INSERT INTO student VALUES ('王五','003','信息学院','计算机班');
INSERT INTO student VALUES ('小六','004','信息学院','计算机班');

INSERT INTO studentb VALUES ('小吴','005','信息学院','计算机班');

INSERT INTO teacher VALUES ('老白','001','信息学院');
INSERT INTO teacher VALUES ('老李','002','信息学院');
INSERT INTO teacher VALUES ('老黄','003','信息学院');
INSERT INTO teacher VALUES ('老江','004','信息学院');

INSERT INTO teacherb VALUES ('老黄','003','信息学院');
INSERT INTO teacherb VALUES ('老江','004','信息学院');
INSERT INTO teacherb VALUES ('老陈','005','信息学院');

INSERT INTO course VALUES ('计算机概述','001','老白','星期二','张三-李四');
INSERT INTO course VALUES ('算法入门阿','002','老李','星期三','张三-王五');
INSERT INTO course VALUES ('计算机原理','003','老江','星期四','李四-王五');
INSERT INTO course VALUES ('计算机炒菜','004','老黄','星期五','李四-小六');
```

# 增删改查

## Insert
```
INSERT INTO student VALUES ('小六','004','信息学院','计算机班');
```
## Delete
```
DELETE FROM table_name WHERE column=value;
```
## Update
```
UPDATE student SET name='小七' WHERE id='004';
```
## Select
```
SELECT * FROM student;
```
# 新建数据库与表

# 排序与过滤

## Order by & Order by DESC

# [连接](https://www.cnblogs.com/BeginMan/p/3754322.html)

INNER JOIN 等值连接：获取两张表之间的交集

LEFT JOIN 左连接：将右表连接到左表，在左表没有匹配的丢弃。空位补null；

RIGHT JOIN 同LEFT JOIN类似

利用以上三种求交集、并集、差集。

# 视图



# 存储过程

//带输入参数的存储过程
```
DROP PROCEDURE insert_into_student_by_procedure_with_parameters;

CREATE PROCEDURE insert_into_student_by_procedure_with_parameters
(IN name VARCHAR(8), 
IN id VARCHAR(8), 
IN school VARCHAR(8), 
IN class VARCHAR(8)) 
INSERT INTO studentb(name, id, school, class) VALUES (
  name, 
  id, 
  school, 
  class);
```

//带输入输出参数的存储过程
```
DROP PROCEDURE IF EXISTS get_name_by_id;

CREATE PROCEDURE get_name_by_id(
  IN cid VARCHAR(8), 
  OUT return_name VARCHAR(8)
  ) 
  SELECT name INTO return_name 
  FROM student 
  WHERE id=cid;
```

# 存储函数





# 数据库事务






## [数据库事务的ACID](https://blog.csdn.net/qq_25448409/article/details/78110430)

原子性（Atomicity）、一致性（Consistency）、隔离性（Isolation）、持久性（Durability）

讲数据库事务一致性怎么能不提数据库的ACID特性。首先介绍事务，什么是事务，事务就是DBMS当中用户程序的任何一次执行，事务是DBMS能看到的基本修改单元。事务是指对系统进行的一组操作，为了保证系统的完整性，事务需要具有ACID特性，具体如下：1.原子性（Atomic）     一个事务包含多个操作，这些操作要么全部执行，要么全都不执行。实现事务的原子性，要支持回滚操作，在某个操作失败后，回滚到事务执行之前的状态。     回滚实际上是一个比较高层抽象的概念，大多数DB在实现事务时，是在事务操作的数据快照上进行的（比如，MVCC），并不修改实际的数据，如果有错并不会提交，所以很自然的支持回滚。     而在其他支持简单事务的系统中，不会在快照上更新，而直接操作实际数据。可以先预演一边所有要执行的操作，如果失败则这些操作不会被执行，通过这种方式很简单的实现了原子性。
2.一致性（Consistency）     一致性是指事务使得系统从一个一致的状态转换到另一个一致状态。事务的一致性决定了一个系统设计和实现的复杂度。事务可以不同程度的一致性：强一致性：读操作可以立即读到提交的更新操作。弱一致性：提交的更新操作，不一定立即会被读操作读到，此种情况会存在一个不一致窗口，指的是读操作可以读到最新值的一段时间。最终一致性：是弱一致性的特例。事务更新一份数据，最终一致性保证在没有其他事务更新同样的值的话，最终所有的事务都会读到之前事务更新的最新值。如果没有错误发生，不一致窗口的大小依赖于：通信延迟，系统负载等。     其他一致性变体还有：单调一致性：如果一个进程已经读到一个值，那么后续不会读到更早的值。会话一致性：保证客户端和服务器交互的会话过程中，读操作可以读到更新操作后的最新值。
3.隔离性（Isolation）     并发事务之间互相影响的程度，比如一个事务会不会读取到另一个未提交的事务修改的数据。在事务并发操作时，可能出现的问题有：脏读：事务A修改了一个数据，但未提交，事务B读到了事务A未提交的更新结果，如果事务A提交失败，事务B读到的就是脏数据。不可重复读：在同一个事务中，对于同一份数据读取到的结果不一致。比如，事务B在事务A提交前读到的结果，和提交后读到的结果可能不同。不可重复读出现的原因就是事务并发修改记录，要避免这种情况，最简单的方法就是对要修改的记录加锁，这回导致锁竞争加剧，影响性能。另一种方法是通过MVCC可以在无锁的情况下，避免不可重复读。幻读：在同一个事务中，同一个查询多次返回的结果不一致。事务A新增了一条记录，事务B在事务A提交前后各执行了一次查询操作，发现后一次比前一次多了一条记录。幻读是由于并发事务增加记录导致的，这个不能像不可重复读通过记录加锁解决，因为对于新增的记录根本无法加锁。需要将事务串行化，才能避免幻读。     事务的隔离级别从低到高有：Read Uncommitted：最低的隔离级别，什么都不需要做，一个事务可以读到另一个事务未提交的结果。所有的并发事务问题都会发生。Read Committed：只有在事务提交后，其更新结果才会被其他事务看见。可以解决脏读问题。Repeated Read：在一个事务中，对于同一份数据的读取结果总是相同的，无论是否有其他事务对这份数据进行操作，以及这个事务是否提交。可以解决脏读、不可重复读。 Serialization：事务串行化执行，隔离级别最高，牺牲了系统的并发性。可以解决并发事务的所有问题。     通常，在工程实践中，为了性能的考虑会对隔离性进行折中。
4.持久性（Durability）     事务提交后，对系统的影响是永久的。
