exist-tei-graphing
===========================

Integrates (TEI) graphing through the jung2 and batik libraries into eXist-db.

## Compile and install

1. clone the github repository: https://github.com/ljo/exist-tei-graphing
2. edit local.build.properties and set exist.dir to point to your eXist-db install directory
3. call "ant" in the directory to create a .xar
4. upload the xar into eXist-db using the dashboard

## Functions
There are currently one main function:

###graphing:relation-graph
graphing:relation-graph($listPersons as element()+, $listRelations as element
()+) as node()

Serializes a relation graph based on provided persons and relations. All other parameters use default values.

Parameters:
    $listPersons+ 	The tei:listPerson elements to create the graph from
    $listRelations+ 	The tei:listRelation elements to create the graph from
Returns:
    node() : The serialized relation graph in default SVG output-type.

###graphing:relation-graph
graphing:relation-graph($listPersons as element()+, $listRelations as element
()+, $configuration as element()) as node()

Serializes a relation graph based on provided persons and relations. All other parameters use default values if empty.

Parameters:
    $listPersons+ 	The tei:listPerson elements to create the graph from
    $listRelations+ 	The tei:listRelation elements to create the graph from
    $configuration 	The configuration, currently only output type, eg &lt;parameters&gt;&lt;param name='output' value='svg'/&gt;&lt;/parameters&gt;. Values for 'output' can be 'svg' or 'graphml'.
Returns:
    node() : The serialized relation graph.


## Usage example

```xquery
xquery version "3.0";
import module namespace graphing="http://exist-db.org/xquery/tei-graphing";
declare namespace tei="http://www.tei-c.org/ns/1.0";
let $doc := doc("/db/dramawebben/data/works/IndebetouH_IDetGrona/IndebetouH_IDetGrona.xml")
return
graphing:relation-graph($doc//tei:listPerson[not(parent::tei:listPerson)], $doc//tei:listRelation, <parameters><param name="output" value="graphml"/></parameters>)
```
