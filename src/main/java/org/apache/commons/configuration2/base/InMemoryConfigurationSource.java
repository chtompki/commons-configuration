/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.base;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

/**
 * <p>
 * A specialized implementation of {@code HierarchicalConfigurationSource} that
 * operates on a structure of {@link ConfigurationNode} objects that are hold in
 * memory.
 * </p>
 * <p>
 * Implementation note: an {@code InMemoryConfigurationSource} can be queried
 * concurrently by multiple threads. However, if updates are performed, client
 * code must ensure proper synchronization.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class InMemoryConfigurationSource extends
        AbstractHierarchicalConfigurationSource<ConfigurationNode>
{
    /** Stores the root configuration node. */
    private volatile ConfigurationNode rootNode;

    /**
     * Creates a new instance of {@code InMemoryConfigurationSource}.
     */
    public InMemoryConfigurationSource()
    {
        super(new ConfigurationNodeHandler());
        rootNode = new DefaultConfigurationNode();
    }

    /**
     * Returns a reference to the root node.
     *
     * @return the root configuration node
     */
    public ConfigurationNode getRootNode()
    {
        return rootNode;
    }

    /**
     * Sets the root node for this configuration source. An {@code
     * InMemoryConfigurationSource} allows changing its root node. This will
     * change the whole content of the source.
     *
     * @param root the new root node (must not be <b>null</b>)
     * @throws IllegalArgumentException if the root node is <b>null</b>
     */
    @Override
    public void setRootNode(ConfigurationNode root)
    {
        if (root == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }

        rootNode = root;
    }
}
