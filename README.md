# SimilarPatchIdentifier (SPI) 2022

## What is SPI?
Inspired by _**Automated Patch Generation with Context-based Change Application**_, SPI is a revision of ConFix which uses change pool made out of patches from recommended projects, each of which is considered to have a similar bug as the target project.

---

## Submodules (Projects included)
**SPI = CC + LCE + ConFix**
- [ConFix](https://github.com/thwak/confix) (Original Repository of ConFix)
- [ChangeCollector (CC)](https://github.com/S0rrow/ChangeCollector) (Initial verion: Integrated into ChangeCollector ~~[AllChangeCollector] (https://github.com/JeongHyunHeo/AllChangeCollector)~~ )
- [Longest Common sub-vector Extractor (LCE)](https://github.com/S0rrow/LCE)

---

## How to run

### Requirements
- JDK**s**
    - Oracle JDK 17 (Used to compile `ChangeCollector` and `Longest Common sub-vector Extractor`)
    - Oracle JDK 8 (Used by Defects4J)
- [Defects4J](https://github.com/rjust/defects4j)
    - See [requirements](https://github.com/rjust/defects4j#requirements)
- Python
    - Python 3.6+
    - PIP packages necessary
        - jproperties

### Pre-run Configuration
#### `preconfig.sh`
- Provided `preconfig.sh` will run all needed installation process before launch.
#### Manual Configuration
- Set up Defects4J with reference to [Steps to set up Defects4J](https://github.com/rjust/defects4j#requirements)
- Install SimilarPatchIdentifier
    > `git clone https://github.com/ISEL-HGU/SimilarPatchIdentifier`
- Change working directory to `SimilarPatchIdentifier`
    > `cd SimilarPatchIdentifier`
- Edit `SPI.ini` file
    - See [SPI.ini settings](#spiini-settings)
- Launch the SimilarPatchIdentifier according to scenario you want to apply.
    - To generate `change vector pool`, See [Generate the Change Vector Pool](#generate-the-change-vector-pool)
    - To find a patch for buggy file from git repository, See [How to find Patch for Git Repository](#how-to-find-patch-for-git-repository)
    - To find a patch for buggy project from `Defects4J Framework`, See [How to find Patch for Defects4J](#how-to-find-patch-for-defects4j)
    - To find multiple patches for buggy projects from `Defects4J Framework`, See [How to launch using batch](#how-to-launch-using-batch)

#### `SPI.ini` settings
##### **SPI**
|key|is_required|description|
|:---|:---:|:---|
|`mode`|Yes|How `SPI` will be run. Can choose among those options:<br>- `defects4j` : Tells `SPI` to try finding a patch out of a `Defects4J` bug.<br>- `defects4j-batch` : Tells `SPI` to try finding a patch out of a `Defects4J` bug, but with a number of bugs given as a list.<br>- `github` : *Currently not fully implemented.* Tells `SPI` to try finding a patch out of a `GitHub` project with a bug.|
|`batch_d4j_file`|In mode `defects4j-batch`|Name of the file which contains names of Defects4J bugs|
|`identifier`|In mode `defects4j`|Alias to the name of the project.<br>*Automatically set when running `SPI` in mode `defects4j-batch`*|
|`version`|In mode `defects4j`|Bug ID of Defects4J bug.|
|`repository_url`|In mode `github`|URL of GitHub project to look for a patch upon|
|`commit_id`|In mode `github`|Commit ID of GitHub project that produces a bug|
|`source_path`|In mode `github`|Source directory path (relative path from project root) for parsing buggy codes|
|`target_path`|In mode `github`|Relative path (from project root) for compiled .class files|
|`test_list`|In mode `github`|List of names of test classes that a project uses|
|`test_class_path`|In mode `github`|Classpath for test execution. Colon(`:`)-separated.|
|`compile_class_path`|In mode `github`|Classpath for candidate compilation. Colon(`:`)-separated.|
|`build_tool`|In mode `github`|How a project is built. Only tools `maven` and `gradle` are possible for option|
|`faulty_file`|In mode `github`|Relative directory (from root of project) of a faulty file. *Automatically set when running `SPI` in mode `defects4j` / `defects4j-batch`*|
|`faulty_line_fix`|In mode `github`|Line number of `faulty_file` to try modifying. *Automatically set when running `SPI` in mode `defects4j` / `defects4j-batch`*|
|`faulty_line_blame`|In mode `github`|Line number of `faulty file` where the bug is made. *Automatically set when running `SPI` in mode `defects4j` / `defects4j-batch`*|
|`JAVA_HOME_8`|**Yes**|Absolute path to JDK 8|
|`byproduct_path`|No|Directory which files and folders made during the progress of `SPI` should be stored into. *Will make folder `byproducts` inside `root` by default.*|
|`root`|No|Directory where `SPI` root directory is placed.|
|`patch_strategy`|No|List of patch strategies (among `flfreq`, `tested-first`, `noctx`, `patch`) to run `SPI` with. Comma-separated. *`flfreq` by default.*|
|`concretization_strategy`|No|List of concretization strategies (among `tcvfl`, `hash-match`, `neightbor`, `tc`) to run `SPI` with. Comma-separated. *`hash-match` by default.*|

##### **Change Collector**
|**key**|**is_required**|**description**|
|:---|:---:|:---|
|`project_root`|no|automatically follow `root` from SPI part|
|`output_dir`|no|automatically follow `byproduct_path` from SPI part|
|`mode`|no|automatically follow `mode` from SPI part|
|`file_name`|no|automatically follow `target_path` from SPI part|
|`commit_id`|no|automatically follow `commit_id` from SPI part|
|`git_url`|no|automatically follow `repository_url` from SPI part|
|`git_name`|no|unnecessary if `git_url` is given|
|`doClean`|no|whether to clean the output directory before recurrent execution with identical output directory|
|`JAVA_HOME.8`|no|automatically follow `JAVA_HOME_8` from SPI part|
|`defects4j_name`|no|automatically follow `identifier` from SPI part|
|`defects4j_id`|no|automatically follow `version` from SPI part|
|`hash_id`|no|automatically given by `launcher.py`|
|`set_file_path`|no|only needed for generating Change Vector Pool. defines the list of BIC commid id, BFC commit id, path for BIC file, path for BFC file, Git url, and JIRA key.|


##### **LCE**
|**key**|**is_required**|**description**|
|:---|:---:|:---|
|`SPI.dir`|no|automatically follow `root` from SPI part|
|`pool_file.dir`|no|automatically given by `launcher.py`|
|`meta_pool_file.dir`|no|automatically given by `launcher.py`|
|`target_vector.dir`|no|automatically given by `launcher.py`|
|`pool.dir`|no|automatically given by `launcher.py`|
|`candidates.dir`|no|automatically given by `launcher.py`|
|`candidate_number`|**yes**|select the number of candidate source codes. default is 10|
|`d4j_project_name`|no|automatically follow `identifier` from SPI part|
|`d4j_project_num`|no|automatically follow `version` from SPI part|
|`doClean`|no|whether to clean the output directory before recurrent execution with identical output directory|


##### **ConFix**
|**key**|**is_required**|**description**|
|:---|:---:|:---|
|`jvm`|no|automatically follow *JAVA_HOME_8* from SPI part|
|`version`|no|automatically given by `launcher.py`|
|`pool.path`|no|automatically given by `launcher.py`|
|`cp.lib`|no|automatically given by `launcher.py`|
|`patch.count`|yes|define the patch generation trial count. default is 200000|
|`max.change.count`|yes|define the threshold of number of changes to use as patch material|
|`max.trials`|yes|define the threshold of patch generation trial|
|`time.budget`|yes|define the time limit of ConFix execution|
|`patch.strategy`|no|automatically given by `launcher.py`|
|`concretize.strategy`|no|automatically given by `launcher.py`|
|`fl.metric`|yes|define how Fault Localization is done. default is perfect. only required for ConFix|


### Generate the Change Vector Pool
- `Change Collector` is a submodule of `SPI` which is responsible for generating the change vector pool.
    - `Change Collector` is a Java project which is built with Gradle.
- To execute the `ChangeCollector` module, you need to have JDK 17 installed.
- To launch `ChangeCollector` to generate the change vector pool, input values descripted below in target properties file.
    - ChangeCollector itself does not use `SPI.ini` file. Instead, it uses `.properties` file for input.
- The path of `.properties` file can be given by argument. If not, `ChangeCollector` will look for `cc.properties` under `{Path to SimilarPatchIdentifier}/core/ChangeCollector` directory.
- The `.properties` file should contain the following values to generate pool.
    - `project_root` : Absolute path to the root directory of the project.
    - `output_dir` : Absolute path to the directory where the output files should be stored.
    - `mode` : For Change Vector Pool generation, the mode should be `poolminer`.
    - `JAVA_HOME.8` : Absolute path to JDK 8.
    - `set_file_path` : Absolute path to the file which contains the list of metadata; {BIC commit ID, BFC commit ID, path for BIC file, path for BFC file, Git URL, JIRA key}.
- Execute the following command to launch `ChangeCollector` to generate the change vector pool.
   - If you want to execute `ChangeCollector` with designated properties file, provide path as additional argument.
> `cd ./core/ChangeCollector {Path of .properties file}`<br>
> `gradle clean run`

### How to find Patch for Git Repository
- To launch SimilarPatchIdentifier to make patch for buggy file from a Git repository, input values descripted below in `SPI.ini` file.
- in `SPI` section
    - `mode` : `github`
    - `repository_url` : URL of the Git repository
    - `commit_id` : Commit ID of the buggy version
    - `target_path` : Path of the buggy file in the repository
    - `test_list` : List of names of test classes that a project uses
    - `test_class_path` : Path of the test class path (Colon(`:`)-separated.)
    - `compile_class_path` : Path of the compile class path (Colon(`:`)-separated.)
    - `build_tool` : How a project is built. Only tools `maven` and `gradle` are possible for option
    - `faulty_line` : Line number of the buggy line
    - `faulty_line_fix` : Line number of the fixed line
    - `faulty_line_blame` : Line number of the blame line
    - `JAVA_HOME_8` : Path of the JDK 8
    - `byproduct_path` : Path of the directory which files and folders made during the progress of SPI should be stored into. Will make folder byproducts inside root by default.
- in `ChangeCollector` section
    - no need to input
- in `LCE` section
    - `candidate_number` : Number of candidates to be generated, default is 10
- in `ConFix` section
    - `patch.count` : Number of patch generation trials, default is 200000
    - `max.change.count` : The threshold of number of changes to use as patch material
    - `max.trials` : The threshold of patch generation trial, default is 100
    - `time.budget` : Time limit of ConFix execution, default is 3
    - `fl.metric` : How Fault Localization is done. default is perfect.

- After setting `SPI.ini` for the Git repository, you can launch SimilarPatchIdentifier with the following command.
    - if you want to rebuild all submodules, add `-r` or `--rebuild` option on execution.

> `python3 launcher.py`

- The result of the execution will be stored in the `byproduct_path` directory.
    - If "diff.txt" is found within path, it means the patch is found.
    - If there is none, it means that there is no patch found for the buggy file.


### How to find Patch for Defects4J Project
- Give inputs needed for launching SimilarPatchIdentifier in `SPI.ini` file.

### How to launch using batch

> `python3 launcher.py`

#### Arguments
- `'-r', '--rebuild'` : Rebuild all submodules (ChangeCollector, LCE) on start of execution. In default, `launcher.py` does not rebuild each submodules on execution.
- `'-d', '--debug'` : Execute single Defects4J project, `Closure-14` for testing a cycle of execution. Debug uses `flfreq` and `hash-match` strategies. SPI consists of three Java projects as submodules. Thus you may need to check if there is no compile error or wrong paths given through debug execution. If no problem occurs, you are clear to launch.

### Upon Execution...
#### "Notify me by Email"
- You may use inserted bash script, `tracker.sh` for notifying execution finish through email. Through bash script, `tracker.sh` will execute `launcher.py` with *rebuild* option given.
- You must use `handong.ac.kr` account only for email.
    - due to Gmail Rules, we cannot use *gmail* accounts for mailing within SERVER #24.
#### How to use *tracker.sh*
> `./tracker.sh` `{location_of_SPI}` `{your@email}`

