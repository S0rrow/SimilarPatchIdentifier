import csv
import getopt
from keras.layers import Input, Dense
from keras.models import Model
from keras.models import load_model
from keras.preprocessing.sequence import pad_sequences
import logging
import numpy as np
import os
import pandas as pd
import pickle
from sklearn.neighbors import KNeighborsClassifier
from sklearn.neighbors import KDTree
from sklearn.preprocessing import MinMaxScaler
import sys

K_NEIGHBORS = 1
CUTOFF = 0

np.set_printoptions(threshold=np.inf)

logging.basicConfig(
    format='%(asctime)s : %(levelname)s : %(message)s',
    level=logging.INFO
)


def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False


def del_index_num(s):
    temp = ''
    is_passed = False
    for c in reversed(s):
        if is_number(c) and not is_passed:
            continue
        else:
            temp += c
            is_passed = True

    temp = list(temp)
    temp.reverse()
    s = ''.join(temp)

    return s


    
# AllChangeCollector --> Vector CSV(train pool) --> AE(dimension reduction on train pool) --> KDTree(effective search) --> KNN algorithm
# project --> AllChangeCollector --> Vector --> KDTree --> Model k nearest neighbor distance #1 ~ #10
