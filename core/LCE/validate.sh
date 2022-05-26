cd result
rm -rf diff*.txt
cd ..

cd candidates
rm -rf *.java
cd ..

python3 validator.py -f meta_resultPool.csv -d jsoup -n 10 >> result/log.txt