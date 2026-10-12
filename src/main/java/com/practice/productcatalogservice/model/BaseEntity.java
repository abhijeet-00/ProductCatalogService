package com.practice.productcatalogservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class BaseEntity {
    private Long id;
    Date createdAt;
    Date updatedAt;
    State state;
}
