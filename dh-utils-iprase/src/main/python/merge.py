import xml.dom.minidom
import glob
import os
import pprint
import json
import pandas as pd

catFolder = "/Volumes/YellowSubmarine/Temi-Superiori/ExportAnnotazioniCAT"
citFolder = "/Volumes/YellowSubmarine/Temi-Superiori/citations"
jsonFolder = "/Volumes/YellowSubmarine/Temi-Superiori/json"

metadataFile = "/Volumes/YellowSubmarine/Temi-Superiori/metadati.csv"
yearsFile = "/Volumes/YellowSubmarine/Temi-Superiori/temi-anno-normalizzato.tsv"

outFolder = "/Volumes/YellowSubmarine/Temi-Superiori/json-complete"

docs = {}

if not os.path.exists(outFolder):
    os.mkdir(outFolder)

pp = pprint.PrettyPrinter(indent=4)

catInfo = {}
jsonInfo = {}
citations = {}
tokenIndexes = {}

tokenInfoCat = {}
tokenInfoCit = {}
tokenInfoJson = {}

def serialize_sets(obj):
    if isinstance(obj, set):
        return list(obj)
    return obj

print("Loading metadata file")
metadata = pd.read_csv(metadataFile, index_col=[0])

print("Loading years file")
years = pd.read_csv(yearsFile, index_col=[0], delimiter="\t", header=None)

print("Loading JSON files")
for f in glob.glob(os.path.join(jsonFolder, '*.txt.json')):
    doc_name = os.path.basename(f).replace(".txt.json", "")
    if len(docs) > 0 and doc_name not in docs:
        continue
    with open(f, "r") as fr:
        tokenCount = 0
        data = json.load(fr)
        jsonInfo[doc_name] = data
        sentences = data['sentences']
        for s in sentences:
            tokenCount += len(s['tokens'])
        tokenInfoJson[doc_name] = tokenCount

print("Loading citations files")
for f in glob.glob(os.path.join(citFolder, '*.txt')):
    doc_name = os.path.basename(f).replace(".txt", "")
    if len(docs) > 0 and doc_name not in docs:
        continue
    if doc_name not in citations:
        citations[doc_name] = {}
        citations[doc_name]['quotes'] = set()
        citations[doc_name]['texts'] = set()
    with open(f, "r") as fr:
        tokenCount = 0
        for line in fr:
            line = line.strip()
            if len(line) > 0:
                tokenCount += 1
                parts = line.split("\t")
                tokenIndexes[tokenCount] = parts[1]
                if parts[2] == "1":
                    citations[doc_name]['quotes'].add(parts[1])
                if parts[3] == "1":
                    citations[doc_name]['texts'].add(parts[1])
            # print(line)
        tokenInfoCit[doc_name] = tokenCount

# done and doneThis are used to remove duplicates in annotations
done = set()
print("Loading CAT files")
for f in glob.glob(os.path.join(catFolder, 'Iprase_*')):
    if not os.path.isdir(f):
        continue
    files = [fl for fl in glob.glob(os.path.join(f, "**/*.xml"), recursive=True)]
    doneThis = set()
    for fi in files:
        # print(fi)
        doc_name = os.path.basename(fi).replace(".txt.json.xml", "")
        doneThis.add(doc_name)
        if doc_name in done:
            continue
        if len(docs) > 0 and doc_name not in docs:
            continue
        doc = xml.dom.minidom.parse(fi)
        # doc_name = None
        # document = doc.getElementsByTagName("Document")
        # for d in document:
        #     doc_name = d.getAttribute("doc_name")
        # doc_name = doc_name.replace(".txt.json", "")
        if doc_name not in catInfo.keys():
            catInfo[doc_name] = {}
        # print(doc_name)
        tokens = doc.getElementsByTagName("token")
        tokenInfoCat[doc_name] = len(tokens)
        markables = doc.getElementsByTagName("Markables")
        for markable in markables:
            for childNode in markable.childNodes:
                if childNode.nodeType == 1:
                    if childNode.nodeName not in catInfo[doc_name].keys():
                        catInfo[doc_name][childNode.nodeName] = []
                    attributes = {}
                    tokens = set()
                    for a in childNode.attributes.values():
                        attributes[a.name] = a.value
                    for c in childNode.childNodes:
                        if c.nodeType == 1 and c.nodeName == "token_anchor":
                            tokens.add(tokenIndexes[int(c.getAttribute("t_id"))])
                    catInfo[doc_name][childNode.nodeName].append({"attributes": attributes, "tokens": tokens})
    done.update(doneThis)

print("Merging information")
for doc_name in jsonInfo:
    md = {}

    thisMetadata = metadata.loc[int(doc_name), : ]
    thisYears = years.loc[int(doc_name), : ]
    annotator = thisMetadata['Chi']
    workType = thisMetadata['SB/A']
    scope = thisMetadata['Ambito']
    title = thisMetadata['Titolo']
    target = thisMetadata['Destinazione']
    year = thisYears[1]
    schoolType1 = thisYears[2]
    schoolName = thisYears[3]
    schoolType2 = thisYears[4]

    if not pd.isna(workType):
        md['worktype'] = workType
    if not pd.isna(annotator):
        md['annotator'] = annotator
    if not pd.isna(scope):
        md['scope'] = scope
    if not pd.isna(title):
        md['title'] = title
    if not pd.isna(target):
        md['target'] = target
    if not pd.isna(year):
        md['year'] = year
        if not pd.isna(schoolType1):
            md['schooltype'] = schoolType1
        if not pd.isna(schoolType2):
            md['schooltypebroad'] = schoolType2
        if not pd.isna(schoolName):
            md['schoolname'] = schoolName

    jsonInfo[doc_name]["metadata"] = md
    if doc_name in catInfo:
        jsonInfo[doc_name]["cat_tasks_humans"] = catInfo[doc_name]
    else:
        print("Unable to find CAT information on file", doc_name)
    if doc_name in citations:
        for sentence in jsonInfo[doc_name]["sentences"]:
            sentenceIndex = sentence["index"]
            for token in sentence["tokens"]:
                tokenIndex = token["index"]
                index = str(sentenceIndex) + "_" + str(tokenIndex)
                token["isQuote"] = index in citations[doc_name]['quotes']
                token["isCitation"] = index in citations[doc_name]['texts']
    else:
        print("Unable to find citations on file ", doc_name)

    outFile = os.path.join(outFolder, doc_name + ".json")
    with open(outFile, "w") as fw:
        json.dump(jsonInfo[doc_name], fw, default=serialize_sets, indent=2, sort_keys=True)

