package pattern;

import nju.gzq.simple.FileHandle;

import java.util.*;

public class Main {
    public static String rootPath = "data_pattern/";

    public static void main(String[] args) {
        //// "apache-ant-1.7.0", "emf-2.4.1"
        String[] projectNames = {"argouml", "columba-1.4-src", "hibernate-distribution-3.3.2.GA", "jEdit-4.2",
                "jfreechart-1.0.19", "apache-jmeter-2.10", "jruby-1.4.0", "sql12", "apache-ant-1.7.0", "emf-2.4.1"};


        //分割数据集
        //splitProjectData();

        // 获取模式
        String[] keyWords = getPatterns("dic/k.txt", false);
        // 预测正负
        double[] result = new double[3];
        for (int i = 0; i < projectNames.length; i++) {
            double[] temp = predictData(projectNames[i], keyWords);
            for (int j = 0; j < result.length; j++) result[j] += temp[j];
        }
        for (int i = 0; i < result.length; i++) result[i] /= projectNames.length;
        System.out.println(result[0] + ", " + result[1] + ", " + result[2]); //*/
    }


    /**
     * 分割项目数据
     */
    public static void splitProjectData() {
        List<String> projects = FileHandle.readFileToLines(rootPath + "projects");
        List<String> comments = FileHandle.readFileToLines(rootPath + "comments");
        List<String> labels = FileHandle.readFileToLines(rootPath + "labels");

        String currentProject = projects.get(0);
        String tempCommentData = "";
        String tempLabelData = "";
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).equals(currentProject)) {
                tempCommentData += comments.get(i) + "\n";
                if (!labels.get(i).equals("WITHOUT_CLASSIFICATION")) tempLabelData += "positive\n";
                else tempLabelData += "negative\n";
            } else {
                //保存上个项目的信息
                FileHandle.writeStringToFile(rootPath + "comment--" + currentProject + ".txt", tempCommentData);
                FileHandle.writeStringToFile(rootPath + "label--" + currentProject + ".txt", tempLabelData);

                //更新临时变量
                currentProject = projects.get(i);
                tempCommentData = "";
                tempLabelData = "";
            }
        }
        // 保存最后一个项目的信息
        FileHandle.writeStringToFile(rootPath + "comment--" + currentProject + ".txt", tempCommentData);
        FileHandle.writeStringToFile(rootPath + "label--" + currentProject + ".txt", tempLabelData);
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
    public static double[] predictData(String projectName, String[] pattern) {
        List<String> comments = FileHandle.readFileToLines(rootPath + "comment--" + projectName + ".txt");
        List<String> labels = FileHandle.readFileToLines(rootPath + "label--" + projectName + ".txt");

        int[] predicts = new int[comments.size()];
        for (int i = 0; i < comments.size(); i++) predicts[i] = classify(comments.get(i), pattern);

        //计算混淆矩阵, precision, recall and F1-measure
        double TP = .0, FP = .0, TN = .0, FN = .0;
        for (int i = 0; i < comments.size(); i++) {
            if (labels.get(i).equals("positive") && predicts[i] == 1) TP++;
            else if (labels.get(i).equals("negative") && predicts[i] == 1) FP++;
            else if (labels.get(i).equals("negative") && predicts[i] == 0) TN++;
            else FN++;
        }

        double precision = .0, recall = .0, f1 = .0;
        if (TP > 0) {
            precision = TP / (TP + FP);
            recall = TP / (TP + FN);
            f1 = 2 * precision * recall / (precision + recall);
        }

        //System.out.println("TP: " + TP + " FP: " + FP);
        //System.out.println("TN: " + TN + " FN: " + FN);
        System.out.println(precision + ", " + recall + ", " + f1);
        return new double[]{precision, recall, f1};
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
}
