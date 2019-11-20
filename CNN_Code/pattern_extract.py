#by xiaoxueren
# extrating patterns for SATD comments

import tensorflow as tf
import numpy as np
import os
import time
import datetime
import data_helpers
from text_cnn import TextCNN
from tensorflow.contrib import learn
import csv

# Data Parameters

tf.flags.DEFINE_string("target", "target", "target project")
# Eval Parameters
tf.flags.DEFINE_integer("batch_size", 64, "Batch Size (default: 64)")

# Misc Parameter
tf.flags.DEFINE_boolean("allow_soft_placement", True, "Allow device soft device placement")
tf.flags.DEFINE_boolean("log_device_placement", False, "Log placement of ops on devices")


FLAGS = tf.flags.FLAGS
FLAGS._parse_flags()
class_names = ('noneSATD', 'SATD')
mp=["apache-ant","apache-jmeter-2.10","argouml", "columba-1.4-src","emf","hibernate-distribution-3.3.2.GA", "jEdit-4.2", "jfreechart-1.0.19", "jruby-1.4.0", "sql12"]
targets=["apache-ant","apache-jmeter-2.10","argouml", "columba-1.4-src","emf","hibernate-distribution-3.3.2.GA", "jEdit-4.2", "jfreechart-1.0.19", "jruby-1.4.0", "sql12"]
model_project =mp[1]
checkpoint_dir="/home/xiaoxueren/Downloads/satd_cnn3/runs-RQ3/1506695094"
## CHANGE THIS: Load data. Load your own data here
projects = FLAGS.target.split()
target_text = np.array([])
target_y = np.array([])
print(model_project)
project=model_project

target_file_path = "./data/" + project + '/'+project+'/'
target_files = list()
for class_name in class_names:
   target_files.append(target_file_path + class_name)
#target_files.append(target_file_path + "todo")

tmp_text, tmp_y = data_helpers.load_data_and_labels(target_files)
print("target_file:\n")

print (project + ": " + str(len(tmp_text)) + " sentences")
target_text = np.concatenate([target_text, tmp_text], 0)

if len(target_y) == 0:
    target_y = np.array(tmp_y)
else:
    target_y = np.concatenate([target_y, tmp_y], 0)
# Map data into vocabulary
vocab_path = checkpoint_dir + "/vocab"
vocab_processor = learn.preprocessing.VocabularyProcessor.restore(vocab_path)

x_text =  np.array(list(vocab_processor.transform(target_text)))

sentence_lookup=x_text

for c in range(len(sentence_lookup.tolist())):

        f=open("./"+model_project,'a')
        f.write("\n")

#dawn.write(str(sentence_lookup))
print("~~~~~~~~~~~~~~~~~~~~~~~~~~~")

vocab_dict = vocab_processor.vocabulary_._mapping
dawn_vocabulary = list(list(zip(vocab_dict)))

print("\nEvaluating...\n")
# Evaluation
# ==================================================
print(checkpoint_dir)
checkpoint_file = tf.train.latest_checkpoint(checkpoint_dir)
print(checkpoint_file)
graph = tf.Graph()
with graph.as_default():
    session_conf = tf.ConfigProto(
      allow_soft_placement=FLAGS.allow_soft_placement,
      log_device_placement=FLAGS.log_device_placement)
    session_conf.gpu_options.allow_growth = True

    sess = tf.Session(config=session_conf)
    with sess.as_default():
        # Load the saved meta graph and restore variables
        saver = tf.train.import_meta_graph("{}.meta".format(checkpoint_file))
        saver.restore(sess, checkpoint_file)
        all_vars = tf.trainable_variables()

        vocabulary = vocab_processor.vocabulary_
        initW = None
        glove = "glove.6B.300d"
        embedding_dimension = 300

        glove_path = "./data/" + glove + ".txt"
        # Load your word embedding here
        initW = data_helpers.load_embedding_vectors_glove(vocabulary, glove_path,
                                                          embedding_dimension)
        dropout_keep_prob = graph.get_operation_by_name("dropout_keep_prob").outputs[0]
        input_x = graph.get_operation_by_name("input_x").outputs[0]
        input_y = graph.get_operation_by_name("input_y").outputs[0]
        embedding = graph.get_operation_by_name("embedding").outputs[0]
        phase_train = graph.get_operation_by_name("phase_train").outputs[0]
        feed_dict ={input_x:x_text, dropout_keep_prob: 1,  embedding: initW, phase_train:False}
        #---------------------------------------------------------

        drop = graph.get_operation_by_name("dropout/dropout/mul").outputs[0]
        dropout=sess.run(drop,feed_dict)
        W = graph.get_tensor_by_name("W:0")
        Woutput=sess.run(W,feed_dict)
        b = graph.get_tensor_by_name("output/b:0")
        boutput = sess.run(b, feed_dict)
        predictions = graph.get_operation_by_name("output/predictions").outputs[0]
        scores = graph.get_tensor_by_name("output/scores:0")
        softmax_score = graph.get_tensor_by_name("output/Softmax:0")
        softmax_score = sess.run(softmax_score, feed_dict)
        all_predictions = []
        all_predictions = sess.run(predictions, feed_dict)
        predictions_human_readable = np.column_stack((np.array(target_text), all_predictions))
        scores = sess.run(scores, feed_dict)

        g = open("./patterns-1019"+project, 'a') # save patterns extracted here
        pattern_size=[1,2,3,4,5,6]    #we define six sizes of patterns

        for s in range(len(all_predictions)):

            if all_predictions[s]==1:
                print("**********************************sentence NO.=" + str(
                    s) + "**************************************")

                for filter in pattern_size:
                    relu = graph.get_tensor_by_name("conv-maxpool-%s/relu:0" % filter)
                    relu = sess.run(relu, feed_dict)
                    patterns=np.argmax(relu[s,:,0,:],0)
                    each_score=np.max(relu[s,:,0,:],0)
                    score=dropout[s,:]
                    sentence_vocab=sentence_lookup[s,:]
                   # print(score)
                    all = np.empty((1, filter+2), dtype=str)
                    score_words = list()
                    pattern_words = []
                    for i in range ((filter-1)*128,filter*128,1):
                        p=[]
                        score_words=np.array([round(float(score[i]*Woutput[i][0]+boutput[0]),15),round(float(score[i] * Woutput[i][1] + boutput[1]),10)])
                        print(score_words)
                        pattern_index=sentence_vocab[patterns[i-(filter-1)*128]:patterns[i-(filter-1)*128]+filter]
                        for d in range(filter):
                            p.append(list(vocab_dict)[pattern_index[d]])
                        pattern_words = np.append(p, [str(score_words[0]), str(score_words[1])]).reshape(-1, filter + 2)
                        flag = 0
                        for m in range(1,len(all),1):
                            for n in range(len(p)):
                                if all[m][n]==pattern_words[0][n]:
                                    flag=1
                                    key=m
                                    break
                        if flag==0:
                            all=np.append(all,pattern_words,axis=0)
                        if flag==1:
                         #   print(all[key])
                          #  print(all[key][filter + 1])
                            all[key][filter] = str(float(all[key][filter])+ score_words[0])
                            all[key][filter+1] = str(float(all[key][filter+1]) + score_words[1])
                    print(all)
                    for dawn in range(1,len(all),1):
                        haha=[float(all[dawn][filter]), float(all[dawn][filter+1])]
                        P_score=np.exp(haha)/np.sum(np.exp(haha))
                        out=np.argmax(P_score)
                        print (P_score)
                        pat=[]
                        if out==1:
                            if P_score[1]>P_score[0]:
                                print(all[dawn][:-2])
                                g.write(" ".join(all[dawn][:-2]))
                                g.write('\t')
                                g.write(''.join(str(np.exp(haha) / np.sum(np.exp(haha)))))
                                g.write('\n')
                                #colored the extracted patterns here
                                for word in all[dawn][:-2]:
                                    if word in target_text[s]:
                                        print("colorrrrrrrrrrrrrrrrrrrrr")
                                        print(word)
                                        target_text[s].replace(word, "\033[1;20;43m"+word+"\033[0m")
                                pat.append(all[dawn][:-2])
                    print("PPPPPPPPPPPPPPPPPPPPPP")
                    print(target_text[s])









