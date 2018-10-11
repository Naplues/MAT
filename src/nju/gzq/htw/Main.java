package nju.gzq.htw;

import nju.gzq.base.*;
import nju.gzq.selector.FileHandle;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String[] projectNames = {"argouml", "columba-1.4-src", "hibernate-distribution-3.3.2.GA", "jEdit-4.2",
                "jfreechart-1.0.19", "apache-jmeter-2.10", "jruby-1.4.0", "sql12"};
        // runHTW();

        for (int i = 0; i < projectNames.length; i++) search("data/" + projectNames[i] + ".csv");

    }


    /**
     * @param filePath
     */
    public static void search(String filePath) {
        List<String> lines = FileHandle.readFileToLines(filePath);
        String[] features = lines.get(0).split(",");
        int[] count = new int[features.length];
        System.out.println(features.length);
        for (int i = 0; i < features.length - 1; i++) {
            for (int j = i + 1; j < features.length; j++) {
                if (features[j].contains(features[i])) {
                    count[i]++;
                }
            }
        }

        int c = 0;
        for (int i = 0; i < count.length; i++) {
            if (count[i] <= 1) continue;
            c++;
            //System.out.println(features[i] + ": " + count[i]);
        }
        //System.out.println(c);

    }


    public static void runHTW() {
        //测试组合效果
        String path = "debt_data\\";
        File[] projects = new File(path).listFiles();

        long[] predictTime = new long[projects.length];

        // for (int i = 1; i <= 100; i++) {
        for (int p = 0; p < projects.length; p++) {
            long startTime = System.currentTimeMillis();
            BaseProject project = new BaseProject(projects[p].getPath(), 0);
            project.setFeatures(BaseRanking.rankByFeature(project, BaseRanking.SUMMATION, BaseRanking.RANK_DESC, 1, 3, 4));
            F1(project, false);
            long endTime = System.currentTimeMillis();
            predictTime[p] += endTime - startTime;
        }
        //   }
/*
        for (int i = 0; i < predictTime.length; i++) {
            System.out.println(predictTime[i] / 100.0);
        }*/
    }

    /**
     * F1 value
     * F1 = 2 * P * R / (P + R)
     *
     * @param project
     * @return
     */
    public static Double F1(BaseProject project, boolean details) {
        Double F1value = .0;
        Double precision = .0, recall = .0;
        BaseFeature[][] features = project.getFeatures();

        for (BaseFeature[] feature : features) {
            Double positive = .0;
            int position = 0;         //计数位置
            int total = 0;            //所有正例数目
            for (int i = 0; i < feature.length; i++) {
                if (feature[i].getTemp() != 0) position++;
                if (feature[i].isLabel()) total++;
            }

            for (int i = 0; i < position; i++) if (feature[i].isLabel()) positive++;

            if (details) {
                System.out.println(positive);
                System.out.println(position + ", " + total);
            }

            Double P = positive / position; // 32/47
            Double R = positive / total;    // 32/195
            Double F1 = 2 * P * R / (P + R);
            F1value += F1;
            precision += P;
            recall += R;
            //System.out.println(P + ", " + R + ", " + F1);
        }
        precision /= features.length;
        recall /= features.length;
        F1value /= features.length;
        System.out.println(project.getProjectName() + ", " + precision + ", " + recall + ", " + F1value);
        return F1value;
    }
}