
> select * from employees order by hire_date desc limit 0,1

描述：从employees中查找最晚入职员工的所有信息

order by hire_date desc 将结果集进行降序排列

limit 0,1

> select * from employees order by hire_date desc limit 2,1

描述：查找入职员工时间排名倒数第三的员工所有信息

order by hire_date desc

limit 2,1

> SELECT salaries.*,dept_manager.dept_no from salaries  
> FROM salaries JOIN dept_manager
> ON salaries.emp_no = dept_manager.emp_no
> ORDER BY salaries.emp_no

描述：查找各个部门当前(to_date='9999-01-01')领导当前薪水详情以及其对应部门编号dept_no




查找所有已经分配部门的员工的last_name和first_name
