package main.methods;

import main.Statistics;
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
        //new NLP().predict();
        new NLP().predictWithLimitedTrainingSet();
    }


    /**
     * 构造数据集
     * 将TM数据集转换为指定格式
     */
    public void prepareData() {

        // 转换测试集格式, 覆盖源文件
        /*
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
        }//*/

        // 构造训练集
        for (String projectName : projects) {
            String trainProjects = methodPath + "train--" + projectName + ".txt";
            String trainLabels = methodPath + "label--" + projectName + ".txt";
            StringBuilder text = new StringBuilder();
            StringBuilder labelText = new StringBuilder();
            for (String testProjectName : projects) {
                if (projectName.equals(testProjectName)) continue;
                List<String> lines = FileHandle.readFileToLines(methodPath + "data--" + testProjectName + ".txt");
                for (String line : lines) text.append(line).append("\n");

                List<String> labelLines = FileHandle.readFileToLines("data/origin/label--" + testProjectName + ".txt");
                for (String line : labelLines) labelText.append(line).append("\n");
            }
            FileHandle.writeStringToFile(trainProjects, text.toString());
            FileHandle.writeStringToFile(trainLabels, labelText.toString());
        }
    }

    /**
     * 多个源项目预测单个目标项目
     *
     * @throws Exception
     */
    /*
    public void predict() throws Exception {

        for (String project : projects) {
            String trainFile = methodPath + "train--" + project + ".txt";
            String testFile = methodPath + "train--" + project + ".txt";
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

     //*/

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
        Statistics.combineResult();
    }//*/

    /**
     * 单个源项目预测单个目标项目
     *
     * @throws IOException
     */
    public void predictWithLimitedTrainingSet() throws IOException {

        List<Double> P = new ArrayList<>();
        List<Double> R = new ArrayList<>();
        List<Double> F1 = new ArrayList<>();
        for (String testProject : projects) {
            StringBuilder text = new StringBuilder("Training project, P, R, F1\n");
            double precision = .0, recall = .0, f1 = .0;

            // 处理测试项目
            String testFile = methodPath + "data--" + testProject + ".txt";
            for (String trainProject : projects) {
                // 处理训练项目
                //if (trainProject.equals(testProject)) continue;
                String trainFile = methodPath + "data--" + trainProject + ".txt";
                String oracleFile = originPath + "label--" + testProject + ".txt";

                List<String> resultFileLines = new ArrayList<>();
                ColumnDataClassifier cdc = new ColumnDataClassifier(methodPath + "cheese2007.prop");
                cdc.trainClassifier(trainFile);

                for (String line : ObjectBank.getLineIterator(testFile, "utf-8")) {
                    Datum<String, String> d = cdc.makeDatumFromLine(line);
                    if (cdc.classOf(d).equals("WITHOUT_CLASSIFICATION")) resultFileLines.add("0");
                    else resultFileLines.add("1");
                }

                List<String> oracleFileLines = FileHandle.readFileToLines(oracleFile);

                double[] scores = evaluate(oracleFileLines, resultFileLines);
                precision += scores[0];
                recall += scores[1];
                f1 += scores[2];
                text.append(trainProject).append(", ")
                        .append(scores[0]).append(", ")
                        .append(scores[1]).append(", ")
                        .append(scores[2]).append("\n");
            } // end for train project
            // 收集结果
            int len = projects.length - 1;
            P.add(precision / len);
            R.add(recall / len);
            F1.add(f1 / len);
            FileHandle.writeStringToFile("result/oto/nlp/" + testProject + ".csv", text.toString());

        } // end for a test project

        // 打印结果
        System.out.println();
        for (int i = 0; i < projects.length; i++)
            System.out.printf("%.3f, %.3f, %.3f\n", P.get(i), R.get(i), F1.get(i));
    }
}