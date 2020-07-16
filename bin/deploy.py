# coding:utf-8

import os
import sys
import re
import shutil

if len(sys.argv) < 2:
    print('missing version')
    exit()

version = sys.argv[1]

if len(sys.argv) < 3:
    print('missing local maven repository path')
    exit()

project_repository_path = sys.argv[2]

if not os.path.exists(project_repository_path):
    print('local maven repository path not found')
    exit()

os.chdir('..')
if not os.path.exists('../wetool'):
    print('wetool repository not exists, please clone it from gitee or github')
    exit()


def replace_version():
    with open('pom.xml', 'r+', encoding='utf-8') as fr:
        content = fr.read()

    with open('pom.xml', 'w+', encoding='utf-8') as fw:
        replaced = re.sub('<wetool.version>.*?</wetool.version>',
                          version.join(['<wetool.version>', '</wetool.version>']), content, 1)
        fw.write(replaced)


def copy_to(name):
    mvn_home = os.sep.join([os.path.expanduser('~'), '.m2',
                            'repository', 'org', 'code4everything', name, version])
    target_path = os.sep.join(
        [project_repository_path, 'org', 'code4everything', name, version])

    shutil.copytree(mvn_home, target_path)


# 拉取代码
print(os.popen('git pull').read())
cwd = os.getcwd()
os.chdir(project_repository_path)
print(os.popen('git pull').read())
os.chdir(cwd)

# 发布support
os.chdir('./wetool-plugin-support')
print('deploy wetool-plugin-support...\r\n')
replace_version()
print(os.popen('mvn clean deploy').read())
copy_to('wetool-plugin-support')

# 切至wetool拉取代码，注释掉maven打包所有依赖的插件
os.chdir('../../wetool')
print(os.popen('git pull').read())

replace_version()

fr = open('./pom.xml', 'r', encoding='utf-8')
content = fr.read()
fr.close()

fw = open('./pom.xml', 'w', encoding='utf-8')
fw.write(content.replace('<plugin>', '<!--', 1).replace('</plugin>', '-->', 1))
fw.close()

# 发布wetool，恢复pom文件
print('deploy wetool...\r\n')
print(os.popen('mvn clean deploy').read())
copy_to('wetool')
fw = open('./pom.xml', 'w', encoding='utf-8')
fw.write(content)
fw.close()

# 提交wetool git记录
print(os.popen('git add .').read())
print(os.popen('git commit -m "shell release v%s"' % version).read())

# 发布test
print('deploy wetool-plugin-test...\r\n')
replace_version()
os.chdir('../wetool-plugin/wetool-plugin-test')
print(os.popen('mvn clean deploy').read())
copy_to('wetool-plugin-test')

# 提交wetool plugin git记录
print(os.popen('git add .').read())
print(os.popen('git commit -m "shell release v%s"' % version).read())

os.chdir(project_repository_path)
print(os.popen('git add .').read())
print(os.popen('git commit -m "shell release %s v%s"' %
               ('wetool', version)).read())
