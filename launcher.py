import shutil
import sys
import os
import zipfile
import subprocess
import traceback

import argparse
import configparser
import jproperties
import datetime as dt
import csv



'''
    TODO:
        - Add '--rebuild' option. If enabled, SPI.py will rebuild all SPI submodules.
        - Config .properties files here. DO NOT additionally make shell scripts.
'''

def parse_argv() -> tuple:
    parser = argparse.ArgumentParser()

    # parser.add_argument("-m", "--mode",     choices = ("github", "batch", "defects4j"),
    #                     help = "Tells in what mode to run SPI.")
    # parser.add_argument("-t", "--target",   type = str,
    #                     help = "Tells what project to run, or what file to read data from.")
    parser.add_argument("-c", "--config",   type = str,     default = "SPI.ini",
                        help = "Tells on what environment to run SPI.")

    parser.add_argument("-d", "--debug",    action = "store_true")

    parser.add_argument("-r", "--rebuild",  action = "store_true",
                        help = "Rebuilds all SPI submodules if enabled.")

    # parser.add_argument("-q", "--quiet",    action = 'store_true',
    #                     help = "Quiet output. Suppresses INFO messages, but errors and warnings will still be printed out.")
    # parser.add_argument("-v", "--verbose",  action = 'store_true',
    #                     help = "Detailed output. Use it to debug.")

    args = parser.parse_args()

    settings = configparser.ConfigParser()
    settings.optionxform = str
    settings.read(args.config)

    cases = list()
    if args.debug == True:
        cases.append(dict())
        settings['SPI']['mode'] = "defects4j" # override mode
        settings['SPI']['project'] = "Closure"
        cases[-1]['project_name'] = 'Closure-14'
        cases[-1]['identifier'], cases[-1]['bug_id'] = cases[-1]['project_name'].split('-')
    else:
        if settings['SPI']['mode'] == 'defects4j':
            print(f"debug : mode defects4j")
            cases.append(dict())
            cases[-1]['identifier'] = settings['SPI']['project']
            cases[-1]['bug_id'] = settings['SPI']['identifier']
            cases[-1]['project_name'] = f"{cases[-1]['identifier']}-{cases[-1]['bug_id']}"
            # cases[-1]['identifier'], cases[-1]['bug_id'] = cases[-1]['project_name'].split('-')

        elif settings['SPI']['mode'] in ('defects4j-batch', 'defects4j-batch-expr'):
            print(f"debug : mode defects4j-batch")
            with open(settings['SPI']['batch_d4j_file'], 'r') as infile:
                for bug in infile.read().splitlines():
                    cases.append(dict())
                    cases[-1]['project_name'] = bug
                    cases[-1]['project'] = bug
                    identifier, bug_id = bug.split('-')
                    cases[-1]['identifier'] = identifier
                    cases[-1]['bug_id'] = bug_id
                

        elif settings['SPI']['mode'] == 'github':
            # Enhance reading options here
            cases.append(dict())
            cases[-1]['repository'] = settings['SPI']['repository_url']
            cases[-1]['project_name'] = cases[-1]['repository'].rsplit('/', 1)[-1]
            cases[-1]['identifier'] = cases[-1]['project_name']

    # settings['verbose'] = args.verbose
    # settings['quiet'] = False if args.verbose else args.quiet # suppresses quiet option if verbose option is given
    settings['SPI']['rebuild'] = str(args.rebuild)

    return (cases, settings)

#######
# Misc
#######

def rebuild(module_name : str, SPI_root) -> bool:
    #print(f"> Rebuilding {module_name}...")
    try:
        assert subprocess.run(("gradle", "distZip", "-q"), cwd = f"./core/{module_name}")
        #assert subprocess.run(("mv", "app/build/distributions/app.zip", "../../pkg/"), cwd = f"./core/{module_name}", shell=True)
        #print(f"> moving app.zip to pkg...")
        if not move(f"./core/{module_name}/app/build/distributions/app.zip", f"{SPI_root}/pkg/", copy_function = shutil.copy):
            print(f"| SPI  | ! Error occurred while moving app.zip to pkg.")
            return False
        #print(f"> unzipping {module_name}...")
        #assert subprocess.run(("unzip", "-q", "app.zip"), cwd = "./pkg", shell=True)
        if not unzip("./pkg/app.zip", "./pkg/"):
            print(f"| SPI  | ! Error occurred while unzipping {module_name}.")
            return False
        #print(f"> creating {module_name} directory...")
        if not os.path.exists(f"./pkg/{module_name}"):
            assert subprocess.run(("mkdir", f"{module_name}"), cwd = "./pkg")
        #assert subprocess.run(("mv", "app", module_name), cwd = "./pkg", shell=True)
        #print(f"> moving app to {module_name}...")
        if not move("./pkg/app", f"./pkg/{module_name}", shutil.copy2):
            print(f"| SPI  | ! Error occurred while moving app to {module_name}.")
            return False

        os.chmod(f"./pkg/{module_name}/app/bin/app", 0o774)
        os.chmod(f"./pkg/{module_name}/app/bin/app.bat", 0o774)

        #assert subprocess.run(("rm", "app.zip"), cwd = "./pkg", shell=True)
        #print(f"> removing app.zip...")
        if not remove("./pkg/app.zip"):
            print(f"| SPI  | Error occurred while removing app.zip.")
            return False

    except Exception as e:
        print(f"| SPI  | Error occurred while rebuilding submodule {module_name}.")
        print(f"| SPI  | Error : {e}")
        return False
    #print(f"> Done.")
    return True

def rebuild_confix(SPI_root : str, JDK8_HOME : str) -> bool:
    try:
        assert subprocess.run(("mvn", "clean", "package", "-q"), cwd = "./core/confix/ConFix-code")
        #assert subprocess.run(("cp", "target/confix-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "../lib/confix-ami_torun.jar"), cwd = "./core/confix/ConFix-code", shell=True)
        if not (copy(f"{SPI_root}/core/confix/ConFix-code/target/confix-0.0.1-SNAPSHOT-jar-with-dependencies.jar", f"{SPI_root}/core/confix/lib/confix-ami_torun.jar")):
            print("Error occurred while copying ConFix jar file.")
            return False

    except AssertionError as e:
        print("| SPI  | ! Error occurred while rebuilding ConFix.")
        print("| SPI  | ! Error : ", e)
        return False

    return True

def move(location, destination, copy_function) -> bool:
    try:
        shutil.move(location, destination, copy_function)
    except Exception as e:
        print(f"| SPI  | ! Error: {location} : {e.strerror}")
        print(f"| SPI  | ! Error occurred while moving {location} to {destination}.")
        return False
    return True

def unzip(file, destination):
    try:
        with zipfile.ZipFile(file, 'r') as zip_ref:
            zip_ref.extractall(destination)
    except Exception as e:
        print(f"| SPI  | ! Error: {file} : {e.strerror}")
        print(f"| SPI  | ! Error occurred while unzipping {file}.")
        return False
    return True

def copy(file, destination):
    try:
        shutil.copy(file, destination)
    except Exception as e:
        print(f"| SPI  | ! Error: {file} : {e.strerror}")
        print(f"| SPI  | ! Error occurred while copying {file} to {destination}.")
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
        print(f"| SPI  | ! Error: {path} : {e.strerror}")
        print(f"| SPI  | ! Error occurred while removing {path}.")
        return False
    return True

def rebuild_all(SPI_root : str, JDK8_HOME : str):
    try:
        #assert subprocess.run(("rm", "-rf", "pkg"))
        if not (remove(f"{SPI_root}/pkg")):
            print(f"| SPI  | ! Error occurred while removing {SPI_root}/pkg.")
        assert subprocess.run(("mkdir", "pkg"))
        #os.mkdir(f"{SPI_root}/pkg")
        # for submodule in ("BuggyChangeCollector", "AllChangeCollector", "LCE"):
        for submodule in ("ChangeCollector", "LCE"):
            assert rebuild(submodule, SPI_root)
            print(f"| SPI  | Successfully rebuilt submodule {submodule}.")

        assert rebuild_confix(SPI_root, JDK8_HOME) # ConFix uses maven unlike any other packages; this should be handled differently.
        print(f"| SPI  | Successfully rebuilt submodule ConFix.")
        print()

    except AssertionError as e:
        print("| SPI  | ! Error occurred while rebuilding modules.")
        print()
        return False
    except Exception as e:
        print(f"| SPI  | ! Error occurred while rebuilding modules: {e}")
        print()
        return False

    print("All submodules have been successfully rebuilt.")
    print()
    return True


#######
# Module launcher
#######

def run_CC(case : dict, is_defects4j : bool, config_SPI: configparser.SectionProxy, config : configparser.SectionProxy) -> bool:
    try:
        # copy .properties file
        # run ACC
        prop_CC = jproperties.Properties()
        for key in config.keys():
            prop_CC[key] = config[key]
        # Explicitly tell 'target'
        prop_CC['hash_id'] = case['hash_id']
        prop_CC['mode'] = 'defects4j' if is_defects4j else 'repository'
        if is_defects4j == True:
            prop_CC['defects4j_name'] = case['identifier']
            prop_CC['defects4j_id'] = case['bug_id']
        else:
            if prop_CC['mode'] == 'file':
                pass

            prop_CC['git_url'] = config_SPI['repository_url']
            prop_CC['git_name'] = config_SPI['project']
            prop_CC['file_name'] = config_SPI['faulty_file']
            prop_CC['commit_id'] = config_SPI['commit_id']

            pass

        with open(f"{case['target_dir']}/properties/CC.properties", "wb") as f:
            prop_CC.store(f, encoding = "UTF-8")

        # assert subprocess.run(["app", "-l", f"{case['target_dir']}/collection.txt"], cwd = "./pkg/AllChangeCollector/bin")
        assert subprocess.run(["./app", f"{case['target_dir']}/properties/CC.properties"], cwd = "./pkg/ChangeCollector/app/bin")
    except Exception as e:
        print(e)
        return False
    return True

def run_LCE(case : dict, is_defects4j : bool, config : configparser.SectionProxy) -> bool:
    try:
        prop_LCE = jproperties.Properties()
        for key in config.keys():
            prop_LCE[key] = config[key]

        prop_LCE['pool_file.dir'] = f"{prop_LCE['SPI.dir'].data}/components/LCE/gumtree_vector.csv"
        prop_LCE['meta_pool_file.dir'] = f"{prop_LCE['SPI.dir'].data}/components/LCE/commit_file.csv"
        prop_LCE['target_vector.dir'] = f"{case['target_dir']}/outputs/ChangeCollector/{case['identifier']}_gumtree_vector.csv"
        prop_LCE['pool.dir'] = f"{case['target_dir']}/outputs/LCE/result/"
        prop_LCE['candidates.dir'] = f"{case['target_dir']}/outputs/LCE/candidates/"

        if is_defects4j == True:
            prop_LCE['d4j_project_name'] = case['identifier']
            prop_LCE['d4j_project_num'] = case['bug_id']
        
        with open(f"{case['target_dir']}/properties/LCE.properties", "wb") as f:
            prop_LCE.store(f, encoding = "UTF-8")

        os.makedirs(prop_LCE['pool.dir'].data)
        os.makedirs(prop_LCE['candidates.dir'].data)

        assert subprocess.run(["./app", f"{case['target_dir']}/properties/LCE.properties"], cwd = "./pkg/LCE/app/bin")
    except Exception as e:
        print(e)
        return False
    return True

def run_ConFix(case : dict, is_defects4j : bool, config_runner : configparser.SectionProxy, config_ConFix : configparser.SectionProxy) -> bool:
    try:
        ini_runner = configparser.ConfigParser()
        ini_runner.optionxform = str
        ini_runner.add_section('Project')
        ini_runner['Project'] = config_runner
        with open(f"{case['target_dir']}/properties/confix_runner.ini", "w") as f:
            ini_runner.write(f)

        prop_ConFix = jproperties.Properties()
        for key in config_ConFix.keys():
            prop_ConFix[key] = config_ConFix[key]
        with open(f"{case['target_dir']}/properties/confix.properties", "wb") as f:
            prop_ConFix.store(f, encoding = "UTF-8")

        jdk8_env = os.environ.copy()
        jdk8_env['JAVA_HOME'] = config_runner['JAVA_HOME_8']

        if is_defects4j == True:
            assert subprocess.run(["python3", "./core/confix/run_confix.py", "-d", "true", "-h", case['hash_id'], "-f", f"{case['target_dir']}/properties/confix_runner.ini"], env = jdk8_env)
        else:
            assert subprocess.run(["python3", "./core/confix/run_confix.py", "-h", case['hash_id'], "-f", f"{case['target_dir']}/properties/confix_runner.ini"], env = jdk8_env)

        
    except Exception as e:
        print(e)
        traceback.print_exc()
        return False
    return True

#######
# Main
#######

def main(argv):
    cases, settings = parse_argv()

    is_defects4j = settings['SPI']['mode'] in ('defects4j', 'defects4j-batch', 'defects4j-batch-expr')
    is_rebuild_required = (settings['SPI']['rebuild'] == 'True')

    # Should be the root of SPI repository
    SPI_root = os.getcwd()

    if is_rebuild_required:
        print("Have been requested to rebuild all submodules. Commencing...")
        if rebuild_all(SPI_root, settings['SPI']['JAVA_HOME_8']):
            print("All submodules have been successfully rebuilt.")

    if settings['SPI']["mode"] is None:
        print("You have not told me what to fix. Exiting program.")
        sys.exit(0)

    
    time_hash = str(abs(hash(f"{dt.datetime.now().strftime('%Y%m%d%H%M%S')}")))[-6:]

    SPI_launch_result_str = str()

    patch_strategies = ("flfreq", "tested-first", "noctx", "patch")
    concretization_strategies = ("tcvfl", "hash-match", "neighbor", "tc")

    log_file = str()

    if settings['SPI']['mode'] != 'defects4j-batch-expr':
        patch_strategies = (settings['ConFix']['patch.strategy'], )
        concretization_strategies = (settings['ConFix']['concretize.strategy'], )

    for patch_strategy in patch_strategies:
        for concretization_strategy in concretization_strategies:
            settings['ConFix']['patch.strategy'] = patch_strategy
            settings['ConFix']['concretize.strategy'] = concretization_strategy
            print(f"Patch Strategy: {patch_strategy}, Concretization Strategy: {concretization_strategy}")

            # Initializations before whole case-loop
            failed = list()
            succeeded = list()


            hash_part = str()
            if 'batch' in settings['SPI']['mode']:
                if 'expr' in settings['SPI']['mode']:
                    hash_part = f"batch_{time_hash}_{patch_strategy}+{concretization_strategy}"
                else:
                    hash_part = f"batch_{time_hash}"
            else:
                hash_part = f"{time_hash}"
            
            ###

            whole_start = dt.datetime.now()

            for case_num, case in enumerate(cases, 1):
                # case['hash_id'] = f"{hash_prefix}_{case['project_name']}"
                case['hash_id'] = f"{hash_part}_{case['project_name']}"
                print(f"| SPI  | Hash ID generated as {case['hash_id']}:")
                if 'batch' in settings['SPI']['mode']:
                    log_file = f"log_{hash_part}.txt"
                else:
                    log_file = f"log_{hash_part}_{case['project_name']}.txt"
                #case['target_dir'] = f"{SPI_root}/target/{case['hash_id']}"
                case['target_dir'] = f"/data/codemodel/turbstructor/SPI_batch_byproducts_221111/{case['hash_id']}"
                os.makedirs(case['target_dir'])

                each_start = dt.datetime.now()

                with open(f"logs/{log_file}", "a") as outfile:
                    outfile.write(f"Launching SPI upon case #{case_num} {case['project_name']}... Started at {each_start.strftime('%Y-%m-%d %H:%M:%S')}.\n")


                if is_defects4j == True:
                    settings['SPI']['project'] = case['identifier']
                    settings['SPI']['identifier'] = case['bug_id']
                    with open(f"{SPI_root}/components/commit_collector/Defects4J_bugs_info/{case['identifier']}.csv", 'r', newline = '') as d4j_meta_file:
                        reader = csv.DictReader(d4j_meta_file)
                        for row in reader:
                            if int(row['Defects4J ID']) == int(case['bug_id']):
                                settings['SPI']['faulty_file'] = row['Faulty file path']
                                settings['SPI']['faulty_line_fix'] = row['fix faulty line']
                                settings['SPI']['faulty_line_blame'] = row['blame faulty line']
                                break

                try:
                    assert subprocess.run(("mkdir", f"{case['target_dir']}/properties"))

                    print(f"\n| SPI  | Case #{case_num} / Step 1. Running Commit Collector...")
                    assert run_CC(case, is_defects4j, settings['SPI'], settings['CC']) is True, "'Commit Collector' Module launch failed"

                    print(f"\n| SPI  | Case #{case_num} / Step 2. Running Longest Common subvector Extractor...")
                    assert run_LCE(case, is_defects4j, settings['LCE']) is True, "'Longest Common subvector Extractor' Module launch failed"

                    print(f"\n| SPI  | Case #{case_num} / Step 3. Running ConFix...")
                    assert run_ConFix(case, is_defects4j, settings['SPI'], settings['ConFix']) is True, "'ConFix' Module launch failed"

                    # Check for patch existence
                    if not os.path.isfile(f"{case['target_dir']}/diff_file.txt"):
                        print(f"| SPI  | ! SPI launch upon Case #{case_num} {case['project_name']} failed to find a patch.")
                        failed.append(case['project_name'])
                        SPI_launch_result_str = "failure"
                    else:
                        print(f"| SPI  | SPI launch upon Case #{case_num} {case['project_name']} found a patch!")
                        succeeded.append(case['project_name'])
                        SPI_launch_result_str = "success"

                        print("\n=== diff_file.txt start ===")
                        with open(f"{case['target_dir']}/diff_file.txt", "r") as f:
                            content = f.read()
                            print(content)
                        print("=== diff_file.txt ended ===")
                except AssertionError as e:
                    print(f"| SPI  | ! Lauanch upon Case #{case_num} {case['project_name']} failed during progresses.")
                    print(f"| SPI  | ! Failed cause: {e}")
                    failed.append(case['project_name'])
                    SPI_launch_result_str = "failure"
                finally:
                    each_end = dt.datetime.now()
                    each_elapsed_time = (each_end - each_start)

                    print(f"| SPI  | Elapsed Time for Case #{case_num} {case['project_name']} : {each_elapsed_time}")

                    with open(f"logs/{log_file}", "a") as outfile:
                        outfile.write(f"Launching SPI upon Case #{case_num} {case['project_name']}... ended at {each_end.strftime('%Y-%m-%d %H:%M:%S')}, with {SPI_launch_result_str}.\n")
                        outfile.write(f"Launching SPI upon Case #{case_num} {case['project_name']} took {each_elapsed_time}.\n")


            whole_end = dt.datetime.now()
            whole_elapsed_time = (whole_end - whole_start)

            if 'batch' in settings['SPI']['mode']:
                with open(f"logs/{log_file}", "a") as outfile:
                    outfile.write(f"Whole batch took {whole_elapsed_time}.\n")
                    outfile.write(f"Whole batch resulted in {len(succeeded)} success(es) and {len(failed)} failure(s).\n")
                    outfile.write("Succeeded case(s):\n")
                    for each in succeeded:
                        outfile.write(f"- {each}\n")
                    outfile.write("\n")
                    outfile.write("Failed case(s):\n")
                    for each in failed:
                        outfile.write(f"- {each}\n")

            print(f"| SPI  | Total Elapsed Time : {whole_elapsed_time}")
            print(f"| SPI  | {len(succeeded)} succeeded, {len(failed)} failed.")

    


if __name__ == '__main__':
    main(sys.argv)
