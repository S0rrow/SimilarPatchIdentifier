if [ -d "result" ]; then
   echo "result directory already exists"
   cd result
   rm -rf *.csv
   cd ..
else
   mkdir result
fi
python3 combiner.py -p /home/codemodel/timatree/AllChangeCollector/app/build/distributions/app/bin/vector -f /home/codemodel/timatree/AllChangeCollector/app/build/distributions/app/bin/commit_file -r result