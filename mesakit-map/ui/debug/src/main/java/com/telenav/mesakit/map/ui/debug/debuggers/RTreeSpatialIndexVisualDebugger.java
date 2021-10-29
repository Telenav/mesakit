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

import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
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

import static com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength.pixels;
import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerStyles.ACTIVE_BOX;
import static com.telenav.mesakit.map.ui.debug.theme.DebugViewerStyles.INACTIVE_BOX;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.ACTIVE_LABEL;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.INACTIVE_LABEL;

/**
 * @author jonathanl (shibo)
 */
public class RTreeSpatialIndexVisualDebugger<T extends Bounded & Intersectable> extends BaseRepeater implements RTreeSpatialIndexDebugger<T>
{
    /** View for debugging */
    private final transient View view;

    /** Identifier maps for debugging */
    private final transient Map<T, DrawableIdentifier> identifierForElement = new HashMap<>();

    private final transient Map<InteriorNode<T>, DrawableIdentifier> identifierForNode = new HashMap<>();

    private final transient Map<Leaf<T>, DrawableIdentifier> identifierForLeaf = new HashMap<>();

    public RTreeSpatialIndexVisualDebugger(Listener listener, String title)
    {
        view = listener.listenTo(new DebugViewer()).view(title);
    }

    @Override
    public void remove(Node<T> node)
    {
        removeNodeFromView(node);
    }

    public void removeNodeFromView(Node<T> node)
    {
        if (view != null)
        {
            view.remove(identifier(node));
            frameComplete();
        }
    }

    @Override
    public void update(Leaf<T> leaf, T element)
    {
        updateView(leaf, element);
    }

    @Override
    public void update(Node<T> node)
    {
        updateView(node);
    }

    public void updateView(Node<T> node)
    {
        if (view != null)
        {
            var identifier = identifier(node);
            var label = identifier.toString();

            addToView(identifier, box(node, label));
        }
    }

    public void updateView(Leaf<T> leaf, T element)
    {
        if (view != null)
        {
            var identifier = identifier(leaf, element);
            var label = identifier + "-" + element.toString();

            addToView(identifier, box(leaf, label));
        }
    }

    private void addToView(DrawableIdentifier identifier, MapBox box)
    {
        // Make all existing labeled shapes inactive
        view.map(at -> ((LabeledMapShape) at)
                .withLabelStyle(INACTIVE_LABEL)
                .withStyle(INACTIVE_BOX));

        // then add the box to the view,
        view.update(identifier, box);

        // pull it to the front,
        view.pullToFront(identifier);

        // and we're done with the frame.
        frameComplete();
    }

    private MapBox box(Node<T> node, String label)
    {
        return MapBox.box()
                .withStyle(ACTIVE_BOX)
                .withLabelStyle(ACTIVE_LABEL)
                .withRoundedLabelCorners(pixels(10))
                .withRectangle(node.bounds())
                .withLabelText(label);
    }

    private void frameComplete()
    {
        if (view instanceof InteractiveView)
        {
            ((InteractiveView) view).frameComplete();
        }
    }

    private DrawableIdentifier identifier(InteriorNode<T> node)
    {
        var identifier = identifierForNode.get(node);
        if (identifier == null)
        {
            identifier = new DrawableIdentifier("Node-" + identifierForNode.size());
            identifierForNode.put(node, identifier);
        }
        return identifier;
    }

    private DrawableIdentifier identifier(Leaf<T> leaf)
    {
        var identifier = identifierForLeaf.get(leaf);
        if (identifier == null)
        {
            identifier = new DrawableIdentifier("Leaf-" + identifierForLeaf.size());
            identifierForLeaf.put(leaf, identifier);
        }
        return identifier;
    }

    private DrawableIdentifier identifier(Leaf<T> leaf, T element)
    {
        var identifier = identifierForElement.get(element);
        if (identifier == null)
        {
            identifier = new DrawableIdentifier(identifier(leaf) + ":" + identifierForElement.size());
            identifierForElement.put(element, identifier);
        }
        return identifier;
    }

    private DrawableIdentifier identifier(Node<T> node)
    {
        return node.isLeaf() ? identifier((Leaf<T>) node) : identifier((InteriorNode<T>) node);
    }
}
