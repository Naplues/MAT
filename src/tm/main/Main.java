package tm.main;

import java.io.File;


import config.Settings;
import tm.process.DataReader;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.Saver;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stopwords.WordsFromFile;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import tm.domain.Document;
import tm.model.EnsembleLearner;

public class Main {

    public static String rootPath = "data/new/"; // data/tm

    public static void main(String args[]) throws Exception {
        // 训练测试数据
        String trainDataPath, testDataPath;
        double ratio = 0.1;
        DataReader.readComments("data/origin/");  //读取注释数据，每个元素代表一条注释
        // 将（训练集和测试集）中的字符串转换为词向量
        WordsFromFile stopWords = new WordsFromFile();
        stopWords.setStopwords(new File("dic/stopwords.txt")); // 停用词列表

        StringToWordVector stw = new StringToWordVector(100000);
        stw.setOutputWordCounts(true); //设置记录单词在文档中出现的次数（词频变量）若使用TFIDF公式，该选项必须设置为true
        stw.setTFTransform(true);      //执行TF转换 TF(t,d)=log(f(t,d)+1)
        stw.setIDFTransform(true);     //执行IDF(t,D)=log(|D|/|{d in D, t in d}|)
        stw.setStemmer(new SnowballStemmer());
        stw.setStopwordsHandler(stopWords);

        //准备实验数据
        // generateData(stw, Settings.projectNames);

        // 每个测试项目
        for (int target = 0; target < Settings.projectNames.length; target++) {
            System.out.println("target project: " + Settings.projectNames[target]);
            testDataPath = rootPath + "data--" + Settings.projectNames[target] + ".arff";
            // 集成学习器
            EnsembleLearner eLearner = new EnsembleLearner();
            // 每个训练项目
            for (int source = 0; source < Settings.projectNames.length; source++) {
                trainDataPath = rootPath + "data--" + Settings.projectNames[source] + ".arff";
                if (source == target) continue;

                // 测试数据
                if (eLearner.getTestData() == null) {
                    Instances tmp = DataSource.read(testDataPath);
                    tmp.setClassIndex(1);  //类标记索引
                    eLearner = new EnsembleLearner(tmp);
                }
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
            } //end for 训练集
            eLearner.evaluate();
        }// end for 测试集*/
    }


    /**
     * 生成数据集
     *
     * @param stw
     * @param projectNames
     * @throws Exception
     */
    public static void generateData(StringToWordVector stw, String[] projectNames) throws Exception {
        for (int i = 0; i < projectNames.length; i++) {
            String filePath = rootPath + "/data--" + projectNames[i] + ".arff";
            DataReader.outputArffData(DataReader.selectProject(projectNames[i]), filePath);
            Instances dataSet = DataSource.read(filePath);
            stw.setInputFormat(dataSet);
            dataSet = Filter.useFilter(dataSet, stw);
            dataSet.setClassIndex(0);

            /*
            // 生成arff文件
            Saver saver = new ArffSaver();
            saver.setInstances(dataSet);
            saver.setFile(new File(rootPath + projectNames[i] + ".arff"));
            saver.writeBatch();
            */
            /*
            // 生成csv文件
            saver = new CSVSaver();
            saver.setInstances(dataSet);
            saver.setFile(new File(rootPath + projectNames[i] + ".csv"));
            saver.writeBatch();
            */
        }
    }
}
