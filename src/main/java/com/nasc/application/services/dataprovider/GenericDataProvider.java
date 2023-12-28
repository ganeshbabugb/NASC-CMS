package com.nasc.application.services.dataprovider;

import com.nasc.application.data.core.base.BaseEntity;
import com.nasc.application.services.base.BaseServiceClass;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

//Credits
//https://vaadin.com/forum/thread/18217660/here-s-a-generic-data-provider-for-the-crud-component
public class GenericDataProvider<T extends BaseEntity> extends AbstractBackEndDataProvider<T, CrudFilter> {
    private final Class<T> typeParameterClass;
    private final BaseServiceClass serviceClass;
    private final List<T> DATABASE;
    private Consumer<Long> sizeChangeListener;

    public GenericDataProvider(Class<T> typeParameterClass, BaseServiceClass serviceClass) {
        this.typeParameterClass = typeParameterClass;
        this.serviceClass = serviceClass;
        DATABASE = serviceClass.findAll();
    }

    public void persist(T item) {
        final Optional<T> existingItem = find(item.getId());
        if (existingItem.isPresent()) {
            int position = DATABASE.indexOf(existingItem.get());
            DATABASE.remove(existingItem.get());
            DATABASE.add(position, item);
        } else {
            DATABASE.add(item);
        }
        serviceClass.save(item);
    }

    public void delete(T item) {
        DATABASE.removeIf(entity -> entity.getId().equals(item.getId()));
        serviceClass.delete(item);
    }

    public void setSizeChangeListener(Consumer<Long> listener) {
        sizeChangeListener = listener;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, CrudFilter> query) {
        int offset = query.getOffset();
        int limit = query.getLimit();
        Stream<T> stream = DATABASE.stream();
        if (query.getFilter().isPresent())
            stream = stream.filter(predicate(query.getFilter().get())).sorted(comparator(query.getFilter().get()));
        return stream.skip(offset).limit(limit);
    }

    @Override
    protected int sizeInBackEnd(Query<T, CrudFilter> query) {
        long count = fetchFromBackEnd(query).count();
        if (sizeChangeListener != null)
            sizeChangeListener.accept(count);
        return (int) count;
    }

    private Predicate<T> predicate(CrudFilter filter) {
        return filter.getConstraints().entrySet().stream().map(constraint -> (Predicate<T>) obj -> {
            try {
                Object value = valueOf(constraint.getKey(), obj);
                return value != null && value.toString().toLowerCase().contains(constraint.getValue().toLowerCase());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }).reduce(Predicate::and).orElse(e -> true);
    }

    private Comparator<T> comparator(CrudFilter filter) {
        return filter.getSortOrders().entrySet().stream().map(sortClause -> {
            try {
                Comparator<T> comparator = Comparator.comparing(obj -> (Comparable) valueOf(sortClause.getKey(), obj));
                if (sortClause.getValue() == SortDirection.DESCENDING) {
                    comparator = comparator.reversed();
                }
                return comparator;
            } catch (Exception ex) {
                return (Comparator<T>) (o1, o2) -> 0;
            }
        }).reduce(Comparator::thenComparing).orElse((o1, o2) -> 0);
    }

    private Object valueOf(String fieldName, T obj) {
        try {
            Field field = typeParameterClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Optional<T> find(Long id) {
        return DATABASE.stream().filter(entity -> entity.getId().equals(id)).findFirst();
    }
}