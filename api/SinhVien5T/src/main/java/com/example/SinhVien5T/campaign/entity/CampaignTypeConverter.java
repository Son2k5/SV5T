package com.example.SinhVien5T.campaign.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CampaignTypeConverter implements AttributeConverter<CampaignType, String> {

    @Override
    public String convertToDatabaseColumn(CampaignType attribute) {
        if (attribute == null) {
            return CampaignType.INDIVIDUAL.name();
        }
        return attribute.name();
    }

    @Override
    public CampaignType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return CampaignType.INDIVIDUAL;
        }
        String clean = dbData.trim().toUpperCase();
        if (clean.equals("1") || clean.equals("TRUE") || clean.equals("GROUP")) {
            return CampaignType.GROUP;
        }
        if (clean.equals("BOTH")) {
            return CampaignType.BOTH;
        }
        return CampaignType.INDIVIDUAL;
    }
}
