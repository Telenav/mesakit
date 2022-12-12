package com.telenav.mesakit.core;

import com.telenav.kivakit.core.project.Project;

/**
 * Base class for MesaKit projects
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused")
public abstract class BaseMesaKitProject extends Project
{
    @Override
    protected Class<?> metadataType()
    {
        return MesaKit.class;
    }
}
