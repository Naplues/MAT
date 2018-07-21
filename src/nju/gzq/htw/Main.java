package nju.gzq.htw;

import nju.gzq.base.*;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        //测试特征选择器
        //testSelector();

        //测试组合效果
        testCombination(1, 3, 4);
    }

    /**
     * 测试组合效果
     */
    public static void testCombination(Integer... features) {
        String path = "debt_data\\";
        File[] projects = new File(path).listFiles();

        for (File p : projects) {
            BaseProject project = new BaseProject(p.getPath(), 0);
            project.setFeatures(BaseRanking.rankByFeature(project, BaseRanking.SUMMATION, BaseRanking.RANK_DESC, features));
            Evaluation.F1(project, false);
        }

    }

    /**
     * 测试特征选择器
     *
     * @throws Exception
     */
    public static void testSelector() {
        //选择特征
        int featureNumber = 5;
        String outputPath = "C:\\Users\\naplues\\Desktop\\result";
        String fileType = "svg";
        int neededFeatureNumber = 5;
        double threshold = 0.0;
        boolean isHorizontal = false;
        int top = 10;
        boolean frequencyInformation = false;
        new MySelector().start(featureNumber, outputPath, fileType, neededFeatureNumber, threshold, isHorizontal, top, frequencyInformation);
    }
}