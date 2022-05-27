import getopt
import sys
import os
import datetime as dt
import pandas as pd
import configparser
import subprocess

def print_help(cmd):
    options_list = ['repository', 'commit_id', 'faulty_file', 'faulty_line', 'source_path', 'target_path', 'test_list', 'test_target_path', 'compile_target_path', 'build_tool']

    print("Usage :")
    print(f"- Defects4J check         : {cmd} --defects4j <Identifier>-<Bug-ID>")
    print(f"- Custom repository check : {cmd} --repository <github_repository> --commit_id <commit_id> --faulty_file <path_from_project_root> --faulty_line <buggy_line_number> --source_path <source_path> --target_path <target_path> --test_list <test_class_name> --test_target_path <test_target_path> --compile_target_path <compile_target_path> --build_tool <maven_or_gradle>")
    print(f"- Custom repository check : {cmd} --case_file <file_containing_arguments>")
    print()
    print("When bringing arguments from file, it should contain arguments in the following order:")
    for option in options_list:
        print(f"- {option}")
    print()

def run_command(cmd):
    print(f'||| Executing command "{cmd}"')
    print()
    print("=" * 80)
    print()

    exit_code = os.system(cmd)

    print()
    print("=" * 80)
    print()
    print(f"||| > Command exited with code {exit_code}")
    print()
    print("=" * 80)

    return exit_code


def main(argv):
    # Parse arguments given

    options_list = ['defects4j', 'custom', 'defects4j_batch']

    config = configparser.ConfigParser()
    config.read('conpatfix_settings.ini')

    print(config.sections())

    # options_list = ['repository', 'commit_id', 'faulty_file', 'faulty_line', 'source_path', 'target_path', 'test_list', 'test_target_path', 'compile_target_path', 'build_tool']
    try:
        # opts, args = getopt.getopt(argv[1:], "", [option + '=' for option in options_list] + ['help', 'defects4j=', 'case_file='])
        opts, args = getopt.getopt(argv[1:], "", options_list)
    except getopt.GetoptError as err:
        print(err)
        sys.exit(-1)
    is_test = False
    case = dict() # {'repository' : None, 'commit_id' : None, 'faulty_file' : None, 'faulty_line' : None, 'source_path' : None, 'target_path' : None, 'test_list' : None, 'test_target_path' : None, 'compile_target_path' : None, 'build_tool' : None, 'hash' : None}
    case['is_defects4j'] = False

    if len(args) != 0:
        print(f"Arguments {args} ignored.")

    cases = list()

    for option, _ in opts:
        if option == '--help':
            print_help(argv[0])
            sys.exit(0)

        elif option == '--defects4j':
            cases.append((True, config['Defects4J']['project'], config['Defects4J']['version']))

        # elif option == "--repository":
        #     case['repository'] = argument

        # elif option == "--case_file":
        #     with open(argument, 'r') as f:
        #         file_lines = f.readlines()
        #         for i, each in enumerate(options_list):
        #             case[each] = file_lines[i].strip()
        # elif option == "--test":
        #     is_test = True
        #     case['hash_id'] = argument

        elif option == '--defects4j_batch':
            with open(config['Defects4J-batch']['target_file'], 'r') as infile:
                for each in infile.read().splitlines():
                    cases.append([True] + each.split('-'))



    # print("Processed arguments are as follows:")
    # for argument in options_list:
    #     print(f"- {argument} : {case[argument]}")

    #     if case.get(argument, None) == None:
    #         input_validity = False
    #         print(f"{argument} is not valid.")

    # if input_validity == False:
    #     print("Error : All arguments should given except case_file.")
    #     print("You can also give file as an argument to 'case_file' option, but the file should contain all arguments required in order:")
    #     for each in case.keys():
    #         if each == 'project_name':
    #             pass
    #         print(f"- {each.replace('_', '-')}")
    #     print()
    #     print("Terminating program.")
    #     sys.exit(-1)
    
    # print()
    # print("Do you wish to continue with those arguments? (Y/n) : ", end = '')
    # answer = input()

    # if answer not in ['', 'Y', 'y']:
    #     exit(1)

    # case['project_name'] = case['repository'].rsplit('/', 1)[-1]

    hash_prefix = str(abs(hash(f"{dt.datetime.now().strftime('%Y%m%d%H%M%S')}")))[-6:]
    if len(cases) > 1:
        hash_prefix = f"batch_{hash_prefix}"

    # if not is_test:
    #     case['hash_id'] = abs(hash(f"{case['project_name']}-{dt.datetime.now().strftime('%Y%m%d%H%M%S')}"))
    # print(f"Hash ID generated as {case['hash_id']}. Find byproducts in ./target/{case['hash_id']}")

    # Run SimonFix Engine from those orders
    
    print()
    print("=" * 80)
    exit_code = 0
    executing_command = ""

    success = 0
    fail = 0

    root = os.getcwd()

    whole_start = dt.datetime.now()

    for each in cases:

        
        case['is_defects4j'] = each[0]
        if case['is_defects4j'] == True:
            case['project'], case['version'] = each[1], each[2]
            case['project_name'] = f"{case['project']}-{case['version']}"
        else:
            pass
        
        # case['hash_id'] = f"{hash_prefix}_{case['project_name']}"
        case['hash_id'] = f"{hash_prefix}_{case['project_name']}"


        each_exit_code = None
        each_start = dt.datetime.now()

        with open(f"{root}/log_{hash_prefix}.txt", "a") as outfile:
            outfile.write(f"Launching ConPatFix upon {case['project_name']}... Start time at {each_start.strftime('%Y-%m-%d %H:%M:%S')}\n")

        target_dir = f"{root}/target/{case['hash_id']}"
        subprocess.run(f"mkdir {target_dir}", shell = True)
        
        try:
            # Commit Collector
            if case['is_defects4j'] == True:
                executing_command = f"python3 ./core/commit_collector.py -d true -h {case['hash_id']} -i {case['project_name']}"
            else:
                executing_command = f"python3 ./core/commit_collector.py -h {case['hash_id']} -i {case['project_name']},{case['faulty_file']},{case['faulty_line']},{case['commit_id']},{case['repository']}"

            print("||| Step 1. Launching Commit Collector...")
            start = dt.datetime.now()
            assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred launching Commit Collector."
            end = dt.datetime.now()
            print(f"||| Step 1 Time taken : {(end - start)}")
            print()

            # AllChangeCollector
            print("||| Step 2. Launching AllChangeCollector...")
            start = dt.datetime.now()
            bfic = pd.read_csv(f"{target_dir}/outputs/commit_collector/BFIC.csv", names = ['Project', 'D4J ID', 'Faulty file path', 'Faulty line', 'FIC_sha', 'BFIC_sha']).values[1]
            case['buggy_file'] = f"{target_dir}/{case['project']}/{bfic[2]}"
            executing_command = f"echo {bfic[2]} {bfic[4]} {case['project']}-{case['version']} {target_dir}/{case['project']} > {target_dir}/collection.txt"
            assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred creating collection.txt."
            # executing_command = f"cd {root}/core/AllChangeCollector/app/build/distributions/app/bin; JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./app -l {target_dir}/collection.txt"
            executing_command = f"mkdir -p {target_dir}/outputs/AllChangeCollector"
            assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred creating AllChangeCollector byproduct directory in Step 2."
            executing_command = f"cd {target_dir}/outputs/AllChangeCollector; JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 {root}/core/AllChangeCollector/app/build/distributions/app/bin/app -l {target_dir}/collection.txt"
            # executing_command = f"cd /home/codemodel/leshen/APR/AllChangeCollector/app/build/distributions/app/bin; JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./app -l {target_dir}/collection.txt"
            assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred launching AllChangeCollector."
            end = dt.datetime.now()
            print(f"||| Step 2 Time taken : {(end - start)}")
            print()

            # Longest Common sub-vector Extractor (LCE)
            print("||| Step 3. Launching Longest Common sub-vector Extractor(LCE)...")
            start = dt.datetime.now()
            # executing_command = f"cp {case['buggy_file']} {root}/LCE/target/targetVector.java"
            # assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred copying buggy file to LCE target directory in Step 3."
            executing_command = f"mkdir -p {target_dir}/outputs/prepare_pool_source"
            assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred creating pool source directory in Step 3."
            executing_command = f"cd {root}/core/LCE; ./run.sh {case['hash_id']} {case['project_name']} /home/codemodel/leshen/CSV_Combiner/result/gumtree_vector.csv /home/codemodel/leshen/CSV_Combiner/result/commit_file.csv {target_dir}/outputs/AllChangeCollector/vector/{case['project_name']}_gumtree_vector.csv {target_dir}/outputs/prepare_pool_source"
            assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred launching LCE in Step 3."
            end = dt.datetime.now()
            print(f"||| Step 3 Time taken : {(end - start)}")
            print()

            print("||| Step 4. Launching ConFix...")
            start = dt.datetime.now()
            if case['is_defects4j'] == True:
                executing_command = f"python3 {root}/core/confix/run_confix_web.py -d true -h {case['hash_id']}"
            else:
                executing_command = f"python3 {root}/core/confix/run_confix_web.py -h {case['hash_id']} -i {case['source_path']},{case['target_path']},{case['test_list']},{case['test_target_path']},{case['compile_target_path']},{case['build_tool']}"
            start = dt.datetime.now()
            each_exit_code = subprocess.run(executing_command, shell = True).returncode #== 0, "Failure occurred launching ConFix in Step 4."
            end = dt.datetime.now()
            print(f"||| Step 4 Time taken : {(end - start)}")

        except AssertionError as e:
            each_exit_code = 1

        each_end = dt.datetime.now()
        each_elapsed_time = (each_end - each_start)

        print(f"||| Elapsed Time for {case['project_name']} : {each_elapsed_time}")
        if each_exit_code != 0:
            print("||| ConPatFix failed to find out the patch, or aborted due to abnormal exit.")
            fail += 1
        else:
            print("||| ConPatFix succeeded to find out the plausible patch!")
            print("||| Generated Patch Content:")
            print()
            executing_command = f"cat {target_dir}/diff_file.txt"
            subprocess.run(executing_command, shell = True)
            success += 1

        with open(f"{root}/log_{hash_prefix}.txt", "a") as outfile:
            outfile.write(f"Launch upon {case['project_name']}... finished with {'failure' if each_exit_code != 0 else 'success'} at time {each_end.strftime('%Y-%m-%d %H:%M:%S')}")

    whole_end = dt.datetime.now()
    elapsed_time = (whole_end - whole_start)

    print()

    print(f"{success} succeeded, {fail} failed.")

    


if __name__ == '__main__':
    main(sys.argv)
