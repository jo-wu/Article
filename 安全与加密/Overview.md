



常见的java加密算法有：

- BASE64 严格地说，属于编码格式，而非加密算法
- MD5(Message Digest algorithm 5，信息摘要算法)
- SHA(Secure Hash Algorithm，安全散列算法)
- HMAC(Hash Message Authentication Code，散列消息鉴别码)


密码破解方法：
暴力破解、彩虹表、查询表、逆向查询表、

对抗彩虹表：加盐哈希（ Hashing with Salt）

要素

- 盐值应该使用加密的安全伪随机数生成器（ Cryptographically Secure Pseudo-Random

Number Generator，CSPRNG ）产生。


- java.security.SecureRandom

## 存储密码的步骤：

1、使用 CSPRNG 生成足够长的随机盐值。
2、将盐值混入密码，并使用标准的密码哈希函数进行加密，如Argon2、 bcrypt 、 scrypt 或 PBKDF2 。
3、将盐值和对应的哈希值一起存入用户数据库。

## 校验密码的步骤：

1、从数据库检索出用户的盐值和对应的哈希值。
2、将盐值混入用户输入的密码，并且使用通用的哈希函数进行加密。
3、比较上一步的结果，是否和数据库存储的哈希值相同。如果它们相同，则表明密码是正确的；否则，该密码错误。


Web网络攻击：
XSS、CSRF、SQL注入

