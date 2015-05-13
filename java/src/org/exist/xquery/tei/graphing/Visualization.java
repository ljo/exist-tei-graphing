package org.exist.xquery.tei.graphing;

import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.exist.Namespaces;
//import org.exist.dom.persistent.BinaryDocument;
//import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.QName;
//import org.exist.security.PermissionDeniedException;
//import org.exist.xmldb.XmldbURI;
import org.exist.xquery.*;
import org.exist.xquery.modules.ModuleUtils;
import org.exist.xquery.tei.TEIGraphingModule;
import org.exist.xquery.tei.graphing.jung.JungRelationGraph;
import org.exist.xquery.tei.graphing.jung.JungRelationGraphVertex;
import org.exist.xquery.tei.graphing.jung.JungRelationGraphEdge;
import org.exist.xquery.value.*;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** 
 * Creates RelationGraph presentations out of TEI encoded texts.
 *
 * @author ljo
 */
public class Visualization extends BasicFunction {
    private final static Logger LOG = LogManager.getLogger(Visualization.class);
    
    public final static FunctionSignature signatures[] = {
        new FunctionSignature(
                              new QName("relation-graph", TEIGraphingModule.NAMESPACE_URI, TEIGraphingModule.PREFIX),
                              "Serializes a relation graph based on provided persons and relations. All other parameters use default values.",
                              new SequenceType[] {
                                  new FunctionParameterSequenceType("listPersons", Type.ELEMENT, Cardinality.ONE_OR_MORE,
                                                                    "The tei:listPerson elements to create the graph from"),
                                  new FunctionParameterSequenceType("listRelations", Type.ELEMENT, Cardinality.ONE_OR_MORE,
                                                                    "The tei:listRelation elements to create the graph from")
                              },
                              new FunctionReturnSequenceType(Type.NODE, Cardinality.EXACTLY_ONE,
                                                             "The serialized relation graph in default SVG output-type.")
                              ),
        new FunctionSignature(
                              new QName("relation-graph", TEIGraphingModule.NAMESPACE_URI, TEIGraphingModule.PREFIX),
                              "Serializes a relation graph based on provided persons and relations. All other parameters use default values if empty.",
                              new SequenceType[] {
                                  new FunctionParameterSequenceType("listPersons", Type.ELEMENT, Cardinality.ONE_OR_MORE,
                                                                    "The tei:listPerson elements to create the graph from"),
                                  new FunctionParameterSequenceType("listRelations", Type.ELEMENT, Cardinality.ONE_OR_MORE,
                                                                    "The tei:listRelation elements to create the graph from"),
                                  new FunctionParameterSequenceType("configuration", Type.ELEMENT, Cardinality.EXACTLY_ONE,
                                                                    "The configuration, eg, for output type, edge shape and layout, &lt;parameters&gt;&lt;param name='output' value='svg'/&gt;&lt;/parameters&gt;.")
                              },
                              new FunctionReturnSequenceType(Type.NODE, Cardinality.EXACTLY_ONE,
                                                             "The serialized relation graph.")
                              )
    };

    private static File dataDir = null;

    private static String relationGraphSource = null;
    private static RelationGraph cachedRelationGraph = null;

    private RelationGraph relationGraph;

    private final Map<String, RelationGraph.Vertex> vertexFromSubjectId = new HashMap();

    public Visualization(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
        String relationGraphPath = null;

        boolean storeRelationGraph = true;
        boolean useStoredRelationGraph = isCalledAs("relation-graph-stored")
            && getSignature().getArgumentCount() == 5 ? true : false;

        Properties parameters = new Properties();

        context.pushDocumentContext();
        ValueSequence result = new ValueSequence();
        try {
            relationGraph = new JungRelationGraph();
            vertexFromSubjectId.clear();
            if (!args[0].isEmpty()) {
                for (int i = 0; i < args[0].getItemCount(); i++) {
                    LOG.debug("Adding listPerson/listOrg #: " + i);
                    parseNamesDates(((NodeValue)args[0].itemAt(i)).getNode());
                }
            }
            LOG.info("Number of Subjects (vertices):" + vertexFromSubjectId.size());
            
            if (!args[1].isEmpty()) {
                for (int i = 0; i < args[1].getItemCount(); i++) {
                    LOG.debug("Adding listRelation #: " + i);
                    parseListRelations(((NodeValue)args[1].itemAt(i)).getNode());
                }
            }
            LOG.info("Number of Relations (edges):" + relationGraph.edgeCount());

            if (isCalledAs("relation-graph")
                && getSignature().getArgumentCount() == 3) {
                if (!args[2].isEmpty()) {
                    parameters = ModuleUtils.parseParameters(((NodeValue)args[2].itemAt(0)).getNode());
                }
            }

	    boolean removeUnconnected = Boolean.parseBoolean(parameters.getProperty("removeunconnected", "false").toLowerCase());
	    if (removeUnconnected) {
		removeUnconnectedGroupOrNonPersonVertices();
	    }
		    
	    
            RelationGraphSerializer rgs = new RelationGraphSerializer(context, relationGraph);
            return rgs.relationGraphReport(parameters, vertexFromSubjectId.size());
        } finally {
            context.popDocumentContext();
        }
    }

    /* private void cleanCaches() {
       cachedVariantGraph = null;
       } */

    /**
     * The method <code>readRelationGraph</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param relationGraphPath a <code>String</code> value
     * @return an <code>RelationGraph</code> value
     * @exception XPathException if an error occurs
     */
    /* public static RelationGraph readRelationGraph(XQueryContext context, final String variantGraphPath) throws XPathException {
       try {
       if (relationGraphSource == null || !relationGraphPath.equals(relationGraphSource)) {
       relationGraphSource = relationGraphPath;
       DocumentImpl doc = (DocumentImpl) context.getBroker().getXMLResource(XmldbURI.createInternal(relationGraphPath));
       if (doc.getResourceType() != DocumentImpl.BINARY_FILE) {
       throw new XPathException("RelationGraph path does not point to a binary resource");
       }
       BinaryDocument binaryDocument = (BinaryDocument)doc;
       File relationGraphFile = context.getBroker().getBinaryFile(binaryDocument);
       if (dataDir == null) {
       dataDir = relationGraphFile.getParentFile();
       }
       cachedRelationGraph = RelationGraph.read(relationGraphFile);
       }
       } catch (PermissionDeniedException e) {
       throw new XPathException("Permission denied to read relation graph resource", e);
       } catch (IOException e) {
       throw new XPathException("Error while reading relation graph resource: " + e.getMessage(), e);
       }
       return cachedRelationGraph;
       } */

    public void parseNamesDates(Node listNamesDates) throws XPathException {
        if (listNamesDates.getNodeType() == Node.ELEMENT_NODE && listNamesDates.getLocalName().equals("listPerson") && listNamesDates.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
            parseListPersons(listNamesDates);
        }
        
        if (listNamesDates.getNodeType() == Node.ELEMENT_NODE && listNamesDates.getLocalName().equals("listOrg") && listNamesDates.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
            parseListOrgs(listNamesDates);
        }
    }

    public void parseListPersons(Node listPerson) throws XPathException {
        String type = "unknown";
        String persId = "unknown";
        String persName = "unknown";
        String sex = "unknown";
        String age = "unknown";
        String occupation = "unknown";
        String role = "person";
        
        if (listPerson.getNodeType() == Node.ELEMENT_NODE && listPerson.getLocalName().equals("listPerson") && listPerson.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
            NamedNodeMap attrs = listPerson.getAttributes();
            if (attrs.getLength() > 0) {
		if (attrs.getNamedItem("type") != null) {
		    type = attrs.getNamedItem("type").getNodeValue();
		}
            }
            //Get the listPerson children
            Node child = listPerson.getFirstChild();
            while (child != null) {
                //Parse each of the child nodes person/personGrp/listPerson
                if (child.getNodeType() == Node.ELEMENT_NODE && child.hasChildNodes()) {
                    if ((child.getLocalName().equals("person") || child.getLocalName().equals("personGrp")) &&
                        child.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                        
                        parsePersons(child, type);
                    } else if (child.getLocalName().equals("listPerson") &&
                               child.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                        parseListPersonGroup(child, type);
                    }
                }
                //next person/PersonGrp/listPerson node
                child = child.getNextSibling();
            }
        }
    }

    public void parseListOrgs(Node listOrg) throws XPathException {
        String type = "unknown";
        String orgId = "unknown";
        String orgName = "unknown";
        
        if (listOrg.getNodeType() == Node.ELEMENT_NODE && listOrg.getLocalName().equals("listOrg") && listOrg.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
            NamedNodeMap attrs = listOrg.getAttributes();
            if (attrs.getLength() > 0) {
                type = attrs.getNamedItem("type").getNodeValue();
            }
            //Get the listOrg children
            Node child = listOrg.getFirstChild();
            while (child != null) {
                //Parse each of the child nodes org/listOrg
                if (child.getNodeType() == Node.ELEMENT_NODE && child.hasChildNodes()) {
                    if (child.getLocalName().equals("org") &&
                        child.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                        
                        parseOrgs(child, type);
                    } else if (child.getLocalName().equals("listOrg") &&
                               child.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                        parseListOrgGroup(child, type);
                    }
                }
                //next org/listOrg node
                child = child.getNextSibling();
            }
        }
    }

    public void parsePersons(Node child, String type) throws XPathException {
        String persId = "unknown";
        String persName = "unknown";
        String sex = "unknown";
        String age = "unknown";
        String occupation = "unknown";
        String role = "person";
        String sortKey = "";
        int weight = 1;
        NamedNodeMap persAttrs = child.getAttributes();
        if (persAttrs.getLength() > 0) {
            try {
                persId = persAttrs.getNamedItemNS(Namespaces.XML_NS, "id").getNodeValue();
            } catch (NullPointerException e1) {
                try {
                    persId = persAttrs.getNamedItem("sameAs").getNodeValue().substring(1);
                } catch (NullPointerException e2) {
                    LOG.error("An element 'person' or 'personGrp' is missing xml:id attribute and has no sameAs attribute.");
                }
            }
            if (persAttrs.getNamedItem("sex") != null && !"".equals(persAttrs.getNamedItem("sex").getNodeValue())) {
                sex = persAttrs.getNamedItem("sex").getNodeValue();
            }

	    if (persAttrs.getNamedItem("role") != null && !"".equals(persAttrs.getNamedItem("role").getNodeValue())) {
                role = persAttrs.getNamedItem("role").getNodeValue();
            }

            if (persAttrs.getNamedItem("sortKey") != null && !"".equals(persAttrs.getNamedItem("sortKey").getNodeValue())) {
                sortKey = persAttrs.getNamedItem("sortKey").getNodeValue();
            }
        }
        //Get the person child nodes
        Node personChild = child.getFirstChild();
        while (personChild != null) {
            //Parse each of the personChild nodes
            if (personChild.getNodeType() == Node.ELEMENT_NODE && personChild.hasChildNodes()) {
                //parsePersonChildren(personChild, persName, sex, age, occupation);
                if (personChild.getLocalName().equals("persName")) {
            
                    String value = personChild.getNodeValue(); // personChild.getFirstChild().getNodeValue();
                    if (value == null) {
                        throw new XPathException("Value for 'persName' cannot be parsed");
                    } else {
                        persName = value;
                    }
                } else if (personChild.getLocalName().equals("occupation")) {
		    String value = null;
                    try {
			value = personChild.getFirstChild().getNodeValue();
                    } catch (NullPointerException npe) {
                        LOG.error("Element 'occupation' is missing text node value.");
                    } catch (Exception e) {
                        throw new XPathException("Value for 'occupation' cannot be parsed");
                    }
                    if (value == null) {
                    } else {
                        occupation = value;
                    }
                } else if (personChild.getLocalName().equals("sex")) {
                    String value = null;
                    try {
                        value = personChild.getFirstChild().getNodeValue();
                    } catch (NullPointerException npe) {
                        LOG.error("Element 'sex' is missing text node value.");
                    } catch (Exception e) {
                        throw new XPathException("Value for 'sex' cannot be parsed");
                    }
                    if (value == null) {
                    } else {
                        sex = value;
                    }
            
                } else if (personChild.getLocalName().equals("age")) {
                    String value = null;
                    try {
                        value = personChild.getFirstChild().getNodeValue();
                    } catch (NullPointerException npe) {
                        NamedNodeMap ageAttrs = child.getAttributes();
                        String ageAtMost;
                        String ageAtLeast;
                        if (ageAttrs.getLength() > 0) {
			    try {
                                value = ageAttrs.getNamedItem("value").getNodeValue();
                            } catch (NullPointerException npev) {
				try {
				    ageAtMost = ageAttrs.getNamedItem("atMost").getNodeValue();
				    value = ageAtMost;
				} catch (NullPointerException npe2) {
				    try {
					ageAtLeast = ageAttrs.getNamedItem("atLeast").getNodeValue();
					value = ageAtLeast;
				    } catch (NullPointerException npe3) {
                                    LOG.error("Element 'age' is missing text node value and has neither value, atLeast nor atMost attribute value.");
				    }
				}
                            }
                        }
                    } catch (Exception e) {
                        throw new XPathException("Value for 'age' cannot be parsed");
                    }

                    if (value == null) {
                        LOG.error("Element age is missing text node value and has neither value, atLeast nor atMost attribute.");
                    } else {
                        age = value;
                    }
                }
            }
            //next personChild node
            personChild = personChild.getNextSibling();    
        }
        if ("unknown".equals(persName)) {
            persName = persId;
        }

        LOG.info("parsePersons::" + persId +":"+ persName +":"+ type +":"+ sex +":"+ age +":"+ occupation +":"+ role);
        
        if (sortKey != null) {
            try {
                weight = Integer.parseInt(sortKey);
                if (child.getLocalName().equals("personGrp")) {
                    vertexFromSubjectId.put(persId, relationGraph.add(new WeightedPersonSubject(persId, persName, type, sex, age, occupation, role, true, weight)));
                } else {
                    vertexFromSubjectId.put(persId, relationGraph.add(new WeightedPersonSubject(persId, persName, type, sex, age, occupation, role, weight)));
                }
            } catch (NumberFormatException e) {
                if (child.getLocalName().equals("personGrp")) {
                    vertexFromSubjectId.put(persId, relationGraph.add(new PersonSubject(persId, persName, type, sex, age, occupation, role, true)));
                } else {
                    vertexFromSubjectId.put(persId, relationGraph.add(new PersonSubject(persId, persName, type, sex, age, occupation, role)));
                }
            }
        } else {
            if (child.getLocalName().equals("personGrp")) {
                vertexFromSubjectId.put(persId, relationGraph.add(new PersonSubject(persId, persName, type, sex, age, occupation, role, true)));
            } else {
                vertexFromSubjectId.put(persId, relationGraph.add(new PersonSubject(persId, persName, type, sex, age, occupation, role)));
            }
        }
    }

    public void parseOrgs(Node child, String type) throws XPathException {
        String orgId = "unknown";
        String orgName = "unknown";
        String sortKey = "";
        int weight = 1;
        NamedNodeMap orgAttrs = child.getAttributes();
        if (orgAttrs.getLength() > 0) {
            try {
                orgId = orgAttrs.getNamedItemNS(Namespaces.XML_NS, "id").getNodeValue();
            } catch (NullPointerException e1) {
                try {
                    orgId = orgAttrs.getNamedItem("sameAs").getNodeValue().substring(1);
                } catch (NullPointerException e2) {
                    LOG.error("An element 'org' is missing xml:id attribute and has no sameAs attribute.");
                }
            }
            if (orgAttrs.getNamedItem("sortKey") != null && !"".equals(orgAttrs.getNamedItem("sortKey").getNodeValue())) {
                sortKey = orgAttrs.getNamedItem("sortKey").getNodeValue();
            }
        }
        //Get the org child nodes
        Node orgChild = child.getFirstChild();
        while (orgChild != null) {
            //Parse each of the orgChild nodes
            if (orgChild.getNodeType() == Node.ELEMENT_NODE && orgChild.hasChildNodes()) {
                if (orgChild.getLocalName().equals("orgName")) {
            
                    String value = orgChild.getFirstChild().getNodeValue();
                    if (value == null) {
                        throw new XPathException("Value for 'orgName' cannot be parsed");
                    } else {
                        orgName = value;
                    }
                }
            }
            //next personChild node
            orgChild = orgChild.getNextSibling();    
        }

        if ("unknown".equals(orgName)) {
            orgName = orgId;
        }

        LOG.info("parseOrgs::" + orgId +":"+ orgName +":"+ type);
        
        if (sortKey != null) {
            try {
                weight = Integer.parseInt(sortKey);
                vertexFromSubjectId.put(orgId, relationGraph.add(new WeightedOrgSubject(orgId, orgName, type, weight)));
            } catch (NumberFormatException e) {
                vertexFromSubjectId.put(orgId, relationGraph.add(new OrgSubject(orgId, orgName, type)));
            }
        } else {
            vertexFromSubjectId.put(orgId, relationGraph.add(new OrgSubject(orgId, orgName, type)));
        }
    }


    public void parsePersonChildren(Node personChild, String persName, String sex, String age, String occupation) throws XPathException {
        if (personChild.getLocalName().equals("persName")) {
            
            String value = personChild.getFirstChild().getNodeValue();
            if (value == null) {
                throw new XPathException("Value for 'persName' cannot be parsed");
            } else {
                persName = value;
            }
        } else if (personChild.getLocalName().equals("occupation")) {
	    String value = null;
	    try {
		value = personChild.getFirstChild().getNodeValue();
	    } catch (NullPointerException npe) {
		LOG.error("Element 'occupation' is missing text node value.");
	    } catch (Exception e) {
		throw new XPathException("Value for 'occupation' cannot be parsed");
	    }
	    if (value == null) {
	    } else {
		occupation = value;
	    }
        } else if (personChild.getLocalName().equals("sex")) {
	    String value = null;
	    try {
		value = personChild.getFirstChild().getNodeValue();
	    } catch (NullPointerException npe) {
		LOG.error("Element 'sex' is missing text node value.");
	    } catch (Exception e) {
		throw new XPathException("Value for 'sex' cannot be parsed");
	    }
	    if (value == null) {
	    } else {
		sex = value;
	    }
        } else if (personChild.getLocalName().equals("age")) {
            String value = null;
	    try {
		value = personChild.getFirstChild().getNodeValue();
	    } catch (NullPointerException npe) {
		LOG.error("Element 'age' is missing text node value.");
	    } catch (Exception e) {
		throw new XPathException("Value for 'age' cannot be parsed");
	    }
	    if (value == null) {
	    } else {
		age = value;
	    }
        }                            
    }


    public void parseListPersonGroup(Node child, String type) throws XPathException {
        //Get the listPerson/listPerson child nodes
        Node listPersonChild = child.getFirstChild();
        while (listPersonChild != null) {
            String persId = "unknown";
            String persName = "unknown";
            String sex = "unknown";
            String age = "unknown";
	    String occupation = "unknown";
            String role = "person";

            //Parse each of the listPersonChild nodes
	    if (listPersonChild.getLocalName().equals("listPerson") &&
                               listPersonChild.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
		parseListPersonGroup(listPersonChild, type);
	    } else if (listPersonChild.getNodeType() == Node.ELEMENT_NODE && listPersonChild.hasChildNodes()) {
                if (listPersonChild.getLocalName().equals("personGrp") &&
                    listPersonChild.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                    LOG.info("listPerson/listPerson/personGrp");
                    NamedNodeMap persGrpAttrs = listPersonChild.getAttributes();
                    if (persGrpAttrs.getLength() > 0) {
                        try {
                            persId = persGrpAttrs.getNamedItemNS(Namespaces.XML_NS, "id").getNodeValue();
			    //LOG.info("Element 'personGrp' xml:id-attribute: " + persId);
                        } catch (NullPointerException e) {
                            try {
                                persId = persGrpAttrs.getNamedItem("sameAs").getNodeValue().substring(1);
                            } catch (NullPointerException e0) {
                                LOG.error("Element personGrp is missing xml:id-attribute and  has no sameAs-attribute.");
                            }
                        }

                        if (persGrpAttrs.getNamedItem("sex") != null && !"".equals(persGrpAttrs.getNamedItem("sex").getNodeValue())) {
                            sex = persGrpAttrs.getNamedItem("sex").getNodeValue();
                        }

			if (persGrpAttrs.getNamedItem("role") != null && !"".equals(persGrpAttrs.getNamedItem("role").getNodeValue())) {
                            role = persGrpAttrs.getNamedItem("role").getNodeValue();
                        }
                    }

                    Node grpChild = listPersonChild.getFirstChild();
                    while (grpChild != null) {
                        //parsePersonChildren(grpChild, persName, sex, age, occupation);
                        if (grpChild.getLocalName().equals("persName")) {
            		    if (grpChild.getFirstChild() != null) {
                               String value = grpChild.getFirstChild().getNodeValue();
                               if (value == null) {
                               	   throw new XPathException("Value for 'persName' cannot be parsed");
                               } else {
     			           persName = value;
                               }
			    } else {
			      LOG.error("No value for 'persName' for persId: " + persId);
			    }
                        } else if (grpChild.getLocalName().equals("occupation")) {
                            String value = grpChild.getFirstChild().getNodeValue();
                            if (value == null) {
                                throw new XPathException("Value for 'occupation' cannot be parsed");
                            } else {
                                occupation = value;
                            }
                        } else if (grpChild.getLocalName().equals("sex")) {
                            String value = grpChild.getFirstChild().getNodeValue();
                            if (value == null) {
                                throw new XPathException("Value for 'sex' cannot be parsed");
                            } else {
                                sex = value;
                            }
            
                        } else if (grpChild.getLocalName().equals("age")) {
                            String value = grpChild.getFirstChild().getNodeValue();
                            if (value == null) {
                                throw new XPathException("Value for 'age' cannot be parsed");
                            } else {
                                age = value;
                            }
                        }                            
                        //next person/listPerson node
                        grpChild = grpChild.getNextSibling();
                    }
		    final String nameOrId = "unknown".equals(persName) ? persId : persName;
                    LOG.info("parseListPersons::personGrp: " + persId + ":" + nameOrId +":"+ type +":"+ sex +":"+ age +":"+ occupation +":"+ role);
                    vertexFromSubjectId.put(persId, relationGraph.add(new PersonSubject(persId, nameOrId, type, sex, age, occupation, role, true)));
                } else if (listPersonChild.getLocalName().equals("person") &&
                           listPersonChild.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                    LOG.info("listPerson/listPerson/person");
                    parsePersons(listPersonChild, type);
                }
            }
            //next listPersonChild node
            listPersonChild = listPersonChild.getNextSibling();
        }
    }

    public void parseListOrgGroup(Node child, String type) throws XPathException {
        //Get the listOrg/listOrg child nodes
        Node listOrgChild = child.getFirstChild();
        while (listOrgChild != null) {
            String orgId = "unknown";
            String orgName = "unknown";

            //Parse each of the listOrgChild nodes
            if (listOrgChild.getNodeType() == Node.ELEMENT_NODE && listOrgChild.hasChildNodes()) {
                
                if (listOrgChild.getLocalName().equals("org") &&
                    listOrgChild.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                    LOG.info("listOrg/listOrg/org");
                    parseOrgs(listOrgChild, type);
                }
            }
            //next listPersonChild node
            listOrgChild = listOrgChild.getNextSibling();
        }
    }

    public void parseListRelations(Node relations) throws XPathException {
        if (relations.getNodeType() == Node.ELEMENT_NODE &&
            relations.getLocalName().equals("listRelation") &&
            relations.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
            //Get the First Child
            Node child = relations.getFirstChild();
            while (child != null) {
                String relSortKey = "";
                String relType = "unknown";
                String name;
                boolean hasMutual = false;
                boolean hasActive = false;
                
                //Parse each of the child nodes
                if (child.getNodeType() == Node.ELEMENT_NODE && child.hasChildNodes()) {
                    if (child.getLocalName().equals("relation") &&
                        child.getNamespaceURI().equals(RelationGraphSerializer.TEI_NS)) {
                
                        NamedNodeMap relAttrs = child.getAttributes();
                        if (relAttrs.getLength() > 0) {
                            name = relAttrs.getNamedItem("name").getNodeValue();
                            if (relAttrs.getNamedItem("type") != null && !"".equals(relAttrs.getNamedItem("type").getNodeValue())) {
                                relType = relAttrs.getNamedItem("type").getNodeValue();
                            }

                            if (relAttrs.getNamedItem("sortKey") != null && !"".equals(relAttrs.getNamedItem("sortKey").getNodeValue())) {
                                relSortKey = relAttrs.getNamedItem("sortKey").getNodeValue();
                            }

                            if (relAttrs.getNamedItem("mutual") != null && !"".equals(relAttrs.getNamedItem("mutual").getNodeValue())) {
                                String[] mutual;
                                mutual = getIds(relAttrs.getNamedItem("mutual").getNodeValue());
                                hasMutual = true;
                                LOG.info("parseListRelations::mutual: " + relType +":"+ name);

                                if (relSortKey != null) {
                                    try {
                                        int weight = Integer.parseInt(relSortKey);
                                        connectMutual(relType, name, weight, mutual);
                                    } catch (NumberFormatException e) {
                                        connectMutual(relType, name, mutual);
                                    }
                                } else {
                                    connectMutual(relType, name, mutual);
                                }

                                } else if (relAttrs.getNamedItem("active") != null && !"".equals(relAttrs.getNamedItem("active").getNodeValue()) && !hasMutual) {
                                String[] active;
                                String[] passive;
                                
                                active = getIds(relAttrs.getNamedItem("active").getNodeValue());

                                if (relAttrs.getNamedItem("passive") != null && !"".equals(relAttrs.getNamedItem("passive").getNodeValue())) { 
                                passive = getIds(relAttrs.getNamedItem("passive").getNodeValue());
                                LOG.info("parseListRelations::activePassive: " + relType +":"+ name);
				if (relSortKey != null) {
                                    try {
                                        int weight = Integer.parseInt(relSortKey);
					connectActivePassive(relType, name, weight, active, passive);
                                    } catch (NumberFormatException e) {
					connectActivePassive(relType, name, active, passive);
                                    }
                                } else {
				    connectActivePassive(relType, name, active, passive);
                                }

				} else {
				LOG.error("parseListRelations::activePassive - @active without @passive: " + relType + ":" + name);
				}
                            }
                        }
                    }
                }
                //next relation element
                child = child.getNextSibling();
            }
        }
    }

    public static String[] getIds(final String attrValue) {
        HashSet<String> set = new HashSet(); 
        for (String idref : attrValue.split("\\s+")) {
            try {
                set.add(idref.substring(1));
            } catch (StringIndexOutOfBoundsException e) {
                LOG.error("Failed: " + attrValue +":" + idref == null ? "empty substring" : idref);
            }


        }
        return set.toArray(new String[0]);
    }

    private void connectMutual(String type, String name, String[] subjectIds) {
        Relation r1 = new Relation(name, type);
        connectMutual(r1, subjectIds);
    }

    private void connectMutual(String type, String name, int weight, String[] subjectIds) {
        Relation r1 = new WeightedRelation(name, type, weight);
        connectMutual(r1, subjectIds);
    }

    private void connectMutual(Relation relation, String[] subjectIds) {
        if (subjectIds != null) {
            String id1 = subjectIds[0];
            for (String id : subjectIds) {
                if (vertexFromSubjectId.get(id) != null) {
                    id1 = id;
                    break;
                }
            }

            for (String id : subjectIds) {
                if (!id.equals(id1)) {
                    if (vertexFromSubjectId.get(id) == null) {
                        LOG.error("Vertex is missing for mutual-id: " + id);
                    } else {
                        relationGraph.connectUndirected(vertexFromSubjectId.get(id1), vertexFromSubjectId.get(id), relation);
                    }
                }
            }
        }
    }

    private void connectActivePassive(String type, String name, int weight, String[] activeSubjectIds, String[] passiveSubjectIds) {
        Relation r1 = new WeightedRelation(name, type, weight);
        connectActivePassive(r1, activeSubjectIds, passiveSubjectIds);
    }

    private void connectActivePassive(String type, String name, String[] activeSubjectIds, String[] passiveSubjectIds) {
        Relation r1 = new Relation(name, type);
	connectActivePassive(r1, activeSubjectIds, passiveSubjectIds);
    }

    private void connectActivePassive(Relation relation, String[] activeSubjectIds, String[] passiveSubjectIds) {
        for (String activeId : activeSubjectIds) {
            for (String passiveId : passiveSubjectIds) {
                if (!activeId.equals(passiveId)) {
                    if (vertexFromSubjectId.get(activeId) == null) {
                        LOG.error("Vertex is missing for activeId: " + activeId);
                    } else if (vertexFromSubjectId.get(passiveId) == null) {
                        LOG.error("Vertex is missing for passiveId: " + passiveId);
                    } else {
                        relationGraph.connectDirected(vertexFromSubjectId.get(activeId), vertexFromSubjectId.get(passiveId), relation);
                    }
                }
            }
        }
    }
    /**
     * Remove unconnected group vertices from the graph.
     *
     */
    private void removeUnconnectedGroupOrNonPersonVertices() {
	//for (String sid : vertexFromSubjectId)
	//relationGraph.subjects();
	for (RelationGraph.Vertex vertex : relationGraph.vertices()) {
	    if (vertex.relations().size() == 0) {
		if ((vertex.subject() instanceof PersonSubject && ((PersonSubject) vertex.subject()).isGroup()) ||
		    (vertex.subject() instanceof PersonSubject && !((PersonSubject) vertex.subject()).getRoleType().toString().equals("person")) ||
		    vertex.subject() instanceof OrgSubject) {
		    LOG.info("No relations for vertex: " + vertex);
		    vertex.delete();
		}
	    }
	}
    }
}
