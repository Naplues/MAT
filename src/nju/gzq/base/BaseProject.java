package nju.gzq.base;

import nju.gzq.selector.FileHandle;

import java.util.List;

/**
 * BaseProject: Built-in Project class which defines the data structure of a target project
 */
public class BaseProject {
    private String[] projectNames;
    private BaseFeature[][] features;


    public BaseProject(String path, int labelIndex) {

        projectNames = new String[1];
        features = new BaseFeature[1][];
        for (int i = 0; i < features.length; i++) {
            projectNames[i] = path;

            List<String> lines = FileHandle.readFileToLines(path); //数据行,每行代表一条特征实例,第一行为名称

            features[i] = new BaseFeature[lines.size() - 1]; //有feature[i].length 个特征实例

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
}
