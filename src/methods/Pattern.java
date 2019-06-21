package methods;

import config.Settings;
import config.FileHandle;
import config.Transform;

import java.util.*;

public class Pattern {
    public static String rootPath = "data/pattern/";

    public static void main(String[] args) {

        //分割数据集
        //splitProjectData();

        // 获取模式
        String[] keyWords = getPatterns("data/dic/k.txt", false);
        // 预测每个项目上的结果
        for (int i = 0; i < Settings.projectNames.length; i++) predictData(Settings.projectNames[i], keyWords);
        System.out.println("Pattern prediction finished!");
    }


    /**
     * 获取模式
     *
     * @param filePath
     * @param fuzzy
     * @return
     */
    public static String[] getPatterns(String filePath, boolean fuzzy) {
        List<String> keyWordsList = FileHandle.readFileToLines(filePath);
        String[] keyWords;
        if (fuzzy) {
            int length = 0;
            for (int i = 0; i < keyWordsList.size(); i++) length += keyWordsList.get(i).split(" ").length;
            keyWords = new String[length];
            for (int i = 0, j = 0; i < keyWordsList.size(); i++) {
                String[] temp = keyWordsList.get(i).split(" ");
                for (int k = 0; k < temp.length; k++) keyWords[j++] = temp[k];
            }

        } else {
            keyWords = new String[keyWordsList.size()];
            for (int i = 0; i < keyWords.length; i++) keyWords[i] = keyWordsList.get(i);
        }
        return keyWords;
    }

    /**
     * 预测类别
     *
     * @param projectName
     * @param pattern
     */
    public static void predictData(String projectName, String[] pattern) {
        List<String> comments = FileHandle.readFileToLines(rootPath + "data--" + projectName + ".txt");
        int[] predictions = new int[comments.size()];
        for (int i = 0; i < comments.size(); i++) predictions[i] = classify(comments.get(i), pattern);

        FileHandle.writeIntegerArrayToFile(rootPath + "result--" + projectName + ".txt", predictions);
    }

    /**
     * 分类
     *
     * @param instance
     * @return
     */
    public static int classify(String instance, String[] keyWords) {
        String[] words = instance.replace("'", "").split(" ");
        for (String word : words) for (String key : keyWords) if (word.contains(key)) return 1;
        return 0;
    }

    /**
     * 分割项目数据
     */
    public static void splitProjectData() {
        List<String> projects = FileHandle.readFileToLines("data/origin/projects");
        List<String> comments = FileHandle.readFileToLines("data/origin/comments");
        List<String> labels = FileHandle.readFileToLines("data/origin/labels");

        String currentProject = projects.get(0); // 初始化当前项目
        StringBuilder tempCommentData = new StringBuilder();
        StringBuilder tempLabelData = new StringBuilder();
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).equals(currentProject)) {
                tempCommentData.append(comments.get(i)).append("\n");
                if (!labels.get(i).equals("WITHOUT_CLASSIFICATION")) tempLabelData.append("positive\n");
                else tempLabelData.append("negative\n");
            } else {
                //保存上个项目的信息
                FileHandle.writeStringToFile(rootPath + "data--" + Transform.projectNames.get(currentProject) + ".txt", tempCommentData.toString());
                FileHandle.writeStringToFile(rootPath + "label--" + Transform.projectNames.get(currentProject) + ".txt", tempLabelData.toString());

                //更新临时变量
                currentProject = projects.get(i);
                tempCommentData = new StringBuilder(comments.get(i) + "\n");
                if (!labels.get(i).equals("WITHOUT_CLASSIFICATION")) tempLabelData = new StringBuilder("positive\n");
                else tempLabelData = new StringBuilder("negative\n");
            }
        }
        // 保存最后一个项目的信息
        FileHandle.writeStringToFile(rootPath + "data--" + Transform.projectNames.get(currentProject) + ".txt", tempCommentData.toString());
        FileHandle.writeStringToFile(rootPath + "label--" + Transform.projectNames.get(currentProject) + ".txt", tempLabelData.toString());
        System.out.println("Split finish!");
    }
}
