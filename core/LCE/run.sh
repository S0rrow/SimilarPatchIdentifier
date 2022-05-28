# remove remaining result files from previous run
# $0 = shell_script
# $1 = hash-id
# $2 = project-id : hash-id and D4J project ID of execution
# 2022052521224055_Closure-14

echo "executing "$0" on batch_"$1"-"$2"..."
cd /home/codemodel/turbstructor/M48A2/target/$1/outputs
mkdir fv4202 # make result directory
cd /home/codemodel/turbstructor/M48A2/core/LCE

# $3 = gumtree_vector : change vector pool from ACC
# $4 = commit_file : commit IDs and file paths of each change vector
# $5 = targetVector : change vector between target vector and a commit just before that target vector
# $6 = result_dir: where resulting vectors will appear
# /home/codemodel/leshen/APR/target/$1_$2/outputs/fv4202

python3 main.py -g $3 -c $4 -t $5 -r /home/codemodel/turbstructor/M48A2/target/$1/outputs/fv4202 > /home/codemodel/turbstructor/M48A2/target/$1/outputs/fv4202/log.txt

# remove remaining diff texts
cd result
rm -rf diff*.txt
cd ..
# remove remaining candidate source codes
cd candidates
rm -rf *.java
cd ..
python3 validator.py -f meta_resultPool.csv -d /home/codemodel/turbstructor/M48A2/core/LCE/pool -n 10 -r /home/codemodel/turbstructor/M48A2/target/$1/outputs/fv4202/ -c $6 >> /home/codemodel/turbstructor/M48A2/target/$1/outputs/fv4202/log.txt