# -*- encoding：utf-8-*-
# by xiaoxueren
# calculate p, r, f1...

import numpy as np
from  sklearn import  metrics


def print_data_distribution(y_classes, class_names):
    """
    :param y_classes: class of each instance, for example, if there are 3 classes, and y[i] is [1,0,0], then instance[i] belongs to class[0]
    :param class_names: name of each class
    :return: None
    """
    count = np.zeros(len(class_names))
    pro = []
    num = []
    for y in y_classes:
        class_index = np.argmax(y)
        count[class_index] = count[class_index] + 1
    for i, class_name in enumerate(class_names):
        print(class_name, count[i])
        pro.append(class_name)
        num.append(count[i])
    return pro, num


def calculate_IR_metrics( y_classes, predictions, class_names, print_incorrect_predictions=None):
    """
    :param x_text: original text of each instance
    :param y_classes: true class of each instance, notice that y[i] is in form of array[n]
    :param predictions: index of predicted class of each instance
    :param class_names: name of each class
    :return: tp,fp,fn,precision,recall,f1
    """
    tp, fp, fn, tn, precision, recall, f1 , tpr, fpr, auc= (np.zeros(len(class_names)) for _ in range(10))
    for class_index in range(len(class_names)):
        # print("pppppprrrrrr")
        # print(predictions)
        pro_class_index=[]
        y_true_class_index=[]
        for instance_index, predicted_class in enumerate(predictions):
            true_class = np.argmax(y_classes[instance_index])  # true class of this instance

            pro_class_index.append(predicted_class)
            y_true_class_index.append(true_class)


          #  true_class = np.argmax(y_classes[instance_index])  # true class of this instance

            if true_class == class_index and predicted_class == class_index:
                tp[class_index] = tp[class_index] + 1

            if true_class == class_index and predicted_class != class_index:
                fn[class_index] = fn[class_index] + 1

            if true_class != class_index and predicted_class == class_index:
                fp[class_index] = fp[class_index] + 1

            if true_class != class_index and predicted_class != class_index:

                tn[class_index] = tn[class_index] + 1

    for i in range(len(class_names)):
        precision[i] = tp[i] / (tp[i] + fp[i])
        recall[i] = tp[i] / (tp[i] + fn[i])
        f1[i] = 2 * precision[i] * recall[i] / (precision[i] + recall[i])
        tpr[i]=tp[i]/(tp[i]+fp[i])
        fpr[i]=fp[i]/(fn[i]+tn[i])
        auc[i] = metrics.roc_auc_score(y_true_class_index,pro_class_index)

    if print_incorrect_predictions != None:

        output_file = print_incorrect_predictions
        text_to_print = list()

        for instance_index, predicted_class in enumerate(predictions):
            true_class = np.argmax(y_classes[instance_index])
            if predicted_class != true_class:
                text_to_print.append(
                    str(instance_index) + ' True : ' + class_names[true_class] + ' Predicted: ' + class_names[
                        predicted_class] )

                # text_to_print = sorted(text_to_print)
                #    for text in text_to_print:
                # print (output_file, text)
                # print (text)


                #     print (output_file, '-----------------------------------------------------\n-----------------------------------------------------')

    return tp, fp, fn, tn, precision, recall, f1,auc