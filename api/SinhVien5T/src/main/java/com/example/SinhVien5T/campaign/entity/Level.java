package com.example.SinhVien5T.campaign.entity;

public enum Level {
    UNIVERSITY,
    CITY,
    NATION,
    UNI_CITY,
    UNI_NATION,
    CITY_NATION,
    ALL;

    public boolean isMultiLevel() {
        return this == ALL || this == UNI_CITY || this == UNI_NATION || this == CITY_NATION;
    }

    public boolean supports(Level target) {
        if (this == ALL) return true;
        if (this == target) return true;
        if (this == UNI_CITY && (target == UNIVERSITY || target == CITY)) return true;
        if (this == UNI_NATION && (target == UNIVERSITY || target == NATION)) return true;
        if (this == CITY_NATION && (target == CITY || target == NATION)) return true;
        return false;
    }

    public Level getDefaultLevel() {
        if (this.supports(UNIVERSITY)) return UNIVERSITY;
        if (this.supports(CITY)) return CITY;
        if (this.supports(NATION)) return NATION;
        return this;
    }
}


