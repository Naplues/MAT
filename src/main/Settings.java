package main;

import others.FileHandle;
import others.tm.process.DataReader;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stopwords.WordsFromFile;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.util.List;

public class Settings {

    public static String rootPath = "exp_data/";


    public static String[] projectNames = {

            "Ant",
            "ArgoUML",
            "Columba",
            "EMF",
            "Hibernate",
            "JEdit",
            "JFreeChart",
            "JMeter",
            "JRuby",
            "SQuirrel",

            "Dubbo",
            "Gradle",
            "Groovy",
            "Hive",
            "Maven",
            "Poi",
            "SpringFramework",
            "Storm",
            "Tomcat",
            "Zookeeper", // */
    };


    public static void main(String[] args) throws Exception {
        //生成tm和mat的数据
        //generateData();
        getNum();
    }

    public static void generateData() throws Exception {
        DataReader.readComments(rootPath + "/origin/");  //读取注释数据，每个元素代表一条注释
        // 将（训练集和测试集）中的字符串转换为词向量
        WordsFromFile stopWords = new WordsFromFile();
        stopWords.setStopwords(new File(rootPath + "/dic/stopwords.txt")); // 停用词列表

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

            filePath = "data/others/data--" + projectNames[i] + ".txt";
            //DataReader.outputArffData(DataReader.selectProject(projectNames[i]), filePath);
        }
    }


    public static void getJitterbugResult() {

        for (String project : projectNames) {
            List<String> easyLines = FileHandle.readFileToLines("data/new/easy/result--" + project + ".txt");
            List<String> hardLines = FileHandle.readFileToLines("data/new/hard/" + project + ".csv");

            for (int i = 1; i < hardLines.size(); i++) {
                String[] split = hardLines.get(i).split(",");
                if (!split[6].trim().equals(project)) continue;
                String id = split[1].trim();
                String prediction = split[3].trim();
                easyLines.set(Integer.parseInt(id), prediction.equals("yes") ? "1" : "0");
            }//*/

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < easyLines.size(); i++) text.append(easyLines.get(i)).append("\n");
            FileHandle.writeStringToFile("data/new/jitterbug/result--" + project + ".txt", text.toString());
        }
    }

    public static void getNum() {
        System.out.println("Total,Recommend,SATD in Recommend,SATD in Easy,Project\n");
        for (String project : projectNames) {
            List<String> easyLines = FileHandle.readFileToLines("data/new/easy/result--" + project + ".txt");
            List<String> hardLines = FileHandle.readFileToLines("data/new/hard/" + project + ".csv");
            int count = 0;
            int yes = 0;
            int hard = 0;
            for (int i = 1; i < hardLines.size(); i++) {
                String[] split = hardLines.get(i).split(",");
                if (!split[6].trim().equals(project)) continue;
                String id = split[1].trim();
                String prediction = split[3].trim();
                count += prediction.equals("undetermined") ? 0 : 1;
                yes += prediction.equals("yes") ? 1 : 0;
                hard++;
            }//*/
            System.out.println(easyLines.size() + "," + count + "," + yes + "," + (easyLines.size() - hard) + "," + project);
        }
    }
}
