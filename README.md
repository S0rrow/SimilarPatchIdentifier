# SPI 2022
Revision of ConFix with patch recommendation data instead of change pool

ACC + LCE + ConFix

Inspired by _**Automated Patch Generation with Context-based Change Application**_ 
- [Original Repository of ConFix](https://github.com/thwak/ConFix)


## Other projects included:
- [ConFix](https://github.com/thwak/confix)
- [AllChangeCollector](https://github.com/JeongHyunHeo/AllcCangeCollector)
- [Longest Common sub-vector Extractor (LCE)](https://github.com/s0rrow/fv4202)

## How to run
1.

## 

## FicCollect [bash script]

- Checkout defects4j data 
- Use git blame to get hash id of BFIC and FIC

### Output

- list FIC in csv format
path: /home/DPMiner/ConPatFix/TEYH_pool/FicCollect/{Project_name}_withFIC.csv
columns: [DefectsfJ ID,Faulty file path,faulty line,FIC]

- list BFIC in csv format
path: /home/DPMiner/ConPatFix/TEYH_pool/PatchSuggestion/output/{Project_name}_withBFIC.csv
columns: [DefectsfJ ID,Faulty file path,faulty line,FIC,BFIC,project,dummy,lable]

##