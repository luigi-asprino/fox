### Input Dataset Creator

A very simple script that allows to create the input dataset for a classification task.
It takes as input an RDF-HDT dataset and produces a TSV file with the following format:

```
<Entity's URI><TAB><JSON Object summarising properties><TAB><Abstract>
```

### Dependency

Input Dataset Creator needs [RDF-HDT CPP-library](https://github.com/rdfhdt/hdt-cpp) being installed.

### Usage
```
$ make all
$ ./bin/input_dataset_creator <path to rdf-hdt file> <path to output file>
```