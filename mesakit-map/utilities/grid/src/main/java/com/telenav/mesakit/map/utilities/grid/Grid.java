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

package com.telenav.mesakit.map.utilities.grid;

import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.core.language.reflection.property.KivaKitIncludeProperty;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.ArrayList;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.ensure.Ensure.fail;

public class Grid
{
    public static final Grid DEFAULT = new Grid(Angle.degrees(2), Latitude.MAXIMUM);

    public static Distance DEFAULT_CELL_SIZE = Distance.miles(100);

    public static int MAXIMUM_WIDTH_IN_CELLS = 10_000;

    public static int MAXIMUM_Height_IN_CELLS = 10_000;

    public static Angle BATTLE_GRID_SIZE = Angle.degrees(0.01);

    private final Distance approximateCellSize;

    private Latitude maximumLatitude;

    private int longitudeCellCount;

    private int latitudeCellCount;

    private final int cellWidthInMicroDegrees;

    private final int cellHeightInMicroDegrees;

    private int globeHeightInMicrodegrees;

    private int globeWidthInMicrodegrees;

    private transient GridCell[][] cells;

    public Grid(Angle cellSize, Latitude maximum)
    {
        approximateCellSize = Distance.EARTH_RADIUS_MINOR.times(cellSize.asRadians());
        var approximateCellSizeInMicroDegrees = (int) (cellSize.asDegrees() * 1000000L);

        initialize(maximum, approximateCellSizeInMicroDegrees);

        cellWidthInMicroDegrees = approximateCellSizeInMicroDegrees;
        cellHeightInMicroDegrees = approximateCellSizeInMicroDegrees;
    }

    public Grid(Distance approximateCellSize, Latitude maximum)
    {
        this.approximateCellSize = approximateCellSize;
        var approximateCellSizeInMicroDegrees = (int) (this.approximateCellSize.asDegrees() * 1000000L);

        initialize(maximum, approximateCellSizeInMicroDegrees);

        cellWidthInMicroDegrees = (globeWidthInMicrodegrees / longitudeCellCount);
        cellHeightInMicroDegrees = (globeHeightInMicrodegrees / latitudeCellCount);
    }

    public Distance approximateCellSize()
    {
        return approximateCellSize;
    }

    public GridCell cell(Location location)
    {
        assert location != null;
        return cellForIndices(indexForLatitude(location.latitude()), indexForLongitude(location.longitude()));
    }

    @KivaKitIncludeProperty
    public Count cellCount()
    {
        return Count.count((long) latitudeCellCount * longitudeCellCount);
    }

    public GridCell cellForIdentifier(GridCellIdentifier identifier)
    {
        return cellForIndices(identifier.latitudeIndex(), identifier.longitudeIndex());
    }

    /**
     * Add a method to just get the identifiers within a bounding box
     */
    public List<GridCellIdentifier> cellIdentifiersWithin(Rectangle bounds)
    {
        if (bounds != null)
        {
            List<GridCellIdentifier> list = new ArrayList<>();
            var bottomIndex = indexForLatitude(bounds.bottomLeft().latitude());
            var leftIndex = indexForLongitude(bounds.bottomLeft().longitude());
            var topIndex = indexForLatitude(bounds.topRight().latitude().decremented());
            var rightIndex = indexForLongitude(bounds.topRight().longitude().decremented());
            for (var latitudeIndex = bottomIndex; latitudeIndex <= topIndex; latitudeIndex++)
            {
                for (var longitudeIndex = leftIndex; longitudeIndex <= rightIndex; longitudeIndex++)
                {
                    list.add(new GridCellIdentifier(this, latitudeIndex, longitudeIndex));
                }
            }
            return list;
        }
        return new ArrayList<>();
    }

    @KivaKitIncludeProperty
    public Size cellSize()
    {
        return new Size(Width.microdegrees(cellWidthInMicroDegrees),
                Height.microdegrees(cellHeightInMicroDegrees));
    }

    public List<GridCell> cells()
    {
        return cellsIntersecting(Rectangle.MAXIMUM);
    }

    public List<GridCell> cellsIntersecting(Rectangle bounds)
    {
        if (bounds != null)
        {
            List<GridCell> list = new ArrayList<>();
            var bottomIndex = indexForLatitude(bounds.bottomLeft().latitude());
            var leftIndex = indexForLongitude(bounds.bottomLeft().longitude());
            var topIndex = indexForLatitude(bounds.topRight().latitude().decremented());
            var rightIndex = indexForLongitude(bounds.topRight().longitude().decremented());
            for (var latitudeIndex = bottomIndex; latitudeIndex <= topIndex; latitudeIndex++)
            {
                for (var longitudeIndex = leftIndex; longitudeIndex <= rightIndex; longitudeIndex++)
                {
                    list.add(cellForIndices(latitudeIndex, longitudeIndex));
                }
            }
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * Converts a location into a grid cell identifier having an origin-based cell coordinate (no coordinates are
     * negative)
     *
     * @param location The location
     * @return The grid cell identifier
     */
    public GridCellIdentifier identifierForLocation(Location location)
    {
        return new GridCellIdentifier(this, indexForLatitude(location.latitude()),
                indexForLongitude(location.longitude()));
    }

    public List<GridCellIdentifier> identifiers()
    {
        List<GridCellIdentifier> identifiers = new ArrayList<>();
        for (var cell : cells())
        {
            identifiers.add(cell.identifier());
        }
        return identifiers;
    }

    /** Return cells north/south */
    @KivaKitIncludeProperty
    public Count latitudeCellCount()
    {
        return Count.count(latitudeCellCount);
    }

    /** Return cells east/west */
    @KivaKitIncludeProperty
    public Count longitudeCellCount()
    {
        return Count.count(longitudeCellCount);
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    private GridCell[][] cellArray()
    {
        if (cells == null)
        {
            cells = new GridCell[latitudeCellCount][longitudeCellCount];
        }
        return cells;
    }

    /**
     * @param latitudeIndex The zero-based latitude cell index
     * @param longitudeIndex The zero-based longitude cell index
     * @return The cell for the given indices
     */
    private GridCell cellForIndices(int latitudeIndex, int longitudeIndex)
    {
        if (latitudeIndex >= 0 && longitudeIndex >= 0 && latitudeIndex < latitudeCellCount
                && longitudeIndex < longitudeCellCount)
        {
            GridCell cell = null;
            if (cellArray() != null)
            {
                cell = cellArray()[latitudeIndex][longitudeIndex];
            }
            if (cell == null)
            {
                cell = new GridCell(new GridCellIdentifier(this, latitudeIndex, longitudeIndex),
                        locationForIndices(latitudeIndex, longitudeIndex),
                        locationForIndices(latitudeIndex + 1, longitudeIndex + 1));
                cellArray()[latitudeIndex][longitudeIndex] = cell;
            }
            return cell;
        }
        else
        {
            return fail("Cell ${debug}, ${debug} is not valid with a cell count of ${debug}, ${debug}",
                    latitudeIndex, longitudeIndex, latitudeCellCount, longitudeCellCount);
        }
    }

    /**
     * @param latitude The latitude
     * @return The zero-based grid index
     */
    private int indexForLatitude(Latitude latitude)
    {
        return Ints.intInRangeInclusive(
                (latitude.asMicrodegrees() + maximumLatitude.asMicrodegrees()) / cellHeightInMicroDegrees, 0,
                latitudeCellCount - 1);
    }

    /**
     * @param longitude The longitude
     * @return The zero-based grid index
     */
    private int indexForLongitude(Longitude longitude)
    {
        return Ints.intInRangeInclusive(
                (longitude.asMicrodegrees() + Longitude.MAXIMUM.asMicrodegrees()) / cellWidthInMicroDegrees, 0,
                longitudeCellCount - 1);
    }

    private void initialize(Latitude maximum, int approximateCellSizeInMicroDegrees)
    {
        maximumLatitude = maximum;

        globeHeightInMicrodegrees = (2 * maximumLatitude.asMicrodegrees());
        globeWidthInMicrodegrees = (2 * Longitude.MAXIMUM.asMicrodegrees());

        longitudeCellCount = (globeWidthInMicrodegrees / approximateCellSizeInMicroDegrees) + 1;
        latitudeCellCount = (globeHeightInMicrodegrees / approximateCellSizeInMicroDegrees) + 1;

        ensure(latitudeCellCount > 0);
        ensure(latitudeCellCount <= 1000);
        ensure(longitudeCellCount > 0);
        ensure(longitudeCellCount <= 1000);
    }

    private Latitude latitudeForIndex(int latitudeIndex)
    {
        var microdegrees = latitudeIndex * cellHeightInMicroDegrees - maximumLatitude.asMicrodegrees();

        // Handle the boundary case here (with a margin of error for rounding)
        if (microdegrees > maximumLatitude.asMicrodegrees() - 100)
        {
            microdegrees = maximumLatitude.asMicrodegrees();
        }

        return Latitude.microdegrees(microdegrees);
    }

    private Location locationForIndices(int latitudeIndex, int longitudeIndex)
    {
        return new Location(latitudeForIndex(latitudeIndex), longitudeForIndex(longitudeIndex));
    }

    private Longitude longitudeForIndex(int longitudeIndex)
    {
        var microdegrees = longitudeIndex * cellWidthInMicroDegrees - Longitude.MAXIMUM.asMicrodegrees();

        // Handle the boundary case here (with a margin of error for rounding)
        if (microdegrees > Longitude.MAXIMUM.asMicrodegrees() - 100)
        {
            microdegrees = Longitude.MAXIMUM.asMicrodegrees();
        }

        return Longitude.microdegrees(microdegrees);
    }
}
