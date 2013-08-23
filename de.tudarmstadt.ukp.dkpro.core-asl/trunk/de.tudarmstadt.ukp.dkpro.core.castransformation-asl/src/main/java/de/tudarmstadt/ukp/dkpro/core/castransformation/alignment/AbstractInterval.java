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

import java.util.Collection;

public abstract class AbstractInterval
    implements Interval
{
    public static Interval grow(final Collection<? extends Interval> ivals)
    {
        if (ivals.size() == 0) {
            return null;
        }

        final Interval first = ivals.iterator().next();
        int start = first.getStart();
        int end = first.getEnd();

        for (final Interval s : ivals) {
            if (s.getStart() < start) {
                start = s.getStart();
            }
            if (s.getEnd() > end) {
                end = s.getEnd();
            }
        }

        return new ImmutableInterval(start, end);
    }

    @Override
    public int getLength()
    {
        return getEnd() - getStart();
    }

    @Override
    public boolean overlaps(final Interval i)
    {
        // Cases:
        //
        //         start                     end
        //           |                        |
        //  1     #######                     |
        //  2        |                     #######
        //  3   ####################################
        //  4        |        #######         |
        //           |                        |

        return (((i.getStart() <= getStart()) && (getStart() < i.getEnd())) || // Case 1-3
                ((i.getStart() < getEnd()) && (getEnd() <= i.getEnd())) || // Case 1-3
        ((getStart() <= i.getStart()) && (i.getEnd() <= getEnd()))); // Case 4
    }

    @Override
    public boolean overlapsLeft(final Interval i)
    {
        return (getStart() <= i.getStart()) && (getEnd() >= i.getStart())
                && (getEnd() <= i.getEnd());
    }

    @Override
    public boolean overlapsRight(final Interval i)
    {
        return (getStart() >= i.getStart()) && (getStart() < i.getEnd())
                && (getEnd() >= i.getEnd());
    }

    @Override
    public Interval overlap(final Interval i)
    {
        final boolean start_inside = (getStart() <= i.getStart()) && (i.getStart() < getEnd());
        final boolean end_inside = (getStart() < i.getEnd()) && (i.getEnd() <= getEnd());

        if (start_inside) {
            if (end_inside) {
                return new ImmutableInterval(i);
            }
            else if (getEnd() <= i.getEnd()) {
                return new ImmutableInterval(i.getStart(), getEnd());
            }
        }
        else if (end_inside && (i.getStart() <= getStart())) {
            return new ImmutableInterval(getStart(), i.getEnd());
        }
        else if ((i.getStart() <= getStart()) && (getEnd() <= i.getEnd())) {
            return new ImmutableInterval(this);
        }

        return null;
    }

    @Override
    public boolean rightAligned(final Interval s)
    {
        return (getEnd() == s.getEnd());
    }

    @Override
    public boolean leftAligned(final Interval s)
    {
        return (getStart() == s.getStart());
    }

    @Override
    public boolean sameExtent(final Interval s)
    {
        return (getStart() == s.getStart()) && (getEnd() == s.getEnd());
    }

    /**
     * Returns true if the current includes the given interval. The inclusion does not have to be
     * proper, so equal boundaries are allowed.
     */
    @Override
    public boolean contains(final Interval i)
    {
        return (getStart() <= i.getStart()) && (getEnd() >= i.getEnd());
    }

    @Override
    public boolean precedes(final Interval s)
    {
        return (getEnd() <= s.getStart());
    }

    @Override
    public boolean immediatelyPrecedes(final Interval s)
    {
        return (getEnd() == s.getStart());
    }

    @Override
    public boolean follows(final Interval s)
    {
        return (getStart() >= s.getEnd());
    }

    @Override
    public boolean immediatelyFollows(final Interval s)
    {
        return (getStart() == s.getEnd());
    }

    @Override
    public boolean startsEarilerThan(final Interval s)
    {
        return getStart() < s.getStart();
    }

    @Override
    public boolean startsLaterThan(final Interval i)
    {
        return getStart() > i.getStart();
    }

    @Override
    public boolean startsSameAs(final Interval i)
    {
        return getStart() == i.getStart();
    }

    @Override
    public boolean endsEarilerThan(final Interval i)
    {
        return getEnd() < i.getEnd();
    }

    @Override
    public boolean endsLaterThan(final Interval i)
    {
        return getEnd() > i.getEnd();
    }

    @Override
    public boolean endsSameAs(final Interval s)
    {
        return getEnd() == s.getEnd();
    }

    @Override
    public String toString()
    {
        return "[" + getStart() + "-" + getEnd() + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + getEnd();
        result = prime * result + getStart();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ImmutableInterval)) {
            return false;
        }
        final ImmutableInterval other = (ImmutableInterval) obj;
        if (getEnd() != other.getEnd()) {
            return false;
        }
        if (getStart() != other.getStart()) {
            return false;
        }
        return true;
    }
}
