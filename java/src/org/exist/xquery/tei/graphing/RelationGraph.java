package org.exist.xquery.tei.graphing;

import java.util.List;
import java.util.Set;

/**
 * @author ljo
 */
public interface RelationGraph {
    Vertex getStart();
    
    Vertex getEnd();
    
    Vertex add(Subject subject);
    
    Edge connectDirected(Vertex from, Vertex to, Relation relation);

    Edge connectUndirected(Vertex from, Vertex to, Relation relation);

    Edge edgeBetween(Vertex a, Vertex b);

    Set<Edge> edgesBetween(Vertex a, Vertex b);

    List<Edge> edgePathBetween(Vertex a, Vertex b);

    Iterable<Vertex> vertices();

    Iterable<Vertex> vertices(Set<Relation> relations);

    List<Vertex> verticesList();

    int vertexCount();

    Iterable<Edge> edges();

    Iterable<Edge> edges(Set<Relation> relations);

    List<Edge> edgesList();

    int edgeCount();

    Set<Relation> relations();

    Set<Subject> subjects();

    /**
     * @author ljo
     */
    interface Edge {

        RelationGraph graph();

        Vertex from();

        Vertex to();

        boolean directed();

        void delete();

        Relation relation();
    }

    /**
     * @author ljo
     */
    interface Vertex {
        Iterable<? extends Edge> incoming();

        Iterable<? extends Edge> incoming(Set<Relation> relations);

        Iterable<? extends Edge> outgoing();

        Iterable<? extends Edge> outgoing(Set<Relation> relations);

        RelationGraph graph();

        void delete();

        Subject subject();

        Set<Relation> relations();

    }
}
