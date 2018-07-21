package nju.gzq.htw;

import nju.gzq.base.*;

import java.io.File;

public class Evaluation {
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
            F1value += 2 * P * R / (P + R);
            precision += P;
            recall += R;
        }
        precision /= features.length;
        recall /= features.length;
        F1value /= features.length;
        System.out.println(project.getProjectName() + ", " + precision + ", " + recall + ", " + F1value);
        return F1value;
    }

    /**
     * 评估
     *
     * @return
     */
    public static Double evaluation(Integer[] features) {

        String path = "debt_data\\";
        File[] projects = new File(path).listFiles();

        Double value = .0;
        for (File p : projects) {
            BaseProject project = new BaseProject(p.getPath(), 0);
            project.setFeatures(BaseRanking.rankByFeature(project, BaseRanking.SUMMATION, BaseRanking.RANK_DESC, features));
            value += Evaluation.F1(project, false);
        }
        value /= projects.length;

        return value;
    }
}
