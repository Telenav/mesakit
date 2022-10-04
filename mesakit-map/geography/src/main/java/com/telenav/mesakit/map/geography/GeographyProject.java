package com.telenav.mesakit.map.geography;

import com.telenav.kivakit.core.project.Project;
import com.telenav.kivakit.core.project.ProjectTrait;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSessionFactory;
import com.telenav.kivakit.serialization.kryo.types.KivaKitCoreKryoTypes;
import com.telenav.mesakit.map.measurements.MeasurementsKryoTypes;

/**
 * This class defines a KivaKit {@link Project}. It cannot be constructed with the new operator since it has a private
 * constructor. To access the singleton instance of this class, call {@link Project#resolveProject(Class)}, or use
 * {@link ProjectTrait#project(Class)}.
 *
 * @author jonathanl (shibo)
 */

public class GeographyProject extends Project
{
    public GeographyProject()
    {
        register(new KryoSerializationSessionFactory(new GeographyKryoTypes()
                .mergedWith(new MeasurementsKryoTypes())
                .mergedWith(new KivaKitCoreKryoTypes())));
    }
}
