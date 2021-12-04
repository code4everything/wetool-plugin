# coding:utf-8

import os
import re
import shutil
import sys

# 插件列表
plugin_list = ['./wetool-plugin-devtool/wetool-plugin-devtool-redis',
               './wetool-plugin-devtool/wetool-plugin-devtool-utilities',
               './wetool-plugin-everywhere',
               './wetool-plugin-ftpclient',
               './wetool-plugin-ftpserver',
               './wetool-plugin-qiniu',
               './wetool-plugin-dbops',
               './wetool-plugin-thirdparty/wetool-plugin-thirdparty-downloader']

# 切换目录，拉取代码
os.chdir('..')
cwd = os.getcwd()
print(os.popen('git pull').read())

# 获取最新版本号
os.chdir('./wetool-plugin-support')
with open('./pom.xml', 'r', encoding='utf-8') as fr:
    res = re.search('<wetool.version>(.*?)</wetool.version>',
                    fr.read(), re.M | re.I)
    version = res.group(1)

# 准备装插件的目录
os.chdir('..')
plugin_path = os.sep.join([cwd, 'plugins'])
if os.path.exists(plugin_path):
    shutil.rmtree(plugin_path)

os.mkdir(plugin_path)


def package(plugin):
    """
    打包插件
    """
    os.chdir(plugin)
    name = plugin[plugin.rfind('/')+1:]

    # 替换pom文件版本
    print('package plugin %s\r\n' % name)
    with open('./pom.xml', 'r', encoding='utf-8') as fr:
        content = fr.read()
    with open('./pom.xml', 'w', encoding='utf-8') as fw:
        fw.write(re.sub('<wetool.version>.*?</wetool.version>',
                        version.join(['<wetool.version>', '</wetool.version>']), content, 1))

    # 替换plugin.json版本号
    plugin_info_path = './src/main/resources/plugin.json'
    with open(plugin_info_path, 'r', encoding='utf-8') as fr:
        content = fr.read()
    with open(plugin_info_path, 'w', encoding='utf-8') as fw:
        content = re.sub('"version": ".*?"', '"version": "%s"' %
                         version, content, 1)
        content = re.sub('"requireWetoolVersion": ".*?"',
                         '"requireWetoolVersion": "%s"' % version, content, 1)
        fw.write(content)

    # 打包，并移动文件
    print(os.popen('mvn clean package').read())
    shutil.copyfile(os.path.sep.join([os.getcwd(), 'target', '%s-%s.jar' % (name, version)]),
                    os.path.sep.join([plugin_path, '%s-%s.jar' % (name, version)]))
    os.chdir(cwd)


for plugin in plugin_list:
    package(plugin)

# 提交git记录
print(os.popen('git add .').read())
print(os.popen('git commit -m "shell package plugin %s"' % version).read())

# 打包外部插件
for arg in sys.argv[1:]:
    path = '../%s-wetool-plugin' % arg
    os.chdir(path)
    print(os.popen('git pull').read())
    package(path)
    os.chdir(path)
    print(os.popen('git add .').read())
    print(os.popen('git commit -m "shell package %s"' % version).read())
    os.chdir(cwd)
