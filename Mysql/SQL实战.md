
---
```
select * from employees 
order by hire_date desc  #将结果集进行降序排列
limit 0,1
```
描述：从employees中查找最晚入职员工的所有信息

---
```
select * from employees 
order by hire_date desc 
limit 2,1
```
描述：查找入职员工时间排名倒数第三的员工所有信息

---
```
select salaries.*,dept_manager.dept_no from salaries, dept_manager 
where salaries.emp_no = dept_manager.emp_no
and salaries.to_date = "9999-01-01"
and dept_manager.to_date = "9999-01-01";
```
描述：查找各个部门当前(to_date='9999-01-01')领导当前薪水详情以及其对应部门编号dept_no

---

查找所有已经分配部门的员工的last_name和first_name

```
select last_name,first_name,dept_no
from dept_emp
join employees
on dept_emp.emp_no = employees.emp_no
```

```
select last_name,first_name,dept_no
from dept_emp, employees
where dept_emp.emp_no = employees.emp_no
```

---
查找所有员工的last_name和first_name以及对应部门编号dept_no，也包括展示没有分配具体部门的员工

解析：由于有些员工可能没有分配部门号，需要用左外连接就好了，即返回左表中所有的行，即便右表没有满足的条件

```
select a.last_name,a.first_name,b.dept_no
from employees a left join dept_emp b
on a.emp_no=b.emp_no
```

---

查找所有员工入职时候的薪水情况，给出emp_no以及salary， 并按照emp_no进行逆序

```
SELECT e.emp_no,s.salary
FROM employees as e
INNER JOIN salaries as s
ON e.emp_no=s.emp_no and e.hire_date=s.from_date
ORDER BY e.emp_no DESC;
```

---

查找薪水涨幅超过15次的员工号emp_no以及其对应的涨幅次数t

```
select emp_no, count(emp_no) as t from salaries
group by emp_no having t > 15
```

---

找出所有员工当前(to_date='9999-01-01')具体的薪水salary情况，对于相同的薪水只显示一次,并按照逆序显示
```
SELECT DISTINCT salary FROM salaries
WHERE to_date = '9999-01-01' ORDER BY salary DESC
```

---

获取所有部门当前manager的当前薪水情况，给出dept_no, emp_no以及salary，当前表示to_date='9999-01-01'

```
select d.dept_no , d.emp_no ,s.salary 
from  salaries s  join dept_manager d  on d.emp_no = s.emp_no 
where  d.to_date = '9999-01-01' 
and  s.to_date = '9999-01-01';
```

---

获取所有非manager的员工emp_no

解析：即employees 里的emp_no不在dept_manager 出现非manager了，关键使用not in
```
select a.emp_no
from employees a
where a.emp_no not in (select b.emp_no from  dept_manager b)
```

---
获取所有员工当前的manager，如果当前的manager是自己的话结果不显示，当前表示to_date='9999-01-01'。
结果第一列给出当前员工的emp_no,第二列给出其manager对应的manager_no。

解析：注意他们虽然职位不一样，但是部门是一样的。
```
select a.emp_no,b.emp_no as manager_no
from dept_emp a,dept_manager b
where a.to_date='9999-01-01' 
and b.to_date='9999-01-01' 
and a.dept_no=b.dept_no 
and a.emp_no !=b.emp_no 
```

---

获取所有部门中当前员工薪水最高的相关信息，给出dept_no, emp_no以及其对应的salary
https://blog.csdn.net/qq_14998713/article/details/78994103
```
SELECT d.dept_no,d.emp_no,MAX(s.salary) AS salary
FROM salaries AS s inner join dept_emp AS d
ON d.emp_no=s.emp_no
WHERE d.to_date='9999-01-01' AND s.to_date='9999-01-01'
GROUP BY d.dept_no
```

---

从titles表获取按照title进行分组，每组个数大于等于2，给出title以及对应的数目t。

```
select title,count(title) as t 
from titles
group by title having t>=2
```

---

https://www.nowcoder.com/practice/c59b452f420c47f48d9c86d69efdff20?tpId=82&tqId=29766&tPage=1&rp=&ru=/ta/sql&qru=/ta/sql/question-ranking

从titles表获取按照title进行分组，每组个数大于等于2，给出title以及对应的数目t。
注意对于重复的emp_no进行忽略。

```
SELECT title, COUNT(DISTINCT emp_no) AS t FROM titles
GROUP BY title HAVING t >= 2
```

---

查找employees表所有emp_no为奇数，且last_name不为Mary的员工信息，并按照hire_date逆序排列
```
select emp_no,birth_date,first_name,last_name,gender,hire_date
from employees
where emp_no%2!=0 and last_name !="Mary"
order by hire_date desc
```

---

统计出当前各个title类型对应的员工当前薪水对应的平均工资。结果给出title以及平均工资avg。

```
select title,avg(salary)
from titles left join salaries
    on titles.emp_no = salaries.emp_no
where titles.to_date='9999-01-01'
    and salaries.to_date='9999-01-01'
group by titles.title
```

---
获取当前（to_date='9999-01-01'）薪水第二多的员工的emp_no以及其对应的薪水salary

```
select emp_no,salary
from salaries
where to_date='9999-01-01'
order by salary desc
limit 1,1
```

---

查找当前薪水(to_date='9999-01-01')排名第二多的员工编号emp_no、薪水salary、last_name以及first_name，不准使用order by
```
select e.emp_no,max(s.salary),e.last_name,e.first_name from employees e, salaries s
where s.salary < (select max(salary) from salaries)
and s.to_date="9999-01-01" and e.emp_no=s.emp_no;
```

---
查找所有员工的last_name和first_name以及对应的dept_name，也包括暂时没有分配部门的员工

```
select a.last_name,a.first_name,b.dept_name
from employees a
left join dept_emp c
on a.emp_no=c.emp_no 
left join departments b
on b.dept_no=c.dept_no
```
---
查找员工编号emp_no为10001其自入职以来的薪水salary涨幅值growth
```
SELECT ( 
(SELECT salary FROM salaries WHERE emp_no = 10001 ORDER BY to_date DESC LIMIT 1) -
(SELECT salary FROM salaries WHERE emp_no = 10001 ORDER BY to_date ASC LIMIT 1)
) AS growth
```