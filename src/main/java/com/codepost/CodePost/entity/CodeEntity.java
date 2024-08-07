package com.codepost.CodePost.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "codedetail")
@CompoundIndexes({
        @CompoundIndex(name = "cName_version_idx", def = "{'cName': 1, 'version': 1}", unique = true)
})
public class CodeEntity {

    private String cName;

    private Double version;

    private String startDate;
    private String endDate;
    private String description;
    private boolean isActive=false;

}
