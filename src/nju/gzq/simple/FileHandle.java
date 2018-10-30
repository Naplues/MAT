package nju.gzq.simple;
/**
 * File handle class
 *
 * @author naplues
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FileHandle {
    
    /**
     * 读取查询文件名
     *
     * @param filePath
     * @return
     */
    public static String[][] readOriginQuery(String filePath) {
        List<String> file = FileHandle.readFileToLines(filePath);
        String[][] queryList = new String[file.size()][];
        for (int i = 0; i < queryList.length; i++)
            queryList[i] = file.get(i).substring(0, file.get(i).length() - 2).split("_");
        return queryList;
    }

    /**
     * 获取查询查询文件完整路径
     *
     * @param dataDir
     * @param fileName
     * @return
     */
    public static String[][] getQueryFile(String dataDir, String fileName) {
        List<String> file = FileHandle.readFileToLines(dataDir + fileName);
        String[][] queryList = readOriginQuery(dataDir + fileName);
        String[][] list = new String[file.size()][];
        for (int i = 0; i < file.size(); i++) {
            list[i] = new String[2];
            for (int j = 0; j < list[i].length; j++)
                list[i][j] = dataDir + "test\\" + queryList[i][j] + ".txt";
        }
        return list;
    }


    /**
     * 读取文件返回一个字符串列表
     *
     * @param filePath
     * @return
     */
    public static List<String> readFileToLines(String filePath, boolean... args) {
        BufferedReader reader = null;
        if (args.length > 0 && args[0])
            reader = FileHandle.getExternelPath(filePath); // 读取文件系统路径
        else
            reader = FileHandle.getActualPath(filePath); // 默认读取实际路径
        List<String> lines = new ArrayList<>();
        try {
            String s = null;
            while ((s = reader.readLine()) != null)
                lines.add(s);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * 读取文件返回一个文本字符串
     *
     * @param filePath
     * @return
     */
    public static String readFileToString(String filePath, boolean... args) {
        String string = "";
        List<String> lines = readFileToLines(filePath, args);
        for (String t : lines) {
            string += t;
        }

        return string;
    }

    /**
     * 将字符串写入文件，false表示覆盖
     *
     * @param filePath
     * @param data
     */
    public static void writeStringToFile(String filePath, String data, boolean... a) {
        try {
            // true = append file
            File file = new File(filePath);
            if (!file.exists())
                file.createNewFile();
            boolean append = false;
            if (a.length == 1) {
                append = a[0];
            }
            FileWriter fileWritter = new FileWriter(file, append);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.close();
            fileWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param filePath
     * @param a
     */
    public static void writeLinesToFile(String filePath, List<String> lines, boolean... a) {
        for (String t : lines) {
            writeStringToFile(filePath, t + "\n", a);
        }
    }

    /**
     * 获取实际的文件路径
     *
     * @param path
     * @return
     */
    public static BufferedReader getActualPath(String path) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取参数文件
     *
     * @param path
     * @return
     */
    public static BufferedReader getExternelPath(String path) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void printLines(List<String> lines) {
        for (String line : lines) {
            System.out.println(line);
        }
    }


}
