package dorm.model;

public enum College {

    LAW("School of Law", "LAW"),
    HEALTH_SCIENCES("College of Health Sciences", "CHS"),
    CTBE_5KILO("College of Technology and Built Environment (5 Kilo)", "CTBE-5K"),
    CTBE_LIDETA("College of Technology and Built Environment (Lideta)", "CTBE-LD"),
    EDUCATION_LANGUAGE("College of Education and Language Studies", "CELS"),
    VETERINARY_AGRICULTURE("College of Veterinary Medicine & Agriculture", "CVMA"),
    NATURAL_COMPUTATIONAL("College of Natural and Computational Sciences", "CNCS"),
    BUSINESS_ECONOMICS("College of Business and Economics", "CBE"),
    SOCIAL_SCIENCES("College of Social Sciences, Art and Humanities", "CSSAH");

    private final String fullName;
    private final String acronym;

    College(String fullName, String acronym) {
        this.fullName = fullName;
        this.acronym = acronym;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAcronym() {
        return acronym;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
