package nju.gzq.simple;

import nju.gzq.selector.FileHandle;

import java.util.*;

/**
 * informational theoretic approach
 */
public class Semantics {

    public static void main(String[] args) {
        List<String> instances = FileHandle.readFileToLines("d/data--test.txt");
        getPMIWords("d/token--test.txt", instances, new String[]{"file", "search"}, 1);
    }

    /**
     * 计算多个单词的互信息词表
     *
     * @param filePath
     * @param instances
     * @param keyWords
     * @param number
     * @return
     */
    public static String[] getPMIWords(String filePath, List<String> instances, String[] keyWords, int number) {
        String[][] temp = new String[keyWords.length][];
        int index = 0;
        for (String key : keyWords) temp[index++] = Semantics.getPMIWords(filePath, instances, key, number); // 点互信息

        int length = 0;
        for (int i = 0; i < temp.length; i++) length += temp[i].length;
        String[] selectedFeatures = new String[length];

        for (int i = 0, k = 0; i < temp.length; i++)
            for (int j = 0; j < temp[i].length; j++) selectedFeatures[k++] = temp[i][j];

        // 去除重复出现的词
        Set<String> set = new HashSet<>();
        for (int i = 0; i < selectedFeatures.length; i++) set.add(selectedFeatures[i]);
        selectedFeatures = new String[set.size()];
        int index2 = 0;
        for (String feature : set) selectedFeatures[index2++] = feature;
        //printCount(selectedFeatures);
        return selectedFeatures;
    }

    /**
     * 计算一个单词的互信息词汇表
     *
     * @param filePath
     * @param documents
     * @param keyWord
     * @param number
     * @return
     */
    private static String[] getPMIWords(String filePath, List<String> documents, String keyWord, int number) {

        //提取特征, 并构建索引, 特征->索引; 索引->特征

        Set<String> set = new HashSet<>();
        for (String document : documents) {
            for (String word : document.split(",")[0].replace("'", " ").split(" ")) set.add(word);
        }

        StringBuilder text = new StringBuilder();
        for (String feature : set) text.append(feature).append("\n");
        FileHandle.writeStringToFile(filePath, text.toString().substring(1));
        if (!set.contains(keyWord)) return new String[]{keyWord};  //返回自己
        List<String> features = FileHandle.readFileToLines(filePath);

        Map<String, Integer> featureToIndex = new HashMap<>();
        for (int i = 0; i < features.size(); i++) featureToIndex.put(features.get(i), i);


        // 构建VSM模型
        int[][] VSM = new int[documents.size()][];
        for (int i = 0; i < VSM.length; i++) {
            VSM[i] = new int[features.size()];
            String[] words = documents.get(i).split(",")[0].replace("'", "").split(" ");
            for (String word : words) VSM[i][featureToIndex.get(word)] += 1;
        }

        double[] count = new double[features.size()];
        double[] together = new double[features.size()];

        for (int j = 0; j < count.length; j++) {
            for (int i = 0; i < VSM.length; i++) {
                if (VSM[i][j] > 0) count[j]++;
                if (VSM[i][j] > 0 && VSM[i][featureToIndex.get(keyWord)] > 0) together[j]++;
            }
        }

        //PMI 分数排名
        Word[] words = new Word[features.size()];
        for (int i = 0; i < words.length; i++) {
            if (together[i] > 0)
                words[i] = new Word(features.get(i), together[i] / (count[i] * count[featureToIndex.get(keyWord)]));
            else words[i] = new Word(features.get(i), .0);
        }
        Arrays.sort(words);

        // 选取前k个特征
        number = number <= words.length ? number : words.length; //更新number值,防止输入较大的个数
        String[] selectedFeatures = new String[number + 1];
        selectedFeatures[0] = keyWord;
        for (int i = 0; i < number; i++) selectedFeatures[i + 1] = words[i].getName();
        return selectedFeatures;
    }

    /**
     * 打印VSM模型
     *
     * @param features
     * @param model
     */
    public static void printVSM(List<String> features, int[][] model) {
        for (int i = 0; i < features.size(); i++) System.out.print(features.get(i) + "\t");
        System.out.println();
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[i].length; j++) System.out.print(model[i][j] + "\t");
            System.out.println();
        }
    }

    public static void printCount(double[] count) {
        for (int i = 0; i < count.length; i++) System.out.print(count[i] + " ");
        System.out.println();
    }
    public static void printCount(String[] count) {
        for (int i = 0; i < count.length; i++) System.out.print(count[i] + " ");
        System.out.println();
    }
    public static void printCount(Word[] words) {
        for (int i = 0; i < words.length; i++) System.out.print(words[i].getName() + ": " + words[i].getValue() + " ");
        System.out.println();
    }

    /**
     * Normalized Google Distance
     */
    public static void NGD() {

    }
}

class Word implements Comparable {
    private String name;
    private Double value;

    public Word(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int compareTo(Object o) {
        Word word = (Word) o;
        return word.getValue().compareTo(this.getValue());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}