## WeTool插件支持

这是一个 [WeTool](https://gitee.com/code4everything/wetool) 插件支持库，开发极简单，简单几步即可实现插件的快速开发。

### [插件仓库](wetool-plugin-repository)

- [插件仓库](wetool-plugin-repository)
- [简易FTP客户端](wetool-plugin-repository/ease-ftp-client/readme.md)
- [七牛云对象存储管理工具](wetool-plugin-repository/ease-qiniu/readme.md)
- [简易FTP服务器](wetool-plugin-repository/ease-ftp-server/readme.md)
- [开发工具](wetool-plugin-repository/ease-devtool/readme.md)
- [Everywhere文件检索工具](wetool-plugin-repository/ease-everywhere/readme.md)
- [JavaQL脚本小程序](wetool-plugin-repository/ease-dbops/readme.md)

### 本仓库说明

- 插件基础支持库：[wetool-plugin-support](wetool-plugin-support)
- 插件开发测试支持库：[wetool-plugin-test](wetool-plugin-test)
- 插件开发示例：[wetool-plugin-sample](wetool-plugin-sample)

### 插件开发

- [快速开始](quick_start.md)
- [参考文档](wetool-plugin-support/readme.md)
- [插件提交](wetool-plugin-repository/readme.md)

### 开发须知

- 安装有Java11及以上版本
- 安装有Lombok工具
- 安装有Maven、Git开发环境
- ~~了解JavaFX图形化技术~~
- ~~安装了SceneBuilder 2.0以上版本~~

### 个人Maven仓库

仓库1

```xml
<repository>
    <id>ease-maven</id>
    <url>https://gitee.com/code4everything/repository/raw/master/maven</url>
</repository>
```

仓库2

```xml
<repository>
    <id>ease-maven</id>
    <url>https://code4everything.github.io/repository/maven</url>
</repository>
```

插件基础支持库

```xml
<dependency>
    <groupId>org.code4everything</groupId>
    <artifactId>wetool-plugin-support</artifactId>
    <version>1.5.0</version>
</dependency>
```

插件测试支持库

```xml
<dependency>
    <groupId>org.code4everything</groupId>
    <artifactId>wetool-plugin-test</artifactId>
    <version>1.5.0</version>
    <scope>test</scope>
</dependency>
```

### 参与贡献

欢迎大家提Issue、Pull Request，参与开发，一起完善本工具，如果觉得项目还不错，请Star哦~
