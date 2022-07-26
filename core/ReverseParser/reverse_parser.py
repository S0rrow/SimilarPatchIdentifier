import git
import getopt
import sys
import os
from subprocess import call
from pydriller import Repository
# import numpy as np
# import pandas as pd

def after_commit_id(commitID, gitDirectory, filePath, target=None):
    pwd = os.getcwd()
    # gitDirectory = pwd+"/"+gitDirectory

    filePath = filePath.split('/')[-1]
    nextID = ''
    breaker = False
    #commitList = list()#, filepath=filePath
    for commit in Repository(gitDirectory).traverse_commits():
        print(f"commit.hash = {commit.hash}")
        for m in commit.modified_files:
            if m.filename == filePath:
                breaker = True
        if breaker :
            nextID = commit.hash
            break
    call(f"echo {nextID} > {target}/afterCID.txt", shell=True)
    #return nextID

def main(argv):
    try:
        opts, args = getopt.getopt(argv[1:], "c:g:t:f:", ["commitID", "gitDirectory", "targetDirectory","filePath"])
    except getopt.GetoptError as err:
        print(err)
        sys.exit(2)
    commitID = ''
    gitDirectory = ''
    targetDirectory = ''
    filePath = ''
    for o, a in opts:
        if o in ("-H", "--help") or o in ("-h", "--hash"):
            print("")
            sys.exit(2)
        elif o in ("-c", "--commitID"):
            commitID = a
        elif o in ("-g", "--gitDirectory"):
            gitDirectory = a
        elif o in ("-t", "--targetDirectory"):
            targetDirectory = a
        elif o in ("-f", "--filePath"):
            filePath = a
        else:
            assert False, "unhandled option"
    print(f"filePath = {filePath}")
    after_commit_id(commitID, gitDirectory, filePath,targetDirectory)
# https://github.com/apache/ant-ivyde.git
# 4b26c431959a9685858e4959afe731bcdab88f08 = after
# 9de47d5a8f29c356405258f295ae0b70d6a0d2b8 = before

if __name__ == '__main__':
    main(sys.argv)
