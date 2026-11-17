/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.corenlp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dkpro.core.corenlp.internal.DKPro2CoreNlp.META_PTB3_ESCAPING;
import static org.dkpro.core.corenlp.internal.DKPro2CoreNlp.resolvePtb3Escaping;

import java.util.Properties;

import org.junit.jupiter.api.Test;

public class DKPro2CoreNlpTest
{
    @Test
    public void thatExplicitPtb3EscapingSettingWins()
    {
        assertThat(resolvePtb3Escaping(true, metadata("false"))).isTrue();
        assertThat(resolvePtb3Escaping(false, metadata("true"))).isFalse();
        assertThat(resolvePtb3Escaping(false, null)).isFalse();
    }

    @Test
    public void thatPtb3EscapingIsTakenFromModelMetadata()
    {
        assertThat(resolvePtb3Escaping(null, metadata("false"))).isFalse();
        assertThat(resolvePtb3Escaping(null, metadata("true"))).isTrue();
    }

    @Test
    public void thatPtb3EscapingIsEnabledByDefault()
    {
        assertThat(resolvePtb3Escaping(null, new Properties())).isTrue();
        assertThat(resolvePtb3Escaping(null, null)).isTrue();
    }

    private static Properties metadata(String aValue)
    {
        Properties props = new Properties();
        props.setProperty(META_PTB3_ESCAPING, aValue);
        return props;
    }
}
