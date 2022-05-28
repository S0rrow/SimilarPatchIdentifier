# $1 = before CID
# $2 = git directory
# $3 = target directory
# $4 = file path
python3 reverse_parser.py -c $1 -g $2 -t $3 -f $4
# result = afterCID.txt

#./run.sh e950938f171b3cd6367e7e6d7b1d252e6f0e8c9b https://github.com/apache/commons-jcs.git target src/test/org/apache/jcs/access/TestCacheAccess.java