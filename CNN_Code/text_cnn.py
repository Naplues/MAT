#by xiaoxueren
# per taining unit
import tensorflow as tf
import math
from sklearn import metrics
import numpy as np




class TextCNN(object):
    """
    A CNN for text classification.
    Uses an embedding layer, followed by a convolutional, max-pooling and softmax layer.
    """
    def __init__(
      self, sequence_length, a0,b0, d_lossweight,num_classes, vocab_size,
      embedding_size, filter_sizes, num_filters, l2_reg_lambda):

        # Placeholders for input, output and dropout
        self.input_x = tf.placeholder(tf.int32, [None, sequence_length], name="input_x")
        self.input_y = tf.placeholder(tf.float32, [None, num_classes], name="input_y")
        self.dropout_keep_prob = tf.placeholder(tf.float32, name="dropout_keep_prob")
        self.phase_train = tf.placeholder(tf.bool, name='phase_train')
        self.embedding = tf.placeholder(tf.float32, [None, embedding_size], name="embedding")
        print(self.input_x)

        # Keeping track of l2 regularization loss (optional)
        self.l2_loss = tf.constant(0.0)
        print(self.phase_train)


        # Embedding layer
        with tf.device('/cpu:0'), tf.name_scope("embedding"):
            W = tf.Variable(
                tf.random_uniform([vocab_size, embedding_size], -1.0, 1.0),
                name="W")
            self.embedded_chars = tf.nn.embedding_lookup(W, self.input_x)
            self.embedded_chars_expanded = tf.expand_dims(self.embedded_chars, -1)



        # Create a convolution + maxpool layer for each filter size
        pooled_outputs = []

        for i, filter_size in enumerate(filter_sizes):


            with tf.name_scope("conv-maxpool-%s" % filter_size):

                # Convolution Layer
                filter_shape = [filter_size, embedding_size, 1, num_filters]
                W = tf.Variable(tf.truncated_normal(filter_shape, stddev=0.1), name="W")
                b = tf.Variable(tf.constant(0.1, shape=[num_filters]), name="b")
                conv = tf.nn.conv2d(
                    self.embedded_chars_expanded,
                    W,
                    strides=[1, 1, 1, 1],
                    padding="VALID",
                    name="conv")
                conv = tf.nn.bias_add(conv, b)

                #batch normalization
                beta = tf.Variable(tf.constant(0.0, shape=[num_filters]),
                                   name='beta', trainable=True)
                gamma = tf.Variable(tf.constant(1.0, shape=[num_filters]),
                                    name='gamma', trainable=True)
                batch_mean, batch_var = tf.nn.moments(conv, [0, 1, 2], name='moments')
                ema = tf.train.ExponentialMovingAverage(decay=0.5)

                def mean_var_with_update():
                    ema_apply_op = ema.apply([batch_mean, batch_var])
                    with tf.control_dependencies([ema_apply_op]):
                        return tf.identity(batch_mean), tf.identity(batch_var)

                mean, var = tf.cond(self.phase_train,
                                    mean_var_with_update,
                                    lambda: (ema.average(batch_mean), ema.average(batch_var)))
                conv_bn = tf.nn.batch_normalization(conv, mean, var, beta, gamma, 1e-3)

                # Apply nonlinearity
                h = tf.nn.relu(conv_bn, name="relu")
                # Maxpooling over the outputs
                pooled = tf.nn.max_pool(
                    h,
                    ksize=[1, sequence_length - filter_size + 1, 1, 1],
                    strides=[1, 1, 1, 1],
                    padding='VALID',
                    name="pool")
                pooled_outputs.append(pooled)


        #print(pooled_outputs)

        # Combine all the pooled features
        num_filters_total = num_filters * len(filter_sizes)
        self.h_pool = tf.concat( pooled_outputs,3)
        self.h_pool_flat = tf.reshape(self.h_pool, [-1, num_filters_total],name="dawn_pool_flat")
        print("***********************")
        print(self.h_pool_flat )

        # Add dropout
        with tf.name_scope("dropout"):

            self.h_drop = tf.nn.dropout(self.h_pool_flat, self.dropout_keep_prob)
            print(self.h_drop)



        # Final (unnormalized) scores and predictions
        with tf.name_scope("output"):

            W = tf.get_variable(
                "W",
                shape=[num_filters_total, num_classes],
                initializer=tf.contrib.layers.xavier_initializer())
            b = tf.Variable(tf.constant(0.1, shape=[num_classes]), name="b")
            self.l2_loss += tf.nn.l2_loss(W)
            self.l2_loss += tf.nn.l2_loss(b)
            self.scores = tf.nn.softmax(tf.nn.xw_plus_b(self.h_drop, W, b, name="scores"))
            self.predictions = tf.argmax(self.scores, 1, name="predictions")

            #print("~~~~~~~~~~~~~~~~~~all_weights~~~~~~~~~~~~~~~~")
            #print(W)
           #weighted loss！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！

        # CalculateMean cross-entropy loss
        with tf.name_scope("loss"):
            #your class weights

            class_weights=tf.constant([1.0, d_lossweight])
            #-----------------------------------------method 1--------------------------------------#
          #  ratio=b0/(a0+b0)
            print(d_lossweight)
          #  pos_weight=ratio/(1-ratio)
          #  labels=self.input_y
           # weighted_losses=tf.nn.weighted_cross_entropy_with_logits(logits=self.scores, targets=labels, pos_weight=10.0)
            #print(pos_weight)
            #-----------------------------------------method 2--------------------------------------#
            weights = tf.reduce_sum(class_weights * self.input_y, axis=1)
            unweighted_losses = tf.nn.softmax_cross_entropy_with_logits(labels=self.input_y, logits=self.scores)

            weighted_losses = unweighted_losses * weights
#--------------------------------------------------------------------------------------------------------
            self.mean_loss = tf.reduce_mean(weighted_losses)

            self.loss = self.mean_loss + l2_reg_lambda * self.l2_loss
            tf.summary.scalar("loss",self.loss)

        # Accuracy
        with tf.name_scope("accuracy"):
            correct_predictions = tf.equal(self.predictions, tf.argmax(self.input_y, 1))
            #print(correct_predictions)
            self.accuracy = tf.reduce_mean(tf.cast(correct_predictions, "float"), name="accuracy")

        # precision
        with tf.name_scope("precision"):
            self.precision = tf.reduce_mean(
                tf.cast(tf.metrics.precision(tf.argmax(self.input_y, 1), self.predictions), 'float'),
                name="precision")
            #print(self.precision)
            tf.summary.scalar("precision", self.precision)
        # recall
        with tf.name_scope("recall"):
            self.recall = tf.reduce_mean(
                tf.cast(tf.metrics.recall(tf.argmax(self.input_y, 1), self.predictions), 'float'), name="recall")
            tf.summary.scalar("recall", self.recall)
        # f1
        with tf.name_scope("f1"):
            self.f1 = 2 * self.recall * self.precision / (self.recall + self.precision)

        with tf.name_scope("auc"):
            self.auc = tf.reduce_mean(tf.metrics.auc(tf.argmax(self.input_y, 1), self.predictions, curve='ROC'))
            tf.summary.scalar("auc", self.auc)
