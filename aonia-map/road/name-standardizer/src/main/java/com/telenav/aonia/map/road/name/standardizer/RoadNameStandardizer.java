////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.standardizer;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.ParsedRoadName;
import com.telenav.kivakit.core.kernel.language.paths.PackagePath;
import com.telenav.kivakit.core.kernel.language.threading.KivaKitThread;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

public interface RoadNameStandardizer
{
    Map<String, RoadNameStandardizer> standardizers = new HashMap<>();

    static RoadNameStandardizer get(final MapLocale locale, final Mode mode)
    {
        synchronized (standardizers)
        {
            final var key = locale.toString() + "-" + mode;
            var standardizer = standardizers.get(key);
            if (standardizer == null)
            {
                final var packagePath = PackagePath.packagePath(RoadNameStandardizer.class);
                standardizer = locale.create(packagePath, "RoadNameStandardizer");
                ensureNotNull(standardizer, "Unable to create road name standardizer");
                standardizer.mode(mode);
                standardizers.put(key, standardizer);
            }
            return standardizer;
        }
    }

    static void loadInBackground(final MapLocale locale, final Mode mode)
    {
        KivaKitThread.run("RoadStandardizerLoader", () -> get(locale, mode));
    }

    enum Mode
    {
        NO_STANDARDIZATION,
        AONIA_STANDARDIZATION
    }

    void mode(Mode mode);

    ParsedRoadName standardize(RoadName name);
}
