package com.example.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"version"})
public abstract class AbstractCommonEntity<T extends Serializable> implements CommonEntity<T> {

    @Id
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    protected T id;

    @Version
    @Column(name = "version")
    private Integer version;

}

