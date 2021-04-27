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

package com.telenav.mesakit.map.ui.desktop.debug.viewer.swing;

import com.telenav.kivakit.core.kernel.language.threading.conditions.StateMachine;
import com.telenav.kivakit.core.kernel.language.time.Duration;
import com.telenav.kivakit.core.kernel.language.values.count.Maximum;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.kivakit.core.kernel.messaging.Message;
import com.telenav.kivakit.core.network.core.Host;
import com.telenav.kivakit.core.network.http.HttpNetworkLocation;
import com.telenav.kivakit.ui.desktop.component.KivaKitPanel;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSize;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Label;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;
import com.telenav.mesakit.map.ui.desktop.coordinates.mappers.CartesianCoordinateMapper;
import com.telenav.mesakit.map.ui.desktop.debug.InteractiveView;
import com.telenav.mesakit.map.ui.desktop.debug.Viewable;
import com.telenav.mesakit.map.ui.desktop.debug.ViewableIdentifier;
import com.telenav.mesakit.map.ui.desktop.debug.viewer.ViewableMap;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapScale;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTile;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileCoordinateSystem;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileGrid;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileImageCache;
import com.telenav.mesakit.map.ui.desktop.tiles.ZoomLevel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat.USER_LABEL;
import static com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint.at;
import static com.telenav.mesakit.map.ui.desktop.debug.viewer.swing.DesktopViewPanel.State.PAUSED;
import static com.telenav.mesakit.map.ui.desktop.debug.viewer.swing.DesktopViewPanel.State.RUNNING;
import static com.telenav.mesakit.map.ui.desktop.debug.viewer.swing.DesktopViewPanel.State.STEPPING;

/**
 * A JPanel implementation of {@link InteractiveView}
 *
 * @author jonathanl (shibo)
 */
class DesktopViewPanel extends KivaKitPanel implements InteractiveView, MouseMotionListener, MouseListener, MouseWheelListener
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public enum State
    {
        RUNNING,
        PAUSED,
        STEPPING
    }

    /**
     * Map of {@link Viewable} objects in the display
     */
    private final ViewableMap viewables = new ViewableMap();

    /**
     * The center of the current view
     */
    private Location viewCenter = Location.ORIGIN;

    /**
     * The current view rectangle in use by the coordinate mappers
     */
    private Rectangle mapBounds;

    /**
     * Translates between Swing coordinates and the current geographic view rectangle
     */
    private MapCoordinateMapper mapper;

    /**
     * True when the view is ready to draw
     */
    private boolean viewReady;

    /**
     * The current zoom level
     */
    private ZoomLevel zoom = ZoomLevel.FURTHEST;

    /**
     * When drawing a zoom rectangle, this is the current rectangle
     */
    private java.awt.Rectangle zoomSelection;

    /**
     * The point where dragging started when zooming or panning
     */
    private Point dragStart;

    /**
     * The original coordinate mapper that was being used when panning started
     */
    private MapCoordinateMapper panMapper;

    /**
     * The original center point that was visible when panning started
     */
    private Location panStart;

    /**
     * True if the view is currently zoomed in
     */
    private boolean isZoomedIn;

    /**
     * The state of the view, {@link State#RUNNING}, {@link State#STEPPING} or {@link State#PAUSED}
     */
    private final StateMachine<State> state = new StateMachine<>(PAUSED);

    /**
     * Delay between updates
     */
    private Duration delay = Duration.NONE;

    /**
     * Cache of slippy tile images
     */
    private final SlippyTileImageCache tileCache = listenTo(new SlippyTileImageCache(Maximum._256)
    {
        @Override
        public Dimension dimension()
        {
            return SlippyTile.STANDARD_TILE_SIZE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected HttpNetworkLocation networkLocation(final SlippyTile tile)
        {
            final var x = tile.getX();
            final var y = tile.getY();
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
     * Grid of slippy tiles
     */
    private SlippyTileGrid tileGrid;

    /**
     * Location of cursor when not dragging
     */
    private Location cursorAt;

    /**
     * Construct
     */
    public DesktopViewPanel()
    {
        // If the panel is resized
        addComponentListener(new java.awt.event.ComponentAdapter()
        {
            @Override
            public void componentResized(final ComponentEvent e)
            {
                // Set up the view for painting
                view(viewCenter, zoom);
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
                    case 'n':
                        nextFrame();
                        break;

                    case 'z':
                        zoomToContents(Percent.of(5));
                        break;

                    case '+':
                    case '=':
                        zoomIn();
                        break;

                    case '-':
                        zoomOut();
                        break;

                    case '<':
                        slowDown();
                        break;

                    case '>':
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
    public void add(final Viewable viewable)
    {
        viewables.add(viewable);
        zoomToContents(Percent.of(5));
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        viewables.clear();
        requestRedraw();
    }

    @Override
    public void frameComplete()
    {
        if (state.is(STEPPING) || state.is(PAUSED))
        {
            state.whileLocked(() ->
            {
                state.transition(PAUSED);
                state.waitForNot(PAUSED);
            });
        }

        delay.sleep();
    }

    @Override
    public void frameSpeed(final Duration delay)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(final MouseEvent e)
    {
        // Select whatever was clicked on,
        viewables.select(e.getPoint());

        // stop any dragging going on,
        cancelDrag();

        // and force a redraw
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(final MouseEvent e)
    {
        // If we're dragging the mouse
        if (isDragging())
        {
            // If we are panning,
            if (isPanning())
            {
                // Get the location we started dragging at
                final var start = panMapper.toMapLocation(at(dragStart));

                // and the location we're at now
                final var at = panMapper.toMapLocation(at(e.getPoint()));

                // and compute the offset we want apply to the original pan view
                final var offset = at.offsetTo(start);

                // set the view to the original view we started panning at with the given offset
                view(panStart.offset(offset), zoom);
            }
            else
            {
                // We're drawing a zoom selection rectangle, so get the width and height of it
                final var width = e.getPoint().x - dragStart.x;
                final var height = heightForWidth(width);

                // If the selection is down and to the right
                if (width > 0)
                {
                    // set the zoom area down and to the right
                    zoomSelection = new java.awt.Rectangle(dragStart.x, dragStart.y, width, height);
                }
                else
                {
                    // for up and to the left selections, we have to invert the AWT rectangle
                    // because it cannot handle negative values
                    zoomSelection = new java.awt.Rectangle(dragStart.x + width, dragStart.y + height,
                            -width, -height);
                }
            }
        }
        else
        {
            cursorAt = mapper.toMapLocation(at(e.getPoint()));
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
        // Record the place we started dragging at
        dragStart = e.getPoint();

        // If we're zoomed in and the control key is down,
        if (isZoomedIn && e.isControlDown())
        {
            // save the starting view and coordinate mapper for panning
            panStart = viewCenter;
            panMapper = mapper;
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
            final var selected = mapper.toMapRectangle(DrawingRectangle.rectangle(zoomSelection));
            if (DEBUG.isDebugOn())
            {
                LOGGER.information("zooming to $ = $", zoomSelection, selected);
            }
            zoom(selected);
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
        if (viewReady)
        {
            // Get drawing surface
            final var mapper = new CartesianCoordinateMapper(mapBounds, drawingSurfaceBounds());
            final var canvas = new MapCanvas(graphics, mapBounds, MapScale.STATE, mapper);

            // Clear the background
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, getWidth(), getHeight());

            // Draw tiles
            tileCache.drawTiles(graphics, this.mapper, tileGrid);

            // Draw viewables
            viewables.draw(canvas);

            // Draw tile outlines
            if (DEBUG.isDebugOn())
            {
                tileGrid.drawOutlines(graphics, this.mapper);
            }

            // If we're drawing a zoom selection rectangle,
            if (zoomSelection != null)
            {
                // draw it on top in white
                graphics.setColor(Color.WHITE);
                graphics.drawRect(
                        zoomSelection.x,
                        zoomSelection.y,
                        zoomSelection.width,
                        zoomSelection.height);
            }

            // If we're just moving around and we know where the cursor is,
            if (!isDragging() && cursorAt != null)
            {
                // draw the cursor's latitude and longitude in the lower right
                final var style = theme().styleCaption();
                final var text = cursorAt.asString(USER_LABEL);
                final var size = canvas.size(style, text);
                Label.label(style)
                        .at(canvas.inCoordinates(getWidth() - size.width() - 10, getHeight() - size.height() - 10))
                        .withText(text)
                        .draw(canvas);
            }

            // Show help message
            Label.label(theme().styleCaption())
                    .at(canvas.inCoordinates(10, 10))
                    .withText(state.at()
                            + " | <space> pause/resume "
                            + "  '<' slower "
                            + "  '>' faster "
                            + "  'n' next frame "
                            + "  '-' zoom out "
                            + "  '+/=' zoom in "
                            + "  'z' zoom to contents "
                            + "| Map data \u00a9 OpenStreetMap contributors."
                    )
                    .draw(canvas);
        }
    }

    @Override
    public void pullToFront(final ViewableIdentifier identifier)
    {
        viewables.pullToFront(identifier);
        requestRedraw();
    }

    @Override
    public void pushToBack(final ViewableIdentifier identifier)
    {
        viewables.pushToBack(identifier);
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final ViewableIdentifier identifier)
    {
        viewables.remove(identifier);
        requestRedraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final ViewableIdentifier identifier, final Viewable viewable)
    {
        viewables.update(identifier, viewable);
        requestRedraw();
    }

    @Override
    public void zoomTo(final Rectangle bounds)
    {
        zoom(bounds);
    }

    @Override
    public void zoomToContents(final Percent margin)
    {
        zoomTo(viewables.bounds().expanded(margin));
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
        panMapper = null;
    }

    private Rectangle computeBounds(final Location center, final ZoomLevel zoom)
    {
        final var mapper = new SlippyTileCoordinateSystem(zoom);
        final var centerPoint = mapper.toDrawingPoint(center);
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("centerPoint = $", centerPoint);
        }
        final var topLeftPoint = this.zoom.inRange(
                new Point((int) centerPoint.getX() - getWidth() / 2, (int) centerPoint.getY() - getHeight() / 2), tileCache.dimension());
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("topLeftPoint = $", topLeftPoint);
        }
        final var bottomRightPoint = this.zoom.inRange(
                new Point((int) centerPoint.getX() + getWidth() / 2, (int) centerPoint.getY() + getHeight() / 2), tileCache.dimension());
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("bottomRightPoint = $", bottomRightPoint);
        }
        final var topLeft = mapper.locationForPoint(topLeftPoint);
        final var bottomRight = mapper.locationForPoint(bottomRightPoint);
        return Rectangle.fromLocations(topLeft, bottomRight);
    }

    private DrawingRectangle drawingSurfaceBounds()
    {
        return DrawingRectangle.rectangle(getBounds());
    }

    private DrawingSize drawingSurfaceSize()
    {
        return DrawingSize.size(getWidth(), getHeight());
    }

    /**
     * @return The height for the given width fitting the aspect ratio of the window
     */
    private int heightForWidth(final int width)
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
        repaint(100);
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
     * Updates the view area to the given rectangle
     */
    private synchronized void view(final Location center, final ZoomLevel zoom)
    {
        // Save view center and zoom level
        viewCenter = center;
        this.zoom = zoom;
        if (zoom.level() < 2)
        {
            viewCenter = Location.ORIGIN;
        }
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("center = " + center + ", zoom = " + zoom);
        }

        // Compute view rectangle
        mapBounds = computeBounds(center, zoom);
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("view = " + mapBounds);
        }

        // Map view rectangle back to pixels
        final var mapper = new SlippyTileCoordinateSystem(zoom);
        final var drawingArea = mapper.toDrawingRectangle(mapBounds);

        // If the drawing area doesn't cover the whole component
        if (drawingArea.width() < getWidth() || drawingArea.height() < getHeight())
        {
            // then put the view area in the middle of the drawing surface
            this.mapper = new CartesianCoordinateMapper(mapBounds, drawingArea.centeredIn(drawingSurfaceSize()));
            isZoomedIn = false;

            // Show mapping of view
            if (DEBUG.isDebugOn())
            {
                LOGGER.information(mapBounds + " -> " + drawingArea);
            }
        }
        else
        {
            // otherwise map to the whole component
            this.mapper = new CartesianCoordinateMapper(mapBounds, drawingSurfaceBounds());
            isZoomedIn = true;

            // Show drawing surface mapping
            if (DEBUG.isDebugOn())
            {
                LOGGER.information(mapBounds + " -> " + drawingSurfaceBounds());
            }
        }

        // Create tile grid for the view
        tileGrid = listenTo(new SlippyTileGrid(mapBounds, zoom));

        // The view is ready to draw
        viewReady = true;

        // Force a redraw
        requestRedraw();
    }

    /**
     * @param bounds The bounds we'd ideally like to be viewing
     */
    private void zoom(final Rectangle bounds)
    {
        view(bounds.center(), ZoomLevel.bestFit(getBounds(), tileCache.dimension(), bounds));
    }

    private void zoomIn()
    {
        view(mapBounds.center(), zoom.zoomIn());
    }

    private void zoomOut()
    {
        view(mapBounds.center(), zoom.zoomOut());
    }
}
