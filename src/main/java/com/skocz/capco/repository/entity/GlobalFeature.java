package com.skocz.capco.repository.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class GlobalFeature {
    @Id
    @GeneratedValue
    private Integer featureId;
    private String name;
    private boolean enabled;
}
