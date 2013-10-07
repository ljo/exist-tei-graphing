package org.exist.xquery.tei.graphing;

import java.util.Comparator;

/**
 * @author ljo
 */
public class PersonSubject implements Subject, Comparator<PersonSubject> {

    private Relation relation;

    private final String id;
    private String name;
    private SubjectType type;
    private Sex sex;
    private String sexString;
    private Age age;
    private String ageString;
    private String occupation;
    private boolean isGroup = false;

    public PersonSubject(String id) {
	this(id, "", SubjectType.UNKNOWN, Sex.UNKNOWN, Age.UNKNOWN, "", false);
    }

    public PersonSubject(String id, String name) {
	this(id, name, SubjectType.UNKNOWN, Sex.UNKNOWN, Age.UNKNOWN, "", false);
    }


    public PersonSubject(String id, String name, String type) {
        this(id, name, SubjectType.fromString(type), Sex.UNKNOWN, Age.UNKNOWN, "", false);
    }
    public PersonSubject(String id, String name, String type, String sex) {
        this(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.UNKNOWN, "", false);
    }

    public PersonSubject(String id, String name, String type, String sex, String age) {
        this(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), "", false);
    }

    public PersonSubject(String id, String name, String type, String sex, String age, String occupation) {
        this(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, false);
    }

    public PersonSubject(String id, String name, String type, String sex, String age, String occupation, boolean isGroup) {
        this(id, name, SubjectType.fromString(type), Sex.fromString(sex), Age.fromString(age), occupation, isGroup);
    }

    public PersonSubject(String id, String name, SubjectType type, Sex sex, Age age, String occupation, boolean isGroup) {
	this.id = id;
	this.name = name;
	this.type = type;
	this.sex = sex;
	this.age = age;
	this.occupation = occupation;
	this.isGroup = isGroup;
    }

    public enum Sex {
        UNKNOWN("unknown"),
        MALE("male"),
        FEMALE("female"),
        NOT_APPLICABLE("not applicable"),
        MIXED("mixed");

        private String name;
        
        private Sex(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static Sex fromString(String string) {
            for (Sex value : values()) {
                if (value.name.equals(string)) {
                    return value;
                }
            }
            return null;            
        }
    }
    public enum Age {
        UNKNOWN("unknown"),
        INFANT("infant"),
        CHILD("child"),
        ADULT("adult"),
        MIXED("mixed");

        private String name;
        
        private Age(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static Age fromString(String string) {
            for (Age value : values()) {
                if (value.name.equals(string)) {
                    return value;
                }
            }
            return null;            
        }
    }

    public enum SubjectType {
        UNKNOWN("unknown"),
        CAST("cast"),
        NONCAST("noncast"),
        EXTERNAL("external"),
        FICTIONAL("fictional"),
        PET("pet");

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
    
    public Sex getSex() {
        return sex;
    }

    public void setSex(final Sex sex) {
	this.sex = sex;
    }

    public void setSex(final Sex sex, final String sexString) {
	this.sex = sex;
	this.sexString = sexString;
    }

    public Age getAge() {
	return age;
    }

    public void setAge(final Age age) {
	this.age = age;
    }

    public void setAge(final Age age, final String ageString) {
	this.age = age;
	this.ageString = ageString;
    }

    public String getOccupation() {
	return occupation;
    }
    
    public void setOccupation(final String occupation) {
	this.occupation = occupation;
    }
    
    public boolean isGroup() {
	return isGroup;
    }

    public void setIsGroup(final boolean isGroup) {
	this.isGroup = isGroup;
    }

    @Override
    public String toString() {
	if ("".equals(name)) {
	    return id;
	}
	return name;
    } 

    @Override
	public int compare(PersonSubject o1, PersonSubject o2) {
	return o1.getId().compareTo(o2.getId());
    }
}
