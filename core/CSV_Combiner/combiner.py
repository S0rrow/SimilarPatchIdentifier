import csv
import getopt
import sys
import os
import numpy as np
import pandas as pd
from subprocess import call
from os import listdir
from os.path import isfile, join

# write in result path the given vector list as csv file
def array2d_to_csv(resultPath, vector):
    vector_arr = np.array(vector)
    # for each in vector_arr:
    #     print(f"[debug.log] each line = {each}")
    with open(resultPath, 'w', newline='') as file:
        csv_writer = csv.writer(file, delimiter=',')
        csv_writer.writerows(vector_arr)

# get pool csv file and target csv file as array.
def csv_to_array(csvfile):
    readfile = open(csvfile, 'r')
    result_array = csv.reader(readfile)
    result_array = np.asarray(list(result_array))
    return result_array

def files_to_list(dir_path):
    result_list = [f for f in listdir(dir_path) if isfile(join(dir_path, f))]
    result_list.sort()
    return result_list

def main(argv):
    try:
        opts, args = getopt.getopt(argv[1:], "p:f:r:", ["cvp_directory","cfl_directory","result_directory"])
    except getopt.GetoptError as err:
        print(f"[debug.log] ERROR on argument options: -p = change vector pool directory, -f = commit file list directory, -r = result directory")
        sys.exit(2)
    cvp_directory = '' # Change vector pool csv directory
    cfl_directory = ''# Commit files list directory
    result_directory = ''
    for o, a in opts:
        if o in ("-p", "--cvp_directory"):
            cvp_directory = a
        elif o in ("-f", "--cfl_directory"):
            cfl_directory = a
        elif o in ("-r", "--result_directory"):
            result_directory = a
        else:
            assert False, "unhandled option"
    # pseudo code
    # read all csv files' file paths under cvp_directory as list
    cvp_list = files_to_list(cvp_directory)

    # read all csv files' file paths under cfl_directory as list
    cfl_list = files_to_list(cfl_directory)

    # extract arrays from each cvp_directory, combining them into one array.
    cvp_combined_array = list()
    print(f"\n[debug.log] extracting change vector pool ...")
    for csv_file in cvp_list:
        csv_array = csv_to_array(cvp_directory+"/"+csv_file)
        for index in range(len(csv_array)):
            cvp_combined_array.append(csv_array[index])
    print(f"\n[debug.log] extracted cvp_combined_array length = {len(cvp_combined_array)}")

    # do same in cfl_directory
    print(f"\n[debug.log] extracting commit file list ...")
    cfl_combined_array = list()
    for csv_file in cfl_list:
        csv_array = csv_to_array(cfl_directory+"/"+csv_file)
        for index in range(len(csv_array)):
            cfl_combined_array.append(csv_array[index])
    print(f"\n[debug.log] extracted cfl_combined_array length = {len(cfl_combined_array)}")
    
    # write csv files from those two arrays.
    array2d_to_csv(result_directory+"/gumtree_vector.csv", cvp_combined_array)
    array2d_to_csv(result_directory+"/commit_file.csv", cfl_combined_array)

    print(f"[debug.log] csv files generation success")

if __name__ == '__main__':
    main(sys.argv)