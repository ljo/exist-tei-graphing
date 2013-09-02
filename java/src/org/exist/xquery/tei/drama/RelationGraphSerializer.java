package org.exist.xquery.tei.drama;

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
import java.util.regex.*;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

import org.apache.log4j.Logger;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.output.ByteArrayOutputStream;
//import org.apache.commons.lang.StringEscapeUtils;
//import org.apache.commons.lang.WordUtils;

import org.exist.collections.Collection;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DocumentImpl;
import org.exist.dom.QName;
//import org.exist.memtree.DocumentBuilderReceiver;
import org.exist.memtree.MemTreeBuilder;
import org.exist.memtree.NodeImpl;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.MimeType;
import org.exist.util.VirtualTempFile;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.*;
import org.exist.xquery.modules.ModuleUtils;
import org.exist.xquery.value.*;
import org.exist.xquery.tei.drama.jung.JungRelationGraph;
import org.exist.xquery.tei.drama.jung.JungRelationGraphEdge;
import org.exist.xquery.tei.drama.jung.JungRelationGraphVertex;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

/**
 * Create various RelationGraph presentations of the collated text witnesses.
 *
 * @author ljo
 */
public class RelationGraphSerializer {
    private final static Logger LOG = Logger.getLogger(RelationGraphSerializer.class);
    
    public static final String TEI_NS = "http://www.tei-c.org/ns/1.0";
    public static final String TEI_PREFIX = "tei";
    public static final String COLLATEX_NS = "http://interedition.eu/collatex/ns/1.0";
    public static final String COLLATEX_PREFIX = "cx";
    public static final String SVG_NS = "http://www.w3.org/2000/svg";
    public static final String SVG_PREFIX = "svg";
    public static final String SVG_ELEM = "svg";
    public static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    private static final String GRAPHML_PREFIX = "gml";
    private static final String GRAPHML_DOC_ELEM = "graphml";
    private static final String GRAPH_ID = "g0";
    private static final String GRAPH_ELEM = "graph";
    private static final String XMLNSXSI_ATT = "xmlns:xsi";
    private static final String XSISL_ATT = "xsi:schemaLocation";
    private static final String GRAPHML_XMLNSXSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String GRAPHML_XSISL = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    private static final String NODE_ELEM = "node";
    private static final String TARGET_ATT = "target";
    private static final String SOURCE_ATT = "source";
    private static final String EDGE_ELEM = "edge";
    private static final String EDGE_TYPE_PATH = "path";
    private static final String EDGE_TYPE_TRANSPOSITION = "transposition";
    private static final String EDGEDEFAULT_DEFAULT_VALUE = "directed";
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
    private static File dataDir = null;

    private XQueryContext context;

    private static RelationGraph relationGraph;
    private final Map<RelationGraph.Vertex, Integer> vertexIds = new HashMap();

    public RelationGraphSerializer(final XQueryContext context, RelationGraph relationGraph) {
        this.context = context;
        this.relationGraph = relationGraph;
    }
    
    
    public ValueSequence relationGraphReport(final String outputFormat) throws XPathException {
        ValueSequence result = new ValueSequence();
        if ("svg".equals(outputFormat)) {
            LOG.info(relationGraph.toString());
            result.add(toSvg((JungRelationGraph) relationGraph));
        } else {
            final MemTreeBuilder builder = context.getDocumentBuilder();
            builder.startDocument();
            if ("graphml".equals(outputFormat)) {
                toGraphML(builder);
            }
            result.add((NodeValue) builder.getDocument().getDocumentElement());
        }
        return result;
    }
    

    private void toGraphML(final MemTreeBuilder builder) {
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
            builder.startElement(new QName(NODE_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("n" + id));
            GraphMLProperty.NODE_NUMBER.write(Integer.toString(id), builder);
            GraphMLProperty.NODE_SUBJECTS.write(vertex.toString(), builder);
            builder.endElement();
        }
        
        int edgeNumber = 0;
        for (RelationGraph.Edge edge : relationGraph.edges()) {
            builder.startElement(new QName(EDGE_ELEM, GRAPHML_NS, GRAPHML_PREFIX), null);
            builder.addAttribute(new QName(ID_ATT, null, null), String.valueOf("e" + edgeNumber));
            builder.addAttribute(new QName(SOURCE_ATT, null, null), String.valueOf("n" + numericId(edge.from())));
            
            builder.addAttribute(new QName(TARGET_ATT, null, null), String.valueOf("n" + numericId(edge.to())));
            GraphMLProperty.EDGE_NUMBER.write(Integer.toString(edgeNumber++), builder);
            GraphMLProperty.EDGE_TYPE.write(EDGE_TYPE_PATH, builder);
            GraphMLProperty.EDGE_RELATION.write(Relation.getVerb(edge), builder);
            builder.endElement();
        }
        
        builder.endElement();
        builder.endElement();
    }

    private enum GraphMLProperty {
        NODE_NUMBER(NODE_ELEM, "number", "int"), //
        NODE_SUBJECTS(NODE_ELEM, "subjects", "string"), //
        EDGE_NUMBER(EDGE_ELEM, "number", "int"), //
        EDGE_TYPE(EDGE_ELEM, "type", "string"), //
        EDGE_RELATION(EDGE_ELEM, "relation", "string");
        
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

    public NodeValue toSvg(JungRelationGraph jvg) throws XPathException {
        Dimension dimension = new Dimension(960, 600);
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
	    
            // draw the graph in the SVG generator
            final VisualizationImageServer<JungRelationGraphVertex, JungRelationGraphEdge> server = createServer(jvg, dimension);
	    
            server.printAll(svgGenerator);
            BufferedImage image = (BufferedImage) server.getImage(new Point2D.Double(dimension.getWidth() / 2, dimension.getHeight() / 2), dimension);
            OutputStream os = System.out;
            //Writer out = new OutputStreamWriter(os, "UTF-8");
            Writer out = new StringWriter();
            svgGenerator.stream(out, true);
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
        createServer(final JungRelationGraph jvg, final Dimension dimension) {
        final Layout<JungRelationGraphVertex, JungRelationGraphEdge> layout = new FRLayout<JungRelationGraphVertex, JungRelationGraphEdge>(jvg);
        layout.setSize(dimension);
        final VisualizationImageServer<JungRelationGraphVertex, JungRelationGraphEdge> vis =
            new VisualizationImageServer<JungRelationGraphVertex, JungRelationGraphEdge>(layout, dimension);
        vis.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<JungRelationGraphVertex>());
        //vis.getRenderContext().setVertexLabelTransformer(new Transformer<JungRelationGraphVertex, String>() {
        //        @Override
        //            public String transform(JungRelationGraphVertex relationGraphVertexModel) {
        //                 return relationGraphVertexModel.toString();
        //      }
        //  });
	
        vis.getRenderContext().setVertexFillPaintTransformer(new Transformer<JungRelationGraphVertex, Paint>() {
                @Override
                    public Paint transform(JungRelationGraphVertex relationGraphVertexModel) {
                    return Color.WHITE;
                }
            });

        //vis.getRenderer().setVertexRenderer(new ShapeRenderer());
        Transformer<JungRelationGraphVertex, Shape> vertexShape = new Transformer<JungRelationGraphVertex, Shape>() {
            @Override
            public Shape transform(JungRelationGraphVertex vm) {
                int sl = vm.toString().length() * 6;
                
                return new Ellipse2D.Double(-25, -10, 50 + sl, 20 + (sl / 4));
            }
        };
        vis.getRenderContext().setVertexShapeTransformer(vertexShape);
        //vis.getRenderContext().setVertexShapeTransformer(new VertexLabelAsShapeRenderer<JungRelationGraphVertex, JungRelationGraphEdge>(vis.getRenderContext()));
        Transformer<JungRelationGraphVertex, Stroke> vertexStroke = new Transformer<JungRelationGraphVertex, Stroke>() {
            float dash[] = { 10.0f };
            public Stroke transform(JungRelationGraphVertex v) {
                if (v.subject() instanceof PersonSubject && ((PersonSubject)v.subject()).getType().toString().equals("noncast") ) {
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                                           BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                    
                } else {
                    return new BasicStroke(2);
                }

            }
        };
        vis.getRenderContext().setVertexStrokeTransformer(vertexStroke);
        //
        vis.getRenderContext().setEdgeLabelTransformer(new Transformer<JungRelationGraphEdge, String>() {
                @Override
                    public String transform(JungRelationGraphEdge relationGraphEdgeModel) {
                    return relationGraphEdgeModel.relation().getVerb();
                }
            });
        vis.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
        //vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

        vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vis.getRenderContext().setLabelOffset(15);

        return vis;
    }

    static class ShapeRenderer implements Renderer.Vertex<JungRelationGraphVertex, JungRelationGraphEdge> {
        @Override
            public void paintVertex(RenderContext<JungRelationGraphVertex, JungRelationGraphEdge> rc,
                                    Layout<JungRelationGraphVertex, JungRelationGraphEdge> layout, JungRelationGraphVertex vertex) {
            GraphicsDecorator graphicsContext = rc.getGraphicsContext();
            Point2D center = layout.transform(vertex);
            Shape shape = null;
            Color color = null;
            if(vertex.relations().equals("social")) {
                shape = new Rectangle((int)center.getX()-10, (int)center.getY()-10, 20, 20);
                color = new Color(127, 127, 0);
            } else if(vertex.relations().equals("personal")) {
                shape = new Rectangle((int)center.getX()-10, (int)center.getY()-20, 20, 40);
                color = new Color(127, 0, 127);
            } else {
                shape = new Ellipse2D.Double(center.getX()-10, center.getY()-10, 20, 20);
                color = new Color(0, 127, 127);
            }
            graphicsContext.setPaint(color);
            graphicsContext.fill(shape);
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
