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

package org.red5.server;

import org.red5.server.api.IClient;
import org.red5.server.api.IClientRegistry;
import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.scope.IBasicScope;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.scope.IScopeHandler;
import org.red5.server.api.service.IServiceCall;
import org.red5.server.jmx.mxbeans.CoreHandlerMXBean;
import org.red5.server.net.IConnectionManager;
import org.red5.server.net.rtmp.RTMPConnManager;
import org.red5.server.net.rtmp.RTMPConnection;
import org.red5.server.net.rtmpt.RTMPTConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base IScopeHandler implementation
 */
public class CoreHandler implements IScopeHandler, CoreHandlerMXBean {

    protected static Logger log = LoggerFactory.getLogger(CoreHandler.class);
 
    public boolean addChildScope(IBasicScope scope) {
        return true;
    }
 
    public boolean connect(IConnection conn, IScope scope) {
        return connect(conn, scope, null);
    }
 
    public boolean connect(IConnection conn, IScope scope, Object[] params) {
        log.debug("connect - conn: {} scope: {}", conn, scope);
        // this is where we create the Client object that consolidates connections from a single client/FP. 
        // Now for more strangeness, I've only been looking at RTMPConnection derivatives, but it's setup() method
        // seems the only way that the session id is passed in to the newly established connection and this is currently *always* passed in
        // as null. I'm guessing that either the Flash Player passes some kind of unique id to us that is not being used, or that the idea
        // originally was to make our own session id, for example by combining client information with the IP address or something like that.
        boolean connect = false;
        // Get session id
        String id = conn.getSessionId();
        log.trace("Session id: {}", id);
        // Use client registry from scope the client connected to
        IScope connectionScope = conn.getScope();
        log.debug("Connection scope: {}", (connectionScope == null ? "is null" : "not null"));
        // 当作用域为空时，似乎会发生不好的事情，如果一个空作用域是可以的，那么
        // 需要删除此块-paul
        if (connectionScope == null) {
        	log.error("No connection scope was found");
        	return connect;
        }
        
        //获取连接作用域的客户端注册表
        IClientRegistry clientRegistry = connectionScope.getContext().getClientRegistry();
        log.debug("Client registry: {}", (clientRegistry == null ? "is null" : "not null"));
        if (clientRegistry == null) {
        	log.error("No client registry was found, clients cannot be looked-up or created");
        	return connect;
        } 
    	//按ID从注册表获取客户端或创建新客户端
        IClient client = clientRegistry.hasClient(id) ? clientRegistry.lookupClient(id) : clientRegistry.newClient(params);    
        
        /*IClient client = conn.getClient();
        if (client == null) {
            if (!clientRegistry.hasClient(id)) {
                if (conn instanceof RTMPTConnection) {
                    log.debug("Creating new client for RTMPT connection");
                    // create a new client using the session id as the client's id
                    client = new Client(id, (ClientRegistry) clientRegistry);
                    clientRegistry.addClient(client);
                    // set the client on the connection
                    conn.setClient(client);
                } else if (conn instanceof RTMPConnection) {
                    log.debug("Creating new client for RTMP connection");
                    // this is a new connection, create a new client to hold it
                    client = clientRegistry.newClient(params);
                    // set the client on the connection
                    conn.setClient(client);
                }
            } else {
                client = clientRegistry.lookupClient(id);
                conn.setClient(client);
            }
        } else {
            // set the client on the connection
            conn.setClient(client);
        }*/
        // add any rtmp connections to the manager
        IConnectionManager<RTMPConnection> connManager = RTMPConnManager.getInstance();
        if (conn instanceof RTMPTConnection) {
            connManager.setConnection((RTMPTConnection) conn);
        } else if (conn instanceof RTMPConnection) {
            connManager.setConnection((RTMPConnection) conn);
        } else {
            log.warn("Connection was not added to manager: {}", conn);
        }
        // assign connection to client
        conn.initialize(client);
        // we could checked for banned clients here
        connect = true;
        return connect;
    }
 
    public void disconnect(IConnection conn, IScope scope) {
        // do nothing here
    }
 
    public boolean join(IClient client, IScope scope) {
        return true;
    }
 
    public void leave(IClient client, IScope scope) {
        // do nothing here
    }
 
    public void removeChildScope(IBasicScope scope) {
        // do nothing here
    }
 
    public boolean serviceCall(IConnection conn, IServiceCall call) {
        final IContext context = conn.getScope().getContext();
        if (call.getServiceName() != null) {
            context.getServiceInvoker().invoke(call, context);
        } else {
            context.getServiceInvoker().invoke(call, conn.getScope().getHandler());
        }
        return true;
    }
 
    public boolean start(IScope scope) {
        return true;
    }
 
    public void stop(IScope scope) {
        // do nothing here
    }
 
    public boolean handleEvent(IEvent event) {
        return false;
    }

}
