package com.sabtok.process.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name="ACTIVITIES_PAGE", schema="z_dev_sabwiki")
public class PageActivity {

    @Id
    @Column(name="ACTIVITY_ID")
    private String activityId;

    @Column(name="PAGE_ID")
    private String pageId;

    @Column(name="ACTION")
    private String action;

    @Column(name="DATE")
    private String date;

    @Column(name="OLD_CONTENT")
    @Lob
    @JsonIgnore
    private String oldContent;

    @Column(name="NEW_CONTENT")
    @Lob
    @JsonIgnore
    private String newContent;

    @Column(name="UPADTE_CREATEd_BY")
    private String updateCreatedBy;

}
