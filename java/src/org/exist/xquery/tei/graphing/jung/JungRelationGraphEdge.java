package org.exist.xquery.tei.graphing.jung;

import java.util.Collections;
import java.util.Set;

import edu.uci.ics.jung.graph.util.EdgeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.exist.xquery.tei.graphing.RelationGraph;
import org.exist.xquery.tei.graphing.Relation;

/**
 * @author ljo
 */
public class JungRelationGraphEdge implements RelationGraph.Edge {
    private final static Logger LOG = LogManager.getLogger(JungRelationGraphEdge.class);
    final JungRelationGraph graph;
    final Relation relation;

    public JungRelationGraphEdge(JungRelationGraph graph, Relation relation) {
        this.graph = graph;
        this.relation = relation;
    }


    @Override
    public Relation relation() {
        return relation;
    }

    @Override
    public RelationGraph graph() {
        return graph;
    }

    @Override
    public RelationGraph.Vertex from() {
        return graph.getEndpoints(this).getFirst();
    }

    @Override
    public RelationGraph.Vertex to() {
        return graph.getEndpoints(this).getSecond();
    }

   @Override
   public boolean directed() {
       return EdgeType.valueOf("DIRECTED").equals(graph.getEdgeType(this));
    }

    @Override
    public void delete() {
        graph.removeEdge(this);
    }

    @Override
    public String toString() {
        return relation.toString();
    }
}
