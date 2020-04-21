package main.methods;


import main.Settings;

import java.util.List;

public class Method implements IMethod {
    public String rootPath = Settings.rootPath;
    public String methodPath = rootPath;         // 每个方法的具体路径在子类中修改
    public String dataPath = rootPath + "tm/";   // TM 和MAT使用

    public String originPath = rootPath + "origin/";


    // 项目名称
    public static String[] projects = Settings.projectNames;

    public void prepareData() {

    }

    public void predict() throws Exception {

    }

    public void predictWithLimitedTrainingSet() throws Exception {

    }

    /**
     * 评估该方法的预测结果
     *
     * @param oracle
     * @param predictions
     */
    public static double[] evaluate(List<String> oracle, List<String> predictions) {
        //计算混淆矩阵, precision, recall and F1-measure
        int TP = 0, FP = 0, FN = 0, TN = 0;
        for (int i = 0; i < oracle.size(); i++) {
            if (oracle.get(i).equals("positive") && predictions.get(i).equals("1")) TP++;
            if (oracle.get(i).equals("positive") && predictions.get(i).equals("0")) FN++;
            if (!oracle.get(i).equals("positive") && predictions.get(i).equals("1")) FP++;
            if (!oracle.get(i).equals("positive") && predictions.get(i).equals("0")) TN++;
        }
        double precision = (double) TP / (TP + FP);
        double recall = (double) TP / (TP + FN);
        double f1 = 2 * precision * recall / (precision + recall);

        System.out.printf("%.3f, %.3f, %.3f, || \n", precision, recall, f1);
        return new double[]{precision, recall, f1};
    }
}
