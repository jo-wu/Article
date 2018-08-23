# Flutter Commands

### 当前目录下使用
> ./flutter/bin/


### 查看版本信息
> flutter --version

### 更新
> flutter upgrade

### 检查依赖
> flutter doctor

### 添加环境变量

````
sed -i '$a 
export PUB_HOSTED_URL=https://pub.flutter-io.cn 
#国内用户需要设置
export FLUTTER_STORAGE_BASE_URL=https://storage.flutter-io.cn 
#国内用户需要设置
export PATH= flutter所在目录/flutter/bin:$PATH
' ~/.bashrc

source ~/.bashrc
````


（1）/etc/profile： 此文件为系统的每个用户设置环境信息,当用户第一次登录时,该文件被执行. 并从/etc/profile.d目录的配置文件中搜集shell的设置。

（2）/etc/environment：是设置整个系统的环境，而/etc/profile是设置所有用户的环境，前者与登录用户无关，后者与登录用户有关。

（3）/etc/bashrc: 为每一个运行bash shell的用户执行此文件.当bash shell被打开时,该文件被读取。 

（4）~/.bash_profile: 每个用户都可使用该文件输入专用于自己使用的shell信息,当用户登录时,该文件仅仅执行一次!默认情况下,他设置一些环境变量,执行用户的.bashrc文件。 

（5）~/.bashrc: 该文件包含专用于你的bash shell的bash信息,当登录时以及每次打开新的shell时,该该文件被读取。

### 获取Android Studio
> wget https://dl.google.com/dl/android/studio/ide-zips/3.1.4.0/android-studio-ide-173.4907809-linux.zip


### 获取Android SDK Tool
> wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz?utm_source=androiddevtools&utm_medium=website

### Android SDK Tool环境变量配置
````
export ANDROID_SDK_HOME=/home/jowu/Android/Sdk/android-sdk-linux
export PATH=$PATH:${ANDROID_SDK_HOME}/tools
export PATH=$PATH:${ANDROID_SDK_HOME}/platform-tools
````

### KVM

> sudo apt-get install qemu-kvm qemu-system libvirt-bin  bridge-utils
> sudo apt-get install virt-manager python-spice-client-gtk


### Android SDK 在线更新镜像服务器资源：
大连东软信息学院镜像服务器地址:
- http://mirrors.neusoft.edu.cn 端口：80
北京化工大学镜像服务器地址:
- IPv4: http://ubuntu.buct.edu.cn/ 端口：80
- IPv4: http://ubuntu.buct.cn/ 端口：80
- IPv6: http://ubuntu.buct6.edu.cn/ 端口：80
上海GDG镜像服务器地址:
- http://sdk.gdgshanghai.com 端口：8000


