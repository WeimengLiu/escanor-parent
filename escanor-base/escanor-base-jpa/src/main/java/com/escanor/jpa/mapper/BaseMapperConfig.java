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

package com.escanor.jpa.mapper;

import com.escanor.core.dto.BaseDto;
import com.escanor.jpa.entity.BaseEntity;
import org.mapstruct.*;

@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR, mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface BaseMapperConfig {

    @Mappings(value = {
            @Mapping(target = "createdBy", expression = "java(entity.getCreatedBy().orElse(null))"),
            @Mapping(target = "createdDate", expression = "java(entity.getOrignalCreatedDate())"),
            @Mapping(target = "lastModifiedBy", expression = "java(entity.getLastModifiedBy().orElse(null))"),
            @Mapping(target = "lastModifiedDate", expression = "java(entity.getOrignalLastModifiedDate())")
        }
    )
    BaseDto baseDto(BaseEntity entity);

    @Mappings({
            @Mapping(target = "orignalLastModifiedDate", ignore = true),
            @Mapping(target = "orignalCreatedDate", ignore = true)
    })
    BaseEntity baseEntity(BaseDto baseDto);
}
