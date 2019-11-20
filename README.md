# MAT


## Identifying self-admitted technical debts: how far are we?

This repository stores the **source codes** of the four state-of-the-art SATD comments detection approaches, and **20 Java projects** whose comments were manually labeled by Maldonado et al. (10) and ourselves (10).

### 1. Folders Introduction

##### MAT/data/{old, new}/{approach}

This floder stores the experimental data and classification result of a specific `approach` based on a specific `dataset`. Note that, `approach` is one of { Pattern, NLP, TM and MAT } and `dataset` is one of {old and new}.

##### MAT/data/result

This folder stores all classification results of the four approaches.


### 2. Approach Summary

Year | Authors          | Approach | isSupervised | Description
---- | :------          | :------: | :----------: | :-------------
2015 | Potdar et al.    | Pattern  |      No      | Pattern (key words) matching
2017 | Maldonado et al. | NLP      |     Yes      | Natural language processing
2018 | Huang et al.     | TM       |     Yes      | Text mining
2019 | Ren et al.       | CNN      |     Yes      | Convolutional Neural Network

### 2. Dataset Summary

##### 2.1 Projects labeled by Maldonado et al.
Project    | Release | Contributors | #Classes | #Comments | #After flitering | SATD | % of SATD
-------    | :-----: | -----------: | -----: | :-------: | :--------------: | ---: | --------:
Ant        | 1.7.0   |     74       |  1,475 |   21,587  |       3,052      | 102  |   0.47%
ArgoUML    | 0.34    |     87       |  2,609 |  67,716   |       5,426      | 969  |   1.43%
Columba    | 1.4     |      9       |  1,711 |  33,895   |       4,090      | 128  |   0.38%
EMF        | 2.4.1   |     30       |  1,458 |  25,229   |       2,585      |  74  |   0.29%
Hibernate  | 3.3.2   |    226       |  1,356 |  11,630   |       2,492      | 377  |   3.24%
JEdit      | 4.2     |     57       |    800 |  16,991   |       4,644      | 195  |   1.15%
JFreeChart | 1.0.19  |     19       |  1,065 |  23,474   |       2,494      | 101  |   0.43%
JMeter     | 2.10    |     33       |  1,181 |  20,084   |       4,148      | 282  |   1.40%
JRuby      | 1.4.0   |    328       |  1,486 |  11,149   |       3,652      | 383  |   3.44%
SQuirrel   | 3.0.3   |     46       |  3,108 |  27,474   |       4,473      | 201  |   0.73%
**Total** | -----   |    -     | **16,249** | **259,229** | **37,056** | **2,812** | **1.08%**

##### 2.2 Projects labeled by ourselves.
Project         | Release | Contributors | #Files | #Comments | #After flitering |  SATD | % of SATD
-------         | :-----: | -----------: | -----: | :-------: | :--------------: |  ---: | --------:
Dubbo           | 2.7.4   |    255       |  1,493 |     5,875 |       1,650      |    91 |  1.55%
Gradle          | 5.6.3   |    409       |  7,965 |    15,901 |       3,325      |   307 |  1.93%
Groovy          | 2.5.8   |    284       |  1,526 |    14,199 |       4,436      |   251 |  1.77%
Hive            | 3.1.2   |    192       |  5,817 |    81,127 |      29,341      | 1,047 |  1.29%
Maven           | 3.6.2   |     87       |    886 |     5,448 |       1,220      |   137 |  2.51%
Poi             | 4.1.1   |     12       |  3,477 |    45,666 |      15,034      |   619 |  1.36%
SpringFramework | 5.2.0   |    401       |  6,355 |    42,574 |       7,713      |   106 |  0.25%
Storm           | 2.1.0   |    304       |  2,267 |    12,258 |       3,640      |    91 |  0.74%
Tomcat          | 9.0.27  |     31       |  2,343 |    37,038 |      12,219      |   273 |  0.74%
Zookeeper       | 3.5.6   |     93       |    677 |     6,894 |       2,692      |    66 |  0.96%
**Total**        | ------  |     -    | **32,806** | **266,980** | **81,270** | **2,988** | **1.12%**
