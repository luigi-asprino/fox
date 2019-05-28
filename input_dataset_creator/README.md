# Utility scripts

This folder contains a series of utility script for manipulating data.

## Input Dataset Creator (IDC)

A very simple script that allows to create the input dataset for a classification task.
It takes as input an RDF-HDT dataset and produces a TSV file with the following format:

```
<Entity's URI><TAB><JSON Object summarising properties><TAB><Abstract>
```

### Dependency

Input Dataset Creator needs [RDF-HDT CPP-library](https://github.com/rdfhdt/hdt-cpp) being installed.

### Usage
```
$ make input_dataset_creator
$ ./bin/input_dataset_creator <path to rdf-hdt file> <path to output file>
```

## Wiki Redirects Filter (WRF)

It is a very simple script that takes as input a TSV file comply with the format imposed 
by IDC and filters out the lines corresponding to entities that redirect to another DBpedia entity.

### Usage
```
$ make resolve_wiki_page_redirects
$ ./bin/resolve_wiki_page_redirects <path rdf-hdt file> <path to input file> <path to output file>
```