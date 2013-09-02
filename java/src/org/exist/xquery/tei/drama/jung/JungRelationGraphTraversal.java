package org.exist.xquery.tei.drama.jung;

import java.util.ArrayDeque;
import static java.util.Collections.singleton;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.exist.xquery.tei.drama.Relation;
import org.exist.xquery.tei.drama.RelationGraph;

/**
 * @author ljo
 */
public class JungRelationGraphTraversal implements Iterable<RelationGraph.Vertex> {
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
        Iterator<RelationGraph.Vertex> rgv = relationGraph.vertices().iterator();
        return rgv; 
    }

    public Iterable<RelationGraph.Edge> edges() {
        return relationGraph.edges();
    }
}
