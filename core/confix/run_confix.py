import logging
import os  # nope
import sys
import shutil
import getopt
import argparse
import subprocess
import configparser
import platform
import traceback

def copy(source, destination):
    try:
        shutil.copy(source, destination)
    except Exception as e:
        print(f"> Error: {source} : {e.strerror}")
        print(f"> ! Error occurred while copying {source} to {destination}.")
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

    

    
    project_info = configparser.ConfigParser()
    project_info.optionxform = str
    project_info.read(project_info_file)

    # root should be that of SPI
    SPI_root = project_info['Project']['root']

    target_project_name = project_info['Project']['identifier']
    target_id = project_info['Project']['version']
    perfect_faulty_path = project_info['Project']['faulty_file']
    perfect_faulty_line = project_info['Project']['faulty_line_fix']

    perfect_faulty_class, _ = perfect_faulty_path.split(".")
    perfect_faulty_class = perfect_faulty_class.replace("/", ".")

    target_root = os.path.join(project_info['Project']['byproduct_path'], hash_id)  # target_dir > target_root // Urgent fix! Should be fixed correctly later.
    target_workspace = os.path.join(target_root, target_project_name) # (new) target_dir > target_workspace
    target_outputs = os.path.join(target_root, "outputs") # output_dir > target_outputs

    ## prepare setup before running confix

    ### for D4J projects
    if is_defects4j == True:
        identifier = project_info['Project']['identifier']
        version = project_info['Project']['version']

        try:
            subprocess.run(["defects4j", "checkout", "-p", identifier, "-v", f"{version}b", "-w", target_workspace], cwd = target_root, check = True)

            if not copy(os.path.join(SPI_root, "core", "confix", "coverages", target_project_name.lower(), f"{target_project_name.lower()}{target_id}b", "coverage-info.obj"), target_workspace):
                raise RuntimeError("Failed to copy coverage info.")
            if not copy(os.path.join(target_root, "properties", "confix.properties"), target_workspace):
                raise RuntimeError("Failed to bring confix.properties to workspace directory.")

            with open(os.path.join(target_workspace, "confix.properties"), "a") as confix_prop_file:
                for d4j_prop, confix_prop in (("dir.src.classes", "src.dir"), ("dir.bin.classes", "target.dir"), ("dir.src.tests", "test.dir"), ("cp.compile", "cp.compile"), ("cp.test", "cp.test")):
                    filename = os.path.join(target_workspace, f"prop_{d4j_prop}.txt")
                    subprocess.run(["defects4j", "export", "-p", d4j_prop, "-o", filename], cwd = target_workspace, check = True)
                    with open(os.path.join(target_workspace, filename), "r") as f:
                        file_content = f.read()
                        confix_prop_file.write(f"{confix_prop}={file_content}\n")

                confix_prop_file.write(f"projectName={target_project_name}\n")
                confix_prop_file.write(f"bugId={target_id}\n")
                confix_prop_file.write(f"pFaultyClass={perfect_faulty_class}\n")
                confix_prop_file.write(f"pFaultyLine={perfect_faulty_line}\n")

            for test_list in ("tests.all", "tests.relevant", "tests.trigger"):
                outfile = os.path.join(target_workspace, test_list)
                subprocess.run(["defects4j", "export", "-p", test_list, "-o", outfile], cwd = target_workspace, check = True)

            with open(os.path.join(target_workspace, "confix.properties"), "a") as f:
                f.write(f"pool.source={os.path.join(target_outputs, 'LCE', 'candidates')}\n")

        except Exception as e:
            print("| ConFix-runner    | ! Failed to run pre-launch configuration.")
            traceback.print_exc()
            sys.exit(-1)
        else:
            print("| ConFix-runner    | Pre-launch configuration finished.")
        

    ### for non-D4J projects
    else:
        # sourcePath = project_info['Project']['source_path']
        # targetPath = project_info['Project']['target_path']
        # testList = project_info['Project']['test_list']
        # testClassPath = project_info['Project']['test_class_path']
        # compileClassPath = project_info['Project']['compile_class_path']
        # buildTool = project_info['Project']['build_tool']

        try:
            if not copy(os.path.join(SPI_root, "core", "confix", "coverages", "math", "math1b", "coverage-info.obj"), target_workspace):
                raise RuntimeError("Failed to copy coverage info.")

            prop_file = os.path.join(target_root, "properties", "confix.properties")
            if not copy(prop_file, target_workspace):
                raise RuntimeError("Failed to bring confix.properties to workspace directory.")
            
            ### fill up the confix.property
            with open(os.path.join(target_workspace, "confix.properties"), "a") as f:
                f.write(f"src.dir={project_info['Project']['source_path']}\n")
                f.write(f"target.dir={project_info['Project']['target_path']}\n")
                f.write(f"cp.compile={project_info['Project']['compile_class_path']}\n")
                f.write(f"cp.test={project_info['Project']['test_class_path']}\n")
                f.write(f"projectName={target_project_name}\n")
                f.write(f"pFaultyClass={perfect_faulty_class}\n")
                f.write(f"pFaultyLine={perfect_faulty_line}\n")
                f.write(f"pool.source={os.path.join(target_outputs, 'LCE', 'candidates')}\n")
            with open(os.path.join(target_workspace, "tests.all"), "w") as f:
                f.write(project_info['Project']['test_list'])
            with open(os.path.join(target_workspace, "tests.relevant"), "w") as f:
                f.write(project_info['Project']['test_list'])
            with open(os.path.join(target_workspace, "tests.trigger"), "w") as f:
                f.write(project_info['Project']['test_list'])

            if buildTool in ("gradle", "Gradle"):
                subprocess.run(["gradle", "build"], cwd = target_workspace, check = True)
            elif buildTool in ("maven", "Maven", "mvn"):
                subprocess.run(["mvn", "compile"], cwd = target_workspace, check = True)
        except Exception as e:
            print("| ConFix-runner    | ! Failed to run pre-launch configuration.")
            traceback.print_exc()
            sys.exit(-1)
        else:
            print("| ConFix-runner    | Pre-launch configuration finished.")


<<<<<<< HEAD
    # Rebuilding ConFix
    try:
        print("| ConFix-runner    | Building ConFix...")
        subprocess.run(("mvn", "clean", "package", "-q"), cwd = os.path.join(SPI_root, "core", "confix", "ConFix-code"), check = True)
    except Exception as e:
        print("| ConFix-runner    | ! Failed to build ConFix.")
        traceback.print_exc()
        sys.exit(-1)
=======
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
        # assert subprocess.run(["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-Xmx16g", "-cp", "/home/codemodel/turbstructor/SimilarPatchIdentifier/core/confix/lib/las.jar:/home/codemodel/turbstructor/SimilarPatchIdentifier/core/confix/lib/confix-ami_torun.jar", "-Duser.language=en", "-Duser.timezone=America/Los_Angeles", "com.github.thwak.confix.main.ConFix"], cwd = target_workspace, stdout = f)

    print("ConFix Execution Finished.")


    if not os.path.isfile(f"{target_workspace}/patches/0/{perfect_faulty_path}"):
        print("ConFix failed to generate plausible patch.")
        sys.exit(-63)

>>>>>>> d9f4352bd (update on LCE App.java)
    else:
        print("| ConFix-runner    | Successfully built ConFix.")


    # Launching ConFix
    try:
        print("| ConFix-runner    | Launching ConFix...")
        if not copy(os.path.join(SPI_root, "core", "confix", "ConFix-code", "target", "confix-0.0.1-SNAPSHOT-jar-with-dependencies.jar"), os.path.join(SPI_root, "core", "confix", "lib", "confix-ami_torun.jar")):
            raise RuntimeError("Failed to copy ConFix.jar.")

        with open(os.path.join(target_workspace, "log.txt"), "w") as f:
            JDK8_HOME = project_info['Project']['JAVA_HOME_8']
            subprocess.run([os.path.join(JDK8_HOME, "bin", "java"), "-Xmx4g", "-cp", f"{os.path.join(SPI_root, 'core', 'confix', 'lib', 'las.jar')}:{os.path.join(SPI_root, 'core', 'confix', 'lib', 'confix-ami_torun.jar')}", "-Duser.language=en", "-Duser.timezone=America/Los_Angeles", "com.github.thwak.confix.main.ConFix"], cwd = target_workspace, stdout = f, check = True)
    except Exception as e:
        print("| ConFix-runner    | ! ConFix Launch failed.")
    else:
        print("| ConFix-runner    | ConFix Launch finished.")


    if os.path.isfile(os.path.join(target_workspace, "patches", "0", perfect_faulty_path)):
        with open(os.path.join(target_root, "diff_file.txt"), "w") as f:
            f.write(f"differences made in {perfect_faulty_path}:\n")

        # ! `git diff` might not be printed approopriatedly if `cwd` is not correctly set. Need more research about this later.
        with open(os.path.join(target_root, "diff_file.txt"), "a") as f:
            subprocess.run(["git", "diff", os.path.join(target_workspace, perfect_faulty_path), os.path.join(target_workspace, "patches", "0", perfect_faulty_path)], cwd = os.path.expanduser('~'), stdout = f)
    else:
        print("ConFix failed to generate plausible patch.")

    # # 패치의 path
    # /home/aprweb/APR_Projects/APR/target/Math/patches/0/org/apache/commons/math/stat/Frequency.java

    # # 주어지는 path
    # src/main/java/org/apache/commons/math/stat/Frequency.java

if __name__ == '__main__':
    main(sys.argv)
