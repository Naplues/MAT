package main.methods;

import others.FileHandle;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现NLP方法对注释进行分类
 */
public class NLP extends Method {

    {
        methodPath = rootPath + "nlp/";
    }

    public static void main(String[] args) throws Exception {
        //prepareData();
        new NLP().predict();
        //resultBySingleProject();
    }


    /**
     * 构造数据集
     * 将TM数据集转换为指定格式
     */
    public void prepareData() {

        // 转换测试集格式, 覆盖源文件
        for (String projectName : projects) {
            String inputPath = dataPath + "data--" + projectName + ".arff";
            String outputPath = methodPath + "data--" + projectName + ".txt";
            StringBuilder text = new StringBuilder();
            List<String> lines = FileHandle.readFileToLines(inputPath);
            for (int i = 7; i < lines.size(); i++) {
                String[] splits = lines.get(i).split(",");
                String comment = splits[0].replace("'", "");
                String label = "WITHOUT_CLASSIFICATION";
                if (splits[1].equals("positive")) label = "SATD";
                text.append(label).append("\t ").append(comment).append("\n");
            }
            FileHandle.writeStringToFile(outputPath, text.toString());
        }

        // 构造训练集
        for (String projectName : projects) {
            String trainProjects = methodPath + "train--" + projectName + ".txt";
            StringBuilder text = new StringBuilder();
            for (String testProjectName : projects) {
                if (projectName.equals(testProjectName)) continue;
                List<String> lines = FileHandle.readFileToLines(methodPath + "data--" + testProjectName + ".txt");
                for (String line : lines) text.append(line).append("\n");
            }
            FileHandle.writeStringToFile(trainProjects, text.toString());
        }
    }

    /**
     * 多个源项目预测单个目标项目
     *
     * @throws Exception
     */
    public void predict() throws Exception {

        for (String project : projects) {
            String trainFile = methodPath + "train--" + project + ".txt";
            String testFile = methodPath + "data--" + project + ".txt";
            String resultFile = methodPath + "result--" + project + ".txt";
            StringBuilder text = new StringBuilder();

            ColumnDataClassifier cdc = new ColumnDataClassifier(methodPath + "cheese2007.prop");
            cdc.trainClassifier(trainFile);

            for (String line : ObjectBank.getLineIterator(testFile, "utf-8")) {
                Datum<String, String> d = cdc.makeDatumFromLine(line);
                System.out.printf("%s  ==>  %s (%.4f)%n", line, cdc.classOf(d), cdc.scoresOf(d).getCount(cdc.classOf(d)));
                if (cdc.classOf(d).equals("WITHOUT_CLASSIFICATION")) text.append("0").append("\n");
                else text.append("1").append("\n");
            }

            FileHandle.writeStringToFile(resultFile, text.toString());
        }
    }

    /**
     * 单个源项目预测单个目标项目
     *
     * @throws IOException
     *//*
    public static void resultBySingleProject() throws IOException {

        for (String testProject : projects) {
            // 处理测试项目
            StringBuilder allText = new StringBuilder();

            String testFile = methodPath + "data--" + testProject + ".txt";
            for (String trainProject : projects) {
                // 处理训练项目
                if (trainProject.equals(testProject)) continue;
                String trainFile = methodPath + "data--" + trainProject + ".txt";
                String resultFile = methodPath + "result--" + testProject + ".txt";
                StringBuilder text = new StringBuilder();

                ColumnDataClassifier cdc = new ColumnDataClassifier(methodPath + "cheese2007.prop");
                cdc.trainClassifier(trainFile);

                for (String line : ObjectBank.getLineIterator(testFile, "utf-8")) {
                    Datum<String, String> d = cdc.makeDatumFromLine(line);
                    if (cdc.classOf(d).equals("WITHOUT_CLASSIFICATION")) text.append("0").append("\n");
                    else text.append("1").append("\n");
                }
                FileHandle.writeStringToFile(resultFile, text.toString());

                List<String> resultFileLines = FileHandle.readFileToLines(resultFile);
                List<String> oracleFileLines = FileHandle.readFileToLines(Settings.rootPath + "/result/" + testProject + ".csv");

                List<String> oracle = new ArrayList<>();
                for (int i = 1; i < oracleFileLines.size(); i++) oracle.add(oracleFileLines.get(i).split(",")[0]);

                allText.append(evaluate(oracle, resultFileLines)).append("\n");
            }
            FileHandle.writeStringToFile(methodPath + "result--" + testProject + ".csv", allText.toString(), true);
        }
    }*/
}