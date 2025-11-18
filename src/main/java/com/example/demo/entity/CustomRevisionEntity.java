package com.example.demo.entity;

import com.example.common.entity.AbstractCommonRevisionEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;

@Entity
@Table(name = "revinfo")
@RevisionEntity(CustomRevisionListener.class)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class CustomRevisionEntity extends AbstractCommonRevisionEntity<Long> {

    @Override
    public boolean canEqual(final Object other) {
        return other instanceof CustomRevisionEntity;
    }
}
