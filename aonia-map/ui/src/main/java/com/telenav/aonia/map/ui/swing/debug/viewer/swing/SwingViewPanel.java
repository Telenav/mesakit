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

package com.telenav.aonia.map.ui.swing.debug.viewer.swing;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.ui.swing.debug.InteractiveView;
import com.telenav.aonia.map.ui.swing.debug.Viewable;
import com.telenav.aonia.map.ui.swing.debug.ViewableIdentifier;
import com.telenav.aonia.map.ui.swing.debug.viewer.ViewableMap;
import com.telenav.aonia.map.ui.swing.map.coordinates.mappers.CoordinateMapper;
import com.telenav.aonia.map.ui.swing.map.coordinates.mappers.SwingMercatorCoordinateMapper;
import com.telenav.aonia.map.ui.swing.map.tiles.SlippyTile;
import com.telenav.aonia.map.ui.swing.map.tiles.SlippyTileCoordinateSystem;
import com.telenav.aonia.map.ui.swing.map.tiles.SlippyTileGrid;
import com.telenav.aonia.map.ui.swing.map.tiles.SlippyTileImageCache;
import com.telenav.aonia.map.ui.swing.map.tiles.ZoomLevel;
import com.telenav.kivakit.core.kernel.language.collections.map.string.VariableMap;
import com.telenav.kivakit.core.kernel.language.threading.conditions.StateMachine;
import com.telenav.kivakit.core.kernel.language.time.Duration;
import com.telenav.kivakit.core.kernel.language.values.count.Maximum;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.kivakit.core.kernel.messaging.Message;
import com.telenav.kivakit.core.network.core.Host;
import com.telenav.kivakit.core.network.core.QueryParameters;
import com.telenav.kivakit.core.network.http.HttpNetworkLocation;
import com.telenav.kivakit.core.network.http.secure.SecureHttpNetworkLocation;
import com.telenav.kivakit.ui.swing.component.KivaKitPanel;

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
import java.awt.geom.Point2D;
import java.util.Random;

import static com.telenav.aonia.map.ui.swing.debug.viewer.swing.SwingViewPanel.State.PAUSED;
import static com.telenav.aonia.map.ui.swing.debug.viewer.swing.SwingViewPanel.State.RUNNING;
import static com.telenav.aonia.map.ui.swing.debug.viewer.swing.SwingViewPanel.State.STEPPING;
import static com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat.USER_LABEL;

/**
 * A JPanel implementation of {@link InteractiveView}
 *
 * @author jonathanl (shibo)
 */
class SwingViewPanel extends KivaKitPanel implements InteractiveView, MouseMotionListener, MouseListener, MouseWheelListener
{
    private static final long serialVersionUID = 2487158835186442787L;

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
    private Rectangle view;

    /**
     * Translates between Swing coordinates and the current geographic view rectangle
     */
    private CoordinateMapper viewMapper;

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
    private CoordinateMapper panMapper;

    /**
     * The original center point that was visible when panning started
     */
    private Location panStart;

    /**
     * True if the view is currently zoomed in
     */
    private boolean isZoomedIn;

    /**
     * Pause lock
     */
    private final StateMachine<State> state = new StateMachine<>(RUNNING);

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
        @SuppressWarnings("SpellCheckingInspection")
        @Override
        protected HttpNetworkLocation networkLocation(final SlippyTile tile)
        {
            final var host = Host.parse("api.tiles.mapbox.com");
            final var id = "mapbox.streets";
            final var x = tile.getX();
            final var y = tile.getY();
            final var z = tile.getZoomLevel().level();
            final var accessToken = "pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw";

            final var queryParameters = new VariableMap<String>();
            final var noCache = new Random().nextInt();
            queryParameters.put("no-cache", "" + noCache);
            queryParameters.put("access_token", accessToken);

            return new SecureHttpNetworkLocation(host.https()
                    .path(Message.format("/v4/${long}/${long}/${long}/${long}.png", id, z, x, y)))
                    .withQueryParameters(new QueryParameters(queryParameters));
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
    public SwingViewPanel()
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
        if (state.is(STEPPING))
        {
            state.transition(PAUSED);
        }

        if (state.is(RUNNING))
        {
            delay.sleep();
        }
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
                final var start = panMapper.locationForPoint(dragStart);

                // and the location we're at now
                final var at = panMapper.locationForPoint(e.getPoint());

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
            cursorAt = viewMapper.locationForPoint(e.getPoint());
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
            panMapper = viewMapper;
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
            final var rectangle = viewMapper.toAonian(zoomSelection);
            if (DEBUG.isDebugOn())
            {
                LOGGER.information("zooming to $ = $", zoomSelection, rectangle);
            }
            view(rectangle);
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
            final var surface = new SwingDrawingSurface(graphics, viewMapper, view);
            surface.highlightColor(Color.RED);

            // Clear the background
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, getWidth(), getHeight());

            // Draw tiles
            tileCache.drawTiles(graphics, viewMapper, tileGrid);

            // Draw viewables
            viewables.draw(surface);

            // Draw tile outlines
            if (DEBUG.isDebugOn())
            {
                tileGrid.drawOutlines(graphics, viewMapper);
            }

            // If we're drawing a zoom selection rectangle,
            if (zoomSelection != null)
            {
                // draw it on top in white
                graphics.setColor(Color.WHITE);
                graphics.drawRect(zoomSelection.x, zoomSelection.y, zoomSelection.width,
                        zoomSelection.height);
            }

            // If we're just moving around and we know where the cursor is,
            if (!isDragging() && cursorAt != null)
            {
                // draw the cursor's latitude and longitude in the lower right
                new LabelRenderer(cursorAt.asString(USER_LABEL)).draw(graphics,
                        new Point2D.Double(getWidth(), getHeight()), LabelRenderer.Position.BOTTOM_RIGHT, getBounds());
            }

            // Show help message
            new LabelRenderer("<space> pause/resume "
                    + "  '<' slower "
                    + "  '>' faster "
                    + "  'n' next frame "
                    + "  '-' zoom out "
                    + "  '+/=' zoom in "
                    + "  'z' zoom to contents")
                    .draw(graphics, new Point2D.Double(20, 20), LabelRenderer.Position.TOP_LEFT, getBounds());
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
        view(bounds);
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
        state.transition(state.is(PAUSED) ? RUNNING : PAUSED);
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

    private Location center()
    {
        final var rectangle = viewMapper.toAonian(getBounds());
        return rectangle.center();
    }

    private Rectangle computeView(final Location center, final ZoomLevel zoom)
    {
        final var mapper = new SlippyTileCoordinateSystem(zoom);
        final var centerPoint = mapper.pointForLocation(center);
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
        view = computeView(center, zoom);
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("view = " + view);
        }

        // Map view rectangle back to pixels
        final var mapper = new SlippyTileCoordinateSystem(zoom);
        final var screenArea = mapper.toAwt(view);

        // If the view doesn't cover the whole component
        if (screenArea.width < getWidth() || screenArea.height < getHeight())
        {
            // then put the view area in the middle
            screenArea.x = (getWidth() - screenArea.width) / 2;
            screenArea.y = (getHeight() - screenArea.height) / 2;
            viewMapper = new SwingMercatorCoordinateMapper(view, screenArea, SlippyTile.STANDARD_TILE_SIZE);
            isZoomedIn = false;

            // Show mapping of view
            if (DEBUG.isDebugOn())
            {
                LOGGER.information(view + " -> " + screenArea);
            }
        }
        else
        {
            // otherwise map the view to the whole component
            viewMapper = new SwingMercatorCoordinateMapper(view, getBounds(), SlippyTile.STANDARD_TILE_SIZE);
            isZoomedIn = true;

            // Show view mapping
            if (DEBUG.isDebugOn())
            {
                LOGGER.information(view + " -> " + getBounds());
            }
        }
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("mapped view rect = " + viewMapper.toAwt(view));
        }

        // Create tile grid for the view
        tileGrid = listenTo(new SlippyTileGrid(view, zoom));

        // The view is ready to draw
        viewReady = true;

        // Force a redraw
        requestRedraw();
    }

    /**
     * @param bounds The bounds we'd ideally like to be viewing
     */
    private void view(final Rectangle bounds)
    {
        view(bounds.center(), ZoomLevel.bestFit(getBounds(), tileCache.dimension(), bounds));
    }

    private void zoomIn()
    {
        view(center(), zoom.zoomIn());
    }

    private void zoomOut()
    {
        view(center(), zoom.zoomOut());
    }
}
