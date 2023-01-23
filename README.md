# 云端同步

使用 [compose-jb](https://github.com/JetBrains/compose-jb)

    开发此软件的初心是为了方便我多设备间快速同步更新apk
    请勿拿来做除此以为的任何用途

    需要和服务端配合，以下是详细使用方法

网盘链接:https://xihan.lanzouf.com/b041olf2d 密码:9ya1
---

# 服务端

* 需要 **JDK11** 或以上

* 需要 [aapt](https://androidaapt.com/)、[7z](https://7-zip.org/) 工具

* 拉取源码后修改下图改成自己的配置

  ![s-0](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/s-0.png)

## Windows

* 下载网盘内或者上述链接中的 **aapt.exe** 移到 **C:\Windows\System32** 或 **自行配置环境变量**

* [7z](https://7-zip.org/) 下载后解压到自己熟悉的位置并配置环境变量(**ps:如何配置环境变量不在本文的范围**)

* 方法1

      第一步:Gradle -> server -> Tasks -> installDist
      会在 CloudSyncApk\server\build\install\server\bin 生成2个脚本
      Windows 配置好 "Java环境变量" 可直接运行 "server.bat"
      如需部署到服务器 把整个 "install" 打包移动
      具体如图

  ![s-1](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/s-1.png)
  ![s-3](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/s-3.png)

* 方法2

        第一步:构建 -> 构建工件 -> server 
      会 CloudSyncApk\out\artifacts\CloudSyncApk_server_main_jar 生成一个 jar 
      如需部署到服务器 把这个移动到指定位置
      运行即可
      具体如图

  ![s-2](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/s-2.png)
  ![s-4](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/s-4.png)

## Linux

* 下载网盘内或者上述链接中的aapt并移到 **/usr/bin** 授予 **777** 权限


* 方法如1同 **Windows** 方法1一样

        CloudSyncApk\server\build\install\server\bin 无后缀的脚本

* 方法2如同 **Windows** 方法2一样

##  Debian

* 安装 **aapt** 执行命令: **apt-get install aapt**

* 安装 **7z** 执行命令: **apt-get install 7z**

* 其他如同Linux一致

## Android

* 额外需要 [Termux](https://f-droid.org/zh/packages/com.termux/) 配合

* 下载安装后 安装 **JDK17**

      在终端内粘贴并回车以下内容
      1.pkg install git

      2.git clone https://kgithub.com/EagleComrade/Termux-java.git

      3.chmod +x install.sh

      4.bash install.sh

      5.java -version

* 安装 **7z**

        pkg install p7zip

* **aapt** 移动至 **/data/data/com.termux/files/usr/bin**

* 其他和Linux差不多 不同的是打包后的文件移动到 **/data/data/com.termux/files/home** 下

---

# 客户端

* 修改下图为自己的配置

  ![c-1](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/c-1.png)

## Windows

* 如图执行编译出exe、msi等安装包，安装后使用，具体如图

![d-1](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/d-1.png)

## Android

* 如图可切换调试并执行编译出apk 安装后使用

![a-1](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/a-1.png)

## Linux

* 这个和 **Windows** 一致 只不过包是 deb

---

# 截图

![d-2](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/d-2.png)
![d-3](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/d-3.png)

![a-2](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/a-2.png)
![a-3](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/a-3.png)
![a-4](https://raw.fastgit.org/xihan123/CloudSyncApk/master/Screenshots/a-4.png)

