package com.ccexid.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件操作工具类
 * 提供文件删除、内容读写等常用文件操作
 *
 * @author xuxueli 2017-12-29 17:56:48
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 递归删除文件或目录
     *
     * @param file 要删除的文件或目录
     * @return 是否删除成功
     */
    public static boolean deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        // 如果是目录，先递归删除子文件和子目录
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }

        // 删除当前文件或目录
        return file.delete();
    }

    /**
     * 删除指定文件
     *
     * @param fileName 文件名
     */
    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * 向文件写入字节数据
     *
     * @param file 目标文件
     * @param data 要写入的字节数据
     */
    public static void writeFileContent(File file, byte[] data) {
        if (data == null) {
            return;
        }

        // 确保父目录存在
        ensureParentDirExists(file);

        // 使用try-with-resources自动关闭流
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.flush();
        } catch (Exception e) {
            logger.error("写入文件内容失败，文件:{}", file.getAbsolutePath(), e);
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param file 要读取的文件
     * @return 文件内容的字节数组，读取失败返回null
     */
    public static byte[] readFileContent(File file) {
        if (!file.exists() || !file.isFile()) {
            logger.warn("文件不存在或不是常规文件:{}", file.getAbsolutePath());
            return null;
        }

        // 检查文件大小是否合理
        long fileLength = file.length();
        if (fileLength > Integer.MAX_VALUE) {
            logger.error("文件过大，无法读取:{}，大小:{}", file.getAbsolutePath(), fileLength);
            return null;
        }

        // 使用try-with-resources自动关闭流
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) fileLength];
            int bytesRead = fis.read(content);
            if (bytesRead != fileLength) {
                logger.warn("文件读取不完整，预期:{}，实际:{}", fileLength, bytesRead);
            }
            return content;
        } catch (Exception e) {
            logger.error("读取文件内容失败，文件:{}", file.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * 确保父目录存在，如果不存在则创建
     *
     * @param file 文件对象
     */
    private static void ensureParentDirExists(File file) {
        Path parentPath = Paths.get(file.getParent());
        if (!Files.exists(parentPath)) {
            try {
                Files.createDirectories(parentPath);
            } catch (IOException e) {
                logger.error("创建父目录失败:{}", parentPath, e);
            }
        }
    }
}
