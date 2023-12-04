package com.nasc.application.services.base;

import java.util.List;

public abstract class BaseServiceClass<T> {
    public abstract List<T> findAll();

    public abstract void save(T item);

    public abstract void delete(T item);
}