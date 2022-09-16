import shutil
import sys
import os
import zipfile
import subprocess

import argparse
import configparser
import jproperties
import datetime as dt
# import pandas as pd



'''
    TODO:
        - Add '--rebuild' option. If enabled, SPI.py will rebuild all SPI submodules.
        - Config .properties files here. DO NOT additionally make shell scripts.
'''

# cases, settings = list(), dict()

def parse_argv() -> tuple:
    parser = argparse.ArgumentParser()

    parser.add_argument("-m", "--mode",     choices = ("github", "batch", "defects4j"),
                        help = "Tells in what mode to run SPI.")
    parser.add_argument("-t", "--target",   type = str,
                        help = "Tells what project to run, or what file to read data from.")
    parser.add_argument("-c", "--config",   type = str,
                        help = "Tells on what environment to run SPI.")

    parser.add_argument("-d", "--debug",    action = "store_true")

    parser.add_argument("-r", "--rebuild",  action = "store_true",
                        help = "Rebuilds all SPI submodules if enabled.")

    parser.add_argument("-q", "--quiet",    action = 'store_true',
                        help = "Quiet output. Suppresses INFO messages, but errors and warnings will still be printed out.")
    parser.add_argument("-v", "--verbose",  action = 'store_true',
                        help = "Detailed output. Use it to debug.")

    args = parser.parse_args()

    cases, settings = list(), dict()
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

    configs = configparser.ConfigParser()
    configs.read("SPI.ini")

    return (cases, settings, configs)

#######
# Misc
#######

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
        # for submodule in ("BuggyChangeCollector", "AllChangeCollector", "LCE"):
        for submodule in ("ChangeCollector", "LCE"):
            assert rebuild(submodule, root)4
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

    print("All submodules have been successfully rebuilt.")
    print()
    return True


#######
# Module launcher
#######

# def run_BCC() -> bool:
#     try:
#         copy("bcc.properties", f"{case['target_dir']}/")
#         assert subprocess.run(["app", f"{case['target_dir']}/bcc.properties"], cwd = "./pkg/BuggyChangeCollector/bin")
#     except:
#         return False
#     return True

def run_CC(case : dict, config : configparser.SectionProxy) -> bool:
    try:
        # copy .properties file
        # run ACC
        # 
        prop_CC = jroperties.Properties()
        for key in config.keys():
            prop_CC[key] = config[key]
        with open(f"{case['target_dir']}/CC.properties", "wb") as f:
            prop_CC.store(f, encoding = "UTF-8")

        # assert subprocess.run(["app", "-l", f"{case['target_dir']}/collection.txt"], cwd = "./pkg/AllChangeCollector/bin")
        assert subprocess.run(["app", f"{case['target_dir']}/properties/CC.properties"], cwd = "./pkg/ChangeCollector/bin")
    except:
        return False
    return True

def run_LCE(case : dict, config : configparser.SectionProxy) -> bool:
    try:
        prop_LCE = jroperties.Properties()
        for key in config.keys():
            prop_LCE[key] = config[key]
        with open(f"{case['target_dir']}/properties/LCE.properties", "wb") as f:
            prop_LCE.store(f, encoding = "UTF-8")

        assert subprocess.run(["app", f"{case['target_dir']}/properties/lce.properties"], cwd = "./pkg/LCE/bin")
    except:
        return False
    return True

def run_ConFix(case : dict, config : configparser.SectionProxy) -> bool:
    try:
        prop_ConFix = jroperties.Properties()
        for key in config.keys():
            prop_ix[key] = config[key]
        with open(f"{case['target_dir']}/properties/ConFix.properties", "wb") as f:
            prop_ConFix.store(f, encoding = "UTF-8")

        if case['is_defects4j'] == True:
            assert subprocess.run(["python3", "run_confix.py", "-d", "true", "-h", case['hash_id']], cwd = "./core/confix/")
        else:
            assert subprocess.run(["python3", "run_confix.py", "-h", case['hash_id'], "-i", f"{case['source_path']},{case['target_path']},{case['test_list']},{case['test_target_path']},{case['compile_target_path']},{case['build_tool']}"], cwd = "./core/confix/")

        
    except:
        return False
    return True

#######
# Main
#######

def main(argv):
    cases, settings, configurations = parse_argv()

    root = os.getcwd()
    SPI_core_directory = f"{root}/core"
    target_dir = f"{root}/target"

    if settings["rebuild"]:
        print("Have been requested to rebuild all submodules. Commencing...")
        if rebuild_all(root):
            print("All submodules have been successfully rebuilt."root)

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

    whole_start = dt.datetime.now()

    # Initialize pre-setups

    # Run SPI modules one by one

    # whole_start = dt.datetime.now()

    out, err = None, None
    if settings["quiet"]:
        out, err = subprocess.DEVNULL, subprocess.DEVNULL

    for case in cases:
        # case['hash_id'] = f"{hash_prefix}_{case['project_name']}"
        case['hash_id'] = f"batch_{hash_suffix}_{case['project_name']}" if settings["mode"] == "batch" else f"{case['project_name']}_{hash_suffix}"
        case['target_dir'] = f"{root}/target/{case['hash_id']}"

        each_exit_code = None
        each_start = dt.datetime.now()

        with open(f"{root}/log_{case['hash_id']}.txt", "a") as outfile:
            outfile.write(f"Launching SPI upon {case['project_name']}... Start time at {each_start.strftime('%Y-%m-%d %H:%M:%S')}\n")

        os.makedirs(target_dir)

        step = 0

        try:
            assert run_CC(case) is True, "'Commit Collector' Module launch failed"
            assert run_LCE(case) is True, "'Longest Common subvector Extractor' Module launch failed"
            assert run_ConFix(case) is True, "'ConFix' Module launch failed"

        except AssertionError as e:
            print(e)
            print("Total SPI has failed.")

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
