package com.example.SinhVien5T.campaign.dto;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StandardDTO {

    private String publicId;

    private String name;

    private String description;

    private List<CriteriaDTO> criteriaDTOList;
}
