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

package com.escanor.jpa.audit;

import com.escanor.jpa.event.EntityChangeEvent;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

/**
 * 实体变更日志拦截器
 */
public class EntityChangeLogInterceptor extends EmptyInterceptor {

    private final EntityChangeLog entityChangeLog;

    /**
     * 构造函数
     *
     * @param entityChangeLog 实体变更日志
     */
    public EntityChangeLogInterceptor(EntityChangeLog entityChangeLog) {
        if (null == entityChangeLog) {
            throw new IllegalArgumentException("entityChangeLog can not be null");
        }
        this.entityChangeLog = entityChangeLog;
    }

    /**
     * 拦截 flush dirty 操作以记录实体更新事件。
     *
     * @param entity        实体对象
     * @param id            实体的标识符
     * @param currentState  实体的当前状态
     * @param previousState 实体的先前状态
     * @param propertyNames 实体属性的名称
     * @param types         实体属性的类型
     * @return 如果操作成功则返回 true，否则返回 false
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        try {
            entityChangeLog.log(EntityChangeEvent.builder().entity(entity).id(id).currentState(currentState).previousState(previousState).propertyNames(propertyNames).eventType(EntityChangeEvent.EventType.UPDATE).build());
        } catch (Exception ignore) {
        }

        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    /**
     * 拦截 save 操作以记录实体创建事件。
     *
     * @param entity        实体对象
     * @param id            实体的标识符
     * @param state         实体的状态
     * @param propertyNames 实体属性的名称
     * @param types         实体属性的类型
     * @return 如果操作成功则返回 true，否则返回 false
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        try {
            entityChangeLog.log(EntityChangeEvent.builder().entity(entity).id(id).currentState(state).propertyNames(propertyNames).eventType(EntityChangeEvent.EventType.CREATE).build());
        } catch (Exception ignore) {
        }
        return super.onSave(entity, id, state, propertyNames, types);
    }

    /**
     * 拦截 delete 操作以记录实体删除事件。
     *
     * @param entity        实体对象
     * @param id            实体的标识符
     * @param state         实体的状态
     * @param propertyNames 实体属性的名称
     * @param types         实体属性的类型
     */
    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        try {
            entityChangeLog.log(EntityChangeEvent.builder().entity(entity).id(id).currentState(state).propertyNames(propertyNames).eventType(EntityChangeEvent.EventType.DELETE).build());
        } catch (Exception ignore) {
        }
        super.onDelete(entity, id, state, propertyNames, types);
    }

}
