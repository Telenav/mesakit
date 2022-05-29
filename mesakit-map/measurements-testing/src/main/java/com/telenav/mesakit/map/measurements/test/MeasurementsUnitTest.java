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

package com.telenav.mesakit.map.measurements.test;

import com.telenav.kivakit.serialization.kryo.test.KryoUnitTest;
import com.telenav.kivakit.serialization.kryo.types.CoreKryoTypes;
import com.telenav.kivakit.serialization.kryo.types.KryoTypes;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.mesakit.map.measurements.MeasurementsKryoTypes;

/**
 * Unit test for classes that involve map measurements
 *
 * @author jonathanl (shibo)
 */
@LexakaiJavadoc(complete = true)
public class MeasurementsUnitTest extends KryoUnitTest
{
    private static final MeasurementsRandomValueFactory factory = new MeasurementsRandomValueFactory();

    @Override
    protected KryoTypes kryoTypes()
    {
        return new CoreKryoTypes().mergedWith(new MeasurementsKryoTypes());
    }

    protected MeasurementsRandomValueFactory random()
    {
        return factory;
    }
}
