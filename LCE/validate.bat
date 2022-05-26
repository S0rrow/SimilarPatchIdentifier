@echo off
cd .\result
if exist ".\*.txt" ( del *.txt )
cd ..
python validator.py -f meta_resultPool.csv -d jsoup -n 1