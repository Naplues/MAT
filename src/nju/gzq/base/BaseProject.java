package nju.gzq.base;

import nju.gzq.selector.FileHandle;

import java.io.File;
import java.util.List;

/**
 * BaseProject: Built-in Project class which defines the data structure of a target project
 */
public class BaseProject {
    private String projectName;
    private String[] dataFileNames;
    private BaseFeature[][] features;


    public BaseProject(String path, int labelIndex) {
        projectName = new File(path).getName();
        File[] dataFiles = new File(path).listFiles(); // all data files in a project
        dataFileNames = new String[dataFiles.length];
        features = new BaseFeature[dataFiles.length][];
        for (int i = 0; i < features.length; i++) {
            dataFileNames[i] = dataFiles[i].getPath();
            //数据行,每行代表一条特征实例,第一行为名称
            List<String> lines = FileHandle.readFileToLines(dataFiles[i].getPath());
            //有feature[i].length 个特征实例
            features[i] = new BaseFeature[lines.size() - 1];

            for (int j = 0; j < features[i].length; j++) {
                features[i][j] = new BaseFeature(lines.get(j + 1).split(","), labelIndex);
            }
        }
    }

    public void setFeatures(BaseFeature[][] features) {
        this.features = features;
    }

    public BaseFeature[][] getFeatures() {
        return features;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String[] getDataFileNames() {
        return dataFileNames;
    }

    public void setDataFileNames(String[] dataFileNames) {
        this.dataFileNames = dataFileNames;
    }
}
