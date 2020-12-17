package main.methods;

import main.Settings;
import main.Statistics;
import others.FileHandle;

import java.util.*;

public class Pattern extends Method {
    {
        methodPath = rootPath + "pattern/";
    }

    public static void main(String[] args) {

        new Pattern().predict();

    }

    /**
     * 分割项目数据 for Pattern
     * 将总数据集根据项目进行分割
     */
    public void prepareData() {
        System.out.println("Preparing data for Pattern");
        List<String> projects = FileHandle.readFileToLines(originPath + "projects");
        List<String> comments = FileHandle.readFileToLines(originPath + "comments");
        List<String> labels = FileHandle.readFileToLines(originPath + "labels");

        String currentProject = projects.get(0); // 初始化当前项目
        StringBuilder tempCommentData = new StringBuilder();
        StringBuilder tempLabelData = new StringBuilder();
        for (int i = 0; i < projects.size(); i++) {
            // 正在处理当前项目
            if (projects.get(i).equals(currentProject)) {
                tempCommentData.append(comments.get(i)).append("\n");
                if (!labels.get(i).equals("WITHOUT_CLASSIFICATION")) tempLabelData.append("positive\n");
                else tempLabelData.append("negative\n");
            } else {
                //保存上个项目的信息
                FileHandle.writeStringToFile(originPath + "data--" + currentProject + ".txt", tempCommentData.toString());
                FileHandle.writeStringToFile(originPath + "label--" + currentProject + ".txt", tempLabelData.toString());

                //更新临时变量
                currentProject = projects.get(i);
                tempCommentData = new StringBuilder(comments.get(i) + "\n");
                if (!labels.get(i).equals("WITHOUT_CLASSIFICATION")) tempLabelData = new StringBuilder("positive\n");
                else tempLabelData = new StringBuilder("negative\n");
            }
        }
        // 保存最后一个项目的信息
        FileHandle.writeStringToFile(originPath + "data--" + currentProject + ".txt", tempCommentData.toString());
        FileHandle.writeStringToFile(originPath + "label--" + currentProject + ".txt", tempLabelData.toString());
    }


    public void predict() {
        prepareData();
        // 获取模式
        String[] keyWords = getPatterns(Settings.rootPath + "dic/k.txt");
        // 预测每个项目上的结果
        for (int i = 0; i < Settings.projectNames.length; i++) predictData(Settings.projectNames[i], keyWords);
        System.out.println("Pattern prediction finished!");

        Statistics.evaluate("Pattern");
    }

    /**
     * 获取模式
     *
     * @param filePath
     * @return
     */
    public String[] getPatterns(String filePath) {
        List<String> keyWordsList = FileHandle.readFileToLines(filePath);
        String[] keyWords = new String[keyWordsList.size()];
        for (int i = 0; i < keyWords.length; i++) keyWords[i] = keyWordsList.get(i);
        return keyWords;
    }

    /**
     * 预测类别
     *
     * @param projectName
     * @param pattern
     */
    public void predictData(String projectName, String[] pattern) {
        List<String> comments = FileHandle.readFileToLines(originPath + "data--" + projectName + ".txt");
        int[] predictions = new int[comments.size()];
        for (int i = 0; i < comments.size(); i++) predictions[i] = classify(comments.get(i), pattern);

        FileHandle.writeIntegerArrayToFile(Settings.resultPath + "MTO_Pattern/result--" + projectName + ".txt", predictions);
    }

    /**
     * 分类
     *
     * @param instance
     * @return
     */
    public int classify(String instance, String[] keyWords) {
        String[] words = instance.replace("'", "").split(" ");
        for (String word : words) for (String key : keyWords) if (word.contains(key)) return 1;
        return 0;
    }
}
