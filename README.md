# SimilarPatchIdentifier (SPI) 2022

## What is SPI?
Inspired by _**Automated Patch Generation with Context-based Change Application**_, SPI is a revision of ConFix which uses change pool made out of patches from recommended projects, each of which is considered to have a similar bug as the target project.

---

## Submodules (Projects included)
**SPI = CC + LCE + ConFix**
- [ConFix](https://github.com/thwak/confix) (Original Repository of ConFix)
- [ChangeCollector (CC)](https://github.com/S0rrow/ChangeCollector)
- ~~[AllChangeCollector](https://github.com/JeongHyunHeo/AllChangeCollector)~~ (Integrated into ChangeCollector)
- [Longest Common sub-vector Extractor (LCE)](https://github.com/S0rrow/LCE)

---

## How to run

### Requirements
- JDK**s**
    - Oracle JDK 17 (Used to compile `ChangeCollector` and `Longest Common sub-vector Extractor`)
    - Oracle JDK 8 (Used by Defects4J)
- [Defects4J](https://github.com/rjust/defects4j)
    - See [requirements](https://github.com/rjust/defects4j#requirements)

### Pre-run Configuration
- Set up Defects4J with reference to [Steps to set up Defects4J](https://github.com/rjust/defects4j#requirements)
- Edit `SPI.ini`

#### SPI.ini settings
##### **SPI**
|key|is_required|description|
|:---|:---:|:---|
|`mode`|Yes|How `SPI` will be run. Can choose among those options:<br>- `defects4j` : Tells `SPI` to try finding a patch out of a `Defects4J` bug.<br>- `defects4j-batch` : Tells `SPI` to try finding a patch out of a `Defects4J` bug, but with a number of bugs given as a list.<br>- `github` : Tells `SPI` to try finding a patch out of a `GitHub` project with a bug.|
|`batch_d4j_file`|mode: `defects4j-batch`|Name of the file which contains names of Defects4J bugs|
|`identifier`|?||
|`version`|?||
|`repository_url`|In `github`|URL of GitHub project to look for a patch upon|
|`commit_id`|In `github`|Commit ID of GitHub project that produces a bug|
|`source_path`|In `github`|Relative path (from root) of Project Directory which contains source codes|
|`target_path`|In `github`|?|
|`test_list`|In `github`|List of names of test classes that a project uses|
|`test_class_path`|In `github`|?|
|`compile_class_path`|In `github`|?|
|`build_tool`|In `github`|How a project is built. Only tools `maven` and `gradle` are possible for option|
|`faulty_file`||Relative directory (from root of project) of a faulty file|
|`faulty_line_fix`||Line number of `faulty_file` to try modifying|
|`faulty_line_blame`||Line number of `faulty file` where the bug is made|
|`JAVA_HOME_8`|Yes|Absolute path of JDK 8 directory|
|`byproduct_path`|No|Directory which files and folders made during the progress of `SPI` should be stored into. If not given, It makes a folder `byproducts` inside `root`|
|`root`|No|Directory where `SPI` root directory is placed.|
|`patch_strategy`|Yes|List of patch strategies (among `flfreq`, `tested-first`, `noctx`, `patch`) to run `SPI` on. Comma-separated.)
|`concretization_strategy`|Yes|List of concretization strategies (among `tcvfl`, `hash-match`, `neightbor`, `tc`) to run `SPI` on. Comma-separated. |

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
|`doClean`|no|whether to clean the output directory before recurrent<br>execution with identical output directory|
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
|`doClean`|no|whether to clean the output directory before recurrent<br>execution with identical output directory|


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

1. run ``python3 launcher.py`` for identifying patches for defects4j projects.
2. give argument option ``-r`` to rebuild all submodules.
3. edit `SPI.ini` to specify the launch options.

#### Arguments
#### SPI.ini

