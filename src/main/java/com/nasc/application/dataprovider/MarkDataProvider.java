package com.nasc.application.dataprovider;

import com.nasc.application.data.core.MarksEntity;
import com.nasc.application.data.repository.MarksRepository;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.stream.Stream;

@SpringComponent
public class MarkDataProvider implements CallbackDataProvider.FetchCallback<MarksEntity, Void> {

    @Autowired
    MarksRepository repo;

    @Override
    public Stream<MarksEntity> fetch(Query<MarksEntity, Void> query) {
        return repo.findAll(PageRequest.of(query.getPage(),
                query.getPageSize())).stream();
    }

}