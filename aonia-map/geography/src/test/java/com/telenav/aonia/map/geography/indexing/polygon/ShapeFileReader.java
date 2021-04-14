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

package com.telenav.aonia.map.geography.indexing.polygon;

import com.telenav.aonia.map.geography.Latitude;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.Longitude;
import com.telenav.aonia.map.geography.shape.polyline.Polygon;
import com.telenav.aonia.map.geography.shape.polyline.PolygonBuilder;
import com.telenav.aonia.map.geography.shape.polyline.Polyline;
import com.telenav.aonia.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.kivakit.core.kernel.language.iteration.Iterables;
import com.telenav.kivakit.core.kernel.language.iteration.Next;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.kivakit.core.kernel.messaging.messages.status.Warning;
import com.telenav.kivakit.core.resource.Resource;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;

import java.io.IOException;

public class ShapeFileReader
{
    private final org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader reader;

    private final Listener listener;

    public ShapeFileReader(final Listener listener, final Resource resource)
    {
        this.listener = listener;
        try
        {
            final var in = resource.openForReading();
            final ValidationPreferences preferences = new ValidationPreferences();
            preferences.setMaxNumberOfPointsPerShape(100000);
            reader = new org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader(in, preferences);
            reader.getHeader();
        }
        catch (final Exception e)
        {
            throw new IllegalStateException("Unable to read shape file " + resource, e);
        }
    }

    public Iterable<Polygon> polygons()
    {
        return Iterables.iterable(() -> new Next<>()
        {
            private PolygonShape shape;

            private int part;

            @Override
            public Polygon onNext()
            {
                try
                {
                    if (shape == null)
                    {
                        shape = nextPolygonShape();
                        part = 0;
                    }
                    if (shape != null)
                    {
                        final var builder = new PolygonBuilder();
                        for (final PointData point : shape.getPointsOfPart(part))
                        {
                            builder.add(new Location(Latitude.degrees(point.getY()),
                                    Longitude.degrees(point.getX())));
                        }
                        if (++part == shape.getNumberOfParts())
                        {
                            shape = null;
                        }
                        if (builder.isValid())
                        {
                            return builder.build();
                        }
                        else
                        {
                            listener.receive(new Warning("Ignoring invalid polygon with $ locations", builder.size()));
                            return onNext();
                        }
                    }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            private PolygonShape nextPolygonShape() throws IOException, InvalidShapeFileException
            {
                AbstractShape shape;
                do
                {
                    shape = reader.next();
                    if (shape != null)
                    {
                        if (shape.getShapeType() == ShapeType.POLYGON)
                        {
                            return (PolygonShape) shape;
                        }
                    }
                }
                while (shape != null);
                return null;
            }
        });
    }

    public Iterable<Polyline> polylines()
    {
        return Iterables.iterable(() -> new Next<>()
        {
            private PolylineShape shape;

            private int part;

            @Override
            public Polyline onNext()
            {
                try
                {
                    if (shape == null)
                    {
                        shape = nextPolylineShape();
                        part = 0;
                    }
                    if (shape != null)
                    {
                        final var builder = new PolylineBuilder();
                        for (final PointData point : shape.getPointsOfPart(part))
                        {
                            builder.add(
                                    new Location(Latitude.degrees(point.getY()), Longitude.degrees(point.getX())));
                        }
                        if (++part == shape.getNumberOfParts())
                        {
                            shape = null;
                        }
                        return builder.build();
                    }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            private PolylineShape nextPolylineShape() throws IOException, InvalidShapeFileException
            {
                AbstractShape shape;
                do
                {
                    shape = reader.next();
                    if (shape != null)
                    {
                        if (shape.getShapeType() == ShapeType.POLYLINE)
                        {
                            return (PolylineShape) shape;
                        }
                    }
                }
                while (shape != null);
                return null;
            }
        });
    }
}
