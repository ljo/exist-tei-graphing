package org.exist.xquery.tei.graphing.jung;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.exist.xquery.tei.graphing.RelationGraph;
import org.exist.xquery.tei.graphing.Relation;
import org.exist.xquery.tei.graphing.Subject;

/**
 * @author ljo
 */
public class JungRelationGraphVertex implements RelationGraph.Vertex {
    private final static Logger LOG = LogManager.getLogger(JungRelationGraphVertex.class);
    private final JungRelationGraph graph;
    private final Subject subject;

    public JungRelationGraphVertex(JungRelationGraph graph, Subject subject) {
        this.graph = graph;
        this.subject = subject;
    }

    @Override
    public Set<? extends RelationGraph.Edge> incoming() {
        return incoming(null);
    }

    @Override
    public Set<? extends RelationGraph.Edge> incoming(final Set<Relation> relations) {
        return paths(graph.getInEdges(this), relations);
    }

    @Override
    public Set<? extends RelationGraph.Edge> outgoing() {
        return outgoing(null);
    }

    @Override
    public Set<? extends RelationGraph.Edge> outgoing(Set<Relation> relations) {
        return paths(graph.getOutEdges(this), relations);
    }

    @Override
    public Set<Relation> relations() {
        final Set<Relation> relations = new HashSet();
        for (RelationGraph.Edge edge : incoming()) {
            relations.add(edge.relation());
        }
	for (RelationGraph.Edge edge : outgoing()) {
            relations.add(edge.relation());
        }
        return relations;
    }

    @Override
    public RelationGraph graph() {
        return graph;
    }

    @Override
    public void delete() {
        graph.removeVertex(this);
    }

    @Override
    public Subject subject() {
        return subject;
    }

    @Override
    public String toString() {
        return subject.toString();
    }

    protected static Set<? extends RelationGraph.Edge> paths(final Iterable<JungRelationGraphEdge> edges, final Set<Relation> relations) {
        Set<RelationGraph.Edge> hs = new HashSet();
        for (RelationGraph.Edge edge : edges) {
	    if (relations == null) {
                 hs.add(edge);
	    } else if (relations.contains(edge.relation())) {
                 hs.add(edge);
            }
        }
        return hs;
    }
}
