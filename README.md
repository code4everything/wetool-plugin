## WeTool插件支持

这是一个WeTool插件支持库，开发极简单（前提你会JavaFX），简单几步即可实现插件的开发，当然正是因为简单，也因为本人水平极为有限，
所以插件库可能还存在诸多不完善的地方，如想到还望告知，能完善那么一星半点的不足也甚好，在此谢过大家了

### 仓库说明

- 插件基础支持库：[wetool-plugin-support](wetool-plugin-support)
- 插件开发测试支持库：[wetool-plugin-test](wetool-plugin-test)
- 插件开发示例：[wetool-plugin-sample](wetool-plugin-sample)

### 开发须知

- 安装有Java8及以上版本（必须）
- 了解JavaFX图形化技术（必须）
- 安装有Lombok工具（必须）
- 安装有Maven、Git开发环境（必须）
- 安装了SceneBuilder 2.0以上版本（建议）

### Maven

个人Maven仓库

``` xml
<repository>
    <id>ease-maven</id>
    <url>https://code4everything.gitee.io/repository/maven</url>
</repository>
```

插件基础支持库

``` xml
<dependency>
    <groupId>org.code4everything</groupId>
    <artifactId>wetool-plugin-support</artifactId>
    <version>1.0.1</version>
</dependency>
```

插件测试支持库

``` xml
<dependency>
    <groupId>org.code4everything</groupId>
    <artifactId>wetool-plugin-test</artifactId>
    <version>1.0.1</version>
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

### 参与贡献

欢迎提Issue、Pull Request，参与开发，如果觉得项目还不错，欢迎Star哦~

> 使用插件前请确保已安装[`wetool.jar`](https://gitee.com/code4everything/wetool)
