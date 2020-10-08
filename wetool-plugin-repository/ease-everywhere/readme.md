### Everywhere For WeTool

基于ApacheLucene搜索框架实现的文件检索工具，通过Lucene语法可搜索路径，文件名，部分文件内容（需配置）

最新下载地址：[wetool-plugin-everywhere-1.1.2.jar](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-everywhere-1.1.2.jar)

文件配置完成后，点击重载配置，程序将重新创建索引（比较耗时），文件配置说明如下：

```json
{
    /*不创建内容索引的文件正则表达式，排序不需要内容检索的文件，提高检索速度*/
    "excludePatterns": [
        "^C:\\\\Windows",
        "^C:\\\\ProgramData",
        "^C:\\\\Program Files",
        "^C:\\\\PerfLogs"
    ],
    /*是否忽略隐藏的文件*/
    "ignoreHiddenFile": true,
    /*创建内容索引的文件正则表达式*/
    "includePatterns": [
        "java$",
        "xml$",
        "json$"
    ],
    /*内容索引的文件最大大小，内容索引的文件应尽量小，超过该大小的文件不会创建内容索引，单位：字节*/
    "sizeLimit": "100,000",
    /*是否创建文件内容索引（全局），此索引比较耗时，如不需要检索文件内容可设置为false*/
    "indexContent": true,
    /*索引的过期时间，单位：分钟*/
    "indexExpire": 1440
}
```

### 运行截图

![everywhere](images/everywhere.png)

### Lucene常用语法

|符号|说明|示例|
|---|---|---|
|?|匹配单个任意字符|te?t|
|*|匹配多个任意字符|t*t|
|~|模糊匹配|test~|
|^|权重，越大说明相关度越高|test^10|

目前Lucene用到的特殊字符`+ – && || ! ( ) { } [ ] ^ ” ~ * ? : /`，搜索特殊字符时，需要`/`进行转义，例如：`()/` => `/(/)//`

> [Lucene语法官方文档](https://lucene.apache.org/core/2_9_4/queryparsersyntax.html)


### 更新历史

#### [v1.1.2](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-everywhere-1.1.2.jar)

- 更新WeTool版本

#### [v1.1.1](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-everywhere-1.1.1.jar)

- 更新WeTool版本

#### [v1.1.0](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-everywhere-1.1.0.jar)

- 更新WeTool版本

#### [v1.0.2](http://share.qiniu.easepan.xyz/tool/wetool/plugin/wetool-plugin-everywhere-1.0.2.jar)

- 支持检索文件、文件夹、文件内容
- 支持正则表达式过滤文件
