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

package com.telenav.mesakit.map.ui.debug.debuggers;

import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Leaf;
import com.telenav.mesakit.map.geography.indexing.rtree.Node;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndexDebugger;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.ui.debug.DebugViewer;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.LabeledMapShape;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapBox;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;
import com.telenav.mesakit.map.ui.desktop.viewer.InteractiveView;
import com.telenav.mesakit.map.ui.desktop.viewer.View;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerStyles.ACTIVE_BOX;
import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerStyles.INACTIVE_BOX;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.ACTIVE_LABEL;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.INACTIVE_LABEL;

/**
 * @author jonathanl (shibo)
 */
public class RTreeSpatialIndexVisualDebugger<T extends Bounded & Intersectable> implements RTreeSpatialIndexDebugger<T>
{
    /** View for debugging */
    private final transient View view;

    /** Identifier maps for debugging */
    private final transient Map<T, DrawableIdentifier> identifierForElement = new HashMap<>();

    private final transient Map<InteriorNode<T>, DrawableIdentifier> identifierForNode = new HashMap<>();

    private final transient Map<Leaf<T>, DrawableIdentifier> identifierForLeaf = new HashMap<>();

    public RTreeSpatialIndexVisualDebugger(final String title)
    {
        view = new DebugViewer().view(title);
    }

    @Override
    public void remove(final Node<T> node)
    {
        removeNodeFromView(node);
    }

    public void removeNodeFromView(final Node<T> node)
    {
        if (view != null)
        {
            view.remove(identifier(node));
            frameComplete();
        }
    }

    @Override
    public void update(final Leaf<T> leaf, final T element)
    {
        updateElementInView(leaf, element);
    }

    @Override
    public void update(final Node<T> node)
    {
        updateNodeInView(node);
    }

    public void updateElementInView(final Leaf<T> leaf, final T element)
    {
        if (view != null)
        {
            final var identifier = identifier(leaf, element);
            final var label = identifier + "-" + element.toString();

            final var box = MapBox.box()
                    .withStyle(ACTIVE_BOX)
                    .withLabelStyle(ACTIVE_LABEL)
                    .withRoundedLabelCorners(DrawingLength.pixels(10))
                    .withRectangle(element.bounds())
                    .withLabel(label);

            view.map(at -> at.withStyle(INACTIVE_BOX));
            view.update(identifier, box);
            view.pullToFront(identifier);
            frameComplete();
        }
    }

    public void updateNodeInView(final Node<T> node)
    {
        if (view != null)
        {
            final var identifier = identifier(node);

            final var box = MapBox.box()
                    .withStyle(ACTIVE_BOX)
                    .withLabelStyle(ACTIVE_LABEL)
                    .withRoundedLabelCorners(DrawingLength.pixels(10))
                    .withRectangle(node.bounds())
                    .withLabel(identifier.toString());

            view.map(at -> ((LabeledMapShape) at).withLabelStyle(INACTIVE_LABEL).withStyle(INACTIVE_BOX));
            view.update(identifier, box);
            view.pullToFront(identifier);
        }
        frameComplete();
    }

    private void frameComplete()
    {
        if (view instanceof InteractiveView)
        {
            ((InteractiveView) view).frameComplete();
        }
    }

    private DrawableIdentifier identifier(final InteriorNode<T> node)
    {
        var identifier = identifierForNode.get(node);
        if (identifier == null)
        {
            identifier = new DrawableIdentifier("Node-" + identifierForNode.size());
            identifierForNode.put(node, identifier);
        }
        return identifier;
    }

    private DrawableIdentifier identifier(final Leaf<T> leaf)
    {
        var identifier = identifierForLeaf.get(leaf);
        if (identifier == null)
        {
            identifier = new DrawableIdentifier("Leaf-" + identifierForLeaf.size());
            identifierForLeaf.put(leaf, identifier);
        }
        return identifier;
    }

    private DrawableIdentifier identifier(final Leaf<T> leaf, final T element)
    {
        var identifier = identifierForElement.get(element);
        if (identifier == null)
        {
            identifier = new DrawableIdentifier(identifier(leaf) + ":" + identifierForElement.size());
            identifierForElement.put(element, identifier);
        }
        return identifier;
    }

    private DrawableIdentifier identifier(final Node<T> node)
    {
        return node.isLeaf() ? identifier((Leaf<T>) node) : identifier((InteriorNode<T>) node);
    }
}
