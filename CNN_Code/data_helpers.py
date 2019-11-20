# -*- encoding：utf-8-*-
# by xiaoxueren
# data processing
import numpy as np
import re
import itertools
from collections import Counter
import tensorflow as tf


# 清洗字符串
def clean_str(string):
    """
    Tokenization/string cleaning for all datasets except for SST.
    Original taken from https://github.com/yoonkim/CNN_sentence/blob/master/process_data.py
    """
    string = re.sub(r"[^A-Za-z0-9(),\+!?\'\`]", " ", string)
    string = re.sub(r"\'s", " \'s", string)
    string = re.sub(r"\'ve", " \'ve", string)
    string = re.sub(r"n\'t", " n\'t", string)
    string = re.sub(r"\'re", " \'re", string)
    string = re.sub(r"\'d", " \'d", string)
    string = re.sub(r"\'ll", " \'ll", string)
    string = re.sub(r",", " , ", string)
    string = re.sub(r"!", " ! ", string)
    string = re.sub(r"\(", " \( ", string)
    string = re.sub(r"\)", " \) ", string)
    string = re.sub(r"\?", " \? ", string)
    string = re.sub(r"\+", " \+ ", string)
    string = re.sub(r"\s{2,}", " ", string)
    return string.strip().lower()

# 判断字符串是否为空
def not_empty(s):
    return s and s.strip()

def load_model(checkpoint_file, allow_soft_placement, log_device_placement, initW, source_x):
    graph = tf.Graph()

    with graph.as_default():
        session_conf = tf.ConfigProto(
            allow_soft_placement = allow_soft_placement,
            log_device_placement = log_device_placement)

        sess = tf.Session(config = session_conf)

        with sess.as_default():

            saver = tf.train.import_meta_graph("{}.meta".format(checkpoint_file))
            saver.restore(sess, checkpoint_file)
            input_x = graph.get_operation_by_name("input_x").outputs[0]
            # em = graph.get_operation_by_name("input_y").outputs[0]
            dropout_keep_prob = graph.get_operation_by_name("dropout_keep_prob").outputs[0]
            embedding         = graph.get_operation_by_name("embedding").outputs[0]
            W                 = graph.get_operation_by_name("W").outputs[0]
            hp                = graph.get_operation_by_name("dawn_pool_flat").outputs[0]
            phase_train       = graph.get_operation_by_name("phase_train").outputs[0]
            #   em = sess.run(embedding,{input_x: x_train, dropout_keep_prob: 0.5, embedding: initW})
            W                 = sess.run(W, {input_x: source_x, dropout_keep_prob:0.5,  embedding: initW,phase_train:True})
            hp                = sess.run(hp, {input_x: source_x, dropout_keep_prob: 0.5,  embedding: initW,phase_train:True})
            return W,hp


# 加载数据
def load_data_and_labels(files):
    """
    Loads MR polarity data from files, splits the data into words and generates labels.
    Returns split sentences and labels.
    """
    # Load data from files

    labelSize = len(files) # = 2
    x_text    = np.array([])
    y_label   = np.array([])
    for fileIndex, file in enumerate(files):
        # read all sentences and split each sentence into words
        sentences = list(open(file, "r",encoding = 'utf-8').readlines())  # 读取文件内容, 转化为列表
        sentences = [s.strip() for s in sentences]                        # 去除前后的空格
        sentences = [clean_str(s) for s in sentences]                     # 清洗字符串
        sentences = list(filter(not_empty, sentences))                    # 过滤掉空的字符串注释 每个s是一个注释 sentences是注释集合
        sentences = np.array(sentences)                                   # 创建数组
        x_text = np.concatenate([x_text, sentences], 0)                   # 先将nonSATD加入列表，后将SATD考入列表
        # generate labels for each sentence(comment)
        labels = [ [0 for x in range(labelSize)] for i in range(len(sentences)) ] # [[0, 0], [0, 0], ..., [0, 0]]
        for label in labels: label[fileIndex] = 1 # for nonSATD [[1, 0], [1, 0], ..., [1, 0]] or for SATD [[0, 1], [0, 1], ..., [0, 1]]
        # 0 代表 nonSATD
        if fileIndex == 0:
            y_label = np.array(labels)
        else:
            y_label = np.concatenate([y_label,labels], 0)

    return [x_text, y_label]


def batch_iter(data, batch_size, num_epochs, shuffle=True):
    """
    Generates a batch iterator for a dataset.
    """
    data = np.array(data)
    data_size = len(data)
    num_batches_per_epoch = int(len(data)/batch_size) + 1
    for epoch in range(num_epochs):
        # Shuffle the data at each epoch
        if shuffle:
            shuffle_indices = np.random.permutation(np.arange(data_size))
            shuffled_data = data[shuffle_indices]
        else:
            shuffled_data = data
        for batch_num in range(num_batches_per_epoch):
            start_index = batch_num * batch_size
            end_index = min((batch_num + 1) * batch_size, data_size)
            yield shuffled_data[start_index:end_index]


def load_embedding_vectors_word2vec(vocabulary, dv,filename, vector_size):
    import gensim
    embedding_vectors = np.random.uniform(-0.25, 0.25, (len(dv), 200))
    # load embedding_vectors from the word2vec
    model = gensim.models.Word2Vec.load(filename)

    for word in dv:
        idx=vocabulary.get(key)
        if idx != 0:
            embedding_vectors[idx] = model.wv(word)


    return embedding_vectors

def load_embedding_vectors_glove(vocabulary, filename, vector_size):
    # load embedding_vectors from the glove
    # initial matrix with random uniform
    embedding_vectors = np.random.uniform(-0.25, 0.25, (len(vocabulary), vector_size))
    f = open(filename)
    for line in f:
        values = line.split()
        word = values[0]
        vector = np.asarray(values[1:], dtype="float32")
        idx = vocabulary.get(word)

        if idx != 0:
            embedding_vectors[idx] = vector

    f.close()
    return embedding_vectors

