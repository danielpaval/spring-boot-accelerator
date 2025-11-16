package com.example.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;

@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractCommonRevisionEntity<ID> extends DefaultRevisionEntity {

    @Column(name = "author")
    private ID author;

}
