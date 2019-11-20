# -*- encoding：utf-8-*-
# by xiaoxueren
#example of CNN model training within a project, split the dataset into training set and dev set
#cross projects training is similar

import tensorflow as tf
import numpy as np
import statistical_analysis as sa
import os
import time
import datetime
import data_helpers
from text_cnn import TextCNN
from tensorflow.contrib import learn
from numpy import ndarray
from  sklearn import  metrics

# Parameters
# ==================================================

# Data loading params
tf.flags.DEFINE_float("dev_sample_percentage", .1, "Percentage of the training data to use for validation")
tf.flags.DEFINE_string("source", "source", "source projects")
tf.flags.DEFINE_string("target", "target", "target project")
#tf.flags.DEFINE_string("source", "argouml", "target project")

# Model Hyperparameters
tf.flags.DEFINE_integer("embedding_dim",300, "Dimensionality of character embedding (default: 128)")
tf.flags.DEFINE_string("filter_sizes", "1,2,3,4,5,6", "Comma-separated filter sizes (default: '3,4,5')")
tf.flags.DEFINE_integer("num_filters", 128, "Number of filters per filter size (default: 128)")
tf.flags.DEFINE_float("dropout_keep_prob", 0.5, "Dropout keep probability (default: 0.5)")
tf.flags.DEFINE_float("l2_reg_lambda", 0.2, "L2 regularizaion lambda (default: 0)")

# Training parameters
tf.flags.DEFINE_integer("batch_size", 256, "Batch Size (default: 64)")
tf.flags.DEFINE_integer("num_epochs", 200, "Number of training epochs (default: 1000)")
tf.flags.DEFINE_integer("evaluate_every", 1, "Evaluate model on dev set after this many epochs (default: 100)")
tf.flags.DEFINE_integer("checkpoint_every", 100, "Save model after this many steps (default: 100)")
# Misc Parameters
tf.flags.DEFINE_boolean("allow_soft_placement", False, "Allow device soft device placement")
tf.flags.DEFINE_boolean("log_device_placement", False, "Log placement of ops on devices")


FLAGS = tf.flags.FLAGS
FLAGS._parse_flags()


#sentence class 0 1
class_names = ('nonSATD', 'SATD')

'''

'''
def train_model(allow_save_model = True, print_intermediate_results = True, d_lossweight= None, pname = None):
    if d_lossweight == None:
        print("The End!!")
    a = 0
    b = 0
    print("\nParameters:")
    for attr, value in sorted(FLAGS.__flags.items()):
        print("{}={}".format(attr.upper(), value))
    print("")

    # Data Preparatopn
    # ==================================================

    # Load data 加载数据, 逐次添加所有项目 
    print("Loading data...")

    # load each source project

    source_text = np.array([])   # 所有源项目的注释
    source_y0 = np.array([])     # 素有源项目的标记

    source_file_path = "./cross_project/" + pname + '/train/'
    source_files = list()

    # SATD nonSATD 两个文件
    for class_name in class_names:
        source_files.append(source_file_path + class_name)
    # tmp_text: 暂存一个项目中的所有注释 this is hack
    # tmp_y   : 暂存一个项目中所有注释的标记 [1, 0] 或者[0, 1]
    tmp_text, tmp_y = data_helpers.load_data_and_labels(source_files)

    # 输出项目名 和该项目下所有的注释数目
    print(pname + ": " + str(len(tmp_text)) + " sentences")
    
    source_text = np.concatenate([source_text, tmp_text], 0)  # 将暂存的注释拼接到源项目注释中
    # 没有标记时 直接赋值 否则进行拼接
    if len(source_y0) == 0:
        source_y0 = np.array(tmp_y)
    else:
        source_y0 = np.concatenate([source_y0, tmp_y], 0)


    # Build 根据所有已分词好的文本建立好一个词典，然后找出每个词在词典中对应的索引，不足长度或者不存在的词补0
    # 找出注释中最长的注释长度，并和100比较，取两者中较小的
    max_document_length = min(100, max([len(x.split(" ")) for x in source_text]))  
    # important here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    vocab_processor     = learn.preprocessing.VocabularyProcessor(max_document_length)
    source_x0           = np.array(list(vocab_processor.fit_transform(source_text)))
    #  target_x = np.array(list(vocab_processor.fit_transform(target_text)))
    # =========================================================================print vocab============================================
    np.random.seed(10)
    shuffle_indices = np.random.permutation(np.arange(len(source_y0))) # 在数据集中随机找一个索引
    x_shuffled      = source_x0[shuffle_indices] # 找到随机的注释内容
    y_shuffled      = source_y0[shuffle_indices] # 找到随机的注释标记

    # Split train/test set 切分训练集和测试集
    # TODO: This is very crude, should use cross-validation
    dev_sample_index   = -1 * int(FLAGS.dev_sample_percentage * float(len(source_y0)))
    source_x, target_x = x_shuffled[:dev_sample_index], x_shuffled[dev_sample_index:]
    source_y, target_y = y_shuffled[:dev_sample_index], y_shuffled[dev_sample_index:]
    print("Vocabulary Size: {:d}".format(len(vocab_processor.vocabulary_)))
    print("Train/Dev split: {:d}/{:d}".format(len(source_y), len(target_y)))
    #=========================================================================print vocab============================================

    # 打印中间结果
    pro = []
    num = []
    if print_intermediate_results:
        print('data distribution in source dataset')
        pro, num = sa.print_data_distribution(source_y, class_names)
        if pro[0] == "nonSATD":
            a = num[0]
        if pro[1] == "SATD":
            b = num[1]
        print('data distribution in target dataset')
        sa.print_data_distribution(target_y, class_names)

        print("Max Document Length: {:d}".format(max_document_length))
        print("Vocabulary Size: {:d}".format(len(vocab_processor.vocabulary_)))
        print("Train/Test size: {:d}/{:d}".format(len(source_y), len(target_y)))

    # Training  ==================================================

    min_loss = 100000000

    max_f1 = 0.0
    predictions_at_min_loss = None
    steps_per_epoch = (int)(len(source_y) / FLAGS.batch_size) + 1

    with tf.Graph().as_default():
        session_conf = tf.ConfigProto(allow_soft_placement = FLAGS.allow_soft_placement, log_device_placement = FLAGS.log_device_placement)
        session_conf.gpu_options.allow_growth = True
        sess = tf.Session(config=session_conf)
        with sess.as_default():
            cnn = TextCNN(
                a0 = a,
                b0 = b,
                d_lossweight    = a/b,
                sequence_length = max_document_length,
                num_classes     = source_y.shape[1],
                vocab_size      = len(vocab_processor.vocabulary_),
                embedding_size  = FLAGS.embedding_dim,
                filter_sizes    = list(map(int, FLAGS.filter_sizes.split(","))),
                num_filters     = FLAGS.num_filters,
                l2_reg_lambda   = FLAGS.l2_reg_lambda)

            # Define Training procedure
            global_step    = tf.Variable(0, name="global_step", trainable = False)
            learning_rate  = tf.train.polynomial_decay(2*1e-3, global_step, steps_per_epoch * FLAGS.num_epochs, 1e-4, power = 1)
            optimizer      = tf.train.AdamOptimizer(learning_rate)
            grads_and_vars = optimizer.compute_gradients(cnn.loss)
            train_op       = optimizer.apply_gradients(grads_and_vars, global_step=global_step)

            if allow_save_model:
                print("!!!!!!os.path")
                  # Keep track of gradient values and sparsity (optional)
                grad_summaries = []
                for g, v in grads_and_vars:
                    if g is not None:
                        grad_hist_summary = tf.summary.histogram("{}/grad/hist".format(v.name), g)
                        sparsity_summary = tf.summary.scalar("{}/grad/sparsity".format(v.name), tf.nn.zero_fraction(g))
                        grad_summaries.append(grad_hist_summary)
                        grad_summaries.append(sparsity_summary)
                grad_summaries_merged = tf.summary.merge(grad_summaries)

                # Output directory for models and summaries
                timestamp = str(int(time.time()))
                out_dir   = os.path.abspath(os.path.join(os.path.curdir, "runs-RQ1",pname ))
                print("Writing to {}\n".format(out_dir))


                # Summaries for loss ,f1, auc
                loss_summary      = tf.summary.scalar("loss", cnn.loss)
                acc_summary       = tf.summary.scalar("accuracy", cnn.accuracy)
                precision_summary = tf.summary.scalar("precision", cnn.precision)
                recall_summary    = tf.summary.scalar("recall",cnn.recall)
                f1_summary        = tf.summary.scalar("f1", cnn.f1)
                auc_summary       = tf.summary.scalar("auc", cnn.auc)


                # Train Summaries
                train_summary_op     = tf.summary.merge([loss_summary, acc_summary,precision_summary,recall_summary,f1_summary,auc_summary, grad_summaries_merged])
                train_summary_dir    = os.path.join(out_dir, 'summaries','train')
                train_summary_writer = tf.summary.FileWriter(train_summary_dir)
                train_summary_writer.add_graph(sess.graph)

                # Dev summaries
                dev_summary_op     = tf.summary.merge([loss_summary, acc_summary, auc_summary,precision_summary,recall_summary,f1_summary,grad_summaries_merged])
                dev_summary_dir    = os.path.join(out_dir, 'summaries', 'dev')
                dev_summary_writer = tf.summary.FileWriter(dev_summary_dir)
                dev_summary_writer.add_graph(sess.graph)



                # Checkpoint directory. Tensorflow assumes this directory already exists so we need to create it
                checkpoint_dir_name = "checkpoint-" + pname
                checkpoint_dir      = os.path.abspath(os.path.join(out_dir, checkpoint_dir_name))
            #    checkpoint_prefix = os.path.join(checkpoint_dir, "model")
                if not os.path.exists(checkpoint_dir):
                    os.makedirs(checkpoint_dir)

                saver = tf.train.Saver(tf.all_variables())
                vocab_dir_name=os.path.join(checkpoint_dir,"vocab")
                # Write vocabulary
                vocab_processor.save(vocab_dir_name)

            # Initialize all variables
            sess.run(tf.global_variables_initializer(), feed_dict = {cnn.phase_train: True})
            sess.run(tf.local_variables_initializer())# this is for version r0.12


            def train_step(x_batch, y_batch):
                sess.run(tf.local_variables_initializer())

                """
                A single training step
              """
                feed_dict = {
                    cnn.input_x: x_batch,
                    cnn.input_y: y_batch,
                    cnn.dropout_keep_prob: FLAGS.dropout_keep_prob,
                    cnn.phase_train: True
                }
                _, step, summaries, loss, mean_loss, l2_loss, accuracy,precision, recall, f1, auc = sess.run(
                    [train_op,global_step, train_summary_op,cnn.loss, cnn.mean_loss, cnn.l2_loss, cnn.accuracy,cnn.precision, cnn.recall,cnn.f1,cnn.auc],
                    feed_dict)
                time_str = datetime.datetime.now().isoformat()
                train_summary_writer.add_summary(summaries, step)
                return accuracy, precision, recall, f1, auc

            def dev_step(x_batch, y_batch, writer=None):

                """
                Evaluates model on a dev set
                """
                feed_dict = {
                    cnn.input_x: x_batch,
                    cnn.input_y: y_batch,
                    cnn.dropout_keep_prob: 1.0,
                    cnn.phase_train: False
                }
                summaries, step,loss, mean_loss, l2_loss,accuracy, precision, recall, f1, auc,   predictions = sess.run(
                    [ dev_summary_op,global_step,cnn.loss, cnn.mean_loss, cnn.l2_loss, cnn.accuracy,cnn.precision, cnn.recall,cnn.f1,cnn.auc,cnn.predictions],
                    feed_dict)

                time_str = datetime.datetime.now().isoformat()

                if print_intermediate_results:
                    print("{}: epoch {}, step {}, loss {:g}, acc {:g}, percision {:g}, recall{:g}, f1{:g}, auc {:g}, mean_loss {}, l2_loss {}".format(
                        time_str,  step/steps_per_epoch, step, loss, accuracy, precision, recall, f1, auc, mean_loss, l2_loss))
                    tp, fp, fn, tn,precision, recall, f1, auc2= sa.calculate_IR_metrics(y_batch, predictions,
                                                                                class_names,None)

                    print("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
                    print(precision[1],recall[1],f1[1], auc2[1])
                if writer !=None:
                    print("devWrite!!!!")
                    writer.add_summary(summaries, step)

                return accuracy, precision, recall, f1, auc, loss, predictions


            # Generate batches
            batches = data_helpers.batch_iter(list(zip(source_x, source_y)), FLAGS.batch_size, FLAGS.num_epochs)

            for batch in batches:
                x_batch, y_batch = zip(*batch)
                train_accuracy,train_precision,train_recall,train_f1,train_auc = train_step(x_batch, y_batch)
                current_step = tf.train.global_step(sess, global_step)
                current_epoch = current_step/steps_per_epoch
                if current_step%steps_per_epoch==0 and current_epoch % FLAGS.evaluate_every == 0:
                    if print_intermediate_results:

                        print("Current train accuracy: %s" % (train_accuracy))
                        print("Current train precision: %s" % (train_precision))
                        print("Current train recall: %s" % (train_recall))
                        print("Current train f1: %s" % (train_f1))
                        print("Current train auc: %s" % (train_auc))

                        print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                    fold_accuracy, precision, recall, f1, auc, loss, predictions = dev_step(target_x, target_y, writer=dev_summary_writer)
                    tp, fp, fn, tn, precision, recall, f1, auc2= sa.calculate_IR_metrics( target_y, predictions,
                                                                            class_names,None)
                    for i in range(len(class_names)):
                        print(class_names[i], precision[i], recall[i], f1[i], auc2[i])
                    if loss < min_loss:
                        if f1[1]> max_f1:
                            min_loss = loss
                            max_f1=f1[1]
                            predictions_at_min_loss = predictions
                            if allow_save_model:
                                save_path = saver.save(sess, checkpoint_dir, global_step=current_step)
                                if print_intermediate_results:
                                    print("Model saved in file: %s" % save_path)

            # Final result
            output_file = open(source_file_path + 'fp_sentences1', 'a',encoding='utf-8')
            print('Final result:')
            fold_accuracy, precision, recall, f1, auc, loss, predictions = dev_step(target_x, target_y)
            print("ACC: %s" % (fold_accuracy))
            print( precision, recall, f1, auc)


            tp, fp, fn, tn, precision, recall, f1 , auc2= sa.calculate_IR_metrics( target_y, predictions, class_names, output_file)
            for i in range(len(class_names)):
                print (class_names[i], precision[i], recall[i], f1[i], auc2[1])

    return min_loss, predictions_at_min_loss, target_y



# =========================================================================================================================
def main():

    d_lossweight = 0.0
    
    #list all projects you want to train here
    pro = ["Ant", "ArgoUML", "Columba", "EMF", "Hibernate", "JEdit", "JFreeChart", "JMeter", "JRuby", "SQuirrel"]
    for kk in range(len(pro)):
        pname = pro[kk]
        print(pname + "\nd_lossweight: " + str(d_lossweight))
        train_model(True, True, d_lossweight, pname)


if __name__ == "__main__":
    main()
