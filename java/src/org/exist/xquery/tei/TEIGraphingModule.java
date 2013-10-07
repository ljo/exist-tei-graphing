package org.exist.xquery.tei;

import java.util.List;
import java.util.Map;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;
import org.exist.xquery.tei.graphing.*;

/**
 * A (TEI) Graphing dynamic visualization module for eXist-db.
 *
 * @author ljo
 */
public class TEIGraphingModule extends AbstractInternalModule {

    public final static String NAMESPACE_URI = "http://exist-db.org/xquery/tei-graphing";
    public final static String PREFIX = "graphing";

    public final static FunctionDef[] functions = {
        new FunctionDef(Visualization.signatures[0], Visualization.class),
        new FunctionDef(Visualization.signatures[1], Visualization.class)
    };

    public TEIGraphingModule(Map<String, List<? extends Object>> parameters) {
        super(functions, parameters, false);
    }

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }

    @Override
    public String getDescription() {
        return "(TEI) Graphing module using jung2 and batik libraries. " + 
	    "This makes it possible to produce SVG and GraphML graphs " +
	    "for dynamic visualization.";
    }

    @Override
    public String getReleaseVersion() {
        return null;
    }
}
