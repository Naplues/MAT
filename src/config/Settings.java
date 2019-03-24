package config;

import tm.process.DataReader;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stopwords.WordsFromFile;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;

public class Settings {
    public static String[] projectNames = {
            "argouml",
            "columba-1.4-src",
            "hibernate-distribution-3.3.2.GA",
            "jEdit-4.2",
            "jfreechart-1.0.19",
            "apache-jmeter-2.10",
            "jruby-1.4.0",
            "sql12",
            "apache-ant-1.7.0",
            "emf-2.4.1"
    };


    public static void main(String[] args) throws Exception {
        //生成tm和mat的数据
        generateData();
    }

    public static void generateData() throws Exception {
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
        for (int i = 0; i < projectNames.length; i++) {
            String filePath = "data/tm/data--" + projectNames[i] + ".arff";
            DataReader.outputArffData(DataReader.selectProject(projectNames[i]), filePath);
            Instances dataSet = ConverterUtils.DataSource.read(filePath);
            stw.setInputFormat(dataSet);
            dataSet = Filter.useFilter(dataSet, stw);
            dataSet.setClassIndex(0);

            filePath = "data/mat/data--" + projectNames[i] + ".txt";
            DataReader.outputArffData(DataReader.selectProject(projectNames[i]), filePath);
        }
    }
}
