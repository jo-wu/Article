U# Hello HTML


## **<center> 目录 </center>**


## **HTML标签**

---
空标签：&lt;br/&gt;、&lt;img&gt;

## **HTML文本**

---
标题（Heading）是通过&lt;h1&gt;~&lt;h6&gt; 等标签进行定义的。

段落（paragraph）通过&lt;p&gt;

换行（break）通过&lt;br/&gt;

**格式化标签**

| 标签         | 描述     |
| :------: | :------: |
|&lt;b&gt;     | 粗体文本 |
|&lt;big&gt;   | 大字号   |
|&lt;em&gt;    | 着重文字 |
|&lt;i&gt;     | 斜体字   |
|&lt;small&gt; | 小号字   |
|&lt;strong&gt;| 加重语气 |
|&lt;sub&gt;   | 下标字   |
|&lt;sup&gt;   | 插入字   |
|&lt;ins&gt;   | 上标字   |
|&lt;del&gt;   | 删除字   |


## **HTML超链接**

---
超链接（anchor） &lt;a&gt;

通用属性：href、target、name
专有属性：

<a name="label">Text to be displayed</a>

<a name="tips">Useful Tips Section</a>

<a href="#tips">Visit the Useful Tips Section</a>


实例：<br/>
<a href="http://www.w3chtml.com/">Visit W3C HTML</a>

当前页打开：<br/>
&lt;a href="http://www.w3chtml.com/"&gt;Visit W3C HTML&lt;/a&gt;

新空白页：<br/>
&lt;a href="http://www.w3chtml.com/" target="_blank"&gt;Visit W3C HTML&lt;/a&gt;



## **HTML图片**

---
图像标签（&lt;img&gt;）和源属性（Src）

在 HTML 中，图像由&lt;img&gt; 标签定义。

&lt;img&gt;是空标签，意思是说，它只包含属性，并且没有闭合标签。

**实例：**
```
<img src="url" />
```


## **HTML表格**

---
表格（table）由 &lt;table&gt; 标签来定义。

行（tablerow）&lt;tr&gt;
数据(tabledata)&lr;td&gt;

| 标签         | 描述     |
| :------: | :------: |
|&lt;thead&gt;| 表格头   |
|&lt;tbody&gt;| 表格主体 |
|&lt;tfoot&gt;| 表格注脚 |


## **HTML列表**

---

| 标签         | 描述  |
| :------: | :------: |
|&lt;ol&gt;| 有序列表  |
|&lt;ul&gt;| 无序列表  |
|&lt;li&gt;| 列表项    |
|&lt;dl&gt;| 自定义列表|
|&lt;dt&gt;| 自定义项目|
|&lt;dd&gt;| 自定义描述|


无序列表
```
<ul>
<li>Coffee</li>
<li>Milk</li>
</ul>
```
**输出：**
<ul>
<li>Coffee</li>
<li>Milk</li>
</ul>

有序列表
```
<ol>
<li>Coffee</li>
<li>Milk</li>
</ol>
```
**输出：**
<ol>
<li>Coffee</li>
<li>Milk</li>
</ol>

自定义列表
```
<dl>
<dt>Coffee</dt>
<dd>Black hot drink</dd>
<dt>Milk</dt>
<dd>White cold drink</dd>
</dl>
```
**输出：**
<dl>
<dt>Coffee</dt>
<dd>Black hot drink</dd>
<dt>Milk</dt>
<dd>White cold drink</dd>
</dl>


## **HTML表单**

---
表单（form） <form>

| 标签         | 描述  |
| :------: | :------: |
|&lt;form&gt;    | 供用户输入的表单  |
|&lt;input&gt;   | 输入域           |
|&lt;textarea&gt;| 文本域           |
|&lt;label&gt;   | 控制的标签       |
|&lt;fieldset&gt;| 定义域           |
|&lt;legend&gt;  | 定义域标题       |
|&lt;select&gt;  | 选择列表         |
|&lt;optgroup&gt;| 选项组           |
|&lt;option&gt;  | 下拉列表中的选项  |
|&lt;button&gt;  | 按钮             |

文本域（Text Fields）
```
<form>
First name: 
<input type="text" name="firstname" />
<br />
Last name: 
<input type="text" name="lastname" />
</form>
```
**输出：**
<form>
First name: 
<input type="text" name="firstname" />
<br />
Last name: 
<input type="text" name="lastname" />
</form>

单选按钮（Radio Buttons）
```
<form>
<input type="radio" name="sex" value="male" /> Male
<br />
<input type="radio" name="sex" value="female" /> Female
</form>
```
**输出：**
<form>
<input type="radio" name="sex" value="male" /> Male
<br />
<input type="radio" name="sex" value="female" /> Female
</form>

表单的动作属性（Action）和确认按钮
```<form name="input" action="html_form_action.asp" method="get">
Username: 
<input type="text" name="user" />
<input type="submit" value="Submit" />
</form>
```
**输出：**
<form name="input" action="html_form_action.asp" method="get">
Username: 
<input type="text" name="user" />
<input type="submit" value="Submit" />
</form>


## **HTML框架**

---


