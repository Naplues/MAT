package nju.gzq.simple;

import java.util.*;

/**
 * 对数据集进行采样
 */
public class Sampling {
    public static void main(String[] args) {
        int[] numbers = {969, 128, 377, 195, 101, 282, 383, 201, 102, 74};

        for (int i = 0; i < Main.projectNames.length; i++) {
            doSampling(Main.projectNames[i], numbers[i], 0.1);
            //getTags(Main.projectNames[i]);
        }
    }

    /**
     * 对每个项目进行采样
     *
     * @param projectName
     * @param ratio
     */
    public static void doSampling(String projectName, int number, double ratio) {

        List<String> lines = FileHandle.readFileToLines("data_pattern/comment--" + projectName + ".txt");
        List<String> posInstances = new ArrayList<>();
        for (int i = 0; i < number; i++) posInstances.add(lines.get(i));
        String text = "";
        int posNumber = (int) (ratio * posInstances.size());

        Random random = new Random(1);
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < posNumber; ) {
            int randomNumber = (int) Math.round(random.nextDouble() * posInstances.size());
            if (set.contains(randomNumber)) continue;
            set.add(randomNumber);
            text += randomNumber + ": " + posInstances.get(randomNumber) + "\n";
            i++;
        }
        System.out.println(posNumber);
        FileHandle.writeStringToFile("sampling/data--" + projectName + ".arff", text);
    }

    /**
     * 对每个项目进行采样
     * data_pattern/comment--
     * @param projectName
     */
    public static void getTags(String projectName) {
        List<String> lines = FileHandle.readFileToLines("data_pattern/comment--" + projectName + ".txt");

        String text = "";
        Set<String> set = new HashSet<>();
        for (int i = 0; i < lines.size(); ) {
            String[] temp = lines.get(i).split(" ");
            for (String word : temp) {
                boolean flag = true;
                for (int j = 0; j < word.length(); j++) {
                    if (!(word.charAt(j) >= 'A' && word.charAt(j) <= 'Z')) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    set.add(word);
                    System.out.println(word);
                }

            }
        }

        for (String tag : set) {
            System.out.println(tag);
        }
    }
}
