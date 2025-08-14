package com.ccexid.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * file tool
 *
 * @author xuxueli 2017-12-29 17:56:48
 */
@Slf4j
public class FileUtil {


    /**
     * delete recursively
     *
     * @param root the root file or directory to delete
     * @return true if deletion is successful, false otherwise
     */
    public static boolean deleteRecursively(File root) {
        try {
            FileUtils.deleteDirectory(root);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * delete file by file name
     *
     * @param fileName the name of the file to delete
     */
    public static void deleteFile(String fileName) {
        // file
        File file = new File(fileName);
        if (file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    /**
     * write byte data to file
     *
     * @param file the file to write to
     * @param data the byte data to write
     */
    public static void writeFileContent(File file, byte[] data) {
        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * read file content as byte array
     *
     * @param file the file to read from
     * @return the file content as byte array, or null if read failed
     */
    public static byte[] readFileContent(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
