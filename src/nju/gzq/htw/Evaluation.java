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
    public static Double F1(BaseProject project) {

        BaseFeature[] feature = project.getFeatures()[0];  //特征
        Double positive = .0;
        int position = 0;         //计数位置
        int total = 0;            //所有正例数目
        for (int i = 0; i < feature.length; i++) {
            if (feature[i].getTemp() != 0) position++;
            if (feature[i].isLabel()) total++;
        }

        for (int i = 0; i < position; i++) if (feature[i].isLabel()) positive++;
        System.out.println(positive);
        System.out.println(position + ", " + total);

        Double P = positive / position; // 32/47
        Double R = positive / total;    // 32/195
        Double F1value = 2 * P * R / (P + R);
        System.out.println(P + ", " + R + ", " + F1value);
        return F1value;
    }

    /**
     * 评估
     *
     * @return
     */
    public static Double evaluation(Integer[] features) {

        String path = "C:\\Users\\gzq\\Desktop\\HTW\\data\\";
        File[] projects = new File(path).listFiles();

        Double value = .0;
        for (File p : projects) {
            BaseProject project = new BaseProject(p.getPath(), 0);
            project.setFeatures(BaseRanking.rankByFeature(project, BaseRanking.SUMMATION, BaseRanking.RANK_DESC, features));
            value += Evaluation.F1(project);
        }
        value /= projects.length;

        return value;
    }
}
