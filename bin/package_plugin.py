# coding:utf-8

import os
import re
import shutil

plugin_list = ['./wetool-plugin-devtool/wetool-plugin-devtool-java',
               './wetool-plugin-devtool/wetool-plugin-devtool-redis',
               './wetool-plugin-devtool/wetool-plugin-devtool-ssh',
               './wetool-plugin-devtool/wetool-plugin-devtool-utilities',
               './wetool-plugin-everywhere',
               './wetool-plugin-ftpclient',
               './wetool-plugin-ftpserver',
               './wetool-plugin-qiniu',
               './wetool-plugin-thirdparty/wetool-plugin-thirdparty-downloader']

os.chdir('..')
cwd = os.getcwd()
print(os.popen('git pull').read())

os.chdir('./wetool-plugin-support')
with open('./pom.xml', 'r', encoding='utf-8') as fr:
    res = re.search('<wetool.version>(.*?)</wetool.version>',
                    fr.read(), re.M | re.I)
    version = res.group(1)

os.chdir('..')
plugin_path = os.sep.join([cwd, 'plugins'])
if os.path.exists(plugin_path):
    shutil.rmtree(plugin_path)

os.mkdir(plugin_path)

for plugin in plugin_list:
    os.chdir(plugin)
    name = plugin[plugin.rfind('/')+1:]
    print('package plugin %s' % name)
    with open('./pom.xml', 'r', encoding='utf-8') as fr:
        content = fr.read()
    with open('./pom.xml', 'w', encoding='utf-8') as fw:
        fw.write(re.sub('<wetool.version>.*?</wetool.version>',
                        version.join(['<wetool.version>', '</wetool.version>']), content, 1))
    print(os.popen('mvn clean package').read())
    shutil.copyfile(os.path.sep.join([os.getcwd(), 'target', '%s-%s.jar' % (name, version)]),
                    os.path.sep.join([plugin_path, '%s-%s.jar' % (name, version)]))
    os.chdir(cwd)

print(os.popen('git add .').read())
print(os.popen('git commit -m "shell package plugin %s"' % version).read())
