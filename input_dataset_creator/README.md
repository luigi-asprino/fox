### Input Dataset Creator

A very simple script that allows to create the input dataset for a classification task.
It takes as input an RDF-HDT dataset and produces a TSV file with the following format:

```
<Entity's URI><TAB><JSON Object summarising properties><TAB><Abstract>
```

### Dependency

Input Dataset Creator needs [RDF-HDT library](https://github.com/rdfhdt/hdt-cpp) being installed.

### Usage
```
$ make all

```