## Ease Ftp Server

基于Apache Ftp Server实现FTP服务器

最新下载地址：[wetool-plugin-ftpserver-1.0.1.jar](http://share.qiniu.segocat.com/tool/wetool/plugin/wetool-plugin-ftpserver-1.0.1.jar)

### 配置说明

在WeTool工作目录下新增`ftp-server-config.json`文件，并完善配置，配置参考如下：

``` json
{
    "startOnStartup": true,
    "port": 21,
    "users": [
        {
            "name": "test",
            "password": "test",
            "homeDirectory": "",
            "enabled": true,
            "maxIdleTime": 0,
            "auth": "rw"
        }
    ]
}
```

### 历史更新

#### [v1.0.1](http://share.qiniu.segocat.com/tool/wetool/plugin/wetool-plugin-ftpserver-1.0.1.jar)

- 多用户监听配置
- 启动/停止FTP服务器
