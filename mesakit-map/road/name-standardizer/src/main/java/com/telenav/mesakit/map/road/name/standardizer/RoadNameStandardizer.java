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

package com.telenav.mesakit.map.road.name.standardizer;

import com.telenav.kivakit.core.path.PackagePath;
import com.telenav.kivakit.core.language.threading.KivaKitThread;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.name.parser.ParsedRoadName;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.ensure.Ensure.ensureNotNull;

public interface RoadNameStandardizer
{
    Map<String, RoadNameStandardizer> standardizers = new HashMap<>();

    static RoadNameStandardizer get(MapLocale locale, Mode mode)
    {
        synchronized (standardizers)
        {
            var key = locale.toString() + "-" + mode;
            var standardizer = standardizers.get(key);
            if (standardizer == null)
            {
                var packagePath = PackagePath.packagePath(RoadNameStandardizer.class);
                standardizer = locale.create(packagePath, "RoadNameStandardizer");
                ensureNotNull(standardizer, "Unable to create road name standardizer");
                standardizer.mode(mode);
                standardizers.put(key, standardizer);
            }
            return standardizer;
        }
    }

    static void loadInBackground(MapLocale locale, Mode mode)
    {
        KivaKitThread.run("RoadStandardizerLoader", () -> get(locale, mode));
    }

    enum Mode
    {
        NO_STANDARDIZATION,
        MESAKIT_STANDARDIZATION
    }

    void mode(Mode mode);

    ParsedRoadName standardize(RoadName name);
}
