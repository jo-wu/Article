# 


---
metaphor n.隐喻





---

# FOREWORD

What do we talk about when we talk about architecture?
当我们谈到架构的时候我们是在谈论什么？

As with any metaphor, describing software through the lens of architecture can hide as much as it can
reveal. It can both promise more than it can deliver and deliver more than it promises.
The obvious appeal of architecture is structure, and structure is something that dominates the
paradigms and discussions of software development—components, classes, functions, modules,
layers, and services, micro or macro. But the gross structure of so many software systems often defies
either belief or understanding—Enterprise Soviet schemes destined for legacy, improbable Jenga
towers reaching toward the cloud, archaeological layers buried in a big-ball-of-mud slide. It’s not
obvious that software structure obeys our intuition the way building structure does.
与任何比喻一样，通过架构镜头描述软件可以尽可能地隐藏
揭示。 它既可以承诺，也可以提供超出承诺的产品。
架构的明显吸引力在于结构，结构是主导的结构
软件开发的范例和讨论 - 组件，类，功能，模块，
层和服务，微观或宏观。 但是，如此多的软件系统的总体结构往往是无法抗拒的
无论是信仰还是理解 - 企业苏联计划注定要留下遗产，不可能的Jenga
塔楼朝向云层，考古层埋在一个巨大的泥球滑道中。 不是
很明显，软件结构遵循建筑结构的方式来实现我们的直觉。

Buildings have an obvious physical structure, whether rooted in stone or concrete, whether arching
high or sprawling wide, whether large or small, whether magnificent or mundane. Their structures
have little choice but to respect the physics of gravity and their materials. On the other hand—except
in its sense of seriousness—software has little time for gravity. And what is software made of?
Unlike buildings, which may be made of bricks, concrete, wood, steel, and glass, software is made of
software. Large software constructs are made from smaller software components, which are in turn
made of smaller software components still, and so on. It’s coding turtles all the way down.
建筑物具有明显的物理结构，无论是石头还是混凝土，无论是拱形还是拱形
高大或宽阔，无论大小，无论是华丽还是平凡。 他们的结构
别无选择，只能尊重重力物理及其材料。 另一方面 - 除了
在其严肃性的意义上 - 软件几乎没有时间引力。 什么是软件？
与可能由砖，混凝土，木材，钢和玻璃制成的建筑不同，软件是由
软件。 大型软件构造由较小的软件组件构成，而软件组件又是如此
由较小的软件组件组成，依此类推。 它一直在编码乌龟。

When we talk about software architecture, software is recursive and fractal in nature, etched and
sketched in code. Everything is details. Interlocking levels of detail also contribute to a building’s
architecture, but it doesn’t make sense to talk about physical scale in software. Software has structure
—many structures and many kinds of structures—but its variety eclipses the range of physical
structure found in buildings. You can even argue quite convincingly that there is more design activity
and focus in software than in building architecture—in this sense, it’s not unreasonable to consider
software architecture more architectural than building architecture!
当我们谈论软件架构时，软件本质上是递归和分形的，蚀刻和
用代码勾画。 一切都是细节。 联锁的细节水平也有助于建筑物
体系结构，但谈论软件中的物理规模是没有意义的。 软件有结构
- 多种结构和多种结构 - 但其多样性使物理范围黯然失色
建筑物中发现的结构。 你甚至可以非常有说服力地争辩说有更多的设计活动
并且专注于软件而不是建筑物 - 从这个意义上说，考虑并不是不合理的
软件架构比建筑架构更具建筑性！

But physical scale is something humans understand and look for in the world. Although appealing and
visually obvious, the boxes on a PowerPoint diagram are not a software system’s architecture.
There’s no doubt they represent a particular view of an architecture, but to mistake boxes for the big
picture—for the architecture—is to miss the big picture and the architecture: Software architecture
doesn’t look like anything. A particular visualization is a choice, not a given. It is a choice founded on
a further set of choices: what to include; what to exclude; what to emphasize by shape or color; what
to de-emphasize through uniformity or omission. There is nothing natural or intrinsic about one view
over another.
但物理尺度是人类在世界上理解和寻找的东西。 虽然有吸引力和
在视觉上显而易见，PowerPoint图表上的框不是软件系统的架构。
毫无疑问，它们代表了一种建筑的特殊视图，但却将错误的方框误认为是大型的
图片 - 用于架构 - 是错过大局和架构：软件架构
看起来不像什么。 特定的可视化是一种选择，而不是给定的。 这是一个基于的选择
进一步的选择：要包括的内容; 什么排除; 什么要强调形状或颜色; 什么
通过统一或遗漏来强调。 一种观点没有任何自然或内在的东西
在另一个。

Although it might not make sense to talk about physics and physical scale in software architecture, we
do appreciate and care about certain physical constraints. Processor speed and network bandwidth
can deliver a harsh verdict on a system’s performance. Memory and storage can limit the ambitions of
any code base. Software may be such stuff as dreams are made on, but it runs in the physical world.

<small>This is the monstrosity in love, lady, that the will is infinite, and the execution confined; that the desire is boundless,and the act a slave to limit.</small>
—William Shakespeare

The physical world is where we and our companies and our economies live. This gives us another
calibration we can understand software architecture by, other less physical forces and quantities
through which we can talk and reason.
<small>Architecture represents the significant design decisions that shape a system, where significant is measured by cost of
change.</small>
—Grady Booch
Time, money, and effort give us a sense of scale to sort between the large and the small, to distinguish
the architectural stuff from the rest. This measure also tells us how we can determine whether an
architecture is good or not: Not only does a good architecture meet the needs of its users, developers,
and owners at a given point in time, but it also meets them over time.
<small>If you think good architecture is expensive, try bad architecture.</small>
—Brian Foote and Joseph Yoder
The kinds of changes a system’s development typically experiences should not be the changes that are
costly, that are hard to make, that take managed projects of their own rather than being folded into the
daily and weekly flow of work.
That point leads us to a not-so-small physics-related problem: time travel. How do we know what
those typical changes will be so that we can shape those significant decisions around them? How do
we reduce future development effort and cost without crystal balls and time machines?
<small>Architecture is the decisions that you wish you could get right early in a project, but that you are not necessarily more
likely to get them right than any other.</small>
—Ralph Johnson

Understanding the past is hard enough as it is; our grasp of the present is slippery at best; predicting
the future is nontrivial.
This is where the road forks many ways.
Down the darkest path comes the idea that strong and stable architecture comes from authority and
rigidity. If change is expensive, change is eliminated—its causes subdued or headed off into a
bureaucratic ditch. The architect’s mandate is total and totalitarian, with the architecture becoming a
dystopia for its developers and a constant source of frustration for all.
Down another path comes a strong smell of speculative generality. A route filled with hard-coded
guesswork, countless parameters, tombs of dead code, and more accidental complexity than you can
shake a maintenance budget at.The path we are most interested is the cleanest one. It recognizes the softness of software and aims to
preserve it as a first-class property of the system. It recognizes that we operate with incomplete
knowledge, but it also understands that, as humans, operating with incomplete knowledge is
something we do, something we’re good at. It plays more to our strengths than to our weaknesses. We
create things and we discover things. We ask questions and we run experiments. A good architecture
comes from understanding it more as a journey than as a destination, more as an ongoing process of
enquiry than as a frozen artifact.
<small>Architecture is a hypothesis, that needs to be proven by implementation and measurement.</small>
—Tom Gilb
To walk this path requires care and attention, thought and observation, practice and principle. This
might at first sound slow, but it’s all in the way that you walk.
<small>The only way to go fast, is to go well.</small>
—Robert C. Martin
Enjoy the journey.
—Kevlin Henney
May 2017


# PREFACE

The title of this book is Clean Architecture. That’s an audacious name. Some would even call it
arrogant. So why did I choose that title, and why did I write this book?
I wrote my very first line of code in 1964, at the age of 12. The year is now 2016, so I have been
writing code for more than half a century. In that time, I have learned a few things about how to
structure software systems—things that I believe others would likely find valuable.
I learned these things by building many systems, both large and small. I have built small embedded
systems and large batch processing systems. I have built real-time systems and web systems. I have
built console apps, GUI apps, process control apps, games, accounting systems, telecommunications
systems, design tools, drawing apps, and many, many others.
I have built single-threaded apps, multithreaded apps, apps with few heavy-weight processes, apps
with many light-weight processes, multiprocessor apps, database apps, mathematical apps,
computational geometry apps, and many, many others.
I’ve built a lot of apps. I’ve built a lot of systems. And from them all, and by taking them all into
consideration, I’ve learned something startling.
The architecture rules are the same!
This is startling because the systems that I have built have all been so radically different. Why should
such different systems all share similar rules of architecture? My conclusion is that the rules of
software architecture are independent of every other variable.
This is even more startling when you consider the change that has taken place in hardware over the
same half-century. I started programming on machines the size of kitchen refrigerators that had half-
megahertz cycle times, 4K of core memory, 32K of disk memory, and a 10 character per second
teletype interface. I am writing this preface on a bus while touring in South Africa. I am using a
MacBook with four i7 cores running at 2.8 gigahertz each. It has 16 gigabytes of RAM, a terabyte of
SSD, and a 2880×1800 retina display capable of showing extremely high-definition video. The
difference in computational power is staggering. Any reasonable analysis will show that this
MacBook is at least 10 22 more powerful than those early computers that I started using half a century
ago.
Twenty-two orders of magnitude is a very large number. It is the number of angstroms from Earth to
Alpha-Centuri. It is the number of electrons in the change in your pocket or purse. And yet that number—that number at least—is the computational power increase that I have experienced in my own
lifetime.
And with all that vast change in computational power, what has been the effect on the software I
write? It’s gotten bigger certainly. I used to think 2000 lines was a big program. After all, it was a full
box of cards that weighed 10 pounds. Now, however, a program isn’t really big until it exceeds
100,000 lines.
The software has also gotten much more performant. We can do things today that we could scarcely
dream about in the 1960s. The Forbin Project, The Moon Is a Harsh Mistress, and 2001: A Space
Odyssey all tried to imagine our current future, but missed the mark rather significantly. They all
imagined huge machines that gained sentience. What we have instead are impossibly small machines
that are still ... just machines.{xx}
And there is one thing more about the software we have now, compared to the software from back
then: It’s made of the same stuff. It’s made of if statements, assignment statements, and while loops.
Oh, you might object and say that we’ve got much better languages and superior paradigms. After all,
we program in Java, or C#, or Ruby, and we use object-oriented design. True—and yet the code is
still just an assemblage of sequence, selection, and iteration, just as it was back in the 1960s and
1950s.
When you really look closely at the practice of programming computers, you realize that very little
has changed in 50 years. The languages have gotten a little better. The tools have gotten fantastically
better. But the basic building blocks of a computer program have not changed.
If I took a computer programmer from 1966 forward in time to 2016 and put her 1 in front of my
MacBook running IntelliJ and showed her Java, she might need 24 hours to recover from the shock.
But then she would be able to write the code. Java just isn’t that different from C, or even from
Fortran.
And if I transported you back to 1966 and showed you how to write and edit PDP-8 code by punching
paper tape on a 10 character per second teletype, you might need 24 hours to recover from the
disappointment. But then you would be able to write the code. The code just hasn’t changed that
much.
That’s the secret: This changelessness of the code is the reason that the rules of software architecture
are so consistent across system types. The rules of software architecture are the rules of ordering and
assembling the building blocks of programs. And since those building blocks are universal and
haven’t changed, the rules for ordering them are likewise universal and changeless.
Younger programmers might think this is nonsense. They might insist that everything is new and
different nowadays, that the rules of the past are past and gone. If that is what they think, they are
sadly mistaken. The rules have not changed. Despite all the new languages, and all the new
frameworks, and all the paradigms, the rules are the same now as they were when Alan Turing wrote
the first machine code in 1946.But one thing has changed: Back then, we didn’t know what the rules were. Consequently, we broke
them, over and over again. Now, with half a century of experience behind us, we have a grasp of
those rules.
And it is those rules—those timeless, changeless, rules—that this book is all about.


# PART I 
# INTRODUCTION

It doesn’t take a huge amount of knowledge and skill to get a program working. Kids in high school
do it all the time. Young men and women in college start billion-dollar businesses based on
scrabbling together a few lines of PHP or Ruby. Hoards of junior programmers in cube farms around
the world slog through massive requirements documents held in huge issue tracking systems to get
their systems to “work” by the sheer brute force of will. The code they produce may not be pretty; but
it works. It works because getting something to work—once—just isn’t that hard.
Getting it right is another matter entirely. Getting software right is hard. It takes knowledge and skills
that most young programmers haven’t yet acquired. It requires thought and insight that most
programmers don’t take the time to develop. It requires a level of discipline and dedication that most
programmers never dreamed they’d need. Mostly, it takes a passion for the craft and the desire to be a
professional.
And when you get software right, something magical happens: You don’t need hordes of programmers
to keep it working. You don’t need massive requirements documents and huge issue tracking systems.
You don’t need global cube farms and 24/7 programming.
When software is done right, it requires a fraction of the human resources to create and maintain.
Changes are simple and rapid. Defects are few and far between. Effort is minimized, and functionality
and flexibility are maximized.
Yes, this vision sounds a bit utopian. But I’ve been there; I’ve seen it happen. I’ve worked in projects
where the design and architecture of the system made it easy to write and easy to maintain. I’ve
experienced projects that required a fraction of the anticipated human resources. I’ve worked on
systems that had extremely low defect rates. I’ve seen the extraordinary effect that good software
architecture can have on a system, a project, and a team. I’ve been to the promised land.
But don’t take my word for it. Look at your own experience. Have you experienced the opposite?
Have you worked on systems that are so interconnected and intricately coupled that every change,
regardless of how trivial, takes weeks and involves huge risks? Have you experienced the impedance
of bad code and rotten design? Has the design of the systems you’ve worked on had a huge negative
effect on the morale of the team, the trust of the customers, and the patience of the managers? Have
you seen teams, departments, and even companies that have been brought down by the rotten structure
of their software? Have you been to programming hell?I have—and to some extent, most of the rest of us have, too. It is far more common to fight your way
through terrible software designs than it is to enjoy the pleasure of working with a good one.

## Chapter 1
## WHAT IS DESIGN AND ARCHITECTURE ?

## Chapter 2
## A TALE OF TWO VALUES

---

# PART II 
# STARTING WITH THE BRICKS : PROGRAMMING PARADIGMS 从基础开始：编程范式

## Chapter 3
## PARADIGM OVERVIEW 范式概述


## Chapter 4
## STRUCTURED PROGRAMMING 结构化程序设计

## Chapter 5
## OBJECT-ORIENTED PROGRAMMING 面向对象程序设计

## Chapter 6
## FUNCTIONAL PROGRAMMING 函数式程序设计

---

# PART III
# DESIGN PRINCIPLES 设计原则

## Chapter 7
## SRP: THE SINGLE RESPONSIBILITY PRINCIPLE 单一责任原则
## Chapter 8
## OCP: THE OPEN-CLOSED PRINCIPLE 开放原则
## Chapter 9
## LSP: THE LISKOV SUBSTITUTION PRINCIPLE LISKOV替代原则
## Chapter 10
## ISP: THE INTERFACE SEGREGATION PRINCIPLE 接口隔离原则
## Chapter 11
## DIP: THE DEPENDENCY INVERSION PRINCIPLE 依赖反转原则

---

# PART IV
# COMPONENT PRINCIPLES 组件原则

## Chapter 12
## COMPONENTS 组件
## Chapter 13
## COMPONENT COHESION 组件衔接
## Chapter 14
## COMPONENT COUPLING 组件耦合

---

# PART V
# ARCHITECTURE 架构

## Chapter 15
## WHAT IS ARCHITECTURE ? 什么是架构？
## Chapter 16
## INDEPENDENCE 独立
## Chapter 17
## BOUNDARIES : DRAWING LINES 边界：绘制线
## Chapter 18
## BOUNDARY ANATOMY 边界解剖
## Chapter 19
## POLICY AND LEVEL 政策和水平
## Chapter 20
## BUSINESS RULES 商业规则
## Chapter 21
## SCREAMING ARCHITECTURE 
## Chapter 22
## THE CLEAN ARCHITECTURE 简洁的架构
## Chapter 23
## PRESENTERS AND HUMBLE OBJECTS
## Chapter 24
## PARTIAL BOUNDARIES 部分边界
## Chapter 25
## LAYERS AND BOUNDARIES 层和边界
## Chapter 26
## THE MAIN COMPONENT 主要组成部分
## Chapter 27
## SERVICES: GREAT AND SMALL 服务：伟大而小巧
## Chapter 28
## THE TEST BOUNDARY 测试边界
## Chapter 29
## CLEAN EMBEDDED ARCHITECTURE 简洁嵌入式建筑

---

# PART VI
# DETAILS

---

# PART VII
# Appendix

---

