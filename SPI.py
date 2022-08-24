import argparse

import getopt
import sys
import os
import datetime as dt
import pandas as pd
import configparser
import subprocess

'''
    TODO:
        - Add '--rebuild' option. If enabled, SPI.py will rebuild all SPI submodules.
        - Config .properties files here. DO NOT additionally make shell scripts.
'''

def print_help(cmd):
    options_list = ['repository', 'commit_id', 'faulty_file', 'faulty_line', 'source_path', 'target_path', 'test_list', 'test_target_path', 'compile_target_path', 'build_tool']

    print("Usage :")
    print(f"- Defects4J check         : {cmd} [-d / --defects4j] <Identifier>-<Bug-ID>")
    print(f"- Custom repository check : {cmd} [-c / --custom] <custom_project_config_file")
    print(f"- Defects4J check w/batch : {cmd} [-b / --defects4j_batch] <cases_file>")
    print()

def parse_argv():
    parser = argparse.ArgumentParser()
    parser.add_argument("-d", "--defects4j",    nargs = "?",   dest = "bug",    default = "Closure-14",             help = "Specifies on which Defects4J bug to run SPI. Runs SPI on Closure-14 if BUG is not given.")
    parser.add_argument("-b", "--batch",        nargs = "?",   dest = "batch_file",   default = "batch_of_D4J_bugs.txt",  help = "Specifies on which Defects4J bugs to run SPI. Retrieves bug names from patch_of_D4J_bugs.txt if FILE is not given.")
    parser.add_argument("-c", "--custom",       nargs = "?",   dest = "custom_file",   default = "custom_project.txt",     help = "Specifies (in detail) on which GitHub project to run SPI. Retrieves data from custom_project.txt if FILE is not given.")

    parser.add_argument("-r", "--rebuild",      action = "store_true",          help = "Rebuilds all SPI submodules if enabled.")

    parser.add_argument("-q", "--quiet",        action = 'store_true',          help = "Quiet output. Suppresses INFO messages, but errors and warnings will still be printed out.")
    parser.add_argument("-v", "--verbose",      action = 'store_true',          help = "Detailed output. Use it to debug.")

    args = parser.parse_args()
    print(args)

    # SPI_config = configparser.ConfigParser()
    # SPI_config.read("SPI_config.ini")

    # custom = configparser.ConfigParser()

    # try:
    #     opts, args = getopt.getopt(argv[1:], options, options_long)
    # except getopt.GetoptError as err:
    #     print(err)
    #     sys.exit(-1)
    
    # is_quiet = False
    # is_on_TUI = False

    # if len(args) != 0:
    #     print(f"Arguments {args} ignored.")

    # cases = []

    # for option, argument in opts:
    #     if option == '--help':
    #         print_help(argv[0])
    #         sys.exit(0)

    #     elif option in ['-d', '--defects4j']:
    #         case = dict()
    #         case['is_defects4j'] = True
    #         case['project_name'] = argument
    #         case['identifier'], case['bug_id'] = argument.split('-')

    #         cases.append(case)

    #     elif option in ['-c', '--custom']:
    #         custom.read(argument)

    #         case = dict()
    #         case.update(custom['Project'])
    #         case['is_defects4j'] = False
    #         case['project_name'] = case['repository'].rsplit('/', 1)[-1]
    #         case['identifier'] = case['project_name']

    #         cases.append(case)

    #     elif option in ['-b', '--defects4j_batch']:
    #         with open(argument, 'r') as infile:
    #             for each in infile.read().splitlines():
    #                 case = dict()
    #                 case['is_defects4j'] = True
    #                 case['project_name'] = each
    #                 case['identifier'], case['bug_id'] = each.split('-')
                    
    #                 cases.append(case)

    #     elif option in ['-q', '--quiet']:
    #         is_quiet = True

    #     elif option in ['--TUI']:
    #         is_on_TUI = True

    return None # return target data


def main(argv):
    cases = parse_argv()


    # Parse arguments given


    # if 

    

    # hash_prefix = str(abs(hash(f"{dt.datetime.now().strftime('%Y%m%d%H%M%S')}")))[-6:]
    # if len(cases) > 1:
    #     hash_prefix = f"{hash_prefix}_batch"

    # # if not is_test:
    # #     case['hash_id'] = abs(hash(f"{case['project_name']}-{dt.datetime.now().strftime('%Y%m%d%H%M%S')}"))
    # # print(f"Hash ID generated as {case['hash_id']}. Find byproducts in ./target/{case['hash_id']}")

    # # Run SimonFix Engine from those orders
    
    # exit_code = 0
    # executing_command = ""

    # success = 0
    # fail = 0

    # step = 0

    # root = os.getcwd()
    # SPI_core_directory = f"{root}/core"
    # target_dir = SPI_config['Default']['target_directory']

    # whole_start = dt.datetime.now()

    # out, err = None, None
    # elif is_quiet:
    #     out, err = subprocess.DEVNULL, subprocess.DEVNULL

    # for case in cases:
    #     # case['hash_id'] = f"{hash_prefix}_{case['project_name']}"
    #     case['hash_id'] = f"{hash_prefix}_{case['project_name']}"


    #     each_exit_code = None
    #     each_start = dt.datetime.now()

    #     with open(f"{root}/log_{hash_prefix}.txt", "a") as outfile:
    #         outfile.write(f"Launching SPI upon {case['project_name']}... Start time at {each_start.strftime('%Y-%m-%d %H:%M:%S')}\n")

    #     target_dir = f"{root}/target/{case['hash_id']}"
    #     os.makedirs(target_dir)

    #     if is_on_TUI:
    #         print(f"EOS0 {case['hash_id']} {target_dir}")

    #     step = 0
        
    #     try:
    #         step = 1
    #         # Commit Collector
    #         if not is_quiet:
    #             print("||| Step 1. Launching Commit Collector...")
    #         start = dt.datetime.now()
    #         # Commence


    #         if case['is_defects4j'] == True:
    #             executing_command = f"python3 {SPI_core_directory}/commit_collector.py -d true -h {case['hash_id']} -i {case['project_name']}"
    #         else:
    #             executing_command = f"python3 {SPI_core_directory}/commit_collector.py -h {case['hash_id']} -i {case['project_name']},{case['faulty_file']},{case['faulty_line']},{case['commit_id']},{case['repository']}"
    #         print(executing_command)
    #         assert subprocess.run(executing_command, shell = True, stdout = out, stderr = err).returncode == 0, "Failure occurred launching Commit Collector."

    #         bfic = pd.read_csv(f"{target_dir}/outputs/commit_collector/BFIC.csv", names = ['Project', 'D4J ID', 'Faulty file path', 'Faulty line', 'FIC_sha', 'BFIC_sha']).values[1]
    #         case['buggy_file'] = f"{target_dir}/{case['identifier']}/{bfic[2]}"
    #         buggy_file_pt1 = f"{target_dir}/{case['identifier']}"
    #         buggy_file_pt2 = f"{bfic[2]}"


    #         end = dt.datetime.now()
    #         if not is_quiet:
    #             print(f"||| Step 1 Time taken : {(end - start)}\n")

    #         if is_on_TUI:
    #             print(f"EOS1 {buggy_file_pt1} {buggy_file_pt2} {bfic[3]}")


    #         step = 2
    #         # AllChangeCollector
    #         if not is_quiet:
    #             print("||| Step 2. Launching AllChangeCollector...")
    #         start = dt.datetime.now()
    #         # Commence


    #         ACC_output_directory = f"{target_dir}/outputs/AllChangeCollector"
    #         JAVA_11_HOME = f"/usr/lib/jvm/java-11-openjdk-amd64"
    #         ACC_core_directory = f"{SPI_core_directory}/AllChangeCollector/app/build/distributions/app/bin/"
    #         os.makedirs(ACC_output_directory)

    #         with open(f"{target_dir}/collection.txt", "w") as outfile:
    #             outfile.write(f"{bfic[2]} {bfic[4]} {case['project_name']} {target_dir}/{case['identifier']}\n")

    #         assert subprocess.run(f"cd {ACC_output_directory}; JAVA_HOME={JAVA_11_HOME} {ACC_core_directory}/app -l {target_dir}/collection.txt", shell = True, stdout = out, stderr = err).returncode == 0, "Failure occurred launching AllChangeCollector."


    #         end = dt.datetime.now()
    #         if not is_quiet:
    #             print(f"||| Step 2 Time taken : {(end - start)}\n")
    #         if is_on_TUI:
    #             print(f"EOS2")


    #         step = 3
    #         # Longest Common sub-vector Extractor (LCE)
    #         if not is_quiet:
    #             print("||| Step 3. Launching Longest Common sub-vector Extractor(LCE)...")
    #         start = dt.datetime.now()
    #         # Commence

    #         candidate_number = 10
    #         LCE_output_directory = f"{target_dir}/outputs/fv4202"

    #         os.makedirs(LCE_output_directory)
    #         os.makedirs(f"{LCE_output_directory}/pool")
    #         os.makedirs(f"{LCE_output_directory}/result")
    #         os.makedirs(f"{target_dir}/outputs/prepare_pool_source")

    #         gumtree_vector = f"{root}/CSV_Combiner/result/gumtree_vector.csv"
    #         commit_file = f"{root}/CSV_Combiner/result/commit_file.csv"
    #         target_vector = f"{ACC_output_directory}/vector/{case['project_name']}_gumtree_vector.csv"
    #         d4j_project_name, d4j_project_num = case['project_name'].split("-")
    #         with open(f"{LCE_output_directory}/lce.properties", "w") as outfile:
    #             outfile.write(f"SPI.dir={root}\npool_file.dir={gumtree_vector}\nmeta_pool_file.dir={commit_file}\ntarget_vector.dir={target_vector}\npool.dir={LCE_output_directory}/pool/\ncandidates.dir={target_dir}/outputs/prepare_pool_source/\ncandidate_number={candidate_number}\nd4j_project_name={d4j_project_name}\nd4j_project_num={d4j_project_num}\n")
    #         assert subprocess.run(f"cd {SPI_core_directory}/LCE; gradle run -args=\"{LCE_output_directory}/lce.properties\"")
    #         #assert subprocess.run(f"cd {SPI_core_directory}/LCE; python3 main.py -g {gumtree_vector} -c {commit_file} -t {target_vector} -r {LCE_output_directory} > {LCE_output_directory}/log.txt", shell = True, stdout = out, stderr = err).returncode == 0, "Failure occurred launching LCE part 1."
    #         #assert subprocess.run(f"cd {SPI_core_directory}/LCE; python3 validator.py -f meta_resultPool.csv -d {LCE_output_directory}/pool -n 10 -r {LCE_output_directory} -c {target_dir}/outputs/prepare_pool_source >> {LCE_output_directory}/log.txt", shell = True, stdout = out, stderr = err).returncode == 0, "Failure occurred launching LCE part 2."


    #         end = dt.datetime.now()
    #         if not is_quiet:
    #             print(f"||| Step 3 Time taken : {(end - start)}\n")            
    #         if is_on_TUI:
    #             print(f"EOS3 {LCE_output_directory}/similar_patch_list.txt")

    #         step = 4
    #         # ConFix
    #         if not is_quiet:
    #             print("||| Step 4. Launching ConFix...")
    #         start = dt.datetime.now()
    #         # Commence


    #         if case['is_defects4j'] == True:
    #             executing_command = f"python3 {SPI_core_directory}/confix/run_confix.py -d true -h {case['hash_id']}"
    #         else:
    #             executing_command = f"python3 {SPI_core_directory}/confix/run_confix.py -h {case['hash_id']} -i {case['source_path']},{case['target_path']},{case['test_list']},{case['test_target_path']},{case['compile_target_path']},{case['build_tool']}"
    #         each_exit_code = subprocess.run(executing_command, shell = True, stdout = out, stderr = err).returncode


    #         end = dt.datetime.now()
    #         if not is_quiet:
    #             print(f"||| Step 4 Time taken : {(end - start)}")
    #         if is_on_TUI:
    #             if each_exit_code == 0:
    #                 print("EOS4 Success") # End of Step 4
    #             else:
    #                 print("EOS4 Failure") # End of Step 4

    #     except AssertionError as e:
    #         each_exit_code = 1
    #         print(f"!EOS{step}")

    #     each_end = dt.datetime.now()
    #     each_elapsed_time = (each_end - each_start)

    #     print(f"||| Elapsed Time for {case['project_name']} : {each_elapsed_time}")
    #     if each_exit_code != 0:
    #         print("||| SPI failed to find out the patch, or aborted due to abnormal exit.")
    #         fail += 1
    #     else:
    #         print("||| SPI succeeded to find out the plausible patch!")
    #         print("||| Generated Patch Content:\n")
    #         subprocess.run(f"cat {target_dir}/diff_file.txt", shell = True)
    #         success += 1

    #     with open(f"{root}/log_{hash_prefix}.txt", "a") as outfile:
    #         outfile.write(f"Launch upon {case['project_name']}... finished with {'failure' if each_exit_code != 0 else 'success'} at time {each_end.strftime('%Y-%m-%d %H:%M:%S')}\n")

    # whole_end = dt.datetime.now()
    # elapsed_time = (whole_end - whole_start)

    # print(f"||| Total Elapsed Time : {elapsed_time}")

    # print(f"{success} succeeded, {fail} failed.")

    


if __name__ == '__main__':
    main(sys.argv)
