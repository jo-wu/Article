# Hello HTML

<small>HyperText Mark-up Language</small>

<small>超文本标记语言</small>


## **<center> 目录 </center>**


参考链接：

HTML 参考手册：http://www.w3school.com.cn/tags/index.asp

HTML 标签缩写参照表：
https://www.cnblogs.com/ideaspace/p/7134381.html?utm_source=itdadao&utm_medium=referral



## **HTML标签**

---
空标签：&lt;br/&gt;、&lt;img&gt;

## **HTML文本**

---
标题（Heading）是通过&lt;h1&gt;~&lt;h6&gt; 等标签进行定义的。

段落（paragraph）通过&lt;p&gt;

换行（break）通过&lt;br/&gt;

**格式化标签**

| 标签         | 描述     | 实际效果     |
| :------: | :------: | :------: |
|&lt;b&gt;     | 粗体文本 |<b>This text is bold</b>
|&lt;big&gt;   | 大字号   |<big>big</big>
|&lt;em&gt;    | 着重文字 |<em>This text is emphasized</em>
|&lt;i&gt;     | 斜体字   |<i>This text is italic</i>
|&lt;small&gt; | 小号字   |<small>small</small>
|&lt;strong&gt;| 加重语气 |<strong>This text is strong</strong>
|&lt;sub&gt;   | 下标字   |<sub>sub</sub>
|&lt;sup&gt;   | 上标字   |<sup>sup</sup>
|&lt;ins&gt;   | 插入字   |<ins>ins</ins>
|&lt;del&gt;   | 删除字   |<del> delete </del>
|&lt;code&gt;  | 代码字体 |<code>This is some computer code</code>

&lt;ins&gt; 标签


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
</form>0
```
**输出：**
<form>
<input type="radio" name="sex" value="male" /> Male
<br />
<input type="radio" name="sex" value="female" /> Female
</form>

表单的动作属性（Action）和确认按钮
```
<form name="input" action="html_form_action.asp" method="get">
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


## **HTML属性**

---

HTML 全局属性
= HTML5 中添加的属性。

| 属性   | 描述  |
| :------: | :------: |
|accesskey	             |规定激活元素的快捷键。
|class	                 |规定元素的一个或多个类名（引用样式表中的类）。
|contenteditable     (H5)|规定元素内容是否可编辑。
|contextmenu	     (H5)|规定元素的上下文菜单。上下文菜单在用户点击元素时显示。
|data-*              (H5)|用于存储页面或应用程序的私有定制数据。
|dir                     |规定元素中内容的文本方向。
|draggable           (H5)|规定元素是否可拖动。
|dropzone            (H5)|规定在拖动被拖动数据时是否进行复制、移动或链接。
|hidden              (H5)|规定元素仍未或不再相关。
|id                      |规定元素的唯一 id。
|lang                    |规定元素内容的语言。
|spellcheck          (H5)|规定是否对元素进行拼写和语法检查。
|style                   |规定元素的行内 CSS 样式。
|tabindex                |规定元素的 tab 键次序。
|title                   |规定有关元素的额外信息。
|translate           (H5)|规定是否应该翻译元素内容。



