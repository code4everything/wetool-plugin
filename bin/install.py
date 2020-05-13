# coding:utf-8

import os

os.chdir('..')
if not os.path.exists('../wetool'):
    print('wetool repository not exists, please clone it from gitee or github')
    exit()

# 拉取代码
print(os.popen('git pull').read())

# 安装support
os.chdir('./wetool-plugin-support')
print('install wetool-plugin-support...\r\n')
print(os.popen('mvn clean install').read())

# 切至wetool拉取代码，注释掉maven打包所有依赖的插件
os.chdir('../../wetool')
print(os.popen('git pull').read())

fr = open('./pom.xml', 'r', encoding='utf-8')
content = fr.read()
fr.close()

fw = open('./pom.xml', 'w', encoding='utf-8')
fw.write(content.replace('<plugin>', '<!--', 1).replace('</plugin>', '-->', 1))
fw.close()

# 打包wetool，恢复pom文件
print('install wetool...\r\n')
print(os.popen('mvn clean install').read())
fw = open('./pom.xml', 'w', encoding='utf-8')
fw.write(content)
fw.close()

# 打包test
print('install wetool-plugin-test...\r\n')
os.chdir('../wetool-plugin/wetool-plugin-test')
print(os.popen('mvn clean install').read())
