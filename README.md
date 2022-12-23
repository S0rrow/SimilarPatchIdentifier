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
- Set up Defects4J with reference to [Steps to set up Defects4J](https://github.com/rjust/defects4j#requirements)
- Edit `SPI.ini`

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
|`jvm`|yes|automatically follow *JAVA_HOME_8* from SPI part|
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

### How to launch

> ```python3 launcher.py```

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

