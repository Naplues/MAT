package nju.gzq.simple;

import nju.gzq.selector.FileHandle;

import java.util.List;

public class Main {
    public static String rootPath = "d/";

    public static void main(String[] args) {
        String[] projectNames = {"argouml", "columba-1.4-src", "hibernate-distribution-3.3.2.GA", "jEdit-4.2",
                "jfreechart-1.0.19", "apache-jmeter-2.10", "jruby-1.4.0", "sql12"};

        String[] keyWords = {"hack", "todo", "workaround", "fix"};
        for (int i = 0; i < projectNames.length; i++)
            readData(rootPath + "data--" + projectNames[i] + ".arff", keyWords, false);
    }

    /**
     * @param filePath
     */
    public static void readData(String filePath, String[] keyWords, boolean extension) {
        List<String> instances = FileHandle.readFileToLines(filePath);
        String[] labels = new String[instances.size()];
        int[] predicts = new int[instances.size()];

        for (int i = 0; i < instances.size(); i++) {
            labels[i] = instances.get(i).split(",")[1];
            predicts[i] = classify(instances.get(i).split(",")[0], keyWords, extension);
        }

        //计算混淆矩阵, precision, recall and F1-measure
        double TP = .0, FP = .0, TN = .0, FN = .0;
        for (int i = 0; i < instances.size(); i++) {
            if (labels[i].equals("positive") && predicts[i] == 1) TP++;
            else if (labels[i].equals("negative") && predicts[i] == 1) FP++;
            else if (labels[i].equals("negative") && predicts[i] == 0) TN++;
            else FN++;
        }

        double precision = TP / (TP + FP);
        double recall = TP / (TP + FN);
        double f1 = 2 * precision * recall / (precision + recall);
        //System.out.println("TP: " + TP + " FP: " + FP);
        //System.out.println("TN: " + TN + " FN: " + FN);
        System.out.println(precision + ", " + recall + ", " + f1);
    }

    /**
     * 分类
     *
     * @param instance
     * @return
     */
    public static int classify(String instance, String[] keyWords, boolean extension) {
        String[] words = instance.replace("'", "").split(" ");
        if (extension) {
            for (String word : words) for (String key : keyWords) if (word.contains(key)) return 1;
        } else {
            for (String word : words) for (String key : keyWords) if (word.equals(key)) return 1;
        }

        return 0;
    }
}
