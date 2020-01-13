## 接口参考文档

### 配置信息

获取用户的配置，第一个字符串类型的参数为`Alibaba Fast Json`中的`Path`：[路径语法参考](https://github.com/alibaba/fastjson/wiki/JSONPath)

```java
T config = WeUtils.getConfig().getConfig(String, Class<T>);
// 或者
Object config = WeUtils.getConfig().getConfig(String);
```

### Bean工厂

```java
// 注册单例的Bean
BeanFactory.register(T);

// 注册视图Bean（同样也是单例Bean），参数说明：tabId，tabName，视图控制器
BeanFactory.registerView(String, String, BaseViewController);

// 注册多例Bean，参数说明：beanName，Bean实例
BeanFactory.register(String, Object);
```

[更多方法请参考](src/main/java/org/code4everything/wetool/plugin/support/factory/BeanFactory.java)

### 仅适用本插件库的特定工具类

[WeUtils](src/main/java/org/code4everything/wetool/plugin/support/util/WeUtils.java)

[FxUtils](src/main/java/org/code4everything/wetool/plugin/support/util/FxUtils.java)

```java
// 打开选项卡，参数说明：视图内容，自定义tabId，自定义tabName
FxUtils.openTab(Node, String, String);

// 获取当前运行的 TabPane
FxUtils.getTabPane();

// 获取当前运行的 Stage
FxUtils.getStage();

// 用系统默认软件打开文件
FxUtils.openFile(File);

// 加载视图，参数说明：WePluginSupporter实现类，视图在classpath中路径，是否缓存
FxUtils.loadFxml(Class<?>, String, boolean);

// 创建菜单
FxUtils.createMenuItem(String, EventHandler<ActionEvent>);
FxUtils.createMenuItem(String, ActionListener);

// 名称唯一的菜单，并添加至插件菜单
FxUtils.makePluginMenu(String)
```
  
[FxDialogs](src/main/java/org/code4everything/wetool/plugin/support/util/FxDialogs.java)

```text
FxDialogs.showDialog
FxDialogs.showChoice
FxDialogs.showTextInput
FxDialogs.showSuccess
FxDialogs.showInformation
FxDialogs.showError
FxDialogs.showException
```
