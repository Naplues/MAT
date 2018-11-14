package nju.gzq.simple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 统计特征单词
 */
public class Count {

    public static void main(String[] args) {

        Map<String, Double> positiveFrequency = new HashMap<>();
        Map<String, Double> negativeFrequency = new HashMap<>();
        Map<Double, String> map = new TreeMap<>();

        for (int i = 0; i < Main.projectNames.length; i++) {

            List<String> lines = FileHandle.readFileToLines(Main.rootPath + "data--" + Main.projectNames[i] + ".arff");
            int postiveNumber = 0, negativeNumber = 0;
            for (String line : lines) {
                String[] temp = line.split(",");
                if (temp[1].equals("positive")) {
                    postiveNumber++;
                    for (String word : temp[0].replace("'", "").split(" ")) {
                        if (positiveFrequency.containsKey(word))
                            positiveFrequency.put(word, positiveFrequency.get(word) + 1);
                        else positiveFrequency.put(word, 1.0);
                    }
                } else {
                    negativeNumber++;
                    for (String word : temp[0].replace("'", "").split(" ")) {
                        if (negativeFrequency.containsKey(word))
                            negativeFrequency.put(word, negativeFrequency.get(word) + 1);
                        else negativeFrequency.put(word, 1.0);
                    }
                }
            }
        }


        for (String key : positiveFrequency.keySet()) {
            double value;
            if (negativeFrequency.containsKey(key))
                value = positiveFrequency.get(key) * positiveFrequency.get(key) / negativeFrequency.get(key);
            else
                value = Double.MAX_VALUE;
            positiveFrequency.put(key, value);
        }


        for (String key : positiveFrequency.keySet()) {
            map.put(positiveFrequency.get(key), key);
        }
        for (Double value : map.keySet()) {
            System.out.println(value + ": " + map.get(value));
        }

    }
}
