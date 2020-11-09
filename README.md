## WeTool插件支持

这是一个WeTool插件支持库，开发极简单，简单几步即可实现插件的快速开发。

### 仓库说明

- 插件基础支持库：[wetool-plugin-support](wetool-plugin-support)
- 插件开发测试支持库：[wetool-plugin-test](wetool-plugin-test)
- 插件开发示例：[wetool-plugin-sample](wetool-plugin-sample)

### 开发须知

- 安装有Java11及以上版本
- 安装有Lombok工具
- 安装有Maven、Git开发环境
- ~~了解JavaFX图形化技术~~
- ~~安装了SceneBuilder 2.0以上版本~~

### Maven

个人Maven仓库

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
    <version>1.2.0</version>
</dependency>
```

插件测试支持库

```xml
<dependency>
    <groupId>org.code4everything</groupId>
    <artifactId>wetool-plugin-test</artifactId>
    <version>1.2.0</version>
    <scope>test</scope>
</dependency>
```

### 插件开发

- [快速开始](quick_start.md)

- [参考文档](wetool-plugin-support/readme.md)

- [插件提交](wetool-plugin-repository/readme.md)

### 插件仓库

- [插件仓库](wetool-plugin-repository)

- [简易FTP客户端](wetool-plugin-repository/ease-ftp-client/readme.md)

- [七牛云对象存储管理工具](wetool-plugin-repository/ease-qiniu/readme.md)

- [简易FTP服务器](wetool-plugin-repository/ease-ftp-server/readme.md)

- [开发工具](wetool-plugin-repository/ease-devtool/readme.md)

- [Everywhere文件检索工具](wetool-plugin-repository/ease-everywhere/readme.md)

### 参与贡献

欢迎提Issue、Pull Request，参与开发，如果觉得项目还不错，欢迎Star哦~

> 使用插件前请确保已安装[`wetool.jar`](https://gitee.com/code4everything/wetool)
