package org.exist.xquery.tei.graphing.jung;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.exist.xquery.tei.graphing.Relation;
import org.exist.xquery.tei.graphing.RelationGraph;

/**
 * @author ljo
 */
public class JungRelationGraphTraversal implements Iterable<RelationGraph.Vertex> {
    private final static Logger LOG = LogManager.getLogger(JungRelationGraphTraversal.class);
    private final RelationGraph relationGraph;
    private final Set<Relation> relations;

    public JungRelationGraphTraversal(RelationGraph relationGraph, Set<Relation> relations) {
        this.relationGraph = relationGraph;
        this.relations = relations;
    }

    public static JungRelationGraphTraversal of(RelationGraph relationGraph) {
        return new JungRelationGraphTraversal(relationGraph, null);
    }

    public static JungRelationGraphTraversal of(RelationGraph relationGraph, Set<Relation> relations) {
        return new JungRelationGraphTraversal(relationGraph, relations);
    }

    public Iterator<RelationGraph.Vertex> iterator() {
        Iterator <RelationGraph.Vertex> rgv = relationGraph.verticesList().iterator();
        return rgv;
    }

    public Iterable<RelationGraph.Edge> edges() {
        return relationGraph.edgesList();
    }
}
