## Ease Ftp Server

基于Apache Ftp Server实现FTP服务器

最新下载地址：[wetool-plugin-ftpserver-1.1.0.jar](http://share.qiniu.segocat.com/tool/wetool/plugin/wetool-plugin-ftpserver-1.1.0.jar)

### 配置说明

在WeTool工作目录的`conf`目录下新增`ftp-server-config.json`文件，并完善配置，配置参考如下：

```json
{
    /*初始化时是否启动FTP服务*/
    "startOnStartup": true,
    /*监听的端口*/
    "port": 21,
    "users": [
        {
            "name": "test",
            "password": "test",
            /*监听的目录*/
            "homeDirectory": "",
            /*是否启动*/
            "enabled": true,
            "maxIdleTime": 0,
            /*用户权限：r, w, rw*/
            "auth": "rw"
        }
    ]
}
```

### 历史更新

#### [v1.1.0](http://share.qiniu.segocat.com/tool/wetool/plugin/wetool-plugin-ftpserver-1.1.0.jar)

- 多更新WeTool版本

#### [v1.0.2](http://share.qiniu.segocat.com/tool/wetool/plugin/wetool-plugin-ftpserver-1.0.2.jar)

- 迁移配置文件路径
- 多更新WeTool版本

#### [v1.0.1](http://share.qiniu.segocat.com/tool/wetool/plugin/wetool-plugin-ftpserver-1.0.1.jar)

- 多用户监听配置
- 启动/停止FTP服务器
