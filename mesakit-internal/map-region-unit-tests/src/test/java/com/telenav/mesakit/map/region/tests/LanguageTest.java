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

package com.telenav.mesakit.map.region.tests;

import com.telenav.kivakit.core.locale.LocaleLanguage;
import com.telenav.mesakit.map.region.testing.RegionUnitTest;
import org.junit.Test;

public class LanguageTest extends RegionUnitTest
{
    @Test
    public void test()
    {
        //noinspection EqualsWithItself
        ensure(LocaleLanguage.ENGLISH.equals(LocaleLanguage.ENGLISH));
        ensureEqual("en", LocaleLanguage.ENGLISH.iso2Code());
        ensureEqual("eng", LocaleLanguage.ENGLISH.iso3Code());
        ensureEqual("de", LocaleLanguage.GERMAN.iso2Code());
        ensureEqual(LocaleLanguage.ENGLISH, LocaleLanguage.languageForIso2Code("en"));
        ensureEqual(LocaleLanguage.ENGLISH, LocaleLanguage.languageForIso2Code("EN"));
    }
}
