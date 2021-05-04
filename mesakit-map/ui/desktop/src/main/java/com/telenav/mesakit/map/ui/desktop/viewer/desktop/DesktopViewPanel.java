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

import com.telenav.kivakit.core.kernel.language.threading.conditions.StateMachine;
import com.telenav.kivakit.core.kernel.language.time.Duration;
import com.telenav.kivakit.core.kernel.language.values.count.Maximum;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.messaging.Message;
import com.telenav.kivakit.core.network.core.Host;
import com.telenav.kivakit.core.network.http.HttpNetworkLocation;
import com.telenav.kivakit.ui.desktop.component.KivaKitPanel;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Label;
import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Point;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Rectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Size;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapScale;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections.CartesianMapProjection;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDrawable;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTile;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileCoordinateSystem;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileGrid;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileImageCache;
import com.telenav.mesakit.map.ui.desktop.tiles.ZoomLevel;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;
import com.telenav.mesakit.map.ui.desktop.viewer.InteractiveView;

import java.awt.Dimension;
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
import java.util.function.Function;

import static com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat.USER_LABEL;
import static com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Rectangle.rectangle;
import static com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Size.pixels;
import static com.telenav.kivakit.ui.desktop.theme.KivaKitColors.DARK_TANGERINE;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.CAPTION;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.MAP_BACKGROUND;
import static com.telenav.mesakit.map.ui.desktop.tiles.SlippyTile.STANDARD_TILE_SIZE;
import static com.telenav.mesakit.map.ui.desktop.tiles.ZoomLevel.FURTHEST;
import static com.telenav.mesakit.map.ui.desktop.viewer.desktop.DesktopViewPanel.State.PAUSED;
import static com.telenav.mesakit.map.ui.desktop.viewer.desktop.DesktopViewPanel.State.RUNNING;
import static com.telenav.mesakit.map.ui.desktop.viewer.desktop.DesktopViewPanel.State.STEPPING;

/**
 * A JPanel implementation of {@link InteractiveView}
 *
 * @author jonathanl (shibo)
 */
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

    /** Map of {@link MapDrawable} objects in the display */
    private final ViewModel viewModel = new ViewModel();

    /** The center of the current view */
    private Location viewCenter = Location.ORIGIN;

    /** The view area being displayed */
    private com.telenav.mesakit.map.geography.shape.rectangle.Rectangle viewArea;

    /** The canvas to draw on */
    private MapCanvas canvas;

    /** Location of cursor when not dragging */
    private Location cursorAt;

    /** Translates between the view area and drawing coordinates */
    private MapProjection projection;

    /** Grid of slippy tiles */
    private SlippyTileGrid mapTiles;

    /** True when this component is ready to paint itself */
    private boolean readyToPaint;

    /** The current zoom level */
    private ZoomLevel zoom = FURTHEST;

    /** When drawing a zoom rectangle, this is the current rectangle */
    private Rectangle zoomSelection;

    /** The point where dragging started when zooming or panning */
    private Point dragStart;

    /** The original center point that was visible when panning started */
    private Location panStart;

    /** The original coordinate mapper that was being used when panning started */
    private MapProjection panProjection;

    /** True if the map is currently zoomed in at all */
    private boolean isZoomedIn;

    /** The state of the interactive view, {@link State#RUNNING}, {@link State#STEPPING} or {@link State#PAUSED} */
    private final StateMachine<State> state = new StateMachine<>(PAUSED);

    /** Delay between frames when running */
    private Duration delay = Duration.NONE;

    /** Cache of slippy tile images */
    private final SlippyTileImageCache mapTileCache = listenTo(new SlippyTileImageCache(Maximum._256)
    {
        @Override
        public Size tileSize()
        {
            return STANDARD_TILE_SIZE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected HttpNetworkLocation networkLocation(final SlippyTile tile)
        {
            final var x = tile.x();
            final var y = tile.y();
            final var z = tile.getZoomLevel().level();

            return new HttpNetworkLocation(Host.parse("b.tile.openstreetmap.org")
                    .http()
                    .path(Message.format("/${long}/${long}/${long}.png", z, x, y)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onCacheUpdated()
        {
            requestRedraw();
        }
    });

    /**
     * Construct
     */
    public DesktopViewPanel()
    {
        setDoubleBuffered(true);

        // If the panel is resized
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(final ComponentEvent e)
            {
                zoomTo(viewCenter, zoom);
            }

            @Override
            public void componentShown(final ComponentEvent e)
            {
                zoomTo(viewCenter, zoom);
            }
        });

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(final KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                {
                    togglePause();
                }

                switch (e.getKeyChar())
                {
                    case 'p':
                        state.transition(PAUSED);
                        break;

                    case 'r':
                        state.transition(RUNNING);
                        break;

                    case 'n':
                        nextFrame();
                        break;

                    case 'f':
                        zoomToContents(Percent.of(5));
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
    public void add(final MapDrawable drawable)
    {
        viewModel.add(drawable);
        zoomToContents(Percent.of(5));
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
    public void frameDelay(final Duration delay)
    {
        this.delay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize()
    {
        // Default component size
        return new Dimension(800, 500);
    }

    @Override
    public boolean isFocusable()
    {
        return true;
    }

    @Override
    public void map(final Function<MapDrawable, MapDrawable> function)
    {
        viewModel.map(function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(final MouseEvent e)
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
    public void mouseDragged(final MouseEvent event)
    {
        final var point = event.getPoint();

        // If we're dragging the mouse
        if (isDragging())
        {
            // and we are panning,
            if (isPanning())
            {
                // get the location we started dragging at,
                final var start = panProjection.toMap(dragStart);

                // and the location we're at now,
                final var at = panProjection.toMap(canvas.at(point));

                if (start != null && at != null)
                {
                    // and compute the offset we want apply to the original pan area
                    final var offset = at.offsetTo(start);

                    // set the view to the original view we started panning at with the given offset
                    zoomTo(panStart.offsetBy(offset), zoom);
                }
            }
            else
            {
                // We're drawing a zoom selection rectangle, so get the width and height of it
                final var width = point.getX() - dragStart.x();
                final var height = heightForWidth(width);

                // If the selection is down and to the right
                if (width > 0)
                {
                    // set the zoom area down and to the right
                    zoomSelection = rectangle(canvas, dragStart.x(), dragStart.y(), width, height);
                }
                else
                {
                    // for up and to the left selections, we have to invert the AWT rectangle
                    // because it cannot handle negative values
                    zoomSelection = rectangle(canvas, dragStart.x() + width, dragStart.y() + height, -width, -height);
                }
            }
        }
        else
        {
            if (projection != null)
            {
                cursorAt = projection.toMap(canvas.at(point));
            }
        }

        // Redraw
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(final MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(final MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved(final MouseEvent e)
    {
        mouseDragged(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(final MouseEvent e)
    {
        // Get the point where the mouse is pressed
        final var dragStart = canvas.at(e.getPoint());

        // project it to map units,
        final var projected = canvas.toMap(dragStart);

        // and if that projection is valid,
        if (projected != null)
        {
            // then the user clicked on the map, which potentially starts a drag operation.
            this.dragStart = dragStart;

            // If we're zoomed in and the control key is down,
            if (isZoomedIn && e.isControlDown())
            {
                // save the starting view center and projection for panning
                panStart = viewCenter;
                panProjection = projection;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // If a zoom area was selected
        if (zoomSelection != null)
        {
            // zoom to that rectangle
            final var selected = projection.toMap(zoomSelection);
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
    public void mouseWheelMoved(final MouseWheelEvent e)
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
    public synchronized void paint(final Graphics uncast)
    {
        final var graphics = (Graphics2D) uncast;

        // If we are ready to paint,
        if (readyToPaint)
        {
            // clear the canvas,
            graphics.setColor(MAP_BACKGROUND.fillColor().asAwtColor());
            graphics.fillRect(0, 0, getWidth(), getHeight());

            // create the canvas to draw on,
            createCanvas(graphics);

            // draw map tiles layer on canvas,
            //mapTileCache.drawTiles(graphics, projection, mapTiles);

            // re-create the canvas because drawing the map tiles changes the projection,
            createCanvas(graphics);

            // draw tile outlines
            if (isDebugOn())
            {
                mapTiles.drawOutlines(canvas);
            }

            // draw viewables on top of tiles,
            viewModel.draw(canvas);

            // and then, if we're drawing a zoom selection rectangle,
            if (zoomSelection != null)
            {
                // draw it on top.
                graphics.setColor(DARK_TANGERINE.asAwtColor());
                graphics.drawRect(
                        (int) zoomSelection.x(),
                        (int) zoomSelection.y(),
                        (int) zoomSelection.width(),
                        (int) zoomSelection.height());
            }

            // If we're just moving around and we know where the cursor is,
            final var margin = 10;
            if (!isDragging())
            {
                // draw the cursor's latitude and longitude in the lower right
                final var style = CAPTION;
                final var cursorText = cursorAt == null ? "" : " \u2503 " + cursorAt.asString(USER_LABEL);
                final var text = state.at().name().toLowerCase()
                        + " \u2503 zoom level " + zoom.level()
                        + cursorText;
                final var textSize = canvas.textSize(style, text);
                Label.label()
                        .at(canvas.at(
                                getWidth() - textSize.widthInUnits() - margin * 3,
                                getHeight() - textSize.heightInUnits() - margin * 3))
                        .withStyle(style)
                        .withRoundedCorners(Length.pixels(10))
                        .withMargin(margin)
                        .withText(text)
                        .draw(canvas);
            }

            // Show help message
            Label.label()
                    .at(canvas.at(margin, margin))
                    .withStyle(CAPTION)
                    .withMargin(margin)
                    .withRoundedCorners(Length.pixels(10))
                    .withText("(p)ause (r)un (n)ext"
                            + " \u2503 (s)lower (q)uicker"
                            + " \u2503 zoom (i)n (o)ut (f)it"
                            + " \u2503 map data \u00a9 openstreetmap contributors"
                    )
                    .draw(canvas);
        }
    }

    @Override
    public void pullToFront(final DrawableIdentifier identifier)
    {
        viewModel.pullToFront(identifier);
        requestRedraw();
    }

    @Override
    public void pushToBack(final DrawableIdentifier identifier)
    {
        viewModel.pushToBack(identifier);
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final DrawableIdentifier identifier)
    {
        viewModel.remove(identifier);
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final DrawableIdentifier identifier, final MapDrawable viewable)
    {
        viewModel.update(identifier, viewable);
        requestRedraw();
    }

    @Override
    public void zoomTo(final com.telenav.mesakit.map.geography.shape.rectangle.Rectangle bounds)
    {
        zoom(bounds);
    }

    @Override
    public void zoomToContents(final Percent margin)
    {
        zoomTo(viewModel.bounds().expanded(margin));
    }

    void nextFrame()
    {
        state.transition(STEPPING);
    }

    @SuppressWarnings("UnusedReturnValue")
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

    private void createCanvas(final Graphics2D graphics)
    {
        projection = new CartesianMapProjection(viewArea(), drawingArea());
        canvas = MapCanvas.canvas(graphics, MapScale.STATE, projection);
    }

    private Rectangle drawingArea()
    {
        // Get the size of the entire zoom level in drawing units,
        final var size = zoom.sizeInDrawingUnits(STANDARD_TILE_SIZE);

        // and if the view doesn't completely cover the drawing area,
        if (size.width().units() < getWidth() || size.height().units() < getHeight())
        {
            // then put the view  area in the middle of the drawing surface
            isZoomedIn = false;
            return size.centeredIn(drawingSurfaceSize().asRectangle());
        }
        else
        {
            // otherwise map the view to the whole drawing surface
            isZoomedIn = true;
            return drawingSurfaceBounds();
        }
    }

    private Rectangle drawingSurfaceBounds()
    {
        final var bounds = getBounds();
        return rectangle(canvas, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    private Size drawingSurfaceSize()
    {
        return pixels(getWidth(), getHeight());
    }

    /**
     * @return The height for the given width fitting the aspect ratio of the window
     */
    private double heightForWidth(final double width)
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

    private void requestRedraw()
    {
        repaint(10);
    }

    private void slowDown()
    {
        delay = delay.longer(Percent.of(25));
        if (delay.isLessThan(Duration.seconds(0.01)))
        {
            delay = Duration.seconds(0.01);
        }
    }

    private void speedUp()
    {
        delay = delay.shorter(Percent.of(25));
    }

    /**
     * @return The view area to display for the given center location and zoom level
     */
    private com.telenav.mesakit.map.geography.shape.rectangle.Rectangle viewArea(final Location centerLocation,
                                                                                 final ZoomLevel zoom)
    {
        final var system = new SlippyTileCoordinateSystem(zoom);

        final var center = system.toDrawing(centerLocation);
        trace("center = $", center);

        final var dx = getWidth() / 2.0;
        final var dy = getHeight() / 2.0;

        final var drawingTopLeft = this.zoom.inRange(
                Point.pixels(
                        center.x() - dx,
                        center.y() - dy), mapTileCache.tileSize());
        trace("topLeft = $", drawingTopLeft);

        final var drawingBottomRight = this.zoom.inRange(
                Point.pixels(
                        center.x() + dx,
                        center.y() + dy), mapTileCache.tileSize());
        trace("bottomRight = $", drawingBottomRight);

        final var topLeft = system.toMap(drawingTopLeft);
        final var bottomRight = system.toMap(drawingBottomRight);

        return com.telenav.mesakit.map.geography.shape.rectangle.Rectangle.fromLocations(topLeft, bottomRight);
    }

    private com.telenav.mesakit.map.geography.shape.rectangle.Rectangle viewArea()
    {
        return viewArea;
    }

    /**
     * @param bounds The bounds we'd ideally like to be viewing
     */
    private void zoom(final com.telenav.mesakit.map.geography.shape.rectangle.Rectangle bounds)
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
    private synchronized void zoomTo(final Location center, final ZoomLevel zoom)
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
        trace("mapArea = " + viewArea());

        // and create tile grid for the given view area.
        mapTiles = listenTo(new SlippyTileGrid(viewArea(), zoom));

        // Request a repaint
        readyToPaint = true;
        requestRedraw();
    }
}