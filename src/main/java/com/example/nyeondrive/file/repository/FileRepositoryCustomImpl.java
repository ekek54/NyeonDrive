package com.example.nyeondrive.file.repository;

import static com.example.nyeondrive.file.entity.QFile.file;
import static com.example.nyeondrive.file.entity.QFileClosure.fileClosure;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
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

    @Override
    public List<File> findAll(
            FileFilterDto fileFilterDto,
            FilePagingDto filePagingDto,
            List<FileOrderDto> fileOrderDtos,
            UUID userId) {
        JPAQuery<File> query = queryFactory
                .select(file)
                .from(file)
                .join(file.ancestorClosures)
                .fetchJoin()
                .where(
                        nameEq(fileFilterDto.name()),
                        contentTypeEq(fileFilterDto.contentType()),
                        isTrashedEq(fileFilterDto.isTrashed()),
                        parentIdEq(fileFilterDto.parentId()),
                        ownerIdEq(userId)
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

    private BooleanExpression ownerIdEq(UUID userId) {
        return userId != null ? file.ownerId.eq(userId) : null;
    }

    private BooleanExpression parentIdEq(Long parentId) {
        return parentId != null ?  fileClosure.ancestor.id.eq(parentId).and(depthEq(1L)): null;
    }

    private BooleanExpression depthEq(Long depth) {
        return depth != null ? fileClosure.depth.eq(depth) : null;
    }

    private BooleanExpression isTrashedEq(Boolean isTrashed) {
        return isTrashed != null ? file.isTrashed.eq(isTrashed) : null;
    }

    private BooleanExpression contentTypeEq(String contentType) {
        return contentType != null ? file.contentType.eq(contentType) : null;
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
