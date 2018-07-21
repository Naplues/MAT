package nju.gzq.htw;

import nju.gzq.selector.Selector;

public class MySelector extends Selector {

    @Override
    public double getValue(Integer[] features) {
        return Evaluation.evaluation(features);
    }

    @Override
    public String getFeatureName(Object valueIndex) {
        switch ((Integer) valueIndex) {
            case 0:
                return "fix";
            case 1:
                return "hack";
            case 2:
                return "should";
            case 3:
                return "todo";
            case 4:
                return "workaround";
            default:
                return "unknown";
        }
    }
}