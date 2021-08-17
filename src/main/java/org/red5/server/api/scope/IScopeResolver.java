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

package org.red5.server.api.scope;

/**
 * Resolve the scope from given a host and path. Resolver implementations depend on context naming strategy and so forth.
 * 从给定的主机和路径解析作用域。解析器实现依赖于上下文命名策略等等
 */
public interface IScopeResolver {
 
    public IGlobalScope getGlobalScope();
 
    public IScope resolveScope(String path);
 
    public IScope resolveScope(IScope root, String path);

}