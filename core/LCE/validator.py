import csv
import getopt
import os
import sys
import numpy as np
from subprocess import call
from pydriller import Repository

def csv_to_array(csv_file):
    f_csv_file = open(csv_file, 'r')
    result_array = csv.reader(f_csv_file)

    result_array = np.asarray(list(result_array))
    return result_array

def after_commit_id(commitID, gitDirectory, filePath, target=None):
    pwd = os.getcwd()
    filePath = filePath.split('/')[-1]
    # gitDirectory = pwd+"/"+gitDirectory
    nextID = ''
    breaker = False
    #commitList = list()#, filepath=filePath
    for commit in Repository(gitDirectory).traverse_commits():
        #print(f"commit.hash = {commit.hash}")
        for m in commit.modified_files:
            if m.filename == filePath:
                breaker = True
        if breaker :
            nextID = commit.hash
            break
    #call(f"echo {nextID} > {target}/afterCID.txt", shell=True)
    return nextID

def seperate_commit_id_and_path(result_array):
    commit_id_before_list = list()
    commit_id_after_list = list()
    file_path_before_list = list()
    file_path_after_list = list()
    lcs_count_list = list()
    url_list = list()
    for i in range(len(result_array)):
        commit_id_before_list.append(result_array[i][0])
        commit_id_after_list.append(result_array[i][1])
        file_path_before_list.append(result_array[i][2])
        file_path_after_list.append(result_array[i][3])
        url_list.append(result_array[i][4])
        lcs_count_list.append(result_array[i][5])
    return commit_id_before_list, commit_id_after_list, file_path_before_list, file_path_after_list, lcs_count_list, url_list

def top_n_to_diffs(commit_id_before_list, commit_id_after_list, file_path_before_list, file_path_after_list, lcs_count_list, n, pool_dir, candidate_result_dir=None, url_list=None):
    project = ''
    pwd = os.getcwd()
    candidate_dir = candidate_result_dir
    url_dict = dict()

    if candidate_dir == None or candidate_dir == '':
        candidate_dir = pwd+"/candidates/"

    for url in url_list:
        url_dict[url] = url_list.count(url)

    for url_key in url_dict.keys():
        call(f"./clone_at.sh {url_key} {pool_dir}", shell = True)

    for i in range(n):
        # https://github.com/apache/ant-ivyde.git
        project = url_list[i].split('/')[-1].split('.')[0]
        git_dir = pool_dir+"/"+project
        if file_path_before_list[i] == file_path_after_list[i]:
            try:
                nextID = after_commit_id(commit_id_before_list[i], url_list[i], file_path_before_list[i])
                print(f"[debug.log] Generating patch candidate #{i}")
                print(f"[debug.log] Extracting git diff files ...")
                call(f"cd {git_dir}\ngit diff --output={pwd}/result/diff_{lcs_count_list[i]}_{i+1}.txt --unified=0 {commit_id_before_list[i]} {nextID} -- {file_path_before_list[i]}",shell=True)

                print(f"[debug.log] > Project           : {project}")
                print(f"[debug.log] > CommitID before   : {commit_id_before_list[i]}")
                print(f"[debug.log] > Path              : {file_path_before_list[i]}")
                print(f"cd {git_dir}\ngit checkout -f {commit_id_before_list[i]}; cp {git_dir}/{file_path_before_list[i]} {candidate_dir}/{project}_rank-{i}_old.java")
                call(f"cd {git_dir}\ngit checkout -f {commit_id_before_list[i]}; cp {git_dir}/{file_path_before_list[i]} {candidate_dir}/{project}_rank-{i}_old.java", shell=True)

                print(f"[debug.log] > CommitID after    : {nextID}")
                print(f"[debug.log] > Path              : {file_path_after_list[i]}")
                print(f"cd {git_dir}\ngit checkout -f {nextID}; cp {git_dir}/{file_path_before_list[i]} {candidate_dir}/{project}_rank-{i}_new.java")
                call(f"cd {git_dir}\ngit checkout -f {nextID}; cp {git_dir}/{file_path_before_list[i]} {candidate_dir}/{project}_rank-{i}_new.java", shell=True)
                print(f"[debug.log] resetting the git header to current HEAD ...")
                call(f"cd {git_dir}\ngit reset --hard HEAD\n", shell=True)
            except:
                print(f"[debug.log] exception occured: {sys.exc_info()[0]}")
        else:
            print(f"[debug.log] file path different : {file_path_before_list[i]} -> {file_path_after_list[i]}")
    return

def main(argv):
    try:
        opts, args = getopt.getopt(argv[1:], "h:f:d:n:r:i:c:", ["help", "file", "directory", "number","resultDirectory", "hashID","candidates"])
    except getopt.GetoptError as err:
        print(err)
        sys.exit(2)
    file = ''
    gitdir = ''
    n = 0
    result_dir = ''
    candidates = ''
    hash = ''
    pool_dir = ''
    for o, a in opts:
        if o in ("-H", "--help") or o in ("-h", "--hash"):
            print("")
            sys.exit()
        elif o in ("-f", "--file"):
            file = a
        elif o in ("-d", "--directory"):
            pool_dir = a
        elif o in ("-n", "--number"):
            n = int(a)
        elif o in ("-r", "--resultDirectory"):
            result_dir = a
        elif o in ("-c", "--candidates"):
            candidates = a
        else:
            assert False, "unhandled option"

    file = result_dir + file
    result_array = csv_to_array(file)
    print(f"[debug.log] result array length : {len(result_array)}")
    commit_id_before_list, commit_id_after_list, file_path_before_list, file_path_after_list, lcs_count_list, url_list = seperate_commit_id_and_path(result_array)
    top_n_to_diffs(commit_id_before_list, commit_id_after_list, file_path_before_list, file_path_after_list, lcs_count_list, n, pool_dir, candidates, url_list)
    # print(f"[debug.log] commit_id_before_list length : {len(commit_id_before_list)}")
    # print(f"[debug.log] commit_id_after_list length : {len(commit_id_after_list)}")
    # print(f"[debug.log] file_path_before_list length : {len(file_path_before_list)}")
    # print(f"[debug.log] file_path_after_list length : {len(file_path_after_list)}")    

if __name__ == '__main__':
    main(sys.argv)


# *_gumtree_vector.csv
# *_commit_file.csv