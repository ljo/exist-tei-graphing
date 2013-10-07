package org.exist.xquery.tei.graphing;

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
    this.type = RelationType.fromString(type);
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

        public static RelationType fromString(final String string) {
            for (RelationType value : values()) {
                if (value.name.equals(string)) {
                    return value;
                }
            }
            return null;            
        }
    }
 
    public String getVerb() {
        return verb;
    }

    public static String getVerb(final RelationGraph.Edge edge) {
        return edge.relation().getVerb();
    }

    public RelationType getType() {
        return type;
    }

    public static String getType(final RelationGraph.Edge edge) {
        return edge.relation().getType().toString();
    }


    @Override
	public int compare(Relation o1, Relation o2) {
	return o1.getVerb().compareTo(o2.getVerb());
    }

}
