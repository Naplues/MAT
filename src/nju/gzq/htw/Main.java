package nju.gzq.htw;

import nju.gzq.base.*;

public class Main {

    public static void main(String[] args) {

        //测试组合效果
        BaseProject project = new BaseProject("debt_data/", 0);
        project.setFeatures(BaseRanking.rankByFeature(project, BaseRanking.SUMMATION, BaseRanking.RANK_DESC, 1, 3, 4));
        F1(project, false);
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
            System.out.println(P + ", " + R + ", " + F1);
        }
        precision /= features.length;
        recall /= features.length;
        F1value /= features.length;
        System.out.println("Total: " + precision + ", " + recall + ", " + F1value);
        return F1value;
    }
}