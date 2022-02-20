package com.skocz.capco.repository.entity;

import lombok.*;

import javax.persistence.*;


@Entity
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class UserFeature {
    @Id
    @GeneratedValue
    private Integer featureId;
    private String name;
    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
