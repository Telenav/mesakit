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

package com.telenav.mesakit.map.ui.desktop.viewer.desktop;

import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.os.Console;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.thread.StateMachine;
import com.telenav.kivakit.core.time.Duration;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.network.core.Host;
import com.telenav.kivakit.network.http.HttpNetworkLocation;
import com.telenav.kivakit.ui.desktop.component.KivaKitPanel;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Box;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Label;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingSize;
import com.telenav.kivakit.ui.desktop.graphics.drawing.surfaces.java2d.Java2dDrawingSurface;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapScale;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections.SphericalMercatorMapProjection;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDrawable;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTile;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileCoordinateSystem;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileGrid;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileImageCache;
import com.telenav.mesakit.map.ui.desktop.tiles.ZoomLevel;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;
import com.telenav.mesakit.map.ui.desktop.viewer.InteractiveView;
import org.jetbrains.annotations.NotNull;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.function.Function;

import static com.telenav.kivakit.core.value.level.Percent.percent;
import static com.telenav.kivakit.interfaces.string.Stringable.Format.USER_LABEL;
import static com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingRectangle.pixels;
import static com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingRectangle.rectangle;
import static com.telenav.kivakit.ui.desktop.graphics.drawing.surfaces.java2d.Java2dDrawingSurface.surface;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.CAPTION;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.MAP_BACKGROUND;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.SELECTION_AREA;
import static com.telenav.mesakit.map.ui.desktop.tiles.SlippyTile.STANDARD_TILE_SIZE;
import static com.telenav.mesakit.map.ui.desktop.tiles.ZoomLevel.FURTHEST;
import static com.telenav.mesakit.map.ui.desktop.viewer.desktop.DesktopViewPanel.State.PAUSED;
import static com.telenav.mesakit.map.ui.desktop.viewer.desktop.DesktopViewPanel.State.RUNNING;
import static com.telenav.mesakit.map.ui.desktop.viewer.desktop.DesktopViewPanel.State.STEPPING;

/**
 * A Swing panel implementation of {@link InteractiveView}
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("GrazieInspection")
class DesktopViewPanel extends KivaKitPanel implements InteractiveView, MouseMotionListener, MouseListener, MouseWheelListener
{
    /**
     * The state of this interactive view panel
     */
    public enum State
    {
        RUNNING,
        PAUSED,
        STEPPING
    }

    /** Location of cursor when not dragging */
    private Location cursorAt;

    /** Delay between frames when running */
    private Duration delay = Duration.ZERO_DURATION;

    /** The point where dragging started when zooming or panning */
    private DrawingPoint dragStart;

    /** The drawing surface of the entire panel */
    private Java2dDrawingSurface drawingSurface;

    /** True if the map is currently zoomed in at all */
    private boolean isZoomedIn;

    /** The canvas to draw on */
    private MapCanvas mapCanvas;

    /** Translates between the view area and drawing coordinates */
    private MapProjection mapProjection;

    /** Cache of slippy tile images */
    private SlippyTileImageCache mapTileCache;

    /** The original coordinate mapper that was being used when panning started */
    private MapProjection panProjection;

    /** The original center point that was visible when panning started */
    private Location panStart;

    /** True when this component is ready to paint itself */
    private boolean readyToPaint;

    /** The state of the interactive view, {@link State#RUNNING}, {@link State#STEPPING} or {@link State#PAUSED} */
    private final StateMachine<State> state = new StateMachine<>(PAUSED);

    /** The view area being displayed */
    private Rectangle viewArea;

    /** The center of the current view */
    private Location viewCenter = Location.ORIGIN;

    /** Map of {@link MapDrawable} objects in the display */
    private final ViewModel viewModel = new ViewModel();

    /** The current zoom level */
    private ZoomLevel zoom = FURTHEST;

    /** When drawing a zoom rectangle, this is the current rectangle */
    private DrawingRectangle zoomSelection;

    /**
     * Construct
     */
    public DesktopViewPanel(Listener listener)
    {
        listener.listenTo(this);

        createMapTileCache();

        setDoubleBuffered(true);

        // If the panel is resized
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                zoomTo(viewCenter, zoom);
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
                zoomTo(viewCenter, zoom);
            }
        });

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                {
                    togglePause();
                }

                switch (e.getKeyChar())
                {
                    case 'p':
                        state.transitionTo(PAUSED);
                        break;

                    case 'r':
                        state.transitionTo(RUNNING);
                        break;

                    case 'n':
                        nextFrame();
                        break;

                    case 'f':
                        zoomToContents(percent(5));
                        break;

                    case '+':
                    case '=':
                    case 'i':
                        zoomIn();
                        break;

                    case '-':
                    case 'o':
                        zoomOut();
                        break;

                    case '<':
                    case 's':
                        slowDown();
                        break;

                    case '>':
                    case 'q':
                        speedUp();
                        break;
                }
            }
        });

        // Add mouse listeners
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addMouseListener(this);

        // Set keyboard focus to the panel
        requestFocus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(MapDrawable drawable)
    {
        viewModel.add(drawable);
        zoomToContents(percent(5));
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        viewModel.clear();
        requestRedraw();
    }

    @Override
    public void frameComplete()
    {
        if (state.is(STEPPING) || state.is(PAUSED))
        {
            state.transitionAndWaitForNot(PAUSED);
        }

        delay.sleep();
    }

    @Override
    public void frameSpeed(Duration delay)
    {
        this.delay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.awt.Dimension getPreferredSize()
    {
        // Default component size
        return new java.awt.Dimension(800, 500);
    }

    @Override
    public boolean isFocusable()
    {
        return true;
    }

    @Override
    public void map(Function<MapDrawable, MapDrawable> function)
    {
        viewModel.map(function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        // Select whatever was clicked on,
        viewModel.select(e.getPoint());

        // stop any dragging going on,
        cancelDrag();

        // and force a redraw
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(MouseEvent event)
    {
        Point2D point = event.getPoint();
        var dragPoint = DrawingPoint.point(drawingSurface, point.getX(), point.getY());

        // If we're dragging the mouse
        if (isDragging())
        {
            // and we are panning,
            if (isPanning())
            {
                // get the location we started dragging at,
                var start = panProjection.toMap(dragStart);

                // and the location we're at now,
                var at = panProjection.toMap(dragPoint);

                if (start != null && at != null)
                {
                    // and compute the offset we want apply to the original pan area
                    var offset = at.offsetTo(start);

                    // set the view to the original view we started panning at with the given offset
                    zoomTo(panStart.offsetBy(offset), zoom);
                }
            }
            else
            {
                // We're drawing a zoom selection rectangle, so get the width and height of it
                Console.println("dragPoint = " + dragPoint);
                Console.println("dragStart = " + dragStart);
                var width = dragPoint.x() - dragStart.x();
                Console.println("width = " + width);
                var height = heightForWidth(width);

                // If the selection is down and to the right
                if (width > 0)
                {
                    // set the zoom area down and to the right
                    zoomSelection = rectangle(drawingSurface, dragStart.x(), dragStart.y(), width, height);
                }
                else
                {
                    // for up and to the left selections, we have to invert the AWT rectangle
                    // because it cannot handle negative values
                    zoomSelection = rectangle(drawingSurface, dragStart.x() + width, dragStart.y() + height, -width, -height);
                }
            }
        }
        else
        {
            cursorAt = pointToLocation(dragPoint);
        }

        // Redraw
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
        mouseDragged(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        // Get the drawing surface point where the mouse is pressed,
        Point2D point = e.getPoint();
        var pressedAt = DrawingPoint.point(drawingSurface, point.getX(), point.getY());

        // project it to a map location,
        var pressedLocation = pointToLocation(pressedAt);
        Console.println("Location = $", pressedLocation);

        // and if that location is valid,
        if (pressedLocation != null)
        {
            // then the user clicked on the map, which potentially starts a drag operation.
            dragStart = pressedAt;
            Console.println("dragStart = " + dragStart);

            // If we're zoomed in and the control key is down,
            if (isZoomedIn && e.isControlDown())
            {
                // save the starting view center and projection for panning
                panStart = viewCenter;
                panProjection = mapProjection;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        // If a zoom area was selected
        if (zoomSelection != null)
        {
            // zoom to that rectangle
            var selected = mapCanvas.toMap(zoomSelection);
            if (selected != null)
            {
                trace("zooming to $ = $", zoomSelection, selected);
                zoom(selected);
            }
        }

        // We are no longer dragging, zooming or panning
        cancelDrag();
        requestRedraw();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
    }

    @Override
    public String name()
    {
        return super.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public synchronized void paint(Graphics uncast)
    {
        var graphics = (Graphics2D) uncast;

        // If we are ready to paint,
        if (readyToPaint)
        {
            // create a drawing surface for the entire panel,
            drawingSurface = createDrawingSurface(graphics);

            // create a canvas area to project the map onto
            mapCanvas = createMapCanvas(graphics);

            // clear the background,
            Box.box()
                    .withStyle(MAP_BACKGROUND)
                    .withArea(drawingSurface.drawingArea())
                    .draw(drawingSurface);

            // draw map tiles layer on canvas,
            var mapTiles = new SlippyTileGrid(this, viewArea(), zoom);
            mapTileCache.drawTiles(mapCanvas, mapTiles);
            if (isDebugOn())
            {
                mapTiles.drawTileOutlines(mapCanvas);
            }

            // draw viewables on top of tiles,
            viewModel.draw(mapCanvas);

            // and then, if we're drawing a zoom selection rectangle,
            if (zoomSelection != null)
            {
                // draw it on top.
                Box.box()
                        .withStyle(SELECTION_AREA)
                        .withArea(zoomSelection)
                        .draw(drawingSurface);
            }

            // If we're just moving around and we know where the cursor is,
            var margin = 10;
            if (!isDragging())
            {
                // draw the cursor's latitude and longitude in the lower right
                var style = CAPTION;
                var cursorText = cursorAt == null ? "" : " \u2503 " + cursorAt.asString(USER_LABEL);
                var text = state.at().name().toLowerCase()
                        + " \u2503 zoom level " + zoom.level()
                        + cursorText;
                var textSize = mapCanvas.textSize(style, text);
                Label.label()
                        .withLocation(drawingSurface.point(
                                getWidth() - textSize.widthInUnits() - margin * 3,
                                getHeight() - textSize.heightInUnits() - margin * 3))
                        .withStyle(style)
                        .withRoundedCorners(DrawingLength.pixels(10))
                        .withMargin(margin)
                        .withText(text)
                        .draw(drawingSurface);
            }

            // Show help message
            Label.label()
                    .withLocation(drawingSurface.point(margin, margin))
                    .withStyle(CAPTION)
                    .withMargin(margin)
                    .withRoundedCorners(DrawingLength.pixels(10))
                    .withText("(p)ause (r)un (n)ext"
                            + " \u2503 (s)lower (q)uicker"
                            + " \u2503 zoom (i)n (o)ut (f)it"
                            + " \u2503 map data \u00a9 openstreetmap contributors"
                    )
                    .draw(drawingSurface);
        }
    }

    @Override
    public void pullToFront(DrawableIdentifier identifier)
    {
        viewModel.pullToFront(identifier);
        requestRedraw();
    }

    @Override
    public void pushToBack(DrawableIdentifier identifier)
    {
        viewModel.pushToBack(identifier);
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(DrawableIdentifier identifier)
    {
        viewModel.remove(identifier);
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(DrawableIdentifier identifier, MapDrawable viewable)
    {
        viewModel.update(identifier, viewable);
        requestRedraw();
    }

    @Override
    public void zoomTo(Rectangle bounds)
    {
        zoom(bounds);
    }

    @Override
    public void zoomToContents(Percent margin)
    {
        zoomTo(viewModel.bounds().expanded(margin));
    }

    void nextFrame()
    {
        state.transitionTo(STEPPING);
    }

    void togglePause()
    {
        state.toggle(RUNNING, PAUSED);
    }

    /**
     * Dragging, zooming and panning are over
     */
    private void cancelDrag()
    {
        dragStart = null;
        zoomSelection = null;
        panStart = null;
        panProjection = null;
    }

    @NotNull
    private Java2dDrawingSurface createDrawingSurface(Graphics2D graphics)
    {
        return surface("drawing-surface", graphics, drawingSurfaceBounds());
    }

    private MapCanvas createMapCanvas(Graphics2D graphics)
    {
        // Create a map projection using this panel's drawing area in pixel coordinates,
        mapProjection = new SphericalMercatorMapProjection(viewArea(), drawingArea().size());

        // create a canvas using the projection
        return MapCanvas.canvas("map-canvas", graphics, MapScale.STATE, drawingArea(), mapProjection);
    }

    private void createMapTileCache()
    {
        mapTileCache = new SlippyTileImageCache(this, Maximum._256)
        {
            @Override
            public DrawingSize tileSize()
            {
                return STANDARD_TILE_SIZE;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected HttpNetworkLocation networkLocation(SlippyTile tile)
            {
                var x = tile.x();
                var y = tile.y();
                var z = tile.getZoomLevel().level();

                return new HttpNetworkLocation(Host.parseHost(this, "b.tile.openstreetmap.org")
                        .http()
                        .path(this, Strings.format("/${long}/${long}/${long}.png", z, x, y)));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected void onCacheUpdated()
            {
                requestRedraw();
            }
        };
    }

    private DrawingRectangle drawingArea()
    {
        // Get the size of the entire zoom level in drawing units,
        var size = zoom.sizeInDrawingUnits(STANDARD_TILE_SIZE);

        isZoomedIn = size.width().units() < getWidth() ||
                size.height().units() < getHeight();

        return size.centeredIn(drawingSurfaceBounds());
    }

    private DrawingRectangle drawingSurfaceBounds()
    {
        var bounds = getBounds();
        return pixels(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * @return The height for the given width fitting the aspect ratio of the window
     */
    private double heightForWidth(double width)
    {
        return getHeight() * width / getWidth();
    }

    /**
     * @return True while the mouse is being dragged
     */
    private boolean isDragging()
    {
        return dragStart != null;
    }

    /**
     * @return True if panning is going on
     */
    private boolean isPanning()
    {
        return panStart != null;
    }

    private Location pointToLocation(DrawingPoint point)
    {
        return mapCanvas.toMap(point);
    }

    private void requestRedraw()
    {
        repaint(10);
    }

    private void slowDown()
    {
        delay = delay.longerBy(percent(25));
        if (delay.isLessThan(Duration.seconds(0.01)))
        {
            delay = Duration.seconds(0.01);
        }
    }

    private void speedUp()
    {
        delay = delay.shorterBy(percent(25));
    }

    /**
     * @return The view area to display for the given center location and zoom level
     */
    private Rectangle viewArea(Location centerLocation,
                               ZoomLevel zoom)
    {
        var tileCoordinates = new SlippyTileCoordinateSystem(zoom);

        var center = tileCoordinates.toDrawing(centerLocation);
        trace("center = $", center);

        var dx = getWidth() / 2.0;
        var dy = getHeight() / 2.0;

        var drawingTopLeft = this.zoom.inRange(
                DrawingPoint.pixels(
                        center.x() - dx,
                        center.y() - dy), mapTileCache.tileSize());
        trace("topLeft = $", drawingTopLeft);

        var drawingBottomRight = this.zoom.inRange(
                DrawingPoint.pixels(
                        center.x() + dx,
                        center.y() + dy), mapTileCache.tileSize());
        trace("bottomRight = $", drawingBottomRight);

        var topLeft = tileCoordinates.toMap(drawingTopLeft);
        var bottomRight = tileCoordinates.toMap(drawingBottomRight);

        return Rectangle.fromLocations(topLeft, bottomRight);
    }

    private Rectangle viewArea()
    {
        return viewArea;
    }

    /**
     * @param bounds The bounds we'd ideally like to be viewing
     */
    private void zoom(Rectangle bounds)
    {
        zoomTo(bounds.center(), ZoomLevel.bestFit(drawingSurfaceBounds(), mapTileCache.tileSize(), bounds));
    }

    private void zoomIn()
    {
        zoomTo(viewArea().center(), zoom.zoomIn());
    }

    private void zoomOut()
    {
        zoomTo(viewArea().center(), zoom.zoomOut());
    }

    /**
     * Moves to the given location on the map at the given zoom level
     */
    private synchronized void zoomTo(Location center, ZoomLevel zoom)
    {
        // Save view center and zoom level,
        viewCenter = center;
        this.zoom = zoom;
        if (zoom.level() < 2)
        {
            viewCenter = Location.ORIGIN;
        }
        trace("center = " + center + ", zoom = " + zoom);

        // compute the view area to display,
        viewArea = viewArea(center, zoom);
        trace("viewArea = " + viewArea());

        // Request a repaint
        readyToPaint = true;
        requestRedraw();
    }
}
