## 一分钟快速了解插件开发

1. 新建一个Maven项目，例如：`wetool-plugin-sample`

2. 在`pom.xml`文件中`project`节点下添加如下内容：

    ```xml
    <properties>
        <java.veresion>11</java.veresion>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <wetool.version>1.5.0</wetool.version>
        <junit.version>4.13.1</junit.version>
        <lombok.version>1.18.16</lombok.version>
    </properties>

    <!--私有仓库-->
    <repositories>
        <repository>
            <id>ease-maven</id>
            <url>https://gitee.com/code4everything/repository/raw/master/maven</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.code4everything</groupId>
            <artifactId>wetool-plugin-support</artifactId>
            <version>${wetool.version}</version>
            <optional>true</optional>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.code4everything</groupId>
            <artifactId>wetool-plugin-test</artifactId>
            <version>${wetool.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <forceJavacCompilerUse>true</forceJavacCompilerUse>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    ```

3. 在`resources`目录下新建一个`Hello Word`视图，例如：`/ease/sample/Sample.fxml`，这里我们需要保证路径的唯一性，至少应该增加两个父级目录，第一级文件夹用`AuthorName`命名，第二级文件夹用`AppName`命名（如果没有界面可跳过本步骤）

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    
    
    <?import javafx.geometry.Insets?>
    <?import javafx.scene.control.Label?>
    <?import javafx.scene.control.TextArea?>
    <?import javafx.scene.layout.*?>
    <?import javafx.scene.text.Font?>
    <VBox prefHeight="300.0" prefWidth="500.0" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1"
          fx:controller="org.code4everything.wetool.plugin.sample.controller.SampleController">
        <HBox prefHeight="100.0" prefWidth="200.0" VBox.vgrow="NEVER">
            <Label alignment="CENTER" maxHeight="1.7976931348623157E308" maxWidth="1.7976931348623157E308"
                   text="Hello WeTool" HBox.hgrow="ALWAYS">
                <font>
                    <Font size="64.0"/>
                </font>
            </Label>
        </HBox>
        <HBox prefHeight="100.0" prefWidth="200.0" VBox.vgrow="ALWAYS">
            <TextArea fx:id="textArea" prefHeight="200.0" prefWidth="200.0" HBox.hgrow="ALWAYS">
                <padding>
                    <Insets bottom="10.0" left="10.0" right="10.0" top="10.0"/>
                </padding>
            </TextArea>
        </HBox>
    </VBox>
    ```

4. 新建视图中绑定的控制器（如果没有界面可跳过本步骤），例如：`org.code4everything.wetool.plugin.sample.controller.SampleController`

    实现`BaseViewController`并将其注册至`BeanFactory`，可以获取`WeTool`的一些能力，比如：用户点击了打开文件、保存文件等
    
    ```java
    public class SampleController implements BaseViewController {
    
        /**
         * 自定义tabId，用来防止与其他插件发生名称冲突
         */
        public static final String TAB_ID = "ease-sample";
    
        /**
         * 自定义tabName，Tab选项卡的标题
         */
        public static final String TAB_NAME = "插件示例";
    
        @FXML
        public TextArea textArea;
    
        @FXML
        private void initialize() {
            BeanFactory.registerView(TAB_ID, TAB_NAME, this);
        }
    
        @Override
        public void setFileContent(String content) {
            textArea.setText(content);
        }
    
        @Override
        public String getSavingContent() {
            return textArea.getText();
        }
    }
    ```

5. 新建一个实现了`org.code4everything.wetool.plugin.support.WePluginSupporter`接口的类，例如：`org.code4everything.wetool.plugin.sample.WetoolSupporter`

    ```java
    @Slf4j
    public class WetoolSupporter implements WePluginSupporter {
    
        /**
         * 初始化操作
         *
         * @return 初始化是否成功，返回true时继续加载插件，否则放弃加载
         */
        @Override
        public boolean initialize() {
            log.info("initialize sample plugin");
            return true;
        }
    
        /**
         * 注册插件到主界面菜单，可返回NULL，可不实现此方法
         *
         * @return 返回的 {@link MenuItem} 将被添加到主界面的插件菜单
         */
        @Override
        public MenuItem registerBarMenu() {
            final MenuItem item = new MenuItem("插件示例");
            // 自定义事件监听
            item.setOnAction(e -> {
                // 注意保证fxml文件的url路径唯一性
                Node node = FxUtils.loadFxml(WetoolSupporter.class, "/ease/sample/Sample.fxml", true);
                FxDialogs.showInformation(SampleController.TAB_NAME, "welcome to wetool plugin");
                FxUtils.openTab(node, SampleController.TAB_ID, SampleController.TAB_NAME);
            });
            return item;
        }
    
        /**
         * 注册插件到系统托盘菜单，可返回NULL，可不实现此方法
         *
         * @return 返回的 {@link MenuItem} 将被添加到系统托盘的插件菜单
         */
        @Override
        public java.awt.MenuItem registerTrayMenu() {
            final java.awt.MenuItem item = new java.awt.MenuItem("插件示例");
            // 自定义事件监听
            item.addActionListener(e -> FxDialogs.showInformation(SampleController.TAB_NAME, "welcome to wetool plugin"));
            return item;
        }
    
        /**
         * 注册成功之后的回调
         */
        @Override
        public void registered(WePluginInfo info, MenuItem barMenu, java.awt.MenuItem trayMenu) {
            log.info("plugin sample registered success");
        }
    }
    ```

6. 在`resources`目录下新建`plugin.json`文件，并根据[`WePluginInfo`](wetool-plugin-support/src/main/java/org/code4everything/wetool/plugin/support/config/WePluginInfo.java)类中的属性进行配置，
例如：

    `requireWetoolVersion`指依赖`wetool-plugin-support`的版本，`supportedClass`表示实现了接口`WePluginSupporter`的类全名
    ``` json
    {
        "author": "ease",
        "name": "sample",
        "version": "1.5.0",
        "requireWetoolVersion": "1.5.0",
        "supportedClass": "org.code4everything.wetool.plugin.sample.WetoolSupporter"
    }
    ```

7. 在`test`目录下新建一个可运行调试的测试类，例如：`org.code4everything.wetool.plugin.sample.SampleTest`

    ``` java
    public class SampleTest {
    
        public static void main(String[] args) {
            WetoolTester.runTest(args);
        }
    }
    ```
   
   > 运行`SampleTest`类即可调试运行

8. 发布，使用`mvn package`打包后，将`jar`包拖入您`wetool.jar`的同级目录的`plugins`目录下即可


> 本示例完整代码请参考：[wetool-plugin-sample](wetool-plugin-sample)

### 注意事项

1. 不要将 `hutool-core` 打包到插件 `jar` 包中了
