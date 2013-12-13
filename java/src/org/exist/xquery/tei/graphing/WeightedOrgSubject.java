package org.exist.xquery.tei.graphing;

import java.util.Comparator;

/**
 * @author ljo
 */
public class WeightedOrgSubject extends OrgSubject implements Comparator<OrgSubject> {
    private int weight = 1;

    public WeightedOrgSubject(String id) {
        super(id, "");
    }

    public WeightedOrgSubject(String id, int weight) {
        super(id, "");
        this.weight = weight;
    }

    public WeightedOrgSubject(String id, String name) {
        super(id, name);
    }

    public WeightedOrgSubject(String id, String name, int weight) {
        super(id, name);
        this.weight = weight;
    }

    public WeightedOrgSubject(String id, String name, String type) {
        super(id, name, SubjectType.fromString(type));
    }

    public WeightedOrgSubject(String id, String name, String type, int weight) {
        super(id, name, SubjectType.fromString(type));
        this.weight = weight;
    }

	public int getWeight() {
        return weight;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    @Override
        public String toString() {
        if ("".equals(getName())) {
            return getId();
        }
        return getName() + "/" + getWeight();
    } 

}
