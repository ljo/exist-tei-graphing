package org.exist.xquery.tei.drama;

import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ljo
 */
public class Relation implements Comparator<Relation> {

    final String verb;
    final RelationType type;

    public Relation(String verb, String type) {
    this.verb = verb;
    this.type = RelationType.SOCIAL;
  }

    public enum RelationType {
        PERSONAL("personal"),
        SOCIAL("social"),
        OTHER("other");
        
        private String name;
        
        private RelationType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
 
    public String getVerb() {
        return verb;
    }

    public static String getVerb(final RelationGraph.Edge edge) {
        return edge.relation().getVerb();
    }

    public String getType() {
        return type.toString();
    }

    public static String getType(final RelationGraph.Edge edge) {
        return edge.relation().getType();
    }


    @Override
	public int compare(Relation o1, Relation o2) {
	return o1.getVerb().compareTo(o2.getVerb());
    }

}
