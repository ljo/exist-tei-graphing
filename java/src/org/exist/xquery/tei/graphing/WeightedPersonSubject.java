package org.exist.xquery.tei.graphing;

import java.util.Comparator;

/**
 * @author ljo
 */
public class WeightedPersonSubject extends PersonSubject implements Comparator<PersonSubject> {
    private int weight = 1;

    public WeightedPersonSubject(String id) {
        super(id, "", SubjectType.UNKNOWN, Sex.UNKNOWN, Age.UNKNOWN, "", RoleType.PERSON, false);
    }

    public WeightedPersonSubject(String id, int weight) {
        super(id, "", SubjectType.UNKNOWN, Sex.UNKNOWN, Age.UNKNOWN, "", RoleType.PERSON, false);
        this.weight = weight;
    }

    public WeightedPersonSubject(String id, String name) {
        super(id, name, SubjectType.UNKNOWN, Sex.UNKNOWN, Age.UNKNOWN, "", RoleType.PERSON, false);
    }

    public WeightedPersonSubject(String id, String name, int weight) {
        super(id, name, SubjectType.UNKNOWN, Sex.UNKNOWN, Age.UNKNOWN, "", RoleType.PERSON, false);
        this.weight = weight;
    }

    public WeightedPersonSubject(String id, String name, String type) {
        super(id, name, SubjectType.fromString(type), Sex.UNKNOWN, Age.UNKNOWN, "", RoleType.PERSON, false);
    }

    public WeightedPersonSubject(String id, String name, String type, int weight) {
        super(id, name, SubjectType.fromString(type), Sex.UNKNOWN, Age.UNKNOWN, "", RoleType.PERSON, false);
        this.weight = weight;
    }

    public WeightedPersonSubject(String id, String name, String type, String sex) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.UNKNOWN, "", RoleType.PERSON, false);
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, int weight) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.UNKNOWN, "", RoleType.PERSON, false);
        this.weight = weight;
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), "", RoleType.PERSON, false);
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, int weight) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), "", RoleType.PERSON, false);
        this.weight = weight;
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, String occupation) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, RoleType.PERSON, false);
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, String occupation, int weight) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, RoleType.PERSON, false);
        this.weight = weight;
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, String occupation, String role) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, RoleType.fromString(role), false);
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, String occupation, String role, int weight) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, RoleType.fromString(role), false);
        this.weight = weight;
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, String occupation, boolean isGroup) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, RoleType.PERSON, isGroup);
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, String occupation, String role, boolean isGroup) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, RoleType.fromString(role), isGroup);
    }

    public WeightedPersonSubject(String id, String name, String type, String sex, String age, String occupation, String role, boolean isGroup, int weight) {
        super(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, RoleType.fromString(role), isGroup);
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
