import argparse
import configparser
import shutil
import sys
import os
import zipfile
import datetime as dt
# import pandas as pd

import subprocess

'''
    TODO:
        - Add '--rebuild' option. If enabled, SPI.py will rebuild all SPI submodules.
        - Config .properties files here. DO NOT additionally make shell scripts.
'''

def parse_argv():
    parser = argparse.ArgumentParser()

    parser.add_argument("-m", "--mode",     choices = ("github", "batch", "defects4j"),
                        help = "Tells in what mode to run SPI.")
    parser.add_argument("-t", "--target",   type = str,
                        help = "Tells what project to run, or what file to read data from.")

    parser.add_argument("-d", "--debug",    action = "store_true")

    parser.add_argument("-r", "--rebuild",  action = "store_true",
                        help = "Rebuilds all SPI submodules if enabled.")

    parser.add_argument("-q", "--quiet",    action = 'store_true',
                        help = "Quiet output. Suppresses INFO messages, but errors and warnings will still be printed out.")
    parser.add_argument("-v", "--verbose",  action = 'store_true',
                        help = "Detailed output. Use it to debug.")

    args = parser.parse_args()

    cases = list()
    case = dict()
    settings = dict()
    if args.debug == True:
        cases.append(dict())
        cases[-1]['mode'] = 'defects4j'
        cases[-1]['project_name'] = 'Closure-14'
        cases[-1]['identifier'], cases[-1]['bug_id'] = cases[-1]['project_name'].split('-')
    else:
        settings['mode'] = args.mode

        if args.mode == 'defects4j':
            cases.append(dict())
            cases[-1]['project_name'] = args.target
            cases[-1]['identifier'], cases[-1]['bug_id'] = cases[-1]['project_name'].split('-')

        elif args.mode == 'batch':
            with open(args.target, 'r') as infile:
                for bug in infile.read().splitlines():
                    cases.append(dict())
                    cases[-1]['project_name'] = bug
                    identifier, bug_id = bug.split('-')
                    cases[-1]['identifier'] = identifier
                    cases[-1]['bug_id'] = bug_id
                

        elif args.mode == 'github':
            # Enhance reading options here
            cases.append(dict())
            cases[-1]['project_name'] = cases[-1]['repository'].rsplit('/', 1)[-1]
            cases[-1]['identifier'] = cases[-1]['project_name']

    settings['verbose'] = args.verbose
    settings['quiet'] = False if args.verbose else args.quiet # suppresses quiet option if verbose option is given
    settings['rebuild'] = args.rebuild

    return cases, settings # return target data

def rebuild(module_name : str, root) -> bool:
    #print(f"> Rebuilding {module_name}...")
    try:
        assert subprocess.run(("gradle", "distZip", "-q"), cwd = f"./core/{module_name}", shell=True)
        #assert subprocess.run(("mv", "app/build/distributions/app.zip", "../../pkg/"), cwd = f"./core/{module_name}", shell=True)
        #print(f"> moving app.zip to pkg...")
        if not move(f"./core/{module_name}/app/build/distributions/app.zip", f"{root}/pkg/", copy_function = shutil.copy):
            print(f"> Error occurred while moving app.zip to pkg.")
            return False
        #print(f"> unzipping {module_name}...")
        #assert subprocess.run(("unzip", "-q", "app.zip"), cwd = "./pkg", shell=True)
        if not unzip("./pkg/app.zip", "./pkg/"):
            print(f"> Error occurred while unzipping {module_name}.")
            return False
        #print(f"> creating {module_name} directory...")
        if not os.path.exists(f"./pkg/{module_name}"):
            assert subprocess.run(("mkdir", f"{module_name}"), cwd = "./pkg", shell=True)
        #assert subprocess.run(("mv", "app", module_name), cwd = "./pkg", shell=True)
        #print(f"> moving app to {module_name}...")
        if not move("./pkg/app", f"./pkg/{module_name}", shutil.copy2):
            print(f"> Error occurred while moving app to {module_name}.")
            return False

        #assert subprocess.run(("rm", "app.zip"), cwd = "./pkg", shell=True)
        #print(f"> removing app.zip...")
        if not remove("./pkg/app.zip"):
            print(f"> Error occurred while removing app.zip.")
            return False

    except Exception as e:
        print(f"> Error occurred while rebuilding submodule {module_name}.")
        print(f"> Error : {e}")
        return False
    #print(f"> Done.")
    return True

def rebuild_confix(root) -> bool:
    # os.system(f"cd {root}/core/confix/ConFix-code ;"
    #         + "mvn clean package ;"
    #         + f"cp target/confix-0.0.1-SNAPSHOT-jar-with-dependencies.jar {root}/core/confix/lib/confix-ami_torun.jar")
    try:
        assert subprocess.run(("mvn", "clean", "package", "-q"), cwd = "./core/confix/ConFix-code", shell=True)
        #assert subprocess.run(("cp", "target/confix-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "../lib/confix-ami_torun.jar"), cwd = "./core/confix/ConFix-code", shell=True)
        if not (copy(f"{root}/core/confix/ConFix-code/target/confix-0.0.1-SNAPSHOT-jar-with-dependencies.jar", f"{root}/core/confix/lib/confix-ami_torun.jar")):
            print("Error occurred while copying ConFix jar file.")
            return False

    except AssertionError as e:
        print("> ! Error occurred while rebuilding ConFix.")
        print("> ! Error : ", e)
        return False

    return True

def move(location, destination, copy_function) -> bool:
    try:
        shutil.move(location, destination, copy_function)
    except Exception as e:
        print(f"> Error: {location} : {e.strerror}")
        print(f"> ! Error occurred while moving {location} to {destination}.")
        return False
    return True

def unzip(file, destination):
    try:
        with zipfile.ZipFile(file, 'r') as zip_ref:
            zip_ref.extractall(destination)
    except Exception as e:
        print(f"> Error: {file} : {e.strerror}")
        print(f"> ! Error occurred while unzipping {file}.")
        return False
    return True

def copy(file, destination):
    try:
        shutil.copy(file, destination)
    except Exception as e:
        print(f"> Error: {file} : {e.strerror}")
        print(f"> ! Error occurred while copying {file} to {destination}.")
        return False
    return True

def remove(path):
    try:
        if(os.path.exists(path)):
            if(os.path.isdir(path)):
                shutil.rmtree(path)
            else:
                os.remove(path)
    except Exception as e:
        print(f"> Error: {path} : {e.strerror}")
        print(f"> ! Error occurred while removing {path}.")
        return False
    return True

def rebuild_all(root):
    try:
        #assert subprocess.run(("rm", "-rf", "pkg"))
        if not (remove(f"{root}/pkg")):
            print(f"> ! Error occurred while removing {root}/pkg.")
        assert subprocess.run(("mkdir", "pkg"), shell=True)
        #os.mkdir(f"{root}/pkg")
        for submodule in ("BuggyChangeCollector", "AllChangeCollector", "LCE"):
            assert rebuild(submodule, root)
            print(f"> Successfully rebuilt submodule {submodule}.")

        assert rebuild_confix(root) # ConFix uses maven unlike any other packages; this should be handled differently.
        print(f"> Successfully rebuilt submodule ConFix.")
        print()

    except AssertionError as e:
        print("> ! Error occurred while rebuilding modules.")
        print()
        return False
    except Exception as e:
        print(f"> ! Error occurred while rebuilding modules: {e}")
        print()
        return False
    return True

def load_properties(filepath, sep='=', comment_char='#')->dict:
    props = {}
    with open(filepath, "rt") as f:
        for line in f:
            l = line.strip()
            if l and not l.startswith(comment_char):
                key_value = l.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip().strip('"') 
                props[key] = value 
    return props

def BuggyChangeCollector():
    return True

def AllChangeCollector():
    return True

def LCE():
    return True

def ConFix():
    return True

def main(argv):
    cases, settings = parse_argv()

    root = os.getcwd()
    SPI_core_directory = f"{root}/core"
    target_dir = f"{root}/target"

    if settings["rebuild"]:
        print("Have been requested to rebuild all submodules. Commencing...")
        if rebuild_all(root):
            print("All submodules have been successfully rebuilt.")

    # print(case)
    # print(settings)

    if settings["mode"] is None:
        print("You have not told me what to fix. Exiting program.")
        sys.exit(0)

    
    # Run SPI modules one by one

    hash_suffix = str(abs(hash(f"{dt.datetime.now().strftime('%Y%m%d%H%M%S')}")))[-6:]
    print(f"[Hash ID generated as {case['hash_id']}]. Find byproducts in ./target/{case['hash_id']}")

    # Run SimonFix Engine from those orders

    exit_code = 0
    executing_command = ""

    success = 0
    fail = 0

    step = 0

    whole_start = dt.datetime.now()

    # Initialize pre-setups

    # Run SPI modules one by one

    # whole_start = dt.datetime.now()

    out, err = None, None
    if settings["quiet"]:
        out, err = subprocess.DEVNULL, subprocess.DEVNULL

    for case in cases:
        # case['hash_id'] = f"{hash_prefix}_{case['project_name']}"
        case["hash_id"] = f"batch_{hash_suffix}_{case['project_name']}" if settings["mode"] == "batch" else f"{case['project_name']}_{hash_suffix}"

        each_exit_code = None
        each_start = dt.datetime.now()

        with open(f"{root}/log_{case['hash_id']}.txt", "a") as outfile:
            outfile.write(f"Launching SPI upon {case['project_name']}... Start time at {each_start.strftime('%Y-%m-%d %H:%M:%S')}\n")

        target_dir = f"{root}/target/{case['hash_id']}"
        os.makedirs(target_dir)

        step = 0

        try:
            #assert subprocess.run(("cp", "SPI.properties", f"{target_dir}/"), shell=True)
            copy(f"{root}/SPI.properties", f"{target_dir}/")
            # Commit Collector
            print("||| Step 1. Launching Commit Collector...")
            start = dt.datetime.now()
            # Commence

            if settings["mode"] == "github":
                pass
                # executing_command = f"{root}/pkg/BuggyChangeCollector/bin/app -h {case['hash_id']} --githubinput {case['project_name']},{case['faulty_file']},{case['faulty_line']},{case['commit_id']},{case['repository']}"
            else:
                executing_command = f"{root}/pkg/BuggyChangeCollector/bin/app -h {case['hash_id']} --defects4j {case['project_name']} --config {target_dir}/SPI.properties"
            print(executing_command)
            assert subprocess.run(executing_command, shell = True).returncode == 0, "Failure occurred launching Commit Collector."

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
            # AllChangeCollector
            print("||| Step 2. Launching AllChangeCollector...")
            start = dt.datetime.now()
            # Commence

            ACC_output_directory = f"{target_dir}/outputs/AllChangeCollector"
            JAVA_11_HOME = f"/usr/lib/jvm/java-11-openjdk-amd64"
            ACC_core_directory = f"{SPI_core_directory}/AllChangeCollector/app/build/distributions/app/bin/"
            os.makedirs(ACC_output_directory)

            with open(f"{target_dir}/collection.txt", "w") as outfile:
                outfile.write(f"{bfic[2]} {bfic[4]} {case['project_name']} {target_dir}/{case['identifier']}\n")

            assert subprocess.run(f"cd {ACC_output_directory}; JAVA_HOME={JAVA_11_HOME} {ACC_core_directory}/app -l {target_dir}/collection.txt", shell = True, stdout = out, stderr = err).returncode == 0, "Failure occurred launching AllChangeCollector."


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

        except AssertionError as e:
            each_exit_code = 1
            print(f"!EOS")

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
