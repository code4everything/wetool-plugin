## Ease FTP

一个小而简、功能全的WeTool FTP插件

最新版本下载地址：[wetool-plugin-ftp-1.0.0.jar]()

### 配置说明

在你的`WeTool`配置文件中，加入`easeFtp`配置属性，并配置至少一个`ftp`服务器，插件即可正常加载

``` json
{
    "easeFtp": {
        "showOnStartup": false,
        "ftps": [
            {
                "name": "EaseFtp",
                "host": "127.0.0.1",
                "port": 21,
                "anonymous": false,
                "username": "root",
                "password": "root",
                "charset": "utf-8",
                "select": true,
                "lazyConnect": true
            }
        ]
    }
}
```

### 历史更新

#### [v1.0.0]()

- 多FTP配置
- 创建目录，上传下载，删除文件
- 复制文件链接
