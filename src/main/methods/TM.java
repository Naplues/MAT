package main.methods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import main.Settings;
import main.Statistics;
import others.FileHandle;
import others.tm.process.DataReader;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stopwords.WordsFromFile;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import others.tm.model.EnsembleLearner;

public class TM extends Method {

    {
        methodPath = rootPath + "tm/";
    }

    public static void main(String args[]) throws Exception {

        new TM().predict();
//        new TM().predictWithLimitedTrainingSet();
    }


    public void prepareData() {
        System.out.println("Preparing data for Pattern");
        DataReader.readComments(originPath);  //读取注释数据，每个元素代表一条注释

        // 将（训练集和测试集）中的字符串转换为词向量
        WordsFromFile stopWords = new WordsFromFile();
        stopWords.setStopwords(new File(rootPath + "/dic/stopwords.txt")); // 停用词列表

        StringToWordVector stw = new StringToWordVector(100000);
        stw.setOutputWordCounts(true); //设置记录单词在文档中出现的次数（词频变量）若使用TFIDF公式，该选项必须设置为true
        stw.setTFTransform(true);      //执行TF转换 TF(t,d)=log(f(t,d)+1)
        stw.setIDFTransform(true);     //执行IDF(t,D)=log(|D|/|{d in D, t in d}|)
        stw.setStemmer(new SnowballStemmer());
        stw.setStopwordsHandler(stopWords);

        for (String project : projects) {
            String filePath = methodPath + "data--" + project + ".arff";
            System.out.println(filePath);
            DataReader.outputArffData(DataReader.selectProject(project), filePath);
        }
    }

    /**
     * n-1 -> 1 prediction
     */
    public void predict() throws Exception {
        prepareData();

        DataReader.readComments(originPath);

        WordsFromFile stopWords = new WordsFromFile();
        stopWords.setStopwords(new File(rootPath + "/dic/stopwords.txt"));

        StringToWordVector stw = new StringToWordVector(100000);
        stw.setOutputWordCounts(true);
        stw.setTFTransform(true);      // TF(t,d)=log(f(t,d)+1)
        stw.setIDFTransform(true);     // IDF(t,D)=log(|D|/|{d in D, t in d}|)
        stw.setStemmer(new SnowballStemmer());
        stw.setStopwordsHandler(stopWords);

        String trainDataPath, testDataPath;
        double ratio = 0.1;
        // Processing each test project
        for (int target = 0; target < Settings.projectNames.length; target++) {
            System.out.print("Target: " + Settings.projectNames[target] + ", ");
            testDataPath = methodPath + "data--" + Settings.projectNames[target] + ".arff";

            EnsembleLearner eLearner = new EnsembleLearner();
            // Processing each training project
            for (int source = 0; source < Settings.projectNames.length; source++) {
                trainDataPath = methodPath + "data--" + Settings.projectNames[source] + ".arff";
                if (source == target) continue;

                if (eLearner.getTestData() == null) {
                    System.out.println(testDataPath);
                    Instances tmp = DataSource.read(testDataPath);
                    tmp.setClassIndex(1);
                    eLearner = new EnsembleLearner(tmp);
                }
                Instances trainSet = DataSource.read(trainDataPath);
                Instances testSet = DataSource.read(testDataPath);
                stw.setInputFormat(trainSet);
                trainSet = Filter.useFilter(trainSet, stw);
                testSet = Filter.useFilter(testSet, stw);
                trainSet.setClassIndex(0);
                testSet.setClassIndex(0);

                // feature selection IG
                AttributeSelection attSelection = new AttributeSelection();
                Ranker ranker = new Ranker();
                ranker.setNumToSelect((int) (trainSet.numAttributes() * ratio)); // the selection ratio
                InfoGainAttributeEval ifg = new InfoGainAttributeEval();
                attSelection.setEvaluator(ifg);
                attSelection.setSearch(ranker);
                attSelection.setInputFormat(trainSet);
                trainSet = Filter.useFilter(trainSet, attSelection);
                testSet = Filter.useFilter(testSet, attSelection);

                // NBM classifier
                Classifier classifier = new NaiveBayesMultinomial();
                classifier.buildClassifier(trainSet);

                for (int i = 0; i < testSet.numInstances(); i++) {
                    Instance instance = testSet.instance(i);
                    double score;
                    if (classifier.classifyInstance(instance) == 1.0) score = 1;
                    else score = -1;
                    eLearner.vote(i, score);
                }
            } // end for training project */
            double[] predictionLabels = eLearner.evaluate();
            String resultPath = Settings.resultPath + "MTO_TM/result--" + Settings.projectNames[target] + ".txt";
            FileHandle.writeDoubleArrayToFile(resultPath, predictionLabels);

        } // end for test project */
        Statistics.evaluate("TM");
    }


    /**
     * 1 -> 1 prediction
     */
    public void predictWithLimitedTrainingSet() throws Exception {
        prepareData();

        List<Double> P = new ArrayList<>();
        List<Double> R = new ArrayList<>();
        List<Double> F1 = new ArrayList<>();

        DataReader.readComments(originPath);  //读取注释数据，每个元素代表一条注释

        // 将（训练集和测试集）中的字符串转换为词向量
        WordsFromFile stopWords = new WordsFromFile();
        stopWords.setStopwords(new File(rootPath + "/dic/stopwords.txt")); // 停用词列表

        StringToWordVector stw = new StringToWordVector(100000);
        stw.setOutputWordCounts(true); //设置记录单词在文档中出现的次数（词频变量）若使用TFIDF公式，该选项必须设置为true
        stw.setTFTransform(true);      //执行TF转换 TF(t,d)=log(f(t,d)+1)
        stw.setIDFTransform(true);     //执行IDF(t,D)=log(|D|/|{d in D, t in d}|)
        stw.setStemmer(new SnowballStemmer());
        stw.setStopwordsHandler(stopWords);

        // 训练测试数据
        String trainDataPath, testDataPath;
        double ratio = 0.1;
        // 每个测试项目
        for (int test = 0; test < Settings.projectNames.length; test++) {
            System.out.println("Target: " + Settings.projectNames[test] + ", ");
            testDataPath = methodPath + "data--" + Settings.projectNames[test] + ".arff";

            StringBuilder text = new StringBuilder("Training project, P, R, F1\n");
            double precision = .0, recall = .0, f1 = .0;
            // 每个训练项目
            for (int train = 0; train < Settings.projectNames.length; train++) {
                //if (source == target) continue;
                trainDataPath = methodPath + "data--" + Settings.projectNames[train] + ".arff";
                // 集成学习器
                Instances tmp = DataSource.read(testDataPath);
                tmp.setClassIndex(1);  //类标记索引
                EnsembleLearner eLearner = new EnsembleLearner(tmp);

                Instances trainSet = DataSource.read(trainDataPath);
                Instances testSet = DataSource.read(testDataPath);
                stw.setInputFormat(trainSet);
                trainSet = Filter.useFilter(trainSet, stw);
                testSet = Filter.useFilter(testSet, stw);
                trainSet.setClassIndex(0);
                testSet.setClassIndex(0);

                // 对训练集进行特征选择 IG
                AttributeSelection attSelection = new AttributeSelection();
                Ranker ranker = new Ranker();
                ranker.setNumToSelect((int) (trainSet.numAttributes() * ratio)); //设置选择特征的数目
                InfoGainAttributeEval ifg = new InfoGainAttributeEval();
                attSelection.setEvaluator(ifg);  //使用IG评估
                attSelection.setSearch(ranker);  // 设置搜索
                attSelection.setInputFormat(trainSet); //设置输入格式
                trainSet = Filter.useFilter(trainSet, attSelection);
                testSet = Filter.useFilter(testSet, attSelection);

                // NBM分类器
                Classifier classifier = new NaiveBayesMultinomial();
                classifier.buildClassifier(trainSet);

                for (int i = 0; i < testSet.numInstances(); i++) {
                    Instance instance = testSet.instance(i);
                    double score;
                    if (classifier.classifyInstance(instance) == 1.0) score = 1;
                    else score = -1;
                    eLearner.vote(i, score);
                }

                double[] predictionLabels = eLearner.evaluate();
                String outPath = Settings.resultPath + "OTO_NLP/result--" + Settings.projectNames[train] + "-" + Settings.projectNames[test] + ".txt";
                FileHandle.writeDoubleArrayToFile(outPath, predictionLabels);

                eLearner.evaluate();
                precision += eLearner.getPrecision();
                recall += eLearner.getRecall();
                f1 += eLearner.getFmeasure();

                text.append(Settings.projectNames[train]).append(", ")
                        .append(eLearner.getPrecision()).append(", ")
                        .append(eLearner.getRecall()).append(", ")
                        .append(eLearner.getFmeasure()).append("\n");
            } //end for training project

            int len = projects.length - 1;
            P.add(precision / len);
            R.add(recall / len);
            F1.add(f1 / len);
            FileHandle.writeStringToFile(Settings.resultPath + "OTO_TM/" + Settings.projectNames[test] + ".csv", text.toString());
        } // end for test project */

        // print result
        List<String> r = new ArrayList<>();
        for (int i = 0; i < projects.length; i++) {
            System.out.printf("Avg., %.3f, %.3f, %.3f\n", P.get(i), R.get(i), F1.get(i));
            r.add("Avg., " + P.get(i) + ", " + R.get(i) + ", " + F1.get(i));
        }

        FileHandle.writeLinesToFile(Settings.resultPath + "OTO_TM/Evaluation_all.csv", r);
    }
}
