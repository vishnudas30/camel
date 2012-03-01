/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.builder;

import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;

/**
 * A builder when using the <a href="http://camel.apache.org/advicewith.html">advice with</a> feature.
 */
public class AdviceWithBuilder<T extends ProcessorDefinition> {

    private final AdviceWithRouteBuilder builder;
    private final String id;
    private final String toString;
    private final Class<T> type;
    private boolean selectFirst;
    private boolean selectLast;
    private int selectFrom = -1;
    private int selectTo = -1;

    public AdviceWithBuilder(AdviceWithRouteBuilder builder, String id, String toString, Class<T> type) {
        this.builder = builder;
        this.id = id;
        this.toString = toString;
        this.type = type;

        if (id == null && toString == null && type == null) {
            throw new IllegalArgumentException("Either id, toString or type must be specified");
        }
    }

    /**
     * Will only apply the first node matched.
     *
     * @return the builder to build the nodes.
     */
    public AdviceWithBuilder<T> selectFirst() {
        selectFirst = true;
        selectLast = false;
        return this;
    }

    /**
     * Will only apply the last node matched.
     *
     * @return the builder to build the nodes.
     */
    public AdviceWithBuilder<T> selectLast() {
        selectLast = true;
        selectFirst = false;
        return this;
    }

    /**
     * Will only apply the n'th node matched.
     *
     * @param index index of node to match (is 0-based)
     * @return the builder to build the nodes.
     */
    public AdviceWithBuilder<T> selectIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be a non negative number, was: " + index);
        }
        selectFrom = index;
        selectTo = index;
        return this;
    }

    /**
     * Will only apply the node in the index range matched.
     *
     * @param from from index of node to start matching (inclusive)
     * @param to to index of node to stop matching (inclusive)
     * @return the builder to build the nodes.
     */
    public AdviceWithBuilder<T> selectRange(int from, int to) {
        if (from < 0) {
            throw new IllegalArgumentException("From must be a non negative number, was: " + from);
        }
        if (from > to) {
            throw new IllegalArgumentException("From must be equal or lower than to. from: " + from + ", to: " + to);
        }
        selectFrom = from;
        selectTo = to;
        return this;
    }

    /**
     * Replaces the matched node(s) with the following nodes.
     *
     * @return the builder to build the nodes.
     */
    public ProcessorDefinition replace() {
        RouteDefinition route = builder.getOriginalRoute();
        PipelineDefinition answer = new PipelineDefinition();
        if (id != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.replaceById(route, id, answer, selectFirst, selectLast, selectFrom, selectTo));
        } else if (toString != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.replaceByToString(route, toString, answer, selectFirst, selectLast, selectFrom, selectTo));
        } else if (type != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.replaceByType(route, type, answer, selectFirst, selectLast, selectFrom, selectTo));
        }
        return answer;
    }

    /**
     * Removes the matched node(s)
     */
    public void remove() {
        RouteDefinition route = builder.getOriginalRoute();
        if (id != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.removeById(route, id, selectFirst, selectLast, selectFrom, selectTo));
        } else if (toString != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.removeByToString(route, toString, selectLast, selectFirst, selectFrom, selectTo));
        } else if (type != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.removeByType(route, type, selectFirst, selectFirst, selectFrom, selectTo));
        }
    }

    /**
     * Insert the following node(s) <b>before</b> the matched node(s)
     *
     * @return the builder to build the nodes.
     */
    public ProcessorDefinition before() {
        RouteDefinition route = builder.getOriginalRoute();
        PipelineDefinition answer = new PipelineDefinition();
        if (id != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.beforeById(route, id, answer, selectFirst, selectLast, selectFrom, selectTo));
        } else if (toString != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.beforeByToString(route, toString, answer, selectLast, selectFirst, selectFrom, selectTo));
        } else if (type != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.beforeByType(route, type, answer, selectFirst, selectLast, selectFrom, selectTo));
        }
        return answer;
    }

    /**
     * Insert the following node(s) <b>after</b> the matched node(s)
     *
     * @return the builder to build the nodes.
     */
    public ProcessorDefinition after() {
        RouteDefinition route = builder.getOriginalRoute();
        PipelineDefinition answer = new PipelineDefinition();
        if (id != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.afterById(route, id, answer, selectFirst, selectLast, selectFrom, selectTo));
        } else if (toString != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.afterByToString(route, toString, answer, selectLast, selectFirst, selectFrom, selectTo));
        } else if (type != null) {
            builder.getAdviceWithTasks().add(AdviceWithTasks.afterByType(route, type, answer, selectFirst, selectLast, selectFrom, selectTo));
        }
        return answer;
    }

}
