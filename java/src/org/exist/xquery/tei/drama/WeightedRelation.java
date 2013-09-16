package org.exist.xquery.tei.drama;

import java.util.Comparator;

import org.exist.xquery.tei.drama.Relation.RelationType;

/**
 * @author ljo
 */
public class WeightedRelation extends Relation implements Comparator<Relation> {

    final int weight;

    public WeightedRelation(String verb, String type, int weight) {
        super(verb, type);
        this.weight = weight;
    }

    public int getWeight() {
        return weight; 
    }

    @Override
        public String toString() {
        return getVerb() + "/" + weight;
    }

}
