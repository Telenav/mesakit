////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.geography.indexing.rtree;

import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;

/**
 * @author jonathanl (shibo)
 */
public interface RTreeSpatialIndexDebugger<T extends Bounded & Intersectable>
{
    static <T extends Bounded & Intersectable> RTreeSpatialIndexDebugger<T> nullDebugger()
    {
        return new RTreeSpatialIndexDebugger<>()
        {
            @Override
            public void remove(Node<T> node)
            {
            }

            @Override
            public void update(Node<T> tNode)
            {

            }

            @Override
            public void update(Leaf<T> leaf, T element)
            {

            }
        };
    }

    void remove(Node<T> node);

    void update(Node<T> node);

    void update(Leaf<T> leaf, T element);
}
