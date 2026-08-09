/*
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
 */
package org.dkpro.core.api.transform.alignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AlignedStringTest
{
    private String baseString;
    private AlignedString bottom;
    private AlignedString top;

    @BeforeEach
    public void setUp() throws Exception
    {
        // 11
        // 012345678901
        baseString = "I am a test.";
        bottom = new AlignedString(baseString);
        top = new AlignedString(bottom);

        System.out.println("-------------------------------------------");
    }

    @AfterEach
    public void after()
    {
        System.out.println("                   1    |    2    |    3    |    4    |    5");
        System.out.println("         012345678901234567890123456789012345678901234567890");
        System.out.println("Base   : " + baseString);
        System.out.println("Bottom : " + bottom.get() + " - " + bottom.dataSegmentsToString());
        System.out.println("Top    : " + top.get() + " - " + top.dataSegmentsToString());
    }

    @Test
    public void testGet()
    {
        assertThat(top.get()).isEqualTo(baseString);
    }

    @Test
    public void testInsert()
    {
        final String insertString = "such ";
        final int insertPos = 2;

        bottom.insert(insertPos, insertString);

        final StringBuilder sb = new StringBuilder(baseString);
        sb.insert(insertPos, insertString);

        assertEquals(sb.toString(), bottom.get());
        assertEquals(sb.toString(), top.get());
    }

    @Test
    public void testInsert2()
    {
        // 0123456789012345678901234567890
        baseString = "This is a hyphen- ated sentence";
        bottom = new AlignedString(baseString);
        top = new AlignedString(bottom);

        System.out.println("Delete word fragment");
        final String fragment = top.get(18, 22);
        top.delete(18, 22);
        System.out.println("Top    : " + top.get() + " - " + top.dataSegmentsToString());
        System.out.println("Bottom : " + bottom.get() + " - " + bottom.dataSegmentsToString());

        System.out.println("Insert word fragment to complete word");
        top.insert(16, fragment);
        System.out.println("Top    : " + top.get() + " - " + top.dataSegmentsToString());
        System.out.println("Bottom : " + bottom.get() + " - " + bottom.dataSegmentsToString());

        System.out.println("Delete hyphen");
        top.delete(16 + fragment.length(), 18 + fragment.length());
        System.out.println("Top    : " + top.get() + " - " + top.dataSegmentsToString());
        System.out.println("Bottom : " + bottom.get() + " - " + bottom.dataSegmentsToString());

        ImmutableInterval uli = new ImmutableInterval(0, 18);
        ImmutableInterval adi = top.inverseResolve(uli);
        System.out.println("ADI    : " + top.get(adi.getStart(), adi.getEnd()));
        System.out.println("ULI    : " + bottom.get(uli.getStart(), uli.getEnd()));

        assertEquals("This is a hyphenated", top.get(adi.getStart(), adi.getEnd()));

        uli = new ImmutableInterval(18, 31);
        adi = top.inverseResolve(uli);
        System.out.println("ADI    : " + top.get(adi.getStart(), adi.getEnd()));
        System.out.println("ULI    : " + bottom.get(uli.getStart(), uli.getEnd()));

        assertEquals(" sentence", top.get(adi.getStart(), adi.getEnd()));
    }

    /**
     * This is how you would expect to do hypenation removal, but it's wrong - use method used in
     * testInsert2. This here will not work, because AlignedString will tr@Disabled(dterpolate the
     * start position of the uli interval (18) within the replaced interval (16-22).
     */
    @Test
    @Disabled("Wrong method to do hypenation removal")
    public void testInsert3()
    {
        // 0123456789012345678901234567890
        baseString = "This is a hyphen- ated sentence";
        bottom = new AlignedString(baseString);
        top = new AlignedString(bottom);

        top.replace(16, 22, "ated");

        ImmutableInterval uli = new ImmutableInterval(18, 31);
        Interval adi = top.inverseResolve(uli);
        System.out.println("ADI    : " + top.get(adi.getStart(), adi.getEnd()));
        System.out.println("ULI    : " + bottom.get(uli.getStart(), uli.getEnd()));

        assertEquals(" sentence", top.get(adi.getStart(), adi.getEnd()));
    }

    @Test
    public void testDelete_1()
    {
        bottom.delete(2, 5);
        top.delete(2, 4);

        final StringBuilder bottomRef = new StringBuilder(baseString);
        bottomRef.delete(2, 5);

        final StringBuilder topRef = new StringBuilder(bottomRef);
        topRef.delete(2, 4);

        assertEquals(bottomRef.toString(), bottom.get());
        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public void testDelete_2()
    {
        bottom.delete(2, 5);
        top.insert(4, "new ");

        final StringBuilder bottomRef = new StringBuilder(baseString);
        bottomRef.delete(2, 5);

        final StringBuilder topRef = new StringBuilder(bottomRef);
        topRef.insert(4, "new ");

        assertEquals(bottomRef.toString(), bottom.get());
        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public void testDelete_3()
    {
        bottom.delete(7, 11);
        bottom.delete(6, 7);

        final StringBuilder bottomRef = new StringBuilder(baseString);
        bottomRef.delete(7, 11);
        bottomRef.delete(6, 7);

        assertEquals(bottomRef.toString(), bottom.get());
    }

    @Test
    public void testDelete_4()
    {
        final StringBuilder bottomRef = new StringBuilder(baseString);
        bottomRef.delete(7, 12);
        bottomRef.delete(6, 9);

        bottom.delete(7, 12);
        bottom.delete(6, 7);

        assertEquals(bottomRef.toString(), bottom.get());
    }

    /**
     * If we delete and then try to resolve a segment start ends at the start boundary of the
     * deleted segment, we do not want the deleted segment to be included in the resolved interval.
     */
    @Test
    public void testResolve()
    {
        top.delete(4, 7);

        final ImmutableInterval ri = new ImmutableInterval(3, 4);
        final Interval i = top.resolve(ri);

        assertEquals(1, i.getLength());
    }

    @Test
    public void testResolve2()
    {
        top.delete(0, 5);
        top.replace(0, 1, "I want a");

        final ImmutableInterval ri = new ImmutableInterval(0, 8);
        final Interval i = top.resolve(ri);

        assertEquals(5, i.getStart());
        assertEquals(6, i.getEnd());
    }

    @Test
    public void testResolve3()
    {
        bottom = new AlignedString(
                "<Post class=\"System\" user=\"11-08-adultsUser12\">11-08-adultsUser13");
        top = new AlignedString(bottom);

        top.replace(0, 47, " ");
        after();
        top.replace(1, 19, "John");
        after();

        ImmutableInterval ri = new ImmutableInterval(1, 5);
        Interval i = top.resolve(ri);

        assertEquals(47, i.getStart());
        assertEquals(65, i.getEnd());

        bottom = new AlignedString(
                "<Post class=\"System\" user=\"11-08-adultsUser12\">11-08-adultsUser13");
        top = new AlignedString(bottom);

        top.replace(47, 65, "John");
        after();
        top.replace(0, 47, " ");

        ri = new ImmutableInterval(1, 5);
        i = top.resolve(ri);

        assertEquals(47, i.getStart());
        assertEquals(65, i.getEnd());
    }

    @Test
    public void testDeleteInsert()
    {
        bottom.delete(2, 5);
        top.insert(4, "new ");
        bottom.insert(8, ", man");

        final StringBuilder bottomRef = new StringBuilder(baseString);
        bottomRef.delete(2, 5);
        bottomRef.insert(8, ", man");

        final StringBuilder topRef = new StringBuilder(bottomRef);
        topRef.insert(4, "new ");

        assertEquals(bottomRef.toString(), bottom.get());
        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public void testReplace()
    {
        top.replace(2, 4, "want");

        final StringBuilder topRef = new StringBuilder(baseString);
        topRef.replace(2, 4, "want");

        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public void testReplace2()
    {
        top.replace(2, 4, "want");
        top.replace(4, 8, "nnahave");

        final StringBuilder topRef = new StringBuilder(baseString);
        topRef.replace(2, 4, "want");
        topRef.replace(4, 8, "nnahave");

        assertEquals(topRef.toString(), top.get());

        final Interval i1 = top.resolve(new ImmutableInterval(2, 11));
        assertEquals(2, i1.getStart());
        assertEquals(6, i1.getEnd());

        final Interval i2 = top.inverseResolve(new ImmutableInterval(i1.getStart(), i1.getEnd()));
        final String replaced = top.get(i2.getStart(), i2.getEnd());

        System.out.println("Inverse resolved: " + i2);

        assertEquals("wannahave", replaced);
        assertEquals(i1.getStart(), i2.getStart());
        assertEquals(i2.getEnd(), i2.getEnd());
    }

    // @Ignore // FIXME http://code.google.com/p/dkpro-core-asl/issues/detail?id=50
    @Test
    public void testReplace3()
    {
        top.replace(0, 1, "i");

        final StringBuilder topRef = new StringBuilder(baseString);
        topRef.replace(0, 1, "i");

        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public void testReplace4()
    {
        top.replace(11, 12, "!");

        final StringBuilder topRef = new StringBuilder(baseString);
        topRef.replace(11, 12, "!");

        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public void testReplace5()
    {
        baseString = "";
        bottom = new AlignedString(baseString);
        top = new AlignedString(bottom);

        top.replace(0, 0, "Hello!");

        final StringBuilder topRef = new StringBuilder(baseString);
        topRef.replace(0, 0, "Hello!");

        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public void testReplace6()
    {
        StringBuilder bottomRef = new StringBuilder(baseString);
        StringBuilder topRef = new StringBuilder(bottomRef);

        top.delete(2, 5);
        topRef.delete(2, 5);

        assertEquals(bottomRef.toString(), bottom.get());
        assertEquals(topRef.toString(), top.get());

        top.insert(2, "was ");
        topRef.insert(2, "was ");

        assertEquals(bottomRef.toString(), bottom.get());
        assertEquals(topRef.toString(), top.get());

        System.out.println("Resolved: " + top.resolve(new ImmutableInterval(2, 5)));
        System.out.println("Inv resolved: " + top.inverseResolve(new ImmutableInterval(2, 5)));
    }

    @Test
    public void testDirty()
    {
        final StringBuilder bottomRef = new StringBuilder(baseString);
        final StringBuilder topRef = new StringBuilder(bottomRef);

        bottom.delete(2, 5);
        bottomRef.delete(2, 5);
        topRef.delete(2, 5);

        assertEquals(bottomRef.toString(), bottom.get());
        assertEquals(topRef.toString(), top.get());

        bottom.insert(8, ", man");
        bottomRef.insert(8, ", man");
        topRef.insert(8, ", man");

        assertEquals(bottomRef.toString(), bottom.get());
        assertEquals(topRef.toString(), top.get());
    }

    /**
     * A change on a level must also become visible on levels that do not wrap the changed level
     * directly but only transitively.
     */
    @Test
    public void testChangePropagatesTransitively()
    {
        final AlignedString l0 = new AlignedString("abcdef");
        final AlignedString l1 = new AlignedString(l0);
        final AlignedString l2 = new AlignedString(l1);

        // Read once so that the caches are populated
        assertEquals("abcdef", l2.get());

        l0.insert(0, "ZZ");

        assertEquals("ZZabcdef", l0.get());
        assertEquals("ZZabcdef", l1.get());
        assertEquals("ZZabcdef", l2.get());
    }

    /**
     * Deleting an empty range must be a no-op rather than fail.
     */
    @Test
    public void testDeleteEmptyRange()
    {
        top.delete(2, 2);

        assertEquals(baseString, top.get());
    }

    /**
     * Appending at the end of the data must work, just like {@link StringBuilder#insert} allows
     * inserting at {@code length()}.
     */
    @Test
    public void testInsertAtEnd()
    {
        final int end = top.get().length();

        top.insert(end, "!");

        assertEquals(baseString + "!", top.get());
    }

    /**
     * Same as {@link #testInsertAtEnd()} but on a plain (non-stacked) AlignedString, showing that
     * the problem is not specific to stacking.
     */
    @Test
    public void testInsertAtEndOnBase()
    {
        final AlignedString as = new AlignedString("abcdef");

        as.insert(6, "!");

        assertEquals("abcdef!", as.get());
    }

    /**
     * Appending via {@code replace()} at the end must work as well - it delegates to
     * {@code insert()}.
     */
    @Test
    public void testReplaceAtEnd()
    {
        final int end = top.get().length();

        top.replace(end, end, "!");

        assertEquals(baseString + "!", top.get());
    }

    /**
     * Appending repeatedly must keep the appended parts in the order in which they were added.
     */
    @Test
    public void testInsertAtEndRepeatedly()
    {
        top.insert(top.get().length(), "X");
        top.insert(top.get().length(), "Y");
        top.insert(top.get().length(), "Z");

        assertEquals(baseString + "XYZ", top.get());
        assertEquals(baseString, bottom.get());
    }

    /**
     * Appending after the end of the data was removed must append at the new end.
     */
    @Test
    public void testInsertAtEndAfterDeletingTheEnd()
    {
        top.delete(4, top.get().length());
        assertEquals("I am", top.get());

        top.insert(top.get().length(), "!");

        assertEquals("I am!", top.get());
    }

    /**
     * Inserting beyond the end of the data must still be rejected.
     */
    @Test
    public void testInsertBeyondEndFails()
    {
        final int beyondEnd = top.get().length() + 1;

        assertThrows(IndexOutOfBoundsException.class, () -> top.insert(beyondEnd, "!"));
    }

    /**
     * Text appended at the end must resolve correctly against the underlying data.
     */
    @Test
    public void testResolveAfterInsertAtEnd()
    {
        top.insert(top.get().length(), "XY");

        assertEquals(baseString + "XY", top.get());

        // The part that exists in the underlying data must still map onto itself
        final Interval i = top.resolve(new ImmutableInterval(0, baseString.length()));

        assertEquals(baseString, bottom.get(i.getStart(), i.getEnd()));
    }

    /**
     * The cached segment start positions must stay consistent with the actual segment chain.
     * <p>
     * {@code getAnchor()} splits a segment and inserts an anchor without firing a change, so
     * {@code _startDirty} stays {@code false} even though the chain was restructured. Positions
     * currently remain correct only because the freshly created segments carry an invalid cache
     * marker which forces a recomputation. This test pins the externally observable invariant.
     */
    @Test
    public void testSegmentStartsRemainConsistentAfterAnchorSplit()
    {
        bottom = new AlignedString("abcdefghij");
        top = new AlignedString(bottom);

        // Prime the caches
        bottom.updateCaches();
        top.updateCaches();

        // Restructure the underlying chain without changing the text
        bottom.getAnchor(5);

        assertEquals("abcdefghij", bottom.get());
        assertEquals("abcdefghij", top.get());

        // Every segment must report the start position that the chain actually implies
        int expectedStart = 0;
        for (final AlignedString.DataSegment s : bottom) {
            assertEquals(expectedStart, s.getStart(),
                    "Segment reports a start position inconsistent with the segment chain");
            expectedStart += s.length();
        }
    }

    /**
     * Deleting multiple regions must leave a zero-length marker behind for every deleted region so
     * that inverse-resolving a fully deleted region yields the position at which it was removed.
     *
     * @see <a href="https://github.com/dkpro/dkpro-core/issues/1482">Issue 1482</a>
     */
    @Test
    public void testDeleteMultipleRegions()
    {
        // 11111111112222
        // 012345678901234567890123
        baseString = "<p>Hello<p>World</p></p>";
        bottom = new AlignedString(baseString);
        top = new AlignedString(bottom);

        final ImmutableInterval[] tags = { new ImmutableInterval(0, 3),
                new ImmutableInterval(8, 11), new ImmutableInterval(16, 20),
                new ImmutableInterval(20, 24) };

        // Delete back-to-front so that the offsets of the not-yet-deleted tags stay valid
        for (int i = tags.length - 1; i >= 0; i--) {
            top.delete(tags[i].getStart(), tags[i].getEnd());
        }

        assertEquals("HelloWorld", top.get());

        // Every deleted tag must inverse-resolve to the empty interval at the position where it
        // used to be - in particular the "<p>" between "Hello" and "World" must not collapse
        // onto the end of the string.
        assertEquals(new ImmutableInterval(0, 0), top.inverseResolve(tags[0]));
        assertEquals(new ImmutableInterval(5, 5), top.inverseResolve(tags[1]));
        assertEquals(new ImmutableInterval(10, 10), top.inverseResolve(tags[2]));
        assertEquals(new ImmutableInterval(10, 10), top.inverseResolve(tags[3]));

        // The text that survived must still resolve correctly in both directions
        assertEquals(new ImmutableInterval(0, 5), top.inverseResolve(new ImmutableInterval(3, 8)));
        assertEquals(new ImmutableInterval(5, 10),
                top.inverseResolve(new ImmutableInterval(11, 16)));
        assertEquals("Hello", bottom.get(top.resolve(new ImmutableInterval(0, 5)).getStart(),
                top.resolve(new ImmutableInterval(0, 5)).getEnd()));
        assertEquals("World", bottom.get(top.resolve(new ImmutableInterval(5, 10)).getStart(),
                top.resolve(new ImmutableInterval(5, 10)).getEnd()));
    }

    /**
     * Same as {@link #testDeleteMultipleRegions()} but deleting front-to-back, which exercises the
     * other branch of {@code replace()}.
     */
    @Test
    public void testDeleteMultipleRegionsAscending()
    {
        baseString = "<p>Hello<p>World</p></p>";
        bottom = new AlignedString(baseString);
        top = new AlignedString(bottom);

        top.delete(0, 3); // <p>
        top.delete(5, 8); // <p>
        top.delete(10, 14); // </p>
        top.delete(10, 14); // </p>

        assertEquals("HelloWorld", top.get());

        assertEquals(new ImmutableInterval(0, 0), top.inverseResolve(new ImmutableInterval(0, 3)));
        assertEquals(new ImmutableInterval(5, 5), top.inverseResolve(new ImmutableInterval(8, 11)));
        assertEquals(new ImmutableInterval(10, 10),
                top.inverseResolve(new ImmutableInterval(16, 20)));
        assertEquals(new ImmutableInterval(10, 10),
                top.inverseResolve(new ImmutableInterval(20, 24)));
    }

    /**
     * For the given interval on the underlying data, get the corresponding interval on this level.
     *
     * Example: 11 11 11 111 12 012 34567 8901 23 45 678 90 AD |111|22ZZ2|3333|44|55|YYY|55|
     *
     * UL |111|XX|22|ZZ|2|XXXXX|3333|XX|44|XXXX|5555|XXXX| 012 34 56 78 9 11111 1111 12 22 2222 2223
     * 3333 01234 5678 90 12 3456 7890 1234
     *
     * As you can see there is a YYY inserted in the AD. Otherwise some parts of the UL (marked "X")
     * have been removed in the AD. Also an ZZ part has been added to UL
     *
     * Calling this method with getStart()=22 getEnd()=30 ("4XXXX555") should return [13, 20]
     * ("455YYY5").
     *
     * Generally: - if the getStart() is within a deleted region, then find the next oblique segment
     * in AD to the right and return its getStart() position. - if the getEnd() is within a deleted
     * region, then find the next oblique segment in AD to the left and return its getEnd()
     * position.
     *
     * Anchors are always in UL. They are referenced from the ObliqueSegments in AD.
     */
    @Test
    public void testInverseResolve()
    {
        bottom = new AlignedString("111XX222XXXXX3333XX44XXXX5555XXXX");
        bottom.insert(7, "ZZ");
        assertEquals("111XX22ZZ2XXXXX3333XX44XXXX5555XXXX", bottom.get());

        top = new AlignedString(bottom);
        top.delete(31, 35);
        assertEquals("111XX22ZZ2XXXXX3333XX44XXXX5555", top.get());
        top.delete(23, 27);
        assertEquals("111XX22ZZ2XXXXX3333XX445555", top.get());
        top.delete(19, 21);
        assertEquals("111XX22ZZ2XXXXX3333445555", top.get());
        top.delete(10, 15);
        assertEquals("111XX22ZZ23333445555", top.get());
        top.delete(3, 5);
        assertEquals("11122ZZ23333445555", top.get());
        top.insert(16, "YYY");
        assertEquals("11122ZZ233334455YYY55", top.get());

        final ImmutableInterval uli = new ImmutableInterval(22, 30);
        System.out.println("ULI    : " + bottom.get(uli.getStart(), uli.getEnd()));

        final ImmutableInterval adi = top.inverseResolve(uli);
        System.out.println("ADI   : " + top.get(adi.getStart(), adi.getEnd()));

        assertEquals(new ImmutableInterval(13, 20), adi);
        assertEquals("455YYY5", top.get(adi.getStart(), adi.getEnd()));
        assertEquals("4XXXX555", bottom.get(uli.getStart(), uli.getEnd()));
    }
}
