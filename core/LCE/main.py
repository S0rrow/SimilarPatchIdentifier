import csv
import getopt
import sys
import os
import numpy as np
import pandas as pd
from keras.preprocessing.sequence import pad_sequences

# find longest common subsequence of target vector within vector pool
def lcs(vector_pool, target_vector, index = -1):
    len_vp = len(vector_pool)
    # print("[debug.log] length vector pool = %d"% (len_vp))
    len_tv = len(target_vector)
    # print("[debug.log] length target vector = %d" % (len_tv))
    return lcs_algo(vector_pool, target_vector, len_vp, len_tv, index)

def int_array_equal(a, b):
    a = a.strip()
    if len(a) != len(b):
        return False
    for i in range(len(a)):
        if a[i]==' ': del a[i]
        if int(a[i]) != int(b[i]):
            return False
    return True

def try_integer(s):
    if not s:
        return s
    try:
        f = float(s)
        i = int(f)
        return i if f == i else f
    except ValueError:
        return s

def backtrack(vector_pool, target_vector, lcs_map, y, x):
    dropped_sequence_length_list = [0 for _ in range(lcs_map[y][x] + 1)]
    result_vector = [0 for _ in range(index+1)]
    result_vector[index] = 0
    i = y
    j = x

    while i > 0 and j > 0:
        # print(f"[debug.log] vector_pool[i-1] = {vector_pool[i-1]}, target_vector[j-1] = {target_vector[j-1]}")
        if vector_pool[i-1] == target_vector[j-1]:
            #print(f"[debug.log] switch 1")
            result_vector[index-1] = int(vector_pool[i-1])
            i -= 1
            j -= 1
            index -= 1
        elif L[i-1][j] > L[i][j-1]:
            #print(f"[debug.log] switch 2")
            dropped_sequence_length_list[index] += 1
            i -= 1
        else:
            #print(f"[debug.log] switch 3")
            j -= 1
    if i > 0:
        dropped_sequence_length_list[index] += i
    if result_vector[len(result_vector)-1]==0:
        result_vector = result_vector[:-1]
    return dropped_sequence_length_list

# Longest Common Sequence algorithm
def lcs_algo(vector_pool, target_vector, len_vp, len_tv, lineindex = -1):
    debug_target = -1
    offset = 65
    score = 1
    L = [[0 for x in range(len_tv+1)] for y in range(len_vp+1)]
    for i in range(len_vp+1):
        for j in range(len_tv+1):
            if i == 0 or j == 0:
                L[i][j] = 0
            elif vector_pool[i-1] == target_vector[j-1]:
                # print(f"[debug.log] vector_pool[i-1] = {vector_pool[i-1]}, target_vector[j-1] = {target_vector[j-1]}")
                L[i][j] = L[i-1][j-1] + 1
            else:
                L[i][j] = max(L[i-1][j], L[i][j-1])
    if lineindex == debug_target-65+offset:
        print(f"[debug.log] debug target line index = {lineindex}")
        print(f"[debug.log] L mapped")
        for iter in range(len_vp):
            print(f"[debug.log] {L[iter]}")
    index = L[len_vp][len_tv]

    result_vector = [0 for _ in range(index+1)]
    result_vector[index] = 0
    i = len_vp
    j = len_tv
    dropped_sequence_length_list = [0 for _ in range(index+1)]
    while i > 0 and j > 0:
        # print(f"[debug.log] vector_pool[i-1] = {vector_pool[i-1]}, target_vector[j-1] = {target_vector[j-1]}")
        #if L[i-1][j] == L[i][j]:
        #    dropped_sequence_length_list[index] += 1
        #    i -= 1
        if L[i-1][j] > L[i][j-1]:
            dropped_sequence_length_list[index] += 1
            i -= 1
        elif vector_pool[i-1] == target_vector[j-1]:
            #print(f"[debug.log] switch 1")
            result_vector[index-1] = int(vector_pool[i-1])
            i -= 1
            j -= 1
            index -= 1
        else:
            #print(f"[debug.log] switch 3")
            j -= 1
    if i > 0:
        dropped_sequence_length_list[index] += i
    if result_vector[len(result_vector)-1]==0:
        result_vector = result_vector[:-1]
    if lineindex == debug_target-65+offset:
        print(f"[debug.log] dropped_sequence_length_list = {dropped_sequence_length_list}")
        print(f"[debug.log] result_vector = {result_vector}")
        sigma = (len_vp == sum(dropped_sequence_length_list) + len(result_vector))
        print(f"[debug.log] sigma result = {len_vp} == {sum(dropped_sequence_length_list) + len(result_vector)} = {sigma}")
    
    # n_i in range n_1 to n_m-1
    trim_1_to_m = dropped_sequence_length_list[1:]
    trim_1_to_m_min_1 = trim_1_to_m[:-1]

    score = 1 # score = 1
    if(len_vp - len(result_vector) > 0):
        score -= sum(trim_1_to_m_min_1) / (len_vp - len(result_vector)) # score -= sum(dropped_sequence_length_list) / (N - k)
        # print(f"[debug.log] score -= {sum(dropped_sequence_length_list)} / {(len_vp - len(result_vector))} = {score}")
    score = score * len(result_vector) / len_tv # score *= k / k_max
    # print(f"[debug.log] score *= {len(result_vector)} / {len_tv} = {score}")
    if lineindex == debug_target-65+offset:
        if len_vp >= len_tv:
            print(f"[debug.log] case 1: target <= pool")
        else:
            print(f"[debug.log] case 2: target > pool")
        
        print(f"[debug.log] index = {lineindex}")
        print(f"[debug.log] target vector = {target_vector}")
        print(f"[debug.log] pool vector = {vector_pool}")
        print(f"[debug.log] result_vector = {result_vector}")
        print(f"[debug.log] dropped sequence length list = {dropped_sequence_length_list}")
        print(f"[debug.log] score -= {sum(trim_1_to_m_min_1)} / {(len_vp - len(result_vector))}")
        print(f"[debug.log] score *= {len(result_vector)} / {len_tv}")
        print(f"[debug.log] calculated score = {score}")
        
        
    return result_vector, score

# write in result path the given vector list as csv file
def array2d_to_csv(resultPath, vector):
    vector_arr = np.array(vector)
    # for each in vector_arr:
    #     print(f"[debug.log] each line = {each}")
    with open(resultPath, 'w', newline='') as file:
        csv_writer = csv.writer(file, delimiter=',')
        csv_writer.writerows(vector_arr)

# write in result path the given vector list as csv file
def array1d_to_csv(resultPath, vector):
    vector_arr = np.array(vector,dtype=np.int32)
    with open(resultPath, 'w', newline='') as file:
        csv_writer = csv.writer(file, delimiter=',')
        csv_writer.writerow(vector_arr)
        

# get pool csv file and target csv file as array.
def csv_to_array(pool_cv):
    f_pool_cv = open(pool_cv, 'r')
    vector_pool = csv.reader(f_pool_cv)
    vector_pool = np.asarray(list(vector_pool))
    # print(f"[debug.log] type(vector_pool) = {type(vector_pool)}")
    # print(f"[debug.log] vector_pool.shape = {vector_pool.shape}")
    # print(f"[debug.log] vector_pool.dtype = {vector_pool.dtype}")
    return vector_pool

# remove trailing commas at the end of each row of csv files
def remove_trailing_commas(vector):
    trimmed = list()
    for i in range(len(vector)):
        if len(vector[i]) != 0:
            vector[i] = np.delete(vector[i], len(vector[i])-1)
        else:
            vector[i] = []
        trimmed.append(vector[i])
    return trimmed

def remove_trailing_commas_1d(vector):
    return np.delete(vector, len(vector)-1)

# remove empty new lines in change vector array and meta vector array
def synchro_line_remove(vector, metavector, target_index):
    # print(f"[debug.log] empty line index = {target_index}")
    # print(f"[debug.log] removing empty line ... vector[target] = {vector[target_index]}")
   
    vector = np.delete(vector, target_index)
    # np.delete(vector, index, axis=None)
    metavector = np.delete(metavector, target_index, axis=0)
    # np.delete(metavector, index, axis=None)
    return vector, metavector

# locate empty new lines in change vector array and meta vector file array
def locate_nearest_empty_line(vector):
    for i in range(len(vector)):
        if len(vector[i]) <= 0:
            # print(f"[debug.log] located line: {i+1}")
            return i

# locate and remove empty lines in change vector array and meta vector array
def clean_change_vector(vector_pool, metavector):
    target = None
    # print(f"[debug.log] before metavector size= {len(metavector)}")
    for i in range(len(vector_pool)):
        target = locate_nearest_empty_line(vector_pool)
        if target == None:
            print(f"[debug.log] removed {i} empty lines from vector pool")
            break
        # print(f"[debug.log] empty line index = {target+i+1}")
        vector_pool, metavector = synchro_line_remove(vector_pool, metavector, target)
    # print(f"[debug.log] processed metavector = {len(metavector)}")
    return vector_pool, metavector
        

# return result list of lcs length of each row from vector pool
def lcs_count(processed_vector_pool, targetVector):
    result_list = list()
    for i in range(len(processed_vector_pool)):
        lcs_result, lcs_score = lcs(list(map(try_integer, processed_vector_pool[i])),list(map(try_integer, targetVector)), i)
        #if lcs_score == 1:
        #    print(f"[debug.log] lcs result {i} = {lcs_result}")
        lcs_score = int(lcs_score*100)
        result_list.append(lcs_score)
    return result_list

# gumtreeVector.csv.trimed, lcs_count_list.csv, max = size of target cv, result_pool_size = 10% of size
def lcs_extract(vector_pool, lcs_count_list, result_pool_size):
    result_list = list()
    result_index_list = list()
    lcs_count_index_dict = dict()
    max_score = 0.00
    min_score = 100.00
    flag = False
    for lcs_list_index in range(len(lcs_count_list)):
        lcs_count_index_dict.setdefault(lcs_count_list[lcs_list_index], []).append(lcs_list_index)
        
    target_score = 100
    result_pool_size_iter = result_pool_size
    while result_pool_size_iter > 0:
        if target_score in lcs_count_index_dict:
            if max_score < target_score: max_score = target_score
            if min_score > target_score: min_score = target_score
            print(f"[debug.log] iterating LCS score = {target_score}% ...")
            result_pool_size_iter -= len(lcs_count_index_dict[target_score])
        if(result_pool_size_iter > 0):
            target_score -= 1
    trail = 100
    for i in range(100, target_score-1,-1):
        if i in lcs_count_index_dict:
            if ((len(result_index_list) + len(lcs_count_index_dict[i])) / result_pool_size - 1) >= 1.5:
                # limited the error rate top margin to 150 %
                print(f"[debug.log] Warning: break due to too large error rate")
                flag = True
                break
            result_index_list.extend(lcs_count_index_dict[i])
            trail = i
    
    print(f"[debug.log] collected maximum LCS score = {max_score:.2f}%")
    if flag:
        print(f"[debug.log] collected minimum LCS score = {trail:.2f}%")
    else:    
        print(f"[debug.log] collected minimum LCS score = {min_score:.2f}%")

    print(f"[debug.log] target result pool size = {result_pool_size}")
    print(f"[debug.log] result index list size = {len(result_index_list)}")
    print(f"[debug.log] error rate = {(len(result_index_list) / result_pool_size - 1)*100:.2f}%")
    # result_index_list.sort()
    
    for index in range(len(result_index_list)):
        result_list.append(vector_pool[result_index_list[index]])

    return result_list, result_index_list

def meta_lcs_extract(result_index_list, meta_vector_pool, lcs_count_list):
    # print(f"[debug.log] result index list:\n{result_index_list}")
    meta_result_list = list()
    tmp_array = np.ndarray
    for i in range(len(result_index_list)):
        meta_result_list.append(meta_vector_pool[result_index_list[i]])
        # print(f"[debug.log]: meta_result_list[i]: {meta_result_list[i]}")
        meta_result_list[i] = np.append(meta_result_list[i], lcs_count_list[result_index_list[i]])
    return meta_result_list

def main(argv):
    try:
        opts, args = getopt.getopt(argv[1:], "h:g:t:c:r:", ["help", "gumtreeVector", "target","commitPool","resultDirectory"])
    except getopt.GetoptError as err:
        print(err)
        sys.exit(2)
    gumtreeVector = ''
    commitPool = ''
    targetVector = ''
    result_size = 0
    result_dir = ''
    for o, a in opts:
        if o in ("-H", "--help") or o in ("-h", "--hash"):
            print("")
            sys.exit()
        elif o in ("-t", "--target"):
            targetVector = a
        elif o in ("-g", "--gumtreeVector"):
            gumtreeVector = a
        elif o in ("-c", "--commitPool"):
            commitPool = a
        elif o in ("-r", "--resultDirectory"):
            result_dir = a
        else:
            assert False, "unhandled option"
    
    print(f"[debug.log] target: {targetVector}, gumtreeVector: {gumtreeVector}, commitPool: {commitPool}, resultDirectory: {result_dir}")
    vector_pool = csv_to_array(gumtreeVector)
    target = csv_to_array(targetVector)
    commit_pool = csv_to_array(commitPool)
    # print(f"[debug.log] commit pool= {commit_pool}")
    print(f"[debug.log] original vector_pool size = {len(vector_pool)}")
    processed_vector_pool = remove_trailing_commas(vector_pool)
    processed_vector_pool, commit_pool = clean_change_vector(processed_vector_pool, commit_pool)
    print(f"[debug.log] changed vector_pool size = {len(processed_vector_pool)}")
    result_size = int(len(processed_vector_pool) / 10)
    target = list(target[0])
    target = remove_trailing_commas_1d(target)
    lcs_count_list = lcs_count(processed_vector_pool, target)
    # length of longest common subsequence
    meta_lcs_count = dict()
    for i in range(100+1):
        meta_lcs_count[i] = lcs_count_list.count(i)
    
    print(f"[debug.log] meta result count: \n{meta_lcs_count.items()}")

    # print(f"[debug.log] LCS count list: \n{lcs_count_list}")

    array1d_to_csv(result_dir+"/lcs_count_list.csv", lcs_count_list)
    result_pool, result_index_list = lcs_extract(processed_vector_pool, lcs_count_list, result_size)
    array1d_to_csv(result_dir+"/result_index_list.csv", result_index_list)

    meta_result_list = meta_lcs_extract(result_index_list, commit_pool, lcs_count_list)

    # print(f"[debug.log] commit id and file path:\n{meta_result_list}")
    # print(f"[debug.log] result pool: \n{result_pool}")

    array2d_to_csv(result_dir+"/meta_resultPool.csv", meta_result_list)
    array2d_to_csv(result_dir+"/resultPool.csv", result_pool)

if __name__ == '__main__':
    main(sys.argv)