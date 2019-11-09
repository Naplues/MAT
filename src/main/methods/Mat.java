package main.methods;

import others.FileHandle;

import java.util.List;

public class Mat extends Method {

    {
        methodPath = rootPath + "mat/";
    }

    //"todo", "hack", "fixme", "xxx"  "workaround","tbd", "dms", "revisit", "notused"
    public static String[] keyWords = {"todo", "hack", "fixme", "xxx",};

    public static void main(String[] args) {
        new Mat().predict();
    }


    public void prepareData() {
        // Do nothing
    }

    public void predict() {
        for (String project : projects) {

            List<String> instances = FileHandle.readFileToLines(dataPath + "data--" + project + ".arff");
            List<String> originComments = FileHandle.readFileToLines(originPath + "data--" + project + ".txt");
            // 测试实例数目 测试文件前7行不是注释,因此要去掉
            int[] predicts = new int[instances.size() - 7];

            for (int i = 7, index = 0; i < instances.size(); i++, index++) {
                predicts[index] = classify(instances.get(i).split(",")[0], keyWords, true);
                //if (originComments.get(index).trim().endsWith("?")) predicts[index] = 1;
            }

            FileHandle.writeIntegerArrayToFile(methodPath + "result--" + project + ".txt", predicts);
        }
        System.out.println("MAT prediction finished!");
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
