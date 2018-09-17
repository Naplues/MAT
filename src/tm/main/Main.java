package tm.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tm.process.DataReader;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stopwords.WordsFromFile;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import tm.domain.Document;
import tm.model.EnsembleLearner;

public class Main {

    public static void main(String args[]) throws Exception {

        // 项目名称
        List<String> projects = new ArrayList<>();
        projects.add("argouml");
        projects.add("columba-1.4-src");
        projects.add("hibernate-distribution-3.3.2.GA");
        projects.add("jEdit-4.2");
        projects.add("jfreechart-1.0.19");
        projects.add("apache-jmeter-2.10");
        projects.add("jruby-1.4.0");
        projects.add("sql12");

        String trainingDataPath = "tmp/trainingData.arff";
        String testingDataPath = "tmp/testingData.arff";

        double ratio = 0.1;
        List<Document> comments = DataReader.readComments("data/");  //读取注释数据，每个元素代表一条注释
        // 每个测试项目
        for (int target = 0; target < projects.size(); target++) {
            System.out.println("targe project: " + projects.get(target));

            EnsembleLearner eLearner = new EnsembleLearner();

            Set<String> projectForTesting = new HashSet<>();
            projectForTesting.add(projects.get(target));

            List<Document> testDoc = DataReader.selectProject(comments, projectForTesting);

            // 每个训练项目
            for (int source = 0; source < projects.size(); source++) {
                if (source == target) continue;

                Set<String> projectForTraining = new HashSet<>();
                projectForTraining.add(projects.get(source));

                List<Document> trainDoc = DataReader.selectProject(comments, projectForTraining);

                // 构建训练集和测试集
                DataReader.outputArffData(trainDoc, trainingDataPath);
                DataReader.outputArffData(testDoc, testingDataPath);

                if (eLearner.getTestData() == null) {
                    Instances tmp = DataSource.read(testingDataPath);
                    tmp.setClassIndex(1);  //类标记索引
                    eLearner = new EnsembleLearner(tmp);
                }

                // string to word vector (both for training and testing data)
                StringToWordVector stw = new StringToWordVector(100000);
                stw.setOutputWordCounts(true);
                stw.setIDFTransform(true);
                stw.setTFTransform(true);
                SnowballStemmer stemmer = new SnowballStemmer();
                stw.setStemmer(stemmer);
                WordsFromFile stopwords = new WordsFromFile();
                stopwords.setStopwords(new File("dic/stopwords.txt"));
                stw.setStopwordsHandler(stopwords);

                Instances trainSet = DataSource.read(trainingDataPath);
                Instances testSet = DataSource.read(testingDataPath);
                stw.setInputFormat(trainSet);
                trainSet = Filter.useFilter(trainSet, stw);
                trainSet.setClassIndex(0);
                testSet = Filter.useFilter(testSet, stw);
                testSet.setClassIndex(0);

                // 生成arff文件
                ArffSaver saver = new ArffSaver();
                saver.setInstances(trainSet);
                saver.setFile(new File("./data/" + projects.get(source) + ".arff"));
                saver.writeBatch();

                // 对训练集进行特征选择 IG
                AttributeSelection attSelection = new AttributeSelection();
                Ranker ranker = new Ranker();
                ranker.setNumToSelect((int) (trainSet.numAttributes() * ratio));
                InfoGainAttributeEval ifg = new InfoGainAttributeEval();
                attSelection.setEvaluator(ifg);
                attSelection.setSearch(ranker);
                attSelection.setInputFormat(trainSet);
                trainSet = Filter.useFilter(trainSet, attSelection);
                testSet = Filter.useFilter(testSet, attSelection);

                // 分类器
                Classifier classifier = new NaiveBayesMultinomial();
                classifier.buildClassifier(trainSet);

                for (int i = 0; i < testSet.numInstances(); i++) {
                    Instance instance = testSet.instance(i);
                    double score;
                    if (classifier.classifyInstance(instance) == 1.0) score = 1;
                    else score = -1;
                    eLearner.vote(i, score);
                }
            }
            eLearner.evaluate();
        }
    }
}
