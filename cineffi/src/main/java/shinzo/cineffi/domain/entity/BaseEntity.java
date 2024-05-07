package shinzo.cineffi.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DialectOverride;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@SuperBuilder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public abstract class BaseEntity {

    @CreatedDate
    @Column(columnDefinition = "TIMESTAMP(3) WITHOUT TIME ZONE", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP(3) WITHOUT TIME ZONE")
    private LocalDateTime modifiedAt;

    @ColumnDefault("false")
    @Builder.Default
    private Boolean isDelete = false;

    protected void setIsDelete(boolean isDelete) { this.isDelete = isDelete; }
}
