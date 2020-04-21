package main.methods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import main.Settings;
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

        // 预测结果
        // new TM().predict();
        new TM().predictWithLimitedTrainingSet();
    }

    /**
     * 生成数据集
     *
     * @throws Exception
     * @par
     */
    public void prepareData() {
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
     * n-1 -> 1 预测
     */
    public void predict() throws Exception {
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

        StringBuilder text = new StringBuilder("Training project, P, R, F1\n");
        // 训练测试数据
        String trainDataPath, testDataPath;
        double ratio = 0.1;
        // 每个测试项目
        for (int target = 0; target < Settings.projectNames.length; target++) {
            System.out.print("Target: " + Settings.projectNames[target] + ", ");
            testDataPath = methodPath + "data--" + Settings.projectNames[target] + ".arff";


            // 测试集包含19个项目
            StringBuilder testText = new StringBuilder("@relation 'technicalDebt'\n\n" +
                    "@attribute Text string\n" +
                    "@attribute class-att {negative,positive}\n\n" +
                    "@data\n");
            for (int source = 0; source < Settings.projectNames.length; source++) {
                if (source == target) continue;
                List<String> lines = FileHandle.readFileToLines(methodPath + "data--" + Settings.projectNames[source] + ".arff");
                for (int i = 7; i < lines.size(); i++) testText.append(lines.get(i)).append("\n");
            }
            FileHandle.writeStringToFile(methodPath + "data--tmp.arff", testText.toString());
            testDataPath = methodPath + "data--tmp.arff";


            // 集成学习器
            EnsembleLearner eLearner = new EnsembleLearner();
            // 每个训练项目
            for (int source = 0; source < Settings.projectNames.length; source++) {
                trainDataPath = methodPath + "data--" + Settings.projectNames[source] + ".arff";
                if (source == target) continue;
                // 测试数据
                if (eLearner.getTestData() == null) {
                    System.out.println(testDataPath);
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
            double[] predictionLabels = eLearner.evaluate();
            String resultPath = methodPath + "result--" + Settings.projectNames[target] + ".txt";
            FileHandle.writeDoubleArrayToFile(resultPath, predictionLabels);

            text.append(Settings.projectNames[target]).append(", ")
                    .append(eLearner.getPrecision()).append(", ")
                    .append(eLearner.getRecall()).append(", ")
                    .append(eLearner.getFmeasure()).append("\n");
        }// end for 测试集*/
        FileHandle.writeStringToFile("result/oto/tm/Self.csv", text.toString());
    }


    /**
     * 1 -> 1 预测 一个项目作为训练集, 一个项目作为测试集, 为每个测试项目训练n-1个模型, 在测试集上获得n-1ge预测结果,
     * 取其性能的平均值作为最终的性能评价指标
     */
    public void predictWithLimitedTrainingSet() throws Exception {
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
        for (int target = 0; target < Settings.projectNames.length; target++) {
            System.out.println("Target: " + Settings.projectNames[target] + ", ");
            testDataPath = methodPath + "data--" + Settings.projectNames[target] + ".arff";

            StringBuilder text = new StringBuilder("Training project, P, R, F1\n");
            double precision = .0, recall = .0, f1 = .0;
            // 每个训练项目
            for (int source = 0; source < Settings.projectNames.length; source++) {
                //if (source == target) continue;
                trainDataPath = methodPath + "data--" + Settings.projectNames[source] + ".arff";
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

                // 进行预测
                eLearner.evaluate();
                precision += eLearner.getPrecision();
                recall += eLearner.getRecall();
                f1 += eLearner.getFmeasure();

                text.append(Settings.projectNames[source]).append(", ")
                        .append(eLearner.getPrecision()).append(", ")
                        .append(eLearner.getRecall()).append(", ")
                        .append(eLearner.getFmeasure()).append("\n");
            } //end for 训练集
            int len = projects.length - 1;
            System.out.printf(projects[target] + " Avg. %.3f, %.3f, %.3f\n", precision / len, recall / len, f1 / len);
            //System.out.println("\n================================");
            FileHandle.writeStringToFile("result/oto/tm/" + Settings.projectNames[target] + ".csv", text.toString());
        } // end for 测试集*/
    }
}
