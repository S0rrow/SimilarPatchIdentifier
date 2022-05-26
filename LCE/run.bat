@echo off
cd .\result
if exist ".\*.csv" ( del *.csv )
if exist ".\*.txt" ( del *.txt )
cd ..
if exist ".\pool\" ( echo pool already exists ) else ( mkdir pool )
cd .\pool
if exist ".\jsoup\" ( echo jsoup already exists ) else ( git clone https://github.com/jhy/jsoup )
cd ..
python ./main.py -g jsoup_gumtree_vector.csv -c jsoup_commit_file.csv -t testVector.csv
