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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Allows to stack strings on top of each other and modifying each of them propagating changes up to
 * the top while leaving lower levels unchanged.
 * 
 * This class is not synchronized internally.
 * 
 * @author Richard Eckart
 */
public class AlignedString
    implements Iterable<AlignedString.DataSegment>
{
    // private static final Log _log = LogFactory.getLog(AlignedString.class);

    private final AlignedString _underlying;

    private final Set<AlignedString> _changeListeners;

    protected final AnchorSegment _first;
    protected final AnchorSegment _last;

    private boolean _stringDirty = true;
    private boolean _startDirty = true;
    private String _content = null;

    {
        _first = new AnchorSegment(null, null);
        _last = new AnchorSegment(null, null);
        _changeListeners = new WeakHashSet<AlignedString>();
    }

    private AlignedString()
    {
        _underlying = null;
    }

    public AlignedString(final String base)
    {
        this(createBase(base));
    }

    private static AlignedString createBase(final String base)
    {
        final AlignedString d = new AlignedString();
        d._first._next = d.new BaseSegment(d._first, d._last, base);
        d._last._prev = d._first._next;
        return d;
    }

    public AlignedString(final AlignedString underlying)
    {
        _underlying = underlying;
        _underlying.addChangeListener(this);

        _first._next = new ObliqueSegment(_first, _last, _underlying.getAnchor(0),
                _underlying.getAnchor(_underlying.length()));
        _last._prev = _first._next;
    }

    private void addChangeListener(final AlignedString l)
    {
        _changeListeners.add(l);
    }

    public void fireChange()
    {
//        if (!_startDirty) {
//            System.out.println("startDirty true");
//        }

        _stringDirty = true;
        _startDirty = true;
        for (final AlignedString a : _changeListeners) {
            a._stringDirty = true;
            a._startDirty = true;
        }
    }

    /**
     * For the given interval on the current data, get the corresponding interval in the wrapped
     * data.
     */
    public Interval resolve(final Interval i)
    {
        if (_underlying == null) {
            return i;
        }

        final DataSegment startSeg = getSegmentAt(i.getStart(), true);

        // Subtract one here in order to get the segment that includes the
        // last character still in the interval. Otherwise we will have the
        // situation that startSeg may point into segment A, then follows a
        // deleted segment B and endSeg will point into C. The resolved interval
        // will thus contain A+B instead of only A.
        final DataSegment endSeg = getSegmentAt(
                (i.getStart() != i.getEnd()) ? i.getEnd() - 1 : i.getEnd(), true);

        // For start find oblique segment here or to left.
        // If none start is start of first segment.
        DataSegment cursor = startSeg;
        int start;
        while (true) {
            if (cursor == null) {
                // If there is nothing start at the beginning
                start = _underlying._first.getStart();
                break;
            }
            else if (cursor instanceof ObliqueSegment) {
                final ObliqueSegment oseg = (ObliqueSegment) cursor;
                if (cursor == startSeg) {
                    // Calculate offset relative to the position of the
                    // ObliqueSegment if we did not need to move
                    final int pos = i.getStart() - oseg.getStart();
                    start = oseg._start.getPosition() + pos;
                    break;
                }
                else {
                    // If we had to move, use the end position of the
                    // ObliqueSegment.
                    start = oseg._end.getPosition();
                    break;
                }
            }
            else {
                cursor = cursor.getPrevious();
            }
        }

        // For end find oblique segment here or to right.
        // If none end is end of last segment of underlying data
        cursor = endSeg;
        int end;
        while (true) {
            if (cursor == null) {
                // Maybe we should be in the start segment and just missed it
                // Try to recover in this case instead of expanding to the
                // end of the underlying data.
                if ((startSeg instanceof ObliqueSegment) && (startSeg.length() >= i.getLength())) {
                    final ObliqueSegment oseg = (ObliqueSegment) startSeg;
                    final int pos = i.getEnd() - oseg.getStart();
                    end = oseg._start.getPosition() + pos;
                }
                else {
                    end = _underlying._last.getEnd();
                }
                break;
            }
            else if (cursor instanceof ObliqueSegment) {
                final ObliqueSegment oseg = (ObliqueSegment) cursor;
                if (cursor == endSeg) {
                    // Calculate offset relative to the position of the
                    // ObliqueSegment if we did not need to move
                    final int pos = i.getEnd() - oseg.getStart();
                    end = oseg._start.getPosition() + pos;
                    break;
                }
                else {
                    // If we had to move, use the end position of the
                    // ObliqueSegment
                    end = oseg._start.getPosition();
                    break;
                }
            }
            else {
                cursor = cursor.getNext();
            }
        }

        if (end < start) {
            throw new IllegalStateException("BUG: End [" + end
                    + "] of resolved interval before start [" + start + "]!");
        }

        return new ImmutableInterval(start, end);
    }

    /**
     * For the given interval on the underlying data, get the corresponding interval on this level.
     * 
     * Example:
     *                   11 11 111 11
     *      012 345 6789 01 23 456 78
     * AD  |111|22ZZ2|3333|44|55|YYY|55|
     *
     * UL  |111|XX|22|ZZ|2|XXXXX|3333|XX|44|XXXX|5555|XXXX|
     *      012 34 56 78 9 11111 1111 12 22 2222 2223 3333
     *                     01234 5678 90 12 3456 7890 1234
     * 
     * As you can see there is a YYY inserted in the AD. Otherwise some parts of the UL (marked "X")
     * have been removed in the AD. Also an ZZ part has been added to UL
     * 
     * Calling this method with start=22 end=30 should return [12, 18] as this is the interval 5
     * from UL plus the "Y" that has been inserted in AD.
     * 
     * Generally: 
     * - if the start is within a deleted region, then find the next oblique segment in
     *   AD to the right and return its start position. 
     * - if the end is within a deleted region, then
     *   find the next oblique segment in AD to the left and return its end position.
     * 
     * Anchors are always in UL. They are referenced from the ObliqueSegments in AD.
     * 
     * @param i
     *            the interval on the underlying data.
     * @return the corresponding interval in the view.
     */
    public ImmutableInterval inverseResolve(final ImmutableInterval i)
    {
        if (_underlying == null) {
            return i;
        }

        // Find the oblique segment which includes the interval start or the
        // next segment to the right if the region which includes the start
        // has been deleted.
        int start = -1;
        AbstractDataSegment seg = _first;
        for (; seg != null; seg = seg.getNext()) {
            if (seg.isAnchor()) {
                continue;
            }

            final Interval ulpos = resolve(new ImmutableInterval(seg.getStart(), seg.getEnd()));

            final int ulend = ulpos.getEnd();

            if (ulend <= i.getStart()) {
                // If the end position of the current segment in the underlying
                // data is left of our seek position, go directly to the next
                // one.
                continue;
            }

            final int ulstart = ulpos.getStart();
            if ((ulstart <= i.getStart()) && (i.getStart() < ulend)) {
                // So the seek pos is within this interval. Calculate offset
                // and from that the position in AD.
                start = seg.getStart() + (i.getStart() - ulstart);
                break;
            }

            // At this point we have found all segments left of the start.
            // If the current segment does not contain the start, then we need
            // to return the start position of the next segment we
            // encounter.
            if (ulstart >= i.getEnd()) {
                seg = seg.getNext();
            }
            break;
        }

        if (start == -1) {
            if (seg != null) {
                start = seg.getStart();
            }
            else {
                // If there is nothing more, return the end.
                start = _last.getPosition();
            }
        }

        // Now we search for the end. We leave seg as is, no need to seek again.
        int end = -1;
        ObliqueSegment last = null;
        for (; seg != null; seg = seg.getNext()) {
            if (seg instanceof ObliqueSegment) {
                final ObliqueSegment oseg = (ObliqueSegment) seg;
                final int ulend = oseg._end.getPosition();

                if (ulend <= i.getEnd()) {
                    // If the end position of the current segment in the underlying
                    // data is left of our seek position, go directly to the next
                    // one.
                    last = oseg;
                    continue;
                }

                final int ulstart = oseg._start.getPosition();
                if ((ulstart <= i.getEnd()) && (i.getEnd() < ulend)) {
                    // So the seek pos is within this interval. Calculate offset
                    // and from that the position in AD.
                    end = seg.getStart() + (i.getEnd() - ulstart);
                    break;
                }

                // Processed everything left of the seek position. Bail out.
                break;
            }

            // If the current segment is not an ObliqueSegment, we cannot
            // determine its position in the underlying data. Go on to find
            // an ObliqueSegment.
        }

        // At this point we have found all segments left of the seek pos
        // but no segment containing it. In that case we need to return
        // the end position of the last segment we encountered.
        if (end == -1) {
            if (last != null) {
                end = last.getEnd();
            }
            else {
                // If we found nothing, return start as end (empty interval)
                end = start;
            }
        }

        return new ImmutableInterval(start, end);
    }

    /**
     * Get data segment currently at the given position. Anchors are never returned.
     * 
     * @return the non-anchor segment at the given position.
     */
    public AbstractDataSegment getSegmentAt(final int position)
    {
        return getSegmentAt(position, false);
    }

    /**
     * Fetch the segment that includes the designated offset position. The segment is searched from
     * left to right. If the position is at the the start of a segment and the parameter
     * {@code includeAnchors} is {@code true}, then the anchor is returned instead of the segment or
     * if the position is right one beyond the end of the data, then the end boundary anchor is
     * returned.
     * 
     * @param position
     *            the offset.
     * @param includeAnchors
     *            whether or not to include anchors.
     */
    private AbstractDataSegment getSegmentAt(final int position, final boolean includeAnchors)
    {
        if (position < 0) {
            throw new IndexOutOfBoundsException("Negative position not allowed: [" + position + "]");
        }

        final AbstractDataSegment first = _first;

        if (first == null) {
            throw new IndexOutOfBoundsException("No data");
        }

        AbstractDataSegment seg = first._next;
        int pEnd = seg.length();
        while ((seg != null) && (position > (pEnd - 1))) {
            seg = seg.getNext();
            if (seg != null) {
                pEnd += seg.length();
            }
        }

        if (seg == null) {
            if ((includeAnchors) && (pEnd == position)) {
                return _last;
            }
            else {
                throw new IndexOutOfBoundsException("Index [" + position + "] not in range [0-"
                        + pEnd + "], [" + (position - pEnd) + "] off");
            }
        }

        // If we can directly hit an anchor, return the anchor
        if (includeAnchors) {
            if (seg._prev.isAnchor() && (((Anchor) seg._prev).getPosition() == position)) {
                return seg._prev;
            }
        }

        return seg;
    }

    public AbstractDataSegment getFirst()
    {
        if (_first._next != _last) {
            return _first._next;
        }
        else {
            return null;
        }
    }

    public AbstractDataSegment getLast()
    {
        if (_last._prev != _first) {
            return _last._prev;
        }
        else {
            return null;
        }
    }

    /**
     * Get an iterator over the internal data segments.
     */
    @Override
    public Iterator<DataSegment> iterator()
    {
        return new DataSegmentIterator((AbstractDataSegment) getFirst());
    }

    /**
     * Gets total length of data.
     */
    public int length()
    {
        int length = 0;
        for (final DataSegment s : this) {
            length += s.length();
        }
        return length;
    }

    /**
     * Fetch data
     */
    public String get()
    {
        if (_stringDirty) {
            // FIXME: inefficient!
            final StringBuilder sb = new StringBuilder();
            for (final DataSegment s : this) {
                sb.append(s.get());
            }
            _content = sb.toString();
            _stringDirty = false;
        }
        return _content;
    }

    public void updateCaches()
    {
        if (_underlying != null) {
            _underlying.updateCaches();
        }

        get();
        if (_startDirty) {
            int length = 0;
            AbstractDataSegment seg = _first;
            while (seg != null) {
                seg._cachedStart = length;
                length += seg.length();
                seg = seg._next;
            }
            _startDirty = false;
            System.out.println("startDirty false");
        }
    }

    /**
     * Fetch data
     */
    public String get(final int start, final int end)
    {
        return get().substring(start, end);
    }

    /**
     * Inserts s at given position.
     */
    public void insert(final int pos, final String s)
    {
        if (s.length() == 0) {
            return;
        }

        // Split up segment.
        final AbstractDataSegment prefix;
        final AbstractDataSegment suffix;

        if (pos == 0) {
            // When inserting at position 0, it's clear where to insert / no splitting
            prefix = _first;
            suffix = _first._next;
        }
        else {
            prefix = getSegmentAt(pos);
            suffix = prefix.split(pos);
        }

        // Insert segment
        final BaseSegment seg = new BaseSegment(prefix, suffix, s);
        prefix._next = seg;
        suffix._prev = seg;

        // // Drop useless segments
        // dropSuperflourous(prefix);
        // dropSuperflourous(suffix);

        fireChange();
    }

    /**
     * If the given segment is a zero-length base segment, then it can be dropped. Zero-length
     * oblique segments need to be retained because through them we can know e.g. if we extended a
     * word. In the following example the empty oblique segments allows us to do an inverse resolve
     * from "hyphen- ated" to "hyphenated".
     * 
     * Underlying:
     * (B:0[This is a hyphen]16)(A:16)(B:16[- ]18)(A:18)(B:18[ated]22)(A:22)(B:22[ sentence]31)
     *
     * Wrapping:
     * (O:0[This is a hyphen]16)(B:16[ated]20)(O:20[]20)(O:20[ sentence]29)
     */
    private void dropSuperflourous(final AbstractDataSegment seg)
    {
        if ((seg instanceof BaseSegment) &&
        // !seg.isAnchor() &&
                (seg.length() == 0)) {
            seg._prev._next = seg._next;
            seg._next._prev = seg._prev;
        }
    }

    /**
     * Deletes data.
     * 
     * @param start
     *            the start offset.
     * @param end
     *            the end offset+1.
     */
    public void delete(final int start, final int end)
    {
        replace(start, end, null);
    }

    /**
     * Replaces data.
     * 
     * @param start
     *            the start offset.
     * @param end
     *            the end offset+1.
     */
    public void replace(final int start, final int end, final String d)
    {
        if (start == end) {
            insert(start, d);
            return;
        }

        // if (_log.isDebugEnabled()) {
        // _log.debug("pre delete("+start+","+end+") - ["+get(start,
        // end)+"] - "+dataSegmentsToString());
        // }

        final AbstractDataSegment segAtStart = getSegmentAt(start);
        final AbstractDataSegment segAtEnd = getSegmentAt(end - 1, end > 1);

        AbstractDataSegment prefix;
        AbstractDataSegment suffix;

        if (segAtStart == segAtEnd) {
            // simple case: start and end within same segment
            prefix = segAtStart;
            prefix.split(start);
            suffix = prefix._next;
            suffix = suffix.split(end);

            if (d == null || d.length() == 0) {
                prefix._next = suffix;
                suffix._prev = prefix;
            }
            else {
                final BaseSegment s = new BaseSegment(prefix, suffix, d);
                prefix._next = s;
                suffix._prev = s;
            }
        }
        else {
            if (d == null || d.length() == 0) {
                AbstractDataSegment s = segAtStart;
                while (s != segAtEnd) {
                    if (s.isAnchor()) {
                        throw new UnsupportedOperationException(
                                "Unable to replace text containing anchors.");
                    }
                    s = s._next;
                }
            }

            // complicated case
            // note: there may be anchors in the middle that we need to preserve
            segAtStart.split(start);
            prefix = segAtStart;
            suffix = segAtEnd.split(end);

            if (d == null || d.length() == 0) {
                AbstractDataSegment s = prefix._next;
                while (s != suffix) {
                    if (s.isAnchor()) {
                        // anchors need to be preserved
                    }
                    else {
                        // non-anchors need to be removed
                        s._prev._next = s._next;
                        s._next._prev = s._prev;
                    }
                    s = s._next;
                }
            }
            else {
                final BaseSegment s = new BaseSegment(prefix, suffix, d);
                prefix._next = s;
                suffix._prev = s;
            }
        }

        // Drop useless segments
        dropSuperflourous(prefix);
        dropSuperflourous(suffix);

        // if (_log.isDebugEnabled()) {
        // _log.debug("post delete("+start+","+end+") - "+dataSegmentsToString());
        // }

        fireChange();
    }

    /**
     * Get an anchor at the specified position. Breaks up the segment at the given point if
     * necessary. If there already is an anchor, it is reused.
     */
    public Anchor getAnchor(final int pos)
    {
        if (pos == 0) {
            return _first;
        }

        // Split up segment
        final AbstractDataSegment prefix = getSegmentAt(pos, true);
        if (prefix.isAnchor()) {
            return (Anchor) prefix;
        }
        else {
            final AbstractDataSegment suffix = prefix.split(pos);

            // Insert segment
            final AnchorSegment seg = new AnchorSegment(prefix, suffix);
            prefix._next = seg;
            suffix._prev = seg;

            // Drop useless segments
            dropSuperflourous(prefix);
            dropSuperflourous(suffix);

            return seg;
        }
    }

    /**
     * Get all the anchors.
     */
    public Collection<Anchor> getAnchors()
    {
        final ArrayList<Anchor> anchors = new ArrayList<Anchor>();
        for (AbstractDataSegment s = _first; s != null; s = s._next) {
            if (s.isAnchor()) {
                anchors.add((Anchor) s);
            }
        }
        anchors.trimToSize();
        return anchors;
    }

    /**
     * Get all the segments.
     */
    public Collection<DataSegment> getSegments()
    {
        final ArrayList<DataSegment> segments = new ArrayList<DataSegment>();
        for (AbstractDataSegment s = _first; s != null; s = s._next) {
            if (!s.isAnchor()) {
                segments.add(s);
            }
        }
        segments.trimToSize();
        return segments;
    }

    /**
     * Create a string representation of the segments. This is for debugging purposes.
     */
    public String dataSegmentsToString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(">>");
        for (final DataSegment s : this) {
            sb.append(s.toString());
        }

        sb.append("<< ");

        // Collection<Anchor> anchors = this.getAnchors();
        // sb.append("A[");
        // sb.append(anchors.size());
        // sb.append("]{");
        // for (Anchor a : anchors) {
        // sb.append("(");
        // sb.append(a.getPosition());
        // sb.append(")");
        // }
        // sb.append("} ");
        //
        // Collection<DataSegment> segments = this.getSegments();
        // sb.append("S[");
        // sb.append(segments.size());
        // sb.append("]{");
        // for (DataSegment s : segments) {
        // sb.append("(");
        // sb.append(s.getClass().getSimpleName().charAt(0));
        // sb.append(":");
        // sb.append(s.getStart());
        // sb.append("..");
        // sb.append(s.getEnd());
        // sb.append(")");
        // }
        // sb.append("} ");

        AbstractDataSegment s = _first;
        while (s != null) {
            sb.append("(");
            sb.append(s.getClass().getSimpleName().charAt(0));
            sb.append(":");
            sb.append(s.getStart());
            if (!s.isAnchor()) {
                sb.append("[" + s.get() + "]");
                sb.append(s.getEnd());
            }
            sb.append(")");
            s = s._next;
        }

        return sb.toString();
    }

    @Override
    public String toString()
    {
        return dataSegmentsToString();
    }

    interface DataSegment
    {
        /**
         * Fetch data
         */
        String get();

        /**
         * Gets total length of data.
         */
        int length();

        DataSegment getPrevious();

        DataSegment getNext();

        int getStart();

        int getEnd();
    }

    interface Anchor
    {
        /**
         * Get the current position of the anchor.
         */
        int getPosition();

        DataSegment getNext();
    }

    /**
     * Base class for the data segments
     * 
     * @author Richard Eckart
     */
    abstract class AbstractDataSegment
        implements AlignedString.DataSegment
    {
        protected AbstractDataSegment _prev;
        protected AbstractDataSegment _next;
        protected int _cachedStart = -1;

        public AbstractDataSegment(final AbstractDataSegment prev, final AbstractDataSegment next)
        {
            _prev = prev;
            _next = next;
        }

        public abstract AbstractDataSegment split(int position);

        @Override
        public DataSegment getPrevious()
        {
            AbstractDataSegment s = _prev;

            // We skip the Anchors with have a zero length.
            while ((s != null) && s.isAnchor()) {
                s = s._prev;
            }

            return s;
        }

        @Override
        public AbstractDataSegment getNext()
        {
            AbstractDataSegment s = _next;

            // We skip the Anchors with have a zero length.
            while ((s != null) && s.isAnchor()) {
                s = s._next;
            }

            return s;
        }

        @Override
        public int getStart()
        {
            if (_startDirty || _cachedStart == -1) {
                int pos = 0;
                AbstractDataSegment seg = this._prev;
                while (seg != null) {
                    pos += seg.length();
                    seg = seg._prev;
                }

                return pos;
            }
            else {
                return _cachedStart;
            }
        }

        @Override
        public int getEnd()
        {
            return getStart() + length();
        }

        /**
         * True if the segment is virtual (not data relevant)
         */
        public abstract boolean isAnchor();
    }

    /**
     * A segment that is not contained in the underlying data
     * 
     * @author Richard Eckart
     */
    class BaseSegment
        extends AbstractDataSegment
    {
        private String _data;

        public BaseSegment(final AbstractDataSegment prev, final AbstractDataSegment next,
                final String data)
        {
            super(prev, next);
            _data = data;
        }

        @Override
        public String get()
        {
            return _data;
        }

        @Override
        public int length()
        {
            return _data.length();
        }

        @Override
        public AbstractDataSegment split(final int position)
        {
            // Calculate positions
            final int pos = position - getStart();

            // Create new segment
            final BaseSegment suffix = new BaseSegment(this, _next, _data.substring(pos,
                    _data.length()));

            // Change current segment
            _data = _data.substring(0, pos);

            // Insert new segment
            _next._prev = suffix;
            _next = suffix;

            return suffix;
        }

        @Override
        public boolean isAnchor()
        {
            return false;
        }

        @Override
        public String toString()
        {
            return "{" + _data + "}";
        }
    }

    /**
     * A data segment that accesses the underlying data.
     * 
     * @author Richard Eckart
     */
    class ObliqueSegment
        extends AbstractDataSegment
    {
        private final Anchor _start;
        private Anchor _end;

        public ObliqueSegment(final AbstractDataSegment prev, final AbstractDataSegment next,
                final Anchor start, final Anchor end)
        {
            super(prev, next);
            _start = start;
            _end = end;
        }

        @Override
        public String get()
        {
            final StringBuilder sb = new StringBuilder();
            for (DataSegment s = _start.getNext(); s != _end.getNext(); s = s.getNext()) {
                sb.append(s.get());
            }
            return sb.toString();
        }

        @Override
        public int length()
        {
            int length = 0;
            for (DataSegment s = _start.getNext(); s != _end.getNext(); s = s.getNext()) {
                length += s.length();
            }
            return length;
        }

        @Override
        public AbstractDataSegment split(final int position)
        {
            // Calculate positions and get anchor
            final int pos = position - getStart();
            final Anchor splitAnchor = _underlying.getAnchor(_start.getPosition() + pos);

            // Create new segment
            final ObliqueSegment suffix = new ObliqueSegment(this, _next, splitAnchor, _end);

            // Change current segment
            _end = splitAnchor;

            // Insert new segment
            _next._prev = suffix;
            _next = suffix;

            return suffix;
        }

        @Override
        public boolean isAnchor()
        {
            return false;
        }

        @Override
        public String toString()
        {
            return "[" + get() + "]";
        }
    }

    /**
     * Segment serving as an anchor for higher level data.
     * 
     * @author Richard Eckart
     */
    class AnchorSegment
        extends AbstractDataSegment
        implements AlignedString.Anchor
    {
        public AnchorSegment(final AbstractDataSegment prev, final AbstractDataSegment next)
        {
            super(prev, next);
        }

        @Override
        public String get()
        {
            return "";
        }

        @Override
        public int length()
        {
            return 0;
        }

        @Override
        public AbstractDataSegment split(final int position)
        {
            // Normally anchors are unsplittable (they have a width of zero),
            // but an attempt to split an anchor will return the anchor itself
            // if the split position is exactly the anchor position.
            if (position == getPosition()) {
                return this;
            }
            else {
                throw new IndexOutOfBoundsException("Split position [" + position
                        + "] does not match anchor position [" + getPosition() + "]");
            }
        }

        @Override
        public boolean isAnchor()
        {
            return true;
        }

        @Override
        public int getPosition()
        {
            return getStart();
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            final DataSegment prev = getPrevious();
            final DataSegment next = getNext();
            if (prev != null) {
                sb.append(prev.get() + "<");
            }
            sb.append(getPosition());
            if (next != null) {
                sb.append(">" + next.get());
            }
            return sb.toString();
        }
    }
}

/**
 * DataSegment iterator.
 * 
 * @author Richard Eckart
 */
class DataSegmentIterator
    implements Iterator<AlignedString.DataSegment>
{
    private final boolean _includeAll;

    private AlignedString.AbstractDataSegment _next = null;

    public DataSegmentIterator(final AlignedString.AbstractDataSegment first)
    {
        _next = first;
        _includeAll = false;
    }

    public DataSegmentIterator(final AlignedString.AbstractDataSegment first,
            final boolean includeAll)
    {
        _next = first;
        _includeAll = includeAll;
    }

    @Override
    public boolean hasNext()
    {
        return _next != null;
    }

    @Override
    public AlignedString.DataSegment next()
    {
        final AlignedString.DataSegment result = _next;
        if (_includeAll) {
            _next = _next._next;
        }
        else {
            _next = _next.getNext();
        }
        return result;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
