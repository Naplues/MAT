package others;

import config.FileHandle;
import config.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 统计特征单词
 */
public class Count {

    public static void main(String[] args) {
        String[] hotWords = {"xxx"};
        //getTopWords(-1);
        getSamplePercent(hotWords, "positive");
        System.out.println("*****************************");
        getSamplePercent(hotWords, "negative");
    }

    public static void getSamplePercent(String[] hotWords, String category) {

        for (int i = 0; i < Settings.projectNames.length; i++) {
            List<String> lines = FileHandle.readFileToLines("data/samples/" + category + "/" + Settings.projectNames[i] + ".txt");
            int count = 0;
            for (String line : lines) {
                String[] temp = line.split(",");
                String[] words = temp[0].replace("'", "").split(" "); //一条注释的单词数组
                //遍历每条注释中的每个单词
                next:
                for (int index = 1; index < words.length; index++) {

                    for (String hotWord : hotWords) {
                        if (words[index].contains(hotWord)) {
                            count++;
                            break next;
                        }
                    }
                }
            }
            System.out.println(count);
        }
    }

    /**
     * 计算单个项目
     *
     * @param projectIndex
     */
    public static void getTopWords(int projectIndex) {
        Map<String, Double> positiveFrequency = getWordFrequency("positive", projectIndex);
        Map<String, Double> negativeFrequency = getWordFrequency("negative", projectIndex);
        Map<String, Double> frequency = new HashMap<>();
        Map<Double, String> map = new TreeMap<>();

        for (String key : positiveFrequency.keySet()) {
            double value;
            if (negativeFrequency.containsKey(key))
                value = positiveFrequency.get(key) / negativeFrequency.get(key);
            else
                value = Double.MAX_VALUE;
            frequency.put(key, value);
        }

        //构建顺序映射
        for (String key : frequency.keySet()) map.put(frequency.get(key), key);

        for (Double value : map.keySet()) {
            if (positiveFrequency.get(map.get(value)) > 1)
                System.out.println(value + ": " + map.get(value));
        }
    }

    /**
     * 获取单词频率
     *
     * @param category
     * @return
     */
    public static Map<String, Double> getWordFrequency(String category, int i) {

        Map<String, Double> frequency = new HashMap<>();
        if (i == -1) {
            for (i = 0; i < Settings.projectNames.length; i++) {
                List<String> lines = FileHandle.readFileToLines("data/samples/" + category + "/" + Settings.projectNames[i] + ".txt");
                for (String line : lines) {
                    String[] temp = line.split(",");
                    String[] words = temp[0].replace("'", "").split(" ");
                    //遍历每条注释中的每个单词
                    for (int index = 1; index < words.length; index++) {
                        if (frequency.containsKey(words[index]))
                            frequency.put(words[index], frequency.get(words[index]) + 1);
                        else frequency.put(words[index], 1.0);
                    }
                }
            }
        } else {
            List<String> lines = FileHandle.readFileToLines("data/samples/" + category + "/" + Settings.projectNames[i] + ".txt");
            for (String line : lines) {
                String[] temp = line.split(",");
                String[] words = temp[0].replace("'", "").split(" ");
                //遍历每条注释中的每个单词
                for (int index = 1; index < words.length; index++) {
                    if (frequency.containsKey(words[index]))
                        frequency.put(words[index], frequency.get(words[index]) + 1);
                    else frequency.put(words[index], 1.0);
                }
            }
        }
        return frequency;
    }
}
