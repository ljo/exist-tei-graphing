package org.exist.xquery.tei;

import java.util.List;
import java.util.Map;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;
import org.exist.xquery.tei.drama.*;

/**
 * An eDrama dynamic visualization module for eXist-db.
 *
 * @author ljo
 */
public class TEIDramaModule extends AbstractInternalModule {

    public final static String NAMESPACE_URI = "http://exist-db.org/xquery/tei-drama";
    public final static String PREFIX = "edrama";

    public final static FunctionDef[] functions = {
        new FunctionDef(Visualization.signatures[0], Visualization.class),
        new FunctionDef(Visualization.signatures[1], Visualization.class)
    };

    public TEIDramaModule(Map<String, List<? extends Object>> parameters) {
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
        return "eDrama module using visualization libraries e.g. batik, jung2 and jfree. " + 
	    "This makes it possible to produce svg and rasterized images " +
	    "for dynamic visualization.";
    }

    @Override
    public String getReleaseVersion() {
        return null;
    }
}
