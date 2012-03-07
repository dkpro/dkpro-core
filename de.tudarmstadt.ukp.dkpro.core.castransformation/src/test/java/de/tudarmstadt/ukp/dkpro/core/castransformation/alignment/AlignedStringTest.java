package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public
class AlignedStringTest
{
	private String baseString;
	private AlignedString bottom;
	private AlignedString top;


	@Before
	public
	void setUp()
	throws Exception
	{
		//                      11
		//            012345678901
		baseString = "I am a test.";
		bottom = new AlignedString(baseString);
		top = new AlignedString(bottom);

		System.out.println("-------------------------------------------");
	}

	@After
	public
	void after()
	{
		System.out.println("                   1    |    2    |    3    |    4    |    5");
		System.out.println("         012345678901234567890123456789012345678901234567890");
		System.out.println("Base   : "+baseString);
		System.out.println("Bottom : "+bottom.get()+" - "+bottom.dataSegmentsToString());
		System.out.println("Top    : "+top.get()+" - "+top.dataSegmentsToString());
	}

	@Test
	public
	void testGet()
	{
		assertEquals(baseString, top.get());
	}

	@Test
	public
	void testInsert()
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
	public
	void testInsert2()
	{
		//            0123456789012345678901234567890
		baseString = "This is a hyphen- ated sentence";
		bottom = new AlignedString(baseString);
		top = new AlignedString(bottom);

		System.out.println("Delete word fragment");
		final String fragment = top.get(18,22);
		top.delete(18,22);
		System.out.println("Top    : "+top.get()+" - "+top.dataSegmentsToString());
		System.out.println("Bottom : "+bottom.get()+" - "+bottom.dataSegmentsToString());

		System.out.println("Insert word fragment to complete word");
		top.insert(16, fragment);
		System.out.println("Top    : "+top.get()+" - "+top.dataSegmentsToString());
		System.out.println("Bottom : "+bottom.get()+" - "+bottom.dataSegmentsToString());

		System.out.println("Delete hyphen");
		top.delete(16+fragment.length(), 18+fragment.length());
		System.out.println("Top    : "+top.get()+" - "+top.dataSegmentsToString());
		System.out.println("Bottom : "+bottom.get()+" - "+bottom.dataSegmentsToString());

		ImmutableInterval uli = new ImmutableInterval(0, 18);
		ImmutableInterval adi = top.inverseResolve(uli);
		System.out.println("ADI    : "+top.get(adi.getStart(), adi.getEnd()));
		System.out.println("ULI    : "+bottom.get(uli.getStart(), uli.getEnd()));

		assertEquals("This is a hyphenated", top.get(adi.getStart(), adi.getEnd()));

		uli = new ImmutableInterval(18, 31);
		adi = top.inverseResolve(uli);
		System.out.println("ADI    : "+top.get(adi.getStart(), adi.getEnd()));
		System.out.println("ULI    : "+bottom.get(uli.getStart(), uli.getEnd()));

		assertEquals(" sentence", top.get(adi.getStart(), adi.getEnd()));
	}

	/**
	 * This is how you would expect to do hypenation removal, but it's wrong - use method used in
	 * testInsert2. This here will not work, because AlignedString will try to interpolate the
	 * start position of the uli interval (18) within the replaced interval (16-22).
	 */
	@Test @Ignore("Wrong method to do hypenation removal")
	public
	void testInsert3()
	{
		//	          0123456789012345678901234567890
		baseString = "This is a hyphen- ated sentence";
		bottom = new AlignedString(baseString);
		top = new AlignedString(bottom);

		top.replace(16, 22, "ated");

		ImmutableInterval uli = new ImmutableInterval(18, 31);
		Interval adi = top.inverseResolve(uli);
		System.out.println("ADI    : "+top.get(adi.getStart(), adi.getEnd()));
		System.out.println("ULI    : "+bottom.get(uli.getStart(), uli.getEnd()));

		assertEquals(" sentence", top.get(adi.getStart(), adi.getEnd()));
	}

	@Test
	public
	void testDelete_1()
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
	public
	void testDelete_2()
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
	public
	void testDelete_3()
	{
		bottom.delete(7, 11);
		bottom.delete(6, 7);

		final StringBuilder bottomRef = new StringBuilder(baseString);
		bottomRef.delete(7, 11);
		bottomRef.delete(6, 7);

		assertEquals(bottomRef.toString(), bottom.get());
	}

	@Test
	public
	void testDelete_4()
	{
		final StringBuilder bottomRef = new StringBuilder(baseString);
		bottomRef.delete(7, 12);
		bottomRef.delete(6, 9);

		bottom.delete(7, 12);
		bottom.delete(6, 7);

		assertEquals(bottomRef.toString(), bottom.get());
	}

	/**
	 * If we delete and then try to resolve a segment start ends at the start
	 * boundary of the deleted segment, we do not want the deleted segment to
	 * be included in the resolved interval.
	 */
	@Test
	public
	void testResolve()
	{
		top.delete(4, 7);

		final ImmutableInterval ri = new ImmutableInterval(3, 4);
		final Interval i = top.resolve(ri);

		assertEquals(1, i.getLength());
	}

	@Test
	public
	void testResolve2()
	{
		top.delete(0, 5);
		top.replace(0, 1, "I want a");

		final ImmutableInterval ri = new ImmutableInterval(0, 8);
		final Interval i = top.resolve(ri);

		assertEquals(5, i.getStart());
		assertEquals(6, i.getEnd());
	}

	@Test
	public
	void testResolve3()
	{
		bottom = new AlignedString("<Post class=\"System\" user=\"11-08-adultsUser12\">11-08-adultsUser13");
		top = new AlignedString(bottom);

		top.replace(0, 47, " ");
		after();
		top.replace(1, 19, "John");
		after();

		ImmutableInterval ri = new ImmutableInterval(1, 5);
		Interval i = top.resolve(ri);

		assertEquals(47, i.getStart());
		assertEquals(65, i.getEnd());

		bottom = new AlignedString("<Post class=\"System\" user=\"11-08-adultsUser12\">11-08-adultsUser13");
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
	public
	void testDeleteInsert()
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
	public
	void testReplace()
	{
		top.replace(2, 4, "want");

		final StringBuilder topRef = new StringBuilder(baseString);
		topRef.replace(2,4,"want");

		assertEquals(topRef.toString(), top.get());
	}

    @Test
	public
	void testReplace2()
	{
		top.replace(2, 4, "want");
		top.replace(4, 8, "nnahave");

		final StringBuilder topRef = new StringBuilder(baseString);
		topRef.replace(2,4,"want");
		topRef.replace(4,8,"nnahave");

		assertEquals(topRef.toString(), top.get());

		final Interval i1 = top.resolve(new ImmutableInterval(2, 11));
		assertEquals(2, i1.getStart());
		assertEquals(6, i1.getEnd());

		final Interval i2 = top.inverseResolve(new ImmutableInterval(i1.getStart(), i1.getEnd()));
		final String replaced = top.get(i2.getStart(), i2.getEnd());

		System.out.println("Inverse resolved: "+i2);

		assertEquals("wannahave", replaced);
		assertEquals(i1.getStart(), i2.getStart());
		assertEquals(i2.getEnd(), i2.getEnd());
	}

//    @Ignore // FIXME http://code.google.com/p/dkpro-core-asl/issues/detail?id=50 
    @Test
    public
    void testReplace3()
    {
        top.replace(0, 1, "i");

        final StringBuilder topRef = new StringBuilder(baseString);
        topRef.replace(0, 1, "i");

        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public
    void testReplace4()
    {
        top.replace(11, 12, "!");

        final StringBuilder topRef = new StringBuilder(baseString);
        topRef.replace(11, 12, "!");

        assertEquals(topRef.toString(), top.get());
    }

    @Test
    public
    void testReplace5()
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
	public
	void testReplace6()
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

		System.out.println("Resolved: "+top.resolve(new ImmutableInterval(2, 5)));
		System.out.println("Inv resolved: "+top.inverseResolve(new ImmutableInterval(2, 5)));
	}

    @Test
	public
	void testDirty()
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
	 * For the given interval on the underlying data, get the corresponding
	 * interval on this level.
	 *
	 * Example:
	 *                  11 11 11 111 12
	 *      012 34567 8901 23 45 678 90
	 * AD  |111|22ZZ2|3333|44|55|YYY|55|
	 *
	 * UL  |111|XX|22|ZZ|2|XXXXX|3333|XX|44|XXXX|5555|XXXX|
	 *      012 34 56 78 9 11111 1111 12 22 2222 2223 3333
	 *                     01234 5678 90 12 3456 7890 1234
	 *
	 * As you can see there is a YYY inserted in the AD. Otherwise some parts
	 * of the UL (marked "X") have been removed in the AD. Also an ZZ part has
	 * been added to UL
	 *
	 * Calling this method with getStart()=22 getEnd()=30 ("4XXXX555") should return
	 * [13, 20] ("455YYY5").
	 *
	 * Generally:
	 * - if the getStart() is within a deleted region, then find the next oblique
	 *   segment in AD to the right and return its getStart() position.
	 * - if the getEnd() is within a deleted region, then find the next oblique
	 *   segment in AD to the left and return its getEnd() position.
	 *
	 * Anchors are always in UL. They are referenced from the ObliqueSegments
	 * in AD.
	 *
	 * @param _start
	 * @param _end
	 * @return
	 */
	@Test
	public
	void testInverseResolve()
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
		System.out.println("ULI    : "+bottom.get(uli.getStart(), uli.getEnd()));

		final ImmutableInterval adi = top.inverseResolve(uli);
		System.out.println("ADI   : "+top.get(adi.getStart(), adi.getEnd()));

		assertEquals(new ImmutableInterval(13, 20), adi);
		assertEquals("455YYY5", top.get(adi.getStart(), adi.getEnd()));
		assertEquals("4XXXX555", bottom.get(uli.getStart(), uli.getEnd()));
	}
}
