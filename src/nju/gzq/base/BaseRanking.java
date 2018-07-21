package nju.gzq.base;

import java.util.Arrays;
import java.util.Comparator;

/**
 * BaseRanking: Built-in Ranking class which defines the default ranking algorithm
 */
public class BaseRanking {

    // combination approach (MULTIPLE x /SUMMATION +)
    public static final int MULTIPLE = 0;
    public static final int SUMMATION = 1;

    // ranking approach (ASC/DESC)
    public static final int RANK_ASC = 0;
    public static final int RANK_DESC = 1;

    /**
     * Ranking according to feature value
     *
     * @param project     project object
     * @param combination combination approach (MULTIPLE x /SUMMATION +)
     * @param ranking     ranking approach (ASC/DESC)
     * @param features    combination feature index
     */
    public static BaseFeature[][] rankByFeature(BaseProject project, int combination, int ranking, Integer... features) {
        BaseFeature[][] result = project.getFeatures();
        rank(result, combination, ranking, features);
        return result;
    }

    /**
     * 排序某类特征
     *
     * @param features
     * @param feature
     */
    public static void rank(BaseFeature[][] features, int combination, int ranking, Integer... feature) {
        for (int i = 0; i < features.length; i++) {
            Arrays.sort(features[i], new Comparator<BaseFeature>() {
                @Override
                public int compare(BaseFeature o1, BaseFeature o2) {
                    Double a1 = 1., a2 = 1.;

                    // combination approach
                    if (combination == MULTIPLE) {
                        for (int f : feature) {
                            a1 *= o1.getValueFromIndex(f);
                            a2 *= o2.getValueFromIndex(f);
                        }
                    } else if (combination == SUMMATION) {
                        a1 = .0;
                        a2 = .0;
                        for (int f : feature) {
                            a1 += o1.getValueFromIndex(f);
                            a2 += o2.getValueFromIndex(f);
                        }
                    }
                    o1.setTemp(a1);
                    o2.setTemp(a2);

                    // ranking approach
                    if (ranking == RANK_ASC) return a1.compareTo(a2);
                    else if (ranking == RANK_DESC) return a2.compareTo(a1);

                    return 0;
                }
            });
        }
    }
}
