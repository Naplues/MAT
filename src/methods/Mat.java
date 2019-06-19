package methods;

import config.FileHandle;
import config.Settings;

import java.util.List;

public class Mat {
    public static String methodPath = "data/mat/";
    //"todo", "hack", "fixme", "xxx"  "workaround",
    public static String[] keyWords = {"todo", "hack", "fixme", "xxx"};

    public static void main(String[] args) {
        //预测正负
        for (int i = 0; i < Settings.projectNames.length; i++) {
            predict(Settings.projectNames[i], keyWords, true, true);
        }
        System.out.println("MAT prediction finished!");
    }

    /**
     * 预测某个项目的 SATD 注释
     *
     * @param projectName
     * @param keyWords
     * @param isFuzzy
     * @param details     打印详细结果
     */
    public static void predict(String projectName, String[] keyWords, boolean isFuzzy, boolean details) {
        List<String> instances = FileHandle.readFileToLines(methodPath + "data--" + projectName + ".arff");
        int[] predicts = new int[instances.size()];

        for (int i = 0; i < instances.size(); i++) {
            predicts[i] = classify(instances.get(i).split(",")[0], keyWords, isFuzzy);
        }

        FileHandle.writeIntegerArrayToFile(methodPath + "result--" + projectName + ".txt", predicts);
    }

    /**
     * 分类
     *
     * @param instance
     * @return
     */
    public static int classify(String instance, String[] keyWords, boolean isFuzzy) {
        String[] words = instance.replace("'", "").split(" ");
        if (isFuzzy) {
            for (String word : words) {
                for (String key : keyWords) {
                    if (word.startsWith(key) || word.endsWith(key)) {
                        if (word.contains("xxx") && !word.equals("xxx")) return 0;
                        return 1;
                    }
                }
            }
        } else {
            for (String word : words) {
                for (String key : keyWords)
                    if (word.equals(key)) return 1;
            }
        }
        return 0;
    }
}
