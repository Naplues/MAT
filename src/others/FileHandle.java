package others;
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
     * 读取文件返回一个字符串列表
     *
     * @param filePath
     * @return
     */
    public static List<String> readFileToLines(String filePath, boolean... args) {
        BufferedReader reader = null;
        if (args.length > 0 && args[0])
            reader = FileHandle.getExternalPath(filePath); // 读取文件系统路径
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
            // 文件不存在
            if (!file.exists()) {
                String[] temp = filePath.split("/");
                String dir = filePath.replace(temp[temp.length - 1], "");
                File mkdir = new File(dir);
                mkdir.mkdirs();
                file.createNewFile();
            }
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
     * 将double数组写入文件
     *
     * @param filePath
     * @param array
     * @param a
     */
    public static void writeDoubleArrayToFile(String filePath, double[] array, boolean... a) {
        StringBuilder text = new StringBuilder();
        for (double arr : array) text.append((int) arr).append("\n");
        writeStringToFile(filePath, text.toString(), a);
    }

    /**
     * 将int数组写入文件
     *
     * @param filePath
     * @param array
     * @param a
     */
    public static void writeIntegerArrayToFile(String filePath, int[] array, boolean... a) {
        StringBuilder text = new StringBuilder();
        for (int arr : array) text.append(arr).append("\n");
        writeStringToFile(filePath, text.toString(), a);
    }

    /**
     * @param filePath
     * @param a
     */
    public static void writeLinesToFile(String filePath, List<String> lines, boolean... a) {
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            text.append(line).append("\n");
        }
        writeStringToFile(filePath, text.toString(), a);
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
    public static BufferedReader getExternalPath(String path) {
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
