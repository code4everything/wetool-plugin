package org.code4everything.wetool.plugin.devtool.java.loader;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;

/**
 * @author pantao
 * @since 2019/10/15
 */
public class DevClassLoader extends ClassLoader {

    private String classFile;

    private String baseFolder;

    public DevClassLoader(String baseFolder, String classFile) {
        this.baseFolder = StrUtil.addSuffixIfNot(baseFolder, File.separator);
        this.classFile = classFile;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        if (StrUtil.isEmpty(name)) {
            byte[] bytes = FileUtil.readBytes(classFile);
            String className = StrUtil.removeSuffix(StrUtil.removePrefix(classFile, baseFolder), ".class");
            clazz = this.defineClass(className.replace(File.separator, "."), bytes, 0, bytes.length);
        } else {
            clazz = getParent().loadClass(name);
        }
        return clazz;
    }
}
