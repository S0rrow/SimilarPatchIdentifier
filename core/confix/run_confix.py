import logging
import os  # nope
import sys
import pandas as pd
import getopt
import argparse
import subprocess
import configparser
import platform

def copy(source, destination):
    try:
        try:
            subprocess.run(["cp", source, destination])
        except Exception as e:
            subprocess.run(["copy", source, destination])
    except Exception as e:
        print(f"> Error: {file} : {e.strerror}")
        print(f"> ! Error occurred while copying {file} to {destination}.")
        return False
    return True

def main(argv):
    try:
        opts, args = getopt.getopt(argv[1:], "d:i:h:f:", ["defects4J", "input", "hash"])
    except getopt.GetoptError as err:
        print(err)
        sys.exit(2)
    is_defects4j = False
    hash_id = ""
    project_info_file = ""
    for o, a in opts:
        if o in ("-d", "--defects4J"):
            is_defects4j = True
        elif o in ("-h", "--hash"):
            hash_id = a
        elif o in ("-f", "--file"): # Required.
            project_info_file = a
        else:
            assert False, "unhandled option"

    

    
    project_information = configparser.ConfigParser()
    project_information.optionxform = str
    project_information.read(project_info_file)

    # root should be that of SPI
    SPI_root = os.getcwd()

    target_project_name = project_information['Project']['project']
    target_id = project_information['Project']['identifier']
    perfect_faulty_path = project_information['Project']['faulty_file']
    perfect_faulty_line = project_information['Project']['faulty_line_fix']

    perfect_faulty_class, _ = perfect_faulty_path.split(".")
    perfect_faulty_class = perfect_faulty_class.replace("/", ".")

    target_root = f"{SPI_root}/target/{hash_id}" # target_dir > target_root
    target_workspace = f"{target_root}/{target_project_name}" # (new) target_dir > target_workspace
    target_outputs = f"{target_root}/outputs" # output_dir > target_outputs

    # currently, we are running confix in APR directory


    ## prepare setup before running confix

    ### for D4J projects
    if is_defects4j == True:
        assert copy(f"{SPI_root}/core/confix/coverages/{target_project_name.lower()}/{target_project_name.lower()}{target_id}b/coverage-info.obj", f"{target_workspace}/")

        copy(f"{target_root}/properties/confix.properties", f"{target_workspace}/")
        with open(f"{target_workspace}/confix.properties", "a") as confix_prop_file:
            for d4j_prop, confix_prop in (("dir.src.classes", "src.dir"), ("dir.bin.classes", "target.dir"), ("dir.src.tests", "test.dir"), ("cp.compile", "cp.compile"), ("cp.test", "cp.test")):
                filename = f"prop_{d4j_prop}.txt"
                subprocess.run(["defects4j", "export", "-p", d4j_prop, "-o", filename], cwd = target_workspace)
                with open(f"{target_workspace}/{filename}", "r") as f:
                    file_content = f.read()
                    confix_prop_file.write(f"{confix_prop}={file_content}\n")

            confix_prop_file.write(f"projectName={target_project_name}\n")
            confix_prop_file.write(f"bugId={target_id}\n")
            confix_prop_file.write(f"pFaultyClass={perfect_faulty_class}\n")
            confix_prop_file.write(f"pFaultyLine={perfect_faulty_line}\n")

        for test_list in ("tests.all", "tests.relevant", "tests.trigger"):
            outfile = f"{target_workspace}/{test_list}"
            subprocess.run(["defects4j", "export", "-p", test_list, "-o", outfile], cwd = target_workspace)

        with open(f"{target_workspace}/confix.properties", "a") as f:
            f.write(f"pool.source={target_outputs}/LCE/candidates\n")
        

    ### for non-D4J projects
    else:
        sourcePath = project_information['Project']['source_path']
        targetPath = project_information['Project']['target_path']
        testList = project_information['Project']['test_list']
        testClassPath = project_information['Project']['test_class_path']
        compileClassPath = project_information['Project']['compile_class_path']
        buildTool = project_information['Project']['build_tool']

        assert copy(f"{SPI_root}/core/confix/coverages/math/math1b/coverage-info.obj", target_workspace)


        prop_file = f"{target_root}/properties/confix.properties"
        assert copy(prop_file, f"{target_workspace}/")
        
        ### fill up the confix.property
        with open(f"{target_workspace}/confix.properties", "a") as f:
            f.write(f"src.dir={project_information['Project']['source_path']}\n")
            f.write(f"target.dir={project_information['Project']['target_path']}\n")
            f.write(f"cp.compile={project_information['Project']['compile_class_path']}\n")
            f.write(f"cp.test={project_information['Project']['test_class_path']}\n")
            f.write(f"projectName={target_project_name}\n")
            f.write(f"pFaultyClass={perfect_faulty_class}\n")
            f.write(f"pFaultyLine={perfect_faulty_line}\n")
            f.write(f"pool.source={target_outputs}/LCE/candidates\n")
        with open(f"{target_workspace}/tests.all", "w") as f:
            f.write(project_information['Project']['test_list'])
        with open(f"{target_workspace}/tests.relevant", "w") as f:
            f.write(project_information['Project']['test_list'])
        with open(f"{target_workspace}/tests.trigger", "w") as f:
            f.write(project_information['Project']['test_list'])

        if buildTool in ("gradle", "Gradle"):
            assert subprocess.run(["gradle", "build"], cwd = target_workspace)
        elif buildTool in ("maven", "Maven", "mvn"):
            assert subprocess.run(["mvn", "compile"], cwd = target_workspace)
    print("Pre-configuration finished.")

    print("Building ConFix...")
    assert subprocess.run(["mvn", "clean", "package"], cwd = f"{SPI_root}/core/confix/ConFix-code")

    print("Executing ConFix...")
    assert copy(f"{SPI_root}/core/confix/ConFix-code/target/confix-0.0.1-SNAPSHOT-jar-with-dependencies.jar", f"{SPI_root}/core/confix/lib/confix-ami_torun.jar")

    with open(f"{target_workspace}/log.txt", "w") as f:
        assert subprocess.run(["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-Xmx4g", "-cp", "../../../core/confix/lib/las.jar:../../../core/confix/lib/confix-ami_torun.jar", "-Duser.language=en", "-Duser.timezone=America/Los_Angeles", "com.github.thwak.confix.main.ConFix"], cwd = target_workspace, stdout = f)
    print("ConFix Execution Finished.")


    if not os.path.isfile(f"{target_workspace}/patches/0/{perfect_faulty_path}"):
        print("ConFix failed to generate plausible patch.")
        sys.exit(-63)

    else:
        with open(f"{target_root}/diff_file.txt", "w") as f:
            f.write(f"differences made in {perfect_faulty_path}:\n")

        with open(f"{target_root}/diff_file.txt", "a") as f:
            subprocess.run(["git", "diff", f"{target_workspace}/{perfect_faulty_path}", f"{target_workspace}/patches/0/{perfect_faulty_path}"], cwd = os.path.expanduser('~'), stdout = f)

    # # 패치의 path
    # /home/aprweb/APR_Projects/APR/target/Math/patches/0/org/apache/commons/math/stat/Frequency.java

    # # 주어지는 path
    # src/main/java/org/apache/commons/math/stat/Frequency.java

if __name__ == '__main__':
    main(sys.argv)
