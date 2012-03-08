/*******************************************************************************
 * Copyright 2008
 * Richard Eckart de Castilho
 * Institut für Sprach- und Literaturwissenschaft
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment;

public
class ImmutableInterval
extends AbstractInterval
{
    private final int	start;
    private final int	end;

    /**
     * Copy constructor.
     *
     * @param interval the original interval.
     */
    public
    ImmutableInterval(
    		final Interval interval)
    {
    	this(interval.getStart(), interval.getEnd());
    }

    /**
     * Constructor.
     *
     * @param s start offset.
     * @param e end offset.
     */
    public
    ImmutableInterval(
    		final int s,
    		final int e)
    {
		start = Math.min(s, e);
		end = Math.max(s, e);
	}

	@Override
	public
    int getStart()
	{
		return start;
	}

	@Override
	public
    int getEnd()
	{
		return end;
	}
}
