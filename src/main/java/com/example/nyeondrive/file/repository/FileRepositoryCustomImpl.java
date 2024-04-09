package com.example.nyeondrive.file.repository;

import static com.example.nyeondrive.file.entity.QFile.file;

import com.example.nyeondrive.file.constant.FileOrderField;
import com.example.nyeondrive.file.dto.service.FileFilterDto;
import com.example.nyeondrive.file.dto.service.FileOrderDto;
import com.example.nyeondrive.file.dto.service.FilePagingDto;
import com.example.nyeondrive.file.entity.File;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
public class FileRepositoryCustomImpl implements FileRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private static final Map<FileOrderField, ComparableExpressionBase<?>> orderFieldMap = Map.of(
            FileOrderField.ID, file.id,
            FileOrderField.NAME, file.fileName.name,
            FileOrderField.EXTENSION, file.fileName.extension,
            FileOrderField.CONTENT_TYPE, file.contentType,
            FileOrderField.SIZE, file.size,
            FileOrderField.CREATED_AT, file.createdAt,
            FileOrderField.TRASHED, file.isTrashed
    );

    public FileRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<File> findAll(
            FileFilterDto fileFilterDto,
            FilePagingDto filePagingDto,
            List<FileOrderDto> fileOrderDtos
    ) {
        JPAQuery<File> query = queryFactory
                .select(file)
                .from(file)
                .where(
                        nameEq(fileFilterDto.name()),
                        parentIdEq(fileFilterDto.parentId()),
                        contentTypeEq(fileFilterDto.contentType()),
                        isTrashedEq(fileFilterDto.isTrashed())
                )
                .orderBy(getOrderSpecifiers(fileOrderDtos));
        if (filePagingDto.isEmpty()) {
            return query.fetch();
        }
        Pageable pageable = PageRequest.of(filePagingDto.page(), filePagingDto.size());
        return query
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

    private OrderSpecifier<?>[] getOrderSpecifiers(List<FileOrderDto> fileOrderDtos) {
        return fileOrderDtos.stream()
                .map(this::getOrderSpecifier)
                .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> getOrderSpecifier(FileOrderDto fileOrderDto) {
        Direction direction = Direction.fromString(fileOrderDto.direction());
        FileOrderField fileOrderField = FileOrderField.of(fileOrderDto.field());
        ComparableExpressionBase<?> comparableExpressionBase = orderFieldMap.get(fileOrderField);
        if (direction.isAscending()) {
            return comparableExpressionBase.asc();
        }
        return comparableExpressionBase.desc();
    }
}
