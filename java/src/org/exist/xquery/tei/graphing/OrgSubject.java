package org.exist.xquery.tei.graphing;

import java.util.Comparator;

/**
 * @author ljo
 */
public class OrgSubject implements Subject, Comparator<OrgSubject> {

    private Relation relation;

    private final String id;
    private String name;
    private SubjectType type;
    private boolean isGroup = true;

    public OrgSubject(String id) {
        this(id, "", SubjectType.UNKNOWN);
    }

    public OrgSubject(String id, String name) {
        this(id, name, SubjectType.UNKNOWN);
    }
    
    
    public OrgSubject(String id, String name, String type) {
        this(id, name, SubjectType.fromString(type));
    }
    
    public OrgSubject(String id, String name, SubjectType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    
    public enum SubjectType {
        UNKNOWN("unknown"),
        CAST("cast"),
        NONCAST("noncast"),
        EXTERNAL("external"),
        FICTIONAL("fictional");

        private String name;
        
        private SubjectType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
        
        public static SubjectType fromString(final String string) {
            for (SubjectType value : values()) {
                if (value.name.equals(string)) {
                    return value;
                }
            }
            return null;        
        }
    }
    
    @Override
	public Relation getRelation() {
	return relation;
    }

    public void setRelation(final Relation relation) {
	this.relation = relation;
    }

    public String getId() {
	return id;
    }
    
    public String getName() {
	return name;
    }
    
    public void setName(final String name) {
	this.name = name;
    }

    public SubjectType getType() {
        return type;
    }

    public void setType(final SubjectType type) {
        this.type = type;
    }
    
    public boolean isGroup() {
	return isGroup;
    }

    @Override
    public String toString() {
	if ("".equals(name)) {
	    return id;
	}
	return name;
    } 

    @Override
	public int compare(OrgSubject o1, OrgSubject o2) {
	return o1.getId().compareTo(o2.getId());
    }
}
