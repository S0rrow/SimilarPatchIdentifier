import csv
import getopt
from keras.layers import Input, Dense
from keras.models import Model
from keras.models import load_model
from keras.preprocessing.sequence import pad_sequences
import logging
import numpy as np
import os #nope
import pandas as pd
import pickle #nope
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

def log(s):
    print(s)
    
# AllChangeCollector --> Vector CSV(train pool) --> AE(dimension reduction on train pool) --> KDTree(effective search) --> KNN algorithm
# project --> AllChangeCollector --> Vector --> KDTree --> Model k nearest neighbor distance #1 ~ #10
