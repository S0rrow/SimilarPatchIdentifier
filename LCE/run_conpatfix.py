import getopt
import sys
import os
import datetime as dt

# from colorama import Fore, Back, Style # Colorized output

# def print(msg):
#     print(Back.CYAN + Fore.BLACK + msg + Style.RESET_ALL)

# def print(msg):
#     print(Back.YELLOW + Fore.BLACK + msg + Style.RESET_ALL)

# def print(msg):
#     print(Back.GREEN + Fore.WHITE + msg + Style.RESET_ALL)

# def print(msg):
#     print(Back.RED + Fore.WHITE + msg + Style.RESET_ALL)

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

def parse_args(args):
    # Parse arguments given
    options_list = ['repository', 'commit_id', 'faulty_file', 'faulty_line', 'source_path', 'target_path', 'test_list', 'test_target_path', 'compile_target_path', 'build_tool']
    try:
        opts, args = getopt.getopt(argv[1:], "", [options + '=' for options in optionss_list] + ['help', 'defects4j=', 'options_file='])
    except getopt.GetoptError as err:
        print(err)
        sys.exit(-1)
    options = dict() # {'repository' : None, 'commit_id' : None, 'faulty_file' : None, 'faulty_line' : None, 'source_path' : None, 'target_path' : None, 'test_list' : None, 'test_target_path' : None, 'compile_target_path' : None, 'build_tool' : None, 'hash' : None}
    options['is_defects4j'] = False
    options['is_test'] = False

    if len(args) != 0:
        print(f"Arguments {args} ignored.")

    for option, argument in opts:
        if option == '--help':
            print_help(argv[0])
            sys.exit(0)

        elif option == '--defects4j':
            options['is_defects4j'] = True
            options['repository'] = argument

        elif option == "--repository":
            options['repository'] = argument

        elif option == "--commit_id":
            options['commit_id'] = argument

        elif option == "--faulty_file":
            options['faulty_file'] = argument

        elif option == "--faulty_line":
            options['faulty_line'] = argument

        elif option == "--source_path":
            options['source_path'] = argument

        elif option == "--target_path":
            options['target_path'] = argument

        elif option == "--test_list":
            options['test_list'] = argument

        elif option == "--test_target_path":
            options['test_target_path'] = argument

        elif option == "--compile_target_path":
            options['compile_target_path'] = argument

        elif option == "--build_tool":
            options['build_tool'] = argument
        
        elif option == "--options_file":
            with open(argument, 'r') as f:
                file_lines = f.readlines()
                for i, each in enumerate(optionss_list):
                    options[each] = file_lines[i].strip()
        elif option == "--test":
            options['is_test'] = True
            options['hash_id'] = argument

    options['project_name'] = options['repository'].rsplit('/', 1)[-1]

    optionss_list = ['repository', 'project_name', 'is_defects4j'] if options['is_defects4j'] == True else optionss_list + ['is_defects4j']
    input_validity = True

    print("Processed arguments are as follows:")
    for argument in optionss_list:
        print(f"- {argument} : {options[argument]}")

        if options.get(argument, None) == None:
            input_validity = False
            print(f"{argument} is not valid.")

    if input_validity == False:
        print("Error : All arguments should given except options_file.")
        print("You can also give file as an argument to 'options_file' options, but the file should contain all arguments required in order:")
        for each in options.keys():
            if each == 'project_name':
                pass
            print(f"- {each.replace('_', '-')}")
        print()
        print("Terminating program.")
        sys.exit(-1)
    
    print()
    print("Do you wish to continue with those arguments? (Y/n) : ", end = '')
    answer = input()

    if answer not in ['', 'Y', 'y']:
        return None
    else:
        return options

def main(argv):
    case = parse_args(argv)

    if not case['is_test']:
        case['hash_id'] = abs(hash(f"{case['project_name']}-{dt.datetime.now().strftime('%Y%m%d%H%M%S')}"))
    print(f"Hash ID generated as {case['hash_id']}. Find byproducts in ./target/{case['hash_id']}")

    # Run SimonFix Engine from those orders
    
    print()
    print("=" * 80)
    exit_code = 0
    executing_command = ""

    whole_start = dt.datetime.now()

    root = os.getcwd()

    for i in range(1): # no loop, used to activate 'break'
        print()

        # # Commit Collector
        
        # if case['is_defects4j'] == True:
        #     executing_command = f"python3 {root}/pool/runner_web/commit_collector_web.py -d true -h {case['hash_id']} -i {case['project_name']}"
        # else:
        #     executing_command = f"python3 {root}/pool/runner_web/commit_collector_web.py -h {case['hash_id']} -i {case['project_name']},{case['faulty_file']},{case['faulty_line']},{case['commit_id']},{case['repository']}"
        # print("||| Step 1. Launching Commit Collector...")
        # start = dt.datetime.now()
        # exit_code = run_command(executing_command)
        # end = dt.datetime.now()
        # print(f"||| Time taken : {(end - start)}")

        # if exit_code != 0:
        #     break

        # print()

        target_dir = f"{root}/target/{hash_id}_{identifier}-{bug_id}"
        bfic = pd.read_csv(f'{target_dir}/outputs/commit_collector/BFIC.csv', names = ['Project', 'D4J ID', 'Faulty file path', 'Faulty line', 'FIC_sha', 'BFIC_sha']).values[1]
        if case['is_defects4j'] == True:
            assert os.system(f"cd {target_dir}; defects4j checkout -p {identifier} -v {bug_id}b -w buggy") == 0, "checkout for buggy project failed"
        #assert os.system(f"cd {target_dir}; defects4j checkout -p {identifier} -v {bug_id}f -w fixed") == 0, "checkout for fixed project failed"

        assert os.system(f"cd {target_dir}; mkdir -p outputs; mkdir -p outputs/prepare_pool_source") == 0, "what?"
        # TODO
        # connect ACC in here
        assert os.system(f"cd ~/leshen/APR/AllChangeCollector\npython3 collect.py -f {bfic[2]} -c {bfic[4]} -p {identifier}-{bug_id} -g {target_dir}/{identifier}") == 0, 'executing ACC failed'
        assert os.system(f"cd {root}/LCE\n./run.sh {hash_id} {identifier}-{bug_id}") == 0, 'executing LCE failed'

        if case['is_defects4j'] == True:
            assert os.system(f"cp {target_dir}/buggy/{bfic[2]} {root}/LCE/target/targetVector.java") == 0, "copying buggy file failed"
        #assert os.system(f"cp {target_dir}/fixed/{bfic[2]} {target_dir}/outputs/prepare_pool_source/{identifier}_rank-1_new.java") == 0, "copying fixed file failed"

        if case['is_defects4j'] == True:
            os.system(f"rm -rf {target_dir}/buggy")
        #os.system(f"rm -rf {target_dir}/fixed")
        
        assert os.system(f"cd {target_dir}; touch done") == 0, "You cannot even make dummy file?"

        # # Change Vector Collector
        
        # if case['is_defects4j'] == True:
        #     executing_command = f"python3 {root}/pool/runner_web/change_vector_collector_web.py -d true -h {case['hash_id']}"
        # else:
        #     executing_command = f"python3 {root}/pool/runner_web/change_vector_collector_web.py -h {case['hash_id']}"
        # print("||| Step 2. Launching Change Vector Collector...")
        # start = dt.datetime.now()
        # exit_code = run_command(executing_command)
        # end = dt.datetime.now()
        # print(f"||| Time taken : {(end - start)}")

        # if exit_code != 0:
        #     break

        # print()

        # # SimFin

        # executing_command = f"python3 ./pool/simfin/gv_ae_web.py -p test -k 10 -h {case['hash_id']}" # -p means predict, -t means train; -k is for top-k neighbors
        # print("||| Step 3. Launching SimFin...")
        # start = dt.datetime.now()
        # exit_code = run_command(executing_command)
        # end = dt.datetime.now()
        # print(f"||| Time taken : {(end - start)}")

        # if exit_code != 0:
        #     break

        # print()

        # Prepare Pool Source

        executing_command = f"python3 ./pool/runner_web/prepare_pool_source_web.py -h {case['hash_id']}"
        print("||| Step 4. Preparing pool source for ConFix...")
        start = dt.datetime.now()
        exit_code = os.system(executing_command)
        end = dt.datetime.now()
        print(f"||| Time taken : {(end - start)}")

        if exit_code != 0:
            break

        print()

        # ConFix

        if case['is_defects4j'] == True:
            executing_command = f"python3 {root}/confix/run_confix_web.py -d true -h {case['hash_id']}"
        else:
            executing_command = f"python3 {root}/confix/run_confix_web.py -h {case['hash_id']} -i {case['source_path']},{case['target_path']},{case['test_list']},{case['test_target_path']},{case['compile_target_path']},{case['build_tool']}"
        print("||| Step 5. Executing ConFix...")
        start = dt.datetime.now()
        exit_code = os.system(executing_command)
        end = dt.datetime.now()
        print(f"||| Time taken : {(end - start)}")

    whole_end = dt.datetime.now()
    elapsed_time = (whole_end - whole_start)

    print()
    if exit_code != 0:
        print("||| SimonFix failed to find out the patch, or aborted due to abnormal exit.")
        print(f"||| Elapsed Time : {elapsed_time}")
    else:
        print("||| SimonFix succeeded to find out the plausible patch.")
        print(f"||| Elapsed Time : {elapsed_time}")
        print("||| Generated Patch Content:")
        print()
        exit_code = os.system(f"cat ./target/{case['hash_id']}/diff_file.txt")
    print()
    print("=" * 80)

    


if __name__ == '__main__':
    main(sys.argv)
