package com.example.nyeondrive.repository;

import static com.example.nyeondrive.entity.QFile.file;

import com.example.nyeondrive.dto.service.FileFilterDto;
import com.example.nyeondrive.dto.service.FilePagingDto;
import com.example.nyeondrive.entity.File;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FileRepositoryCustomImpl implements FileRepositoryCustom {
    JPAQueryFactory queryFactory;

    public FileRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<File> findAllWithFilterAndPaging(FileFilterDto fileFilterDto, FilePagingDto filePagingDto) {
        Pageable pageable = PageRequest.of(filePagingDto.page(), filePagingDto.size());
        return queryFactory
                .select(file)
                .from(file)
                .where(
                        nameEq(fileFilterDto.name()),
                        parentIdEq(fileFilterDto.parentId()),
                        contentTypeEq(fileFilterDto.contentType()),
                        isTrashedEq(fileFilterDto.isTrashed())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression isTrashedEq(Boolean isTrashed) {
        return isTrashed != null ? file.isTrashed.eq(isTrashed) : null;
    }

    private BooleanExpression contentTypeEq(String contentType) {
        return contentType != null ? file.contentType.eq(contentType) : null;
    }

    private BooleanExpression parentIdEq(Long parentId) {
        return parentId != null ? file.parent.id.eq(parentId) : null;
    }

    private BooleanExpression nameEq(String name) {
        return name != null ? file.fileName.name.eq(name) : null;
    }
}
