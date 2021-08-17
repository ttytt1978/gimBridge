/*
 * RED5 Open Source Media Server - https://github.com/Red5/
 * 
 * Copyright 2006-2016 by respective authors (see below). All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.red5.server.messaging;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Event object corresponds to the connect/disconnect events among providers/consumers on pipes. 
 * This object is immutable except for the parameter map and tasks.
 * 
 * 该对象是一个围绕providers/consumers管道周围的 连接或断开连接事件，除了map和tasks对象是不可变的。
 * 
 * @author The Red5 Project
 * @author Steven Gong (steven.gong@gmail.com)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class PipeConnectionEvent extends EventObject {

    private static final long serialVersionUID = 9078843765378168072L;

    /** Pipe connection event type 
     * 管道连接事件类型
     * */
    public enum EventType {
        /** Provider connects in pull mode 
         *  提供者 拉 模式
         * */
        PROVIDER_CONNECT_PULL,
        /** Provider connects in push mode
         *  提供者 推 模式
         *  */
        PROVIDER_CONNECT_PUSH,
        /** Provider disconnects
         *  提供者断开连接
         *  */
        PROVIDER_DISCONNECT,
        /** Consumer connects in pull mode
         *  消费者  拉 模式
         *  */
        CONSUMER_CONNECT_PULL,
        /** Consumer connects in push mode
         * 消费者  推 模式
         *  */
        CONSUMER_CONNECT_PUSH,
        /** Consumer disconnects
         * 消费者断开连接 
         *  */
        CONSUMER_DISCONNECT
    };
 
    private final transient IProvider provider;
 
    private final transient IConsumer consumer;
 
    private final EventType type;
 
    private final ConcurrentMap<String, Object> paramMap = new ConcurrentHashMap<>();
 
    private final LinkedList<Runnable> taskList = new LinkedList<>();
 
    private PipeConnectionEvent(AbstractPipe source, EventType type, IConsumer consumer, Map<String, Object> paramMap) {
        super(source);
        this.type = type;
        this.consumer = consumer;
        this.provider = null;
        setParamMap(paramMap);
    }
 
    private PipeConnectionEvent(AbstractPipe source, EventType type, IProvider provider, Map<String, Object> paramMap) {
        super(source);
        this.type = type;
        this.consumer = null;
        this.provider = provider;
        setParamMap(paramMap);
    }
 
    public IProvider getProvider() {
        return provider;
    }
 
    public IConsumer getConsumer() {
        return consumer;
    }
 
    public EventType getType() {
        return type;
    }
 
    public Map<String, Object> getParamMap() {
        return paramMap;
    }
 
    public void setParamMap(Map<String, Object> paramMap) {
        if (paramMap != null && !paramMap.isEmpty()) {
            this.paramMap.putAll(paramMap);
        }
    } 
    
    public void addTask(Runnable task) {
        taskList.add(task);
    }
 
    List<Runnable> getTaskList() {
        return taskList;
    }
 
    public final static PipeConnectionEvent build(AbstractPipe source, EventType type, IConsumer consumer, Map<String, Object> paramMap) {
        return new PipeConnectionEvent(source, type, consumer, paramMap);
    }
 
    public final static PipeConnectionEvent build(AbstractPipe source, EventType type, IProvider provider, Map<String, Object> paramMap) {
        return new PipeConnectionEvent(source, type, provider, paramMap);
    }

}
