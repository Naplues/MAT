package mat;

import config.FileHandle;
import config.Settings;

import java.util.List;

public class Main {
    public static String rootPath = "data_new/mat/";
    //"todo", "workaround", "fixme", "xxx"  "workaround", "stupid", "yuck", "ugly", "dms", "revisit", "tbd", "nasty", "suck", "fuck", "notes"
    static String[] keyWords = {"todo", "fixme", "hack", "xxx", "workaround", "tbd", "stupid", "yuck", "ugly", "nasty", "suck", "fuck", "notes"};

    public static void main(String[] args) {
        // predict(Settings.projectNames[8], keyWords, false, true);

        //预测正负
        for (int i = 0; i < Settings.projectNames.length; i++) {
            System.out.print(Settings.projectNames[i] + ",");
            predict(Settings.projectNames[i], keyWords, true, true);
        }
    }

    /**
     * 预测某个项目的 SATD 注释
     *
     * @param projectName
     * @param keyWords
     * @param isFuzzy
     * @param details     打印详细结果
     */
    public static double[] predict(String projectName, String[] keyWords, boolean isFuzzy, boolean details) {
        List<String> instances = FileHandle.readFileToLines(rootPath + "data--" + projectName + ".txt");
        String[] labels = new String[instances.size()];
        int[] predicts = new int[instances.size()];

        for (int i = 0; i < instances.size(); i++) {
            labels[i] = instances.get(i).split(",")[1];
            predicts[i] = classify(instances.get(i).split(",")[0], keyWords, isFuzzy);
        }

        //计算混淆矩阵, precision, recall and F1-measure
        double TP = .0, FP = .0, TN = .0, FN = .0;
        int count = 0;
        for (int i = 0; i < instances.size(); i++) {
            if (labels[i].equals("positive") && predicts[i] == 1) TP++;
            else if (labels[i].equals("negative") && predicts[i] == 1) FP++;
            else if (labels[i].equals("negative") && predicts[i] == 0) TN++;
            else FN++;
            if (predicts[i] == 1) count++;
        }

        double precision = .0, recall = .0, f1 = .0;
        if (TP > 0) {
            precision = TP / (TP + FP);
            recall = TP / (TP + FN);
            f1 = 2 * precision * recall / (precision + recall);
        }

        if (details) {
            //  System.out.println(TP + ", " + FP + ", " + FN + ", " + TN);
            System.out.println(String.format("%.3f", precision) + ", " + String.format("%.3f", recall) + ", " + String.format("%.3f", f1));
        }
        return new double[]{precision, recall, f1};
    }

    /**
     * 分类
     *
     * @param instance
     * @return
     */
    public static int classify(String instance, String[] keyWords, boolean isFuzzy) {
        String[] words = instance.replace("'", "").split(" ");
        if (isFuzzy) {
            for (String word : words) {
                for (String key : keyWords) {
                    if (word.startsWith(key) || word.endsWith(key)) {
                        if (word.contains("xxx") && !word.equals("xxx")) return 0;
                        if (word.contains("notes") && !word.equals("notes")) return 0;
                        return 1;
                    }
                }
            }
        } else {
            for (String word : words) {
                for (String key : keyWords)
                    if (word.equals(key)) return 1;
            }
        }
        return 0;
    }
}
