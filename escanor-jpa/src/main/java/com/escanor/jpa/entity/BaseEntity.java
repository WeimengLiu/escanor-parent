/*
 * Copyright (c) 2024 Weimeng Liu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.escanor.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Auditable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Data
@MappedSuperclass
public abstract class BaseEntity implements Auditable<String, Integer, LocalDateTime>, Serializable, Persistable<Integer> {
    private static final long serialVersionUID = 141481953116476181L;

    @Id
    private Integer id;

    private @Nullable String createdBy;

    @Temporal(TemporalType.TIMESTAMP) //
    private @Nullable Date createdDate;

    private @Nullable String lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP) //
    private @Nullable Date lastModifiedDate;

    @Nullable
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id of the entity.
     *
     * @param id the id to set
     */
    protected void setId(@Nullable Integer id) {
        this.id = id;
    }

    /**
     * Must be {@link Transient} in order to ensure that no JPA provider complains because of a missing setter.
     *
     * @see org.springframework.data.domain.Persistable#isNew()
     */
    @Transient // DATAJPA-622
    @Override
    public boolean isNew() {
        return null == getId();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!getClass().equals(ProxyUtils.getUserClass(obj))) {
            return false;
        }

        Persistable<?> that = (Persistable<?>) obj;

        return null != this.getId() && this.getId().equals(that.getId());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#getCreatedBy()
     */
    @Override
    public Optional<String> getCreatedBy() {
        return Optional.ofNullable(createdBy);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#setCreatedBy(java.lang.Object)
     */
    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#getCreatedDate()
     */
    @Override
    public Optional<LocalDateTime> getCreatedDate() {
        return null == createdDate ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(createdDate.toInstant(), ZoneId.systemDefault()));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#setCreatedDate(java.time.temporal.TemporalAccessor)
     */
    @Override
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#getLastModifiedBy()
     */
    @Override
    public Optional<String> getLastModifiedBy() {
        return Optional.ofNullable(lastModifiedBy);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#setLastModifiedBy(java.lang.Object)
     */
    @Override
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#getLastModifiedDate()
     */
    @Override
    public Optional<LocalDateTime> getLastModifiedDate() {
        return null == lastModifiedDate ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(lastModifiedDate.toInstant(), ZoneId.systemDefault()));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Auditable#setLastModifiedDate(java.time.temporal.TemporalAccessor)
     */
    @Override
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = Date.from(lastModifiedDate.atZone(ZoneId.systemDefault()).toInstant());
    }
}
