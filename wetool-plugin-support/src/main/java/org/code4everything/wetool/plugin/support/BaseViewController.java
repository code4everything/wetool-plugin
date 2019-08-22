package org.code4everything.wetool.plugin.support;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import javafx.scene.input.DragEvent;

import java.io.File;
import java.util.List;

/**
 * @author pantao
 * @since 2019/7/4
 **/
public interface BaseViewController {

    /**
     * 用户点击了批量打开文件
     *
     * @param files 文件列表
     *
     * @since 1.5.0
     */
    default void openMultiFiles(List<File> files) {}

    /**
     * 用户点击了打开文件，自定义文件读取，本方法与 {@link #setFileContent(String)} 方法二者实现其一即可
     *
     * @param file 文件
     *
     * @since 1.5.0
     */
    default void openFile(File file) {
        setFileContent(FileUtil.readUtf8String(file));
    }

    /**
     * 用户点击了打开文件，以UTF8编码（默认）读取用户打开的文件，如文件为其他编码或需要自定义读取文件，请实现 {@link #openFile(File)} 方法
     *
     * @param content 文件内容（UTF8）
     *
     * @since 1.5.0
     */
    default void setFileContent(String content) {}

    /**
     * 用户点击了保存文件，自定义保存，本方法与 {@link #getSavingContent()} 方法二者实现其一即可
     *
     * @param file 将内容保存到指定的文件
     *
     * @since 1.5.0
     */
    default void saveFile(File file) {
        String content = getSavingContent();
        if (StrUtil.isNotEmpty(content)) {
            FileUtil.writeUtf8String(content, file);
        }
    }

    /**
     * 用户点击了保存文件，以UTF8的方式保存字符串，自定义保存请实现 {@link #saveFile(File)} 方法
     *
     * @return 需要保存的字符串
     *
     * @since 1.5.0
     */
    default String getSavingContent() {return "";}

    /**
     * 拖曳文件
     *
     * @param event 拖曳事件
     *
     * @since 1.5.0
     */
    default void dragFileOver(DragEvent event) {}

    /**
     * 拖曳文件
     *
     * @param event 拖曳事件
     *
     * @since 1.5.0
     */
    default void dragFileDropped(DragEvent event) {}
}
