package org.exist.xquery.tei.graphing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.regex.*;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.OutsidePositioner;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.exist.dom.QName;
import org.exist.dom.memtree.MemTreeBuilder;
import org.exist.xquery.*;
import org.exist.xquery.modules.ModuleUtils;
import org.exist.xquery.value.*;
import org.exist.xquery.tei.graphing.Relation.RelationType;
import org.exist.xquery.tei.graphing.jung.JungRelationGraph;
import org.exist.xquery.tei.graphing.jung.JungRelationGraphEdge;
import org.exist.xquery.tei.graphing.jung.JungRelationGraphVertex;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

/**
 * Create various RelationGraph presentations of relations in 
 * TEI namesdates — Names, Dates, People, and Places.
 *
 * @author ljo
 */
public class RelationGraphSerializer {
    private final static Logger LOG = LogManager.getLogger(RelationGraphSerializer.class);
    
    public static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
    public static final String TEI_PREFIX = "tei";
    public static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";
    public static final String COLLATEX_PREFIX = "cx";
    public static final String SVG_NS = "http://www.w3.org/2000/svg";
    public static final String SVG_PREFIX = "svg";
    public static final String SVG_ELEM = "svg";
    public static final String G_ELEM = "g";
    public static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    private static final String GRAPHML_PREFIX = "gml";
    private static final String GRAPHML_DOC_ELEM = "graphml";
    private static final String GRAPH_ID = "g0";
    private static final String GRAPH_ELEM = "graph";
    private static final String XMLNSXSI_ATT = "xmlns:xsi";
    private static final String XSISL_ATT = "xsi:schemaLocation";
    private static final String XMLNSXSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String GRAPHML_XSISL = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    private static final String NODE_ELEM = "node";
    private static final String TARGET_ATT = "target";
    private static final String SOURCE_ATT = "source";
    private static final String DIRECTED_ATT = "directed";
    private static final String EDGE_ELEM = "edge";
    private static final String EDGEDEFAULT_DEFAULT_VALUE = "undirected";
    private static final String EDGEDEFAULT_ATT = "edgedefault";

    private static final String PARSENODEIDS_ATT = "parse.nodeids";
    private static final String PARSENODEIDS_DEFAULT_VALUE = "canonical";
    private static final String PARSEEDGEIDS_ATT = "parse.edgeids";
    private static final String PARSEEDGEIDS_DEFAULT_VALUE = "canonical";
    private static final String PARSEORDER_ATT = "parse.order";
    private static final String PARSEORDER_DEFAULT_VALUE = "nodesfirst";

    private static final String ATTR_TYPE_ATT = "attr.type";
    private static final String ATTR_NAME_ATT = "attr.name";
    private static final String FOR_ATT = "for";
    private static final String ID_ATT = "id";
    private static final String KEY_ELEM = "key";
    private static final String DATA_ELEM = "data";
    public static final String GEXF_NS = "http://www.gexf.net/1.2draft";
    private static final String GEXF_PREFIX = "gxf";
    private static final String GEXF_DOC_ELEM = "gexf";
    private static final String GEXF_XSISL = "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd";
    private static final String GEXF_VERSION_ATT = "version";
    private static final String GEXF_VERSION = "1.2";
    private static final String META_ELEM = "meta";
    private static final String LASTMODIFIEDDATE_ATT = "lastmodifieddate";
    private static final String CREATOR_ELEM = "creator";
    private static final String DESC_ELEM = "description";
    private static final String DEFAULTEDGETYPE_ATT = "defaultedgetype";
    private static final String ATTRS_ELEM = "attributes";
    private static final String CLASS_ATT = "class";
    private static final String MODE_ATT = "mode";
    private static final String ATTR_ELEM = "attribute";
    private static final String DEFAULT_ELEM = "default";
    private static final String TITLE_ATT = "title";
    private static final String TYPE_ATT = "type";
    private static final String NODES_ELEM = "nodes";
    private static final String LABEL_ATT = "label";
    private static final String EDGES_ELEM = "edges";
    private static final String ATTVALUES_ELEM = "attvalues";
    private static final String ATTVALUE_ELEM = "attvalue";
    private static final String VALUE_ATT = "value";
    private static final String WEIGHT_ATT = "weight";

    private static File dataDir = null;

    private XQueryContext context;

    private static RelationGraph relationGraph;
    private final Map<RelationGraph.Vertex, Integer> vertexIds = new HashMap();

    public RelationGraphSerializer(final XQueryContext context, RelationGraph relationGraph) {
        this.context = context;
        this.relationGraph = relationGraph;
    }
    
    public ValueSequence relationGraphReport(final Properties parameters, final int numberOfVertices) throws XPathException {
        ValueSequence result = new ValueSequence();
	final MemTreeBuilder builder = context.getDocumentBuilder();
	switch (parameters.getProperty("output", "svg").toLowerCase()) {
	case "gexf":
	    toGexf(builder);
	    result.add((NodeValue) builder.getDocument().getDocumentElement());
	    break;
	case "graphml":
	    toGraphML(builder);
            result.add((NodeValue) builder.getDocument().getDocumentElement());
	    break;
	case "svg":
	    result.add(toSvg((JungRelationGraph) relationGraph, numberOfVertices, parameters));
	    break;
	default:
	    result.add(toSvg((JungRelationGraph) relationGraph, numberOfVertices, parameters));
	    break;
	}
        return result;
    }

    private void toGraphML(final MemTreeBuilder builder) {
	builder.startDocument();
        builder.startElement(new QName(GRAPHML_DOC_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
        for (GraphMLProperty p : GraphMLProperty.values()) {
            p.declare(builder);
        }
        
        builder.startElement(new QName(GRAPH_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
        builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf(GRAPH_ID));
        builder.addAttribute(new QName(EDGEDEFAULT_ATT, null, null), String.valueOf(EDGEDEFAULT_DEFAULT_VALUE));
        builder.addAttribute(new QName(PARSENODEIDS_ATT, null, null), String.valueOf(PARSENODEIDS_DEFAULT_VALUE));
        builder.addAttribute(new QName(PARSEEDGEIDS_ATT, null, null), String.valueOf(PARSEEDGEIDS_DEFAULT_VALUE));
        builder.addAttribute(new QName(PARSEORDER_ATT, null, null), String.valueOf(PARSEORDER_DEFAULT_VALUE));
        
        for (RelationGraph.Vertex vertex : relationGraph.vertices()) {
            final int id = numericId(vertex);
            LOG.debug("GraphML vertex #: " + id);
            builder.startElement(new QName(NODE_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("n" + id));
            GraphMLProperty.NODE_NUMBER.write(Integer.toString(id), builder);
            GraphMLProperty.NODE_SUBJECT.write(vertex.toString(), builder);
	    if (vertex.subject() instanceof OrgSubject) {
		GraphMLProperty.NODE_TYPE.write("organisation", builder);
	    } else if (vertex.subject() instanceof PersonSubject) {
		if (((PersonSubject) vertex.subject()).getType().toString().equals("pet")) {
		    GraphMLProperty.NODE_TYPE.write("pet", builder);
		} else if (((PersonSubject) vertex.subject()).getType().toString().equals("noncast")) {
		    GraphMLProperty.NODE_TYPE.write("noncast person", builder);
		} else {
		    GraphMLProperty.NODE_TYPE.write("cast person", builder);
		}
	    }
	    builder.endElement();
        }
        
        int edgeNumber = 0;
        for (RelationGraph.Edge edge : relationGraph.edges()) {
            LOG.debug("GraphML edge #: " + edgeNumber);
            builder.startElement(new QName(EDGE_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("e" + edgeNumber));
            builder.addAttribute(new QName(DIRECTED_ATT, null, null), String.valueOf(edge.directed()));

            builder.addAttribute(new QName(SOURCE_ATT, null, null), String.valueOf("n" + numericId(edge.from())));
            
            builder.addAttribute(new QName(TARGET_ATT, null, null), String.valueOf("n" + numericId(edge.to())));
            GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), builder);
            GraphMLProperty.EDGE_RELATION.write(Relation.getVerb(edge), builder);
	    if (edge.relation() instanceof WeightedRelation) {
		GraphMLProperty.EDGE_WEIGHT.write(Integer.toString(((WeightedRelation)edge.relation()).getWeight()), builder);
	    }
            GraphMLProperty.EDGE_TYPE.write(Relation.getType(edge), builder);
            builder.endElement();
        }
        
        builder.endElement();
        builder.endElement();
    }

    private enum GraphMLProperty {
        NODE_NUMBER(NODE_ELEM, "number", "int"), //
        NODE_SUBJECT(NODE_ELEM, "subject", "string"), //
        NODE_TYPE(NODE_ELEM, "type", "string"), //
        EDGE_NUMBER(EDGE_ELEM, "number", "int"), //
        EDGE_RELATION(EDGE_ELEM, "relation", "string"), //
        EDGE_WEIGHT(EDGE_ELEM, "weight", "int"), //
        EDGE_TYPE(EDGE_ELEM, "type", "string");

        private String name;
        private String forElement;
        private String type;
        
        private GraphMLProperty(String forElement, String name, String type) {
            this.name = name;
            this.forElement = forElement;
            this.type = type;
        }
        
        public void write(final String data, MemTreeBuilder builder) {
            builder.startElement(new QName(DATA_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
            builder.addAttribute(new QName(KEY_ELEM, null, null), String.valueOf("d" + ordinal()));
            builder.characters((CharSequence) data);
            builder.endElement();
        }
        
        public void declare(MemTreeBuilder builder) {
            builder.startElement(new QName(KEY_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("d" + ordinal()));
            builder.addAttribute(new QName(FOR_ATT, null, null), String.valueOf(forElement));
            builder.addAttribute(new QName(ATTR_NAME_ATT, null, null), String.valueOf(name));
            builder.addAttribute(new QName(ATTR_TYPE_ATT, null, null), String.valueOf(type));
            builder.endElement();
        }
    }

    private void toGexf(final MemTreeBuilder builder) {
	builder.startDocument();
	builder.startElement(new QName(GEXF_DOC_ELEM, GEXF_NS, GEXF_PREFIX), null);
	builder.addAttribute(new QName(XMLNSXSI_ATT, null, null), String.valueOf(XMLNSXSI));
        builder.addAttribute(new QName(XSISL_ATT, null, null), String.valueOf(GEXF_XSISL));
        builder.addAttribute(new QName(GEXF_VERSION_ATT, null, null), String.valueOf(GEXF_VERSION));
	builder.startElement(new QName(META_ELEM, GEXF_NS, GEXF_PREFIX), null);
	builder.startElement(new QName(CREATOR_ELEM, GEXF_NS, GEXF_PREFIX), null);
	builder.endElement();
	builder.endElement();
        builder.startElement(new QName(GRAPH_ELEM, GEXF_NS, GEXF_PREFIX), null);
        builder.addAttribute(new QName(DEFAULTEDGETYPE_ATT, null, null), String.valueOf(EDGEDEFAULT_DEFAULT_VALUE));
	//builder.addAttribute(new QName(MODE_ATT, null, null), String.valueOf("static"));

	builder.startElement(new QName(ATTRS_ELEM, GEXF_NS, GEXF_PREFIX), null);
	builder.addAttribute(new QName(CLASS_ATT, null, null), String.valueOf("node"));
        for (GexfNodeProperty p : GexfNodeProperty.values()) {
            p.declare(builder);
        }
	builder.endElement();
	
	builder.startElement(new QName(ATTRS_ELEM, GEXF_NS, GEXF_PREFIX), null);
	builder.addAttribute(new QName(CLASS_ATT, null, null), String.valueOf("edge"));
	for (GexfEdgeProperty p : GexfEdgeProperty.values()) {
            p.declare(builder);
        }
	builder.endElement();

	builder.startElement(new QName(NODES_ELEM, GEXF_NS, GEXF_PREFIX), null);
        for (RelationGraph.Vertex vertex : relationGraph.vertices()) {
            final int id = numericId(vertex);
            LOG.debug("Gexf vertex #: " + id);
            builder.startElement(new QName(NODE_ELEM, GEXF_NS, GEXF_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("n" + id));
	    builder.addAttribute(new QName(LABEL_ATT, null, null), vertex.toString());
	    
	    builder.startElement(new QName(ATTVALUES_ELEM, GEXF_NS, GEXF_PREFIX), null);
            GexfNodeProperty.NODE_NUMBER.write(Integer.toString(id), builder);
            GexfNodeProperty.NODE_SUBJECT.write(vertex.toString(), builder);
	    if (vertex.subject() instanceof OrgSubject) {
		GexfNodeProperty.NODE_TYPE.write("organisation", builder);
	    } else if (vertex.subject() instanceof PersonSubject) {
		if (((PersonSubject) vertex.subject()).getType().toString().equals("pet")) {
		    GexfNodeProperty.NODE_TYPE.write("pet", builder);
		} else if (((PersonSubject) vertex.subject()).getType().toString().equals("noncast")) {
		    GexfNodeProperty.NODE_TYPE.write("noncast person", builder);
		} else {
		    GexfNodeProperty.NODE_TYPE.write("cast person", builder);
		}
	    }
	    builder.endElement();
	    builder.endElement();
        }
	builder.endElement();
	
	builder.startElement(new QName(EDGES_ELEM, GEXF_NS, GEXF_PREFIX), null);
        int edgeNumber = 0;
        for (RelationGraph.Edge edge : relationGraph.edges()) {
            LOG.debug("Gexf edge #: " + edgeNumber);
            builder.startElement(new QName(EDGE_ELEM, GEXF_NS, GEXF_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("e" + edgeNumber++));
	    if (edge.directed()) {
		builder.addAttribute(new QName(TYPE_ATT, null, null), "directed");
	    }
            builder.addAttribute(new QName(SOURCE_ATT, null, null), String.valueOf("n" + numericId(edge.from())));
            builder.addAttribute(new QName(TARGET_ATT, null, null), String.valueOf("n" + numericId(edge.to())));
            builder.addAttribute(new QName(LABEL_ATT, null, null), Relation.getVerb(edge));
	    if (edge.relation() instanceof WeightedRelation) {
		builder.addAttribute(new QName(WEIGHT_ATT, null, null), Integer.toString(((WeightedRelation)edge.relation()).getWeight()));
	    }
	    builder.startElement(new QName(ATTVALUES_ELEM, GEXF_NS, GEXF_PREFIX), null);
	    GexfEdgeProperty.EDGE_TYPE.write(Relation.getType(edge), builder);
            builder.endElement();
	    
	    builder.endElement();
        }
	builder.endElement();
		
        builder.endElement();
        builder.endElement();
    }

    private enum GexfNodeProperty {
	NODE_NUMBER("number", "integer"), //
	NODE_SUBJECT("subject", "string"), //
	NODE_TYPE("type", "string");

        private String name;
        private String type;
        
        private GexfNodeProperty(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public void write(final String data, MemTreeBuilder builder) {
            builder.startElement(new QName(ATTVALUE_ELEM, GEXF_NS, GEXF_PREFIX), null);
            builder.addAttribute(new QName(FOR_ATT, null, null), String.valueOf("dn" + ordinal()));
            builder.addAttribute(new QName(VALUE_ATT, null, null), String.valueOf(data));
            builder.endElement();
        }
        
        public void declare(MemTreeBuilder builder) {
	    builder.startElement(new QName(ATTR_ELEM, GEXF_NS, GEXF_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("dn" + ordinal()));
            builder.addAttribute(new QName(TITLE_ATT, null, null), String.valueOf(name));
            builder.addAttribute(new QName(TYPE_ATT, null, null), String.valueOf(type));
            builder.endElement();
        }
    }

        private enum GexfEdgeProperty {
	EDGE_NUMBER("number", "integer"), //
	EDGE_RELATION("relation", "string"), //
	EDGE_WEIGHT("weight", "integer"), //
	EDGE_TYPE("type", "string");

        private String name;
        private String type;
        
        private GexfEdgeProperty(String name, String type) {
            this.name = name;
            this.type = type;
        }

	public void write(final String data, MemTreeBuilder builder) {
            builder.startElement(new QName(ATTVALUE_ELEM, GEXF_NS, GEXF_PREFIX), null);
            builder.addAttribute(new QName(FOR_ATT, null, null), String.valueOf("de" + ordinal()));
            builder.addAttribute(new QName(VALUE_ATT, null, null), String.valueOf(data));
            builder.endElement();
        }

	public void declare(MemTreeBuilder builder) {
	    builder.startElement(new QName(ATTR_ELEM, GEXF_NS, GEXF_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("de" + ordinal()));
            builder.addAttribute(new QName(TITLE_ATT, null, null), String.valueOf(name));
            builder.addAttribute(new QName(TYPE_ATT, null, null), String.valueOf(type));
            builder.endElement();
        }
    }


    public NodeValue toSvg(JungRelationGraph jvg, final int numberOfVertices, final Properties parameters) throws XPathException {
	int svgWidth = 960;
	int svgHeight = 600;
	Dimension dimension = new Dimension(svgWidth, svgHeight);
	try {
	    svgWidth = Integer.parseInt(parameters.getProperty("svg-width", "960"));
	} catch (NumberFormatException e) {
	}
	try {
	    svgHeight = Integer.parseInt(parameters.getProperty("svg-height", "600"));
	} catch (NumberFormatException e) {
	}
	dimension = new Dimension(svgWidth, svgHeight);
	if (svgWidth == 960 && svgHeight == 600) {
	    if (numberOfVertices > 82) {
		dimension = new Dimension(2200, 1400);
	    } else if (numberOfVertices > 55) {
		dimension = new Dimension(1600, 1000);
	    } else if (numberOfVertices > 27) {
		dimension = new Dimension(1200, 800);
	    }
	}

        NodeValue nv = null;
        try {
	    // Get a DOMImplementation and create an XML document
            DOMImplementation domImpl =
               GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument(SVG_NS, SVG_ELEM, null);
            SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
            ctx.setComment("Generated by an eXist-db application with Batik SVG Generator");
	    // Create an instance of the SVG Generator
            SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false);
            svgGenerator.setUnsupportedAttributes(null);
            // draw the graph in the SVG generator
            final VisualizationImageServer<JungRelationGraphVertex, JungRelationGraphEdge> server = createServer(jvg, dimension, parameters);
	    
            //Element svg = document.getDocumentElement();
            //svgGenerator.getRoot(svg);
            //svg.setAttributeNS(null, "overflow", "visible"); 

            server.printAll(svgGenerator);
            BufferedImage image = (BufferedImage) server.getImage(new Point2D.Double(dimension.getWidth() / 2, dimension.getHeight() / 2), dimension);

            OutputStream os = System.out;
            //Writer out = new OutputStreamWriter(os, "UTF-8");
            Writer out = new StringWriter();
            svgGenerator.stream(out, false);

            os.flush();
            os.close();
            try {
                nv = ModuleUtils.stringToXML(context, out.toString());
            } catch (SAXException e) {
                throw new XPathException(e.getMessage());
            }

        } catch (SVGGraphics2DIOException e) {
	    
        } catch (IOException e) {

        }
        return nv;

    }
    
    public VisualizationImageServer<JungRelationGraphVertex, JungRelationGraphEdge>
        createServer(final JungRelationGraph jvg, final Dimension dimension, final Properties parameters) {
        Layout<JungRelationGraphVertex, JungRelationGraphEdge> layout;
	switch (parameters.getProperty("layout", "frlayout").toLowerCase()) {
	case "circle" : case "circlelayout":
	    layout = new CircleLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	case "dag" : case "daglayout":
	    layout = new DAGLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	case "fr" : case "frlayout":
	    layout = new FRLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	case "isom" : case "isomlayout":
	    layout = new ISOMLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	case "kk" : case "kklayout":
	    layout = new KKLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	case "spring" : case "springlayout":
	    layout = new SpringLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	case "static" : case "staticlayout":
	    layout = new StaticLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	default:
	    layout = new FRLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
	    break;
	}
        layout.setSize(new Dimension(dimension.width - 80, dimension.height));

	switch (parameters.getProperty("layout", "frlayout").toLowerCase()) {
	case "fr" : case "frlayout" :
	    try {
		int maxIterations = Integer.parseInt(parameters.getProperty("maxiterations", "700"));
		((FRLayout) layout).setMaxIterations(maxIterations);
	    } catch (NumberFormatException e) {
	    }
	    try {
		double attractionMultiplier = Double.parseDouble(parameters.getProperty("attractionmultiplier", "0.75"));
		((FRLayout) layout).setAttractionMultiplier(attractionMultiplier);
	    } catch (NumberFormatException e) {
	    }
	    try {
		double repulsionMultiplier = Double.parseDouble(parameters.getProperty("repulsionmultiplier", "0.75"));
		((FRLayout) layout).setRepulsionMultiplier(repulsionMultiplier);
	    } catch (NumberFormatException e) {
	    }
	    break;
	case "kk" : case "kklayout":
	    boolean adjustForGravity = Boolean.parseBoolean(parameters.getProperty("adjustforgravity", "true"));
	    ((KKLayout)layout).setAdjustForGravity(adjustForGravity);
	    boolean exchangeVertices = Boolean.parseBoolean(parameters.getProperty("exchangevertices", "true"));
	    ((KKLayout)layout).setExchangeVertices(exchangeVertices);
	    try {
		int maxIterations = Integer.parseInt(parameters.getProperty("maxiterations", "2000"));
		((KKLayout) layout).setMaxIterations(maxIterations);
	    } catch (NumberFormatException e) {
	    }
	    break;
	case "spring" : case "springlayout":
	    try {
		double forceMultiplier = Double.parseDouble(parameters.getProperty("forcemultiplier", "1.0 / 3.0"));
		((SpringLayout) layout).setForceMultiplier(forceMultiplier);
	    } catch (NumberFormatException e) {
	    }
	    try {
		double repulsionRange = Integer.parseInt(parameters.getProperty("repulsionrange", "100"));
		((SpringLayout) layout).setForceMultiplier(repulsionRange);
	    } catch (NumberFormatException e) {
	    }
	    try {
		double stretch = Double.parseDouble(parameters.getProperty("stretch", "0.7"));
		((SpringLayout) layout).setStretch(stretch);
	    } catch (NumberFormatException e) {
	    }
	    break;
	default: break;
	}

        final VisualizationImageServer<JungRelationGraphVertex, JungRelationGraphEdge> vis =
            new VisualizationImageServer<JungRelationGraphVertex, JungRelationGraphEdge>(layout, dimension);

	// common
	String labelOffset = parameters.getProperty("labeloffset", "");
	if (!"".equals(labelOffset)) {
	    try {
		int offset = Integer.parseInt(labelOffset);
		vis.getRenderContext().setLabelOffset(offset);
	    } catch (NumberFormatException e) {
	    }
	}

	// tooltips
        //vv.setVertexToolTipTransformer(new ToStringLabeller<JungRelationGraphVertex>());
        //vv.setEdgeToolTipTransformer(new ToStringLabeller<JungrelationGraphEdge>());

        // Vertices
	switch (parameters.getProperty("vertexlabelpositioner", "inside").toLowerCase()) {
	case "inside":
	    vis.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
	    break;
	case "outside":
	    vis.getRenderer().getVertexLabelRenderer().setPositioner(new OutsidePositioner());
	    break;
	default:
	    vis.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
	    break;
	}

	switch (parameters.getProperty("vertexlabelposition", "center").toLowerCase()) {
	case "auto":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
	    break;
	case "center":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
	    break;
	case "east":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.E);
	    break;
	case "north":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.N);
	    break;
	case "northeast":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.NE);
	    break;
	case "northwest":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.NW);
	    break;
	case "south":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
	    break;
	case "southeast":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.SE);
	    break;
	case "southwest":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.SW);
	    break;
	case "west":
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.W);
	    break;
	default:
	    vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
	    break;
	}

	//vis.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<JungRelationGraphVertex>());
        vis.getRenderContext().setVertexLabelTransformer(new Transformer<JungRelationGraphVertex, String>() {
                @Override
                    public String transform(JungRelationGraphVertex vertex) {
                    return "<html><center>" + vertex.toString();
              }
        });

	String vertexFillPaint = parameters.getProperty("vertexfillpaint", "white").toLowerCase();

	vis.getRenderContext().setVertexFillPaintTransformer(new VertexFillPainter(vertexFillPaint));

        //vis.getRenderer().setVertexRenderer(new ShapeRenderer());
        Transformer<JungRelationGraphVertex, Shape> vertexShape = new Transformer<JungRelationGraphVertex, Shape>() {
            @Override
            public Shape transform(JungRelationGraphVertex vm) {
		int sl = "center".equals(parameters.getProperty("vertexlabelposition", "center").toLowerCase()) ? vm.toString().length() * 6 : vm.toString().length();

                //int sl = vm.toString().length() * 6;
                if (vm.subject() instanceof OrgSubject) {
                    return new Rectangle(-25, -10, 50 + sl, 20);
                } else {
                    return new Ellipse2D.Double(-25, -10, 50 + sl, 20);
                }
                
            }
        };
        vis.getRenderContext().setVertexShapeTransformer(vertexShape);
        //vis.getRenderContext().setVertexShapeTransformer(new VertexLabelAsShapeRenderer<JungRelationGraphVertex, JungRelationGraphEdge>(vis.getRenderContext()));
	final boolean dashedStrokeOrgs = Boolean.parseBoolean(parameters.getProperty("dashedstrokeorgs", "false"));
        Transformer<JungRelationGraphVertex, Stroke> vertexStroke = new Transformer<JungRelationGraphVertex, Stroke>() {
            float dash[] = { 10.0f };
            public Stroke transform(JungRelationGraphVertex v) {
                try {
                    if ((v.subject() instanceof PersonSubject && ((PersonSubject) v.subject()).getType().toString().equals("noncast")) ||
			(v.subject() instanceof OrgSubject && dashedStrokeOrgs)) {
                        return new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                                               BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                    } else {
                        return new BasicStroke(2);
                    }

                } catch (NullPointerException e) {
                    LOG.error("Problem with vertex: " + v.toString() + ", probably an unsupported subject type.");
                    return new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
                                               BasicStroke.JOIN_MITER, 20.0f, dash, 0.0f);
                }
            }
        };
        vis.getRenderContext().setVertexStrokeTransformer(vertexStroke);

        // Edges
	boolean showEdgeLabels = Boolean.parseBoolean(parameters.getProperty("svg-showedgelabels", "true").toLowerCase());
	if (showEdgeLabels) {
	    vis.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
	    vis.getRenderContext().setEdgeLabelTransformer(new Transformer<JungRelationGraphEdge, String>() {
		    @Override
                    public String transform(JungRelationGraphEdge edge) {
			return "<html><center>" + edge.relation().getVerb();
		    }
		});
	}

	EdgeStrokeRenderer esr = new EdgeStrokeRenderer(Boolean.parseBoolean(parameters.getProperty("svg-useedgeweight", "true").toLowerCase()));
	vis.getRenderContext().setEdgeStrokeTransformer(esr);

	switch (parameters.getProperty("edgeshape", "line").toLowerCase()) {
	case "bent": case "bentline":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.BentLine());
	    break;
	case "box":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Box());
	    break;
	case "cubic" : case "cubiccurve":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.CubicCurve());
	    break;
	case "line":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
	    break;
	case "loop":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Loop());
	    break;
	case "orthogonal":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Orthogonal());
	    break;
	case "quad" : case "quadcurve":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve());
	    break;
	case "simpleloop":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.SimpleLoop());
	    break;
	case "wedge":
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Wedge(10));
	    break;
	default:
	    vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
	    break;
	}

        return vis;
    }

    static class ShapeRenderer implements Renderer.Vertex<JungRelationGraphVertex, JungRelationGraphEdge> {
        @Override
            public void paintVertex(RenderContext<JungRelationGraphVertex, JungRelationGraphEdge> rc,
                                    Layout<JungRelationGraphVertex, JungRelationGraphEdge> layout, JungRelationGraphVertex vertex) {
            GraphicsDecorator graphicsContext = rc.getGraphicsContext();
            Point2D center = layout.transform(vertex);
            Shape shape = null;
            Color colour = null;
            if(vertex.subject() instanceof PersonSubject && ((PersonSubject) vertex.subject()).getType().equals("cast")) {
                shape = new Rectangle((int) center.getX() - 10, (int) center.getY() - 10, 20, 20);
                colour = new Color(127, 127, 0);
            } else if(vertex.subject() instanceof PersonSubject && ((PersonSubject) vertex.subject()).getType().equals("noncast")) {
                shape = new Rectangle((int) center.getX() - 10, (int) center.getY() - 20, 20, 40);
                colour = new Color(127, 0, 127);
            } else {
                shape = new Ellipse2D.Double(center.getX() - 10, center.getY() - 10, 20, 20);
                colour = new Color(0, 127, 127);
            }
            graphicsContext.setPaint(colour);
            graphicsContext.fill(shape);
        }
    }

    static class VertexFillPainter implements Transformer<JungRelationGraphVertex, Paint> {
	private String vertexFillPaint = "white";
	private String colourType = "white";
	private Color colour = Color.WHITE;
	private Color femaleColour = new Color(Integer.parseInt("ff7f97", 16));
	private Color maleColour = new Color(Integer.parseInt("6c9cd1", 16));
	private Color otherColour = colour;
	private Color childColour = new Color(Integer.parseInt("f9c05d", 16));
	private Color uniColour = Color.WHITE;
	private Map<String,String> kv;

	public VertexFillPainter(String vertexFillPaint) {
	    this.vertexFillPaint = vertexFillPaint;
	    StringTokenizer st = new StringTokenizer(vertexFillPaint, ",");
	    colourType = st.nextToken().trim();

	    if ("gender".equals(colourType)) {
		kv = getKeyValuePairs(st);
		try {
		    femaleColour = new Color(Integer.parseInt(kv.get("female"), 16));
		    maleColour = new Color(Integer.parseInt(kv.get("male"), 16));
		    otherColour = new Color(Integer.parseInt(kv.get("other"), 16));
		} catch (NumberFormatException e) {
		    LOG.error("Cannot create gender colour, key-value-pairs are not valid colour names or hex values: " + kv);
		}
	    } else if ("age".equals(colourType)) {
		kv = getKeyValuePairs(st);
		try {
		    childColour = new Color(Integer.parseInt(kv.get("children"), 16));
		    otherColour = new Color(Integer.parseInt(kv.get("other"), 16));
		} catch (NumberFormatException e) {
		    LOG.error("Cannot create age color, key-value-pair is not a valid color name or hex value: " + kv);
		}
	    } else {
		try {
		    uniColour = new Color(Integer.parseInt(colourType, 16));
		} catch (NumberFormatException e) {
		    LOG.error("Cannot create color, value is not a valid color name or hex value: " + colourType);
		}
	    }
	}

        @Override
	public Paint transform(JungRelationGraphVertex vertex) {
	    switch (colourType) {
	    case  "white" :
		colour = Color.WHITE;
		break;
	    case "gender" :
		if (vertex.subject() instanceof PersonSubject  && ((PersonSubject) vertex.subject()).getSex().toString().equals("female")) {
		    colour = femaleColour;
		} else if (vertex.subject() instanceof PersonSubject  && ((PersonSubject) vertex.subject()).getSex().toString().equals("male")) {
		    colour = maleColour;
		} else if (vertex.subject() instanceof PersonSubject) {
		    colour = otherColour;
		} else if (vertex.subject() instanceof OrgSubject) {
		    colour = uniColour;
		}
		break;
	    case "age" :
		if (vertex.subject() instanceof PersonSubject  && (((PersonSubject) vertex.subject()).getAge().toString().equals("child") || ((PersonSubject) vertex.subject()).getAge().toString().equals("infant"))) {
		    colour = childColour;
		} else if (vertex.subject() instanceof PersonSubject) {
		    colour = otherColour;
		} else if (vertex.subject() instanceof OrgSubject) {
		    colour = uniColour;
		}
		break;
	    default :
		colour = uniColour;
		break;
	    }
	    return colour;
	}
    }

    private static Map<String,String> getKeyValuePairs(StringTokenizer st) {
	final Map<String,String> kvMap = new HashMap<String,String>();
	while (st.hasMoreTokens()) {
	    String pair = st.nextToken().trim();
	    StringTokenizer stEq = new StringTokenizer(pair, "=");
	    if (stEq.hasMoreTokens()) {
		 String key = stEq.nextToken().trim();
		 String value = "";
		 if (stEq.hasMoreTokens()) {
		     value = stEq.nextToken().trim();
		 }
		 if (!"".equals(key) && !"".equals(value)) {
		     kvMap.put(key, value);
		 }
	    }
	}
	return kvMap;
    }

    static class EdgeStrokeRenderer implements Transformer<JungRelationGraphEdge, Stroke> {
        private final Stroke basic = new BasicStroke(1);
        private final Stroke basic2 = new BasicStroke(2);
        private final Stroke basic3 = new BasicStroke(3);
        private final Stroke basic4 = new BasicStroke(4);
        private final Stroke dashed = RenderContext.DASHED;
        private final Stroke dotted = RenderContext.DOTTED;

	private boolean useWeight = true;

	public EdgeStrokeRenderer(boolean useWeight) {
	    this.useWeight = useWeight;
	}

        @Override
	public Stroke transform(JungRelationGraphEdge edge) {
            if (edge.relation() instanceof WeightedRelation) {
		if (useWeight) {
		    if (((WeightedRelation)edge.relation()).getWeight() > 20) {
			return basic4;
		    } else if (((WeightedRelation)edge.relation()).getWeight() > 10) {
			return basic3;
		    } else if (((WeightedRelation)edge.relation()).getWeight() > 4) {
			return basic2;
		    } else if (((WeightedRelation)edge.relation()).getWeight() > 1) {
			return basic;
		    } else {
			return dotted;
		    }
		} else {
		    return basic;
		}
            } else {
                try {
                    LOG.debug("RelationType: " + edge.relation().getType() == null ? "failed" : edge.relation().getType());
                    if(edge.relation().getType().equals(RelationType.SOCIAL)) {
                        return basic;
                    } else if(edge.relation().getType().equals(RelationType.PERSONAL)) {
                        return dashed;
                    } else {
			return dotted;
                    }
                } catch (NullPointerException e) {
                    LOG.error("RelationType: " + edge.toString());
                    return dotted;
                }

            }
        }
    }
    
    private int numericId(RelationGraph.Vertex vertex) {
        Integer id = vertexIds.get(vertex);
        if (id == null) {
            id = vertexIds.size();
            vertexIds.put(vertex, id);
        }
        return id;
    }
}
