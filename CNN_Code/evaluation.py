#by xiaoxueren
#evaluation of CNN model

import tensorflow as tf
import numpy as np
import os
import time
import datetime
import data_helpers
from data_helpers import load_model as lm
from text_cnn2 import TextCNN
from tensorflow.contrib import learn
import csv
from  sklearn import  metrics
import statistical_analysis as sa

# Parameters
# ==================================================

# Data Parameters


tf.flags.DEFINE_string("target", "target", "target project")
# Eval Parameters
tf.flags.DEFINE_integer("batch_size", 64, "Batch Size (default: 64)")
tf.flags.DEFINE_string("checkpoint_dir", "F:\\cnn326\\runs-RQ1\\", "Checkpoint directory from training run")


# Misc Parameters
tf.flags.DEFINE_boolean("allow_soft_placement", True, "Allow device soft device placement")
tf.flags.DEFINE_boolean("log_device_placement", False, "Log placement of ops on devices")


FLAGS = tf.flags.FLAGS
FLAGS._parse_flags()
class_names = ('nonSATD', 'SATD')

#print("\nParameters:")
for attr, value in sorted(FLAGS.__flags.items()):
    print("{}={}".format(attr.upper(), value))
#print("")

targets=["emf","apache-ant","apache-jmeter-2.10","argouml", "columba-1.4-src","hibernate-distribution-3.3.2.GA", "jEdit-4.2", "jfreechart-1.0.19", "jruby-1.4.0", "sql12"]

#model_project="jEdit-4.2"

for times in range(0,1,1):
    for project in targets:

        file=FLAGS.checkpoint_dir+project
        checkpoint_file = tf.train.latest_checkpoint(file)
        print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~result!!!")
        print(project)
        model_project=project
           # print("eval_pro")
        target_text = np.array([])
        target_y = np.array([])

        target_file_path = "./within_project/"+project+'/test/'
        target_files = list()
        for class_name in class_names:
            target_files.append(target_file_path + class_name)
        tmp_text, tmp_y = data_helpers.load_data_and_labels(target_files)
    #    print("target_file:\n")
     #   print(tmp_text)
      #  print (project + ": " + str(len(tmp_text)) + " sentences")
        target_text = np.concatenate([target_text, tmp_text], 0)

        if len(target_y) == 0:
            target_y = np.array(tmp_y)
        else:
            target_y = np.concatenate([target_y, tmp_y], 0)

    # Map data into vocabulary
        vocab_path = file + "/checkpoint-"+model_project+"/vocab"
        vocab_processor = learn.preprocessing.VocabularyProcessor.restore(vocab_path)
        target_x = np.array(list(vocab_processor.transform(target_text)))

        print("\nEvaluating...\n")

        # Evaluation
        # ==================================================
        graph = tf.Graph()

        with graph.as_default():
            session_conf = tf.ConfigProto(
              allow_soft_placement=FLAGS.allow_soft_placement,
              log_device_placement=FLAGS.log_device_placement)
            sess = tf.Session(config=session_conf)
            with sess.as_default():
                # Load the saved meta graph and restore variables
                saver = tf.train.import_meta_graph("{}.meta".format(checkpoint_file))
                saver.restore(sess, checkpoint_file)


                input_x = graph.get_operation_by_name("input_x").outputs[0]
                phase_train = graph.get_operation_by_name("phase_train").outputs[0]
                dropout_keep_prob = graph.get_operation_by_name("dropout_keep_prob").outputs[0]
            #    hp1 = graph.get_operation_by_name("dawn_pool_flat").outputs[0]
                embedding = graph.get_operation_by_name("embedding").outputs[0]

                # Tensors we want to evaluate
                predictions = graph.get_operation_by_name("output/predictions").outputs[0]

                # Generate batches for one epoch
             #   batches = data_helpers.batch_iter(list(target_x), FLAGS.batch_size, 1, shuffle=False)

                # Collect the predictions here

#                W, hp = lm(checkpoint_file, True, False, initW, target_x)
                all_predictions = []

                all_predictions = sess.run(predictions, {input_x: target_x,  dropout_keep_prob: 1.0,
                                                         phase_train: False})

             #   all_predictions=np.concatenate(all_predictions,batch_predictions)
              #  print(all_predictions.tolist())
               # print(target_y)

        # Print accuracy if y_test is defined




        tp, fp, fn, tn, precision, recall, f1, auc= sa.calculate_IR_metrics( target_y, all_predictions,
                                                                            class_names,None)

        print(f1)
        print(auc)
        print(precision)
        print(recall)
        print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

        # Save the evaluation to a csv
        predictions_human_readable = np.column_stack((np.array(target_text), all_predictions))
        out_path = os.path.join(FLAGS.checkpoint_dir, "prediction.csv")
    #    print("Saving evaluation to {0}".format(out_path))
        with open(out_path, 'w') as f:
            csv.writer(f).writerows(predictions_human_readable)
