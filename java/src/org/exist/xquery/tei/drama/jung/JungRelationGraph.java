package org.exist.xquery.tei.drama.jung;

import java.io.StringWriter;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import org.exist.xquery.tei.drama.Relation;
import org.exist.xquery.tei.drama.RelationGraph;
import org.exist.xquery.tei.drama.Subject;

/**
 * @author ljo
 */
public class JungRelationGraph extends SparseMultigraph<JungRelationGraphVertex, JungRelationGraphEdge> implements RelationGraph {

    JungRelationGraphVertex start;
    JungRelationGraphVertex end;

    public JungRelationGraph() {
        super();
    }

    @Override
        public Vertex getStart() {
        return start;
    }

    @Override
        public Vertex getEnd() {
        return end;
    }

    @Override
        public Iterable<Vertex> vertices() {
        return vertices(null);
    }

    @Override
        public Iterable<Vertex> vertices(Set<Relation> relations) {
        return JungRelationGraphTraversal.of(this, relations);
    }

    @Override
    public int vertexCount() {
        return getVertexCount();
    }

    @Override
        public Iterable<Edge> edges() {
        return edges(null);
    }

    @Override
        public Iterable<Edge> edges(Set<Relation> relations) {
        return JungRelationGraphTraversal.of(this, relations).edges();
    }

    @Override
    public int edgeCount() {
        return getEdgeCount();
    }

    @Override
        public Vertex add(Subject subject) {
        final JungRelationGraphVertex vertex = new JungRelationGraphVertex(this, subject);
        if (getVertexCount() == 0) {
            start = vertex;
        }
        end = vertex;

        addVertex(vertex);

        return vertex;
    }

    @Override
        public Edge connectDirected(Vertex from, Vertex to, Relation relation) {
        if (from.equals(to)) {
            throw new IllegalArgumentException("Edge must not be reflexive: " + from + " : " + to);
        }
        for (Edge edge : from.outgoing()) {
            if (to.equals(edge.to())) {
                return edge;
            }
        }
        
        final JungRelationGraphEdge edge = new JungRelationGraphEdge(this, relation);
        addEdge(edge, (JungRelationGraphVertex) from, (JungRelationGraphVertex) to, EdgeType.DIRECTED);
        return edge;
    }

    @Override
        public Edge connectUndirected(Vertex from, Vertex to, Relation relation) {
        if (from.equals(to)) {
            throw new IllegalArgumentException("Edge must not be reflexive: " + from + " : " + to);
        }
        for (Edge edge : from.outgoing()) {
            if (to.equals(edge.to())) {
                return edge;
            }
        }
        
        final JungRelationGraphEdge edge = new JungRelationGraphEdge(this, relation);
        addEdge(edge, (JungRelationGraphVertex) from, (JungRelationGraphVertex) to, EdgeType.UNDIRECTED);
        return edge;
    }

    @Override
        public Edge edgeBetween(Vertex a, Vertex b) {
        return findEdge((JungRelationGraphVertex) a, (JungRelationGraphVertex) b);
    }

    @Override
        public Set<Edge> edgesBetween(Vertex a, Vertex b) {
        return new HashSet(findEdgeSet((JungRelationGraphVertex) a, (JungRelationGraphVertex) b));
    }

    @Override
        public List<Edge> edgePathBetween(Vertex from, Vertex to) {
        DijkstraShortestPath<JungRelationGraphVertex, JungRelationGraphEdge> alg = new DijkstraShortestPath(this);
        List<JungRelationGraphEdge> path = alg.getPath((JungRelationGraphVertex)from, (JungRelationGraphVertex)to);
        return new ArrayList<Edge>(path);
    }

    @Override
        public Set<Relation> relations() {
        Set<Relation> relations = new HashSet();
        for (Edge edge : start.outgoing()) {
            relations.add(edge.relation());
        }
        return relations;
    }

    @Override
        public Set<Subject> subjects() {
        Set<Subject> subjects = new HashSet();
        for (Vertex vertex : vertices()) {
            subjects.add(vertex.subject());
        }
        return subjects;
    }

    @Override
        public String toString() {
        StringWriter writer = new StringWriter();
        for (Relation relation : relations()) {
            writer.append(relation.toString());
        }
        return writer.toString();
    }
}
