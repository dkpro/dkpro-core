package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment;

import java.util.Comparator;

public
interface Interval
{
    public final static Comparator<Interval> SEG_START_CMP = new Comparator<Interval>() {
		@Override
		public int
		compare(
				final Interval a0,
				final Interval a1)
		{
			final int a0s = a0.getStart();
			final int a1s = a1.getStart();

			if (a0s == a1s) {
				return a0.getEnd() - a1.getEnd();
			} else {
				return a0s - a1s;
			}
		}
    };

    public final static Comparator<Interval> SEG_END_CMP = new Comparator<Interval>() {
		@Override
		public int
		compare(
				final Interval a0,
				final Interval a1)
		{
			final int a0e = a0.getEnd();
			final int a1e = a1.getEnd();

			if (a0e == a1e) {
				return a0.getStart() - a1.getStart();
			} else {
				return a0e - a1e;
			}
		}
    };

	/**
	 * Get the length of the interval.
	 *
	 * @return the interval length.
	 */
	int getLength();

	/**
	 * Get the start of the interval.
	 *
	 * @return the start offset.
	 */
	int getStart();

	/**
	 * Get the end of the interval.
	 *
	 * @return the end offset.
	 */
	int getEnd();

	/**
	 * Test if the current and the given reference interval overlap.
	 *
	 * @param interval the reference interval.
	 * @return <code>true</code> if there is an overlap.
	 */
	boolean overlaps(
			final Interval interval);

	boolean overlapsLeft(
			final Interval interval);

	/**
	 * this:     [  s  ][e]
	 * i:   	ss-----ee
	 *
	 * @param i
	 * @return
	 */
	boolean overlapsRight(
			final Interval interval);

	Interval overlap(
			final Interval interval);

	boolean rightAligned(
			final Interval interval);

	boolean leftAligned(
			final Interval interval);

	boolean sameExtent(
			final Interval interval);

	boolean contains(
			final Interval interval);

	boolean precedes(
			final Interval interval);

	boolean immediatelyPrecedes(
			final Interval interval);

	boolean follows(
			final Interval interval);

	boolean immediatelyFollows(
			final Interval interval);

	boolean startsEarilerThan(
			final Interval interval);

	boolean startsLaterThan(
			final Interval interval);

	boolean startsSameAs(
			final Interval interval);

	boolean endsEarilerThan(
			final Interval interval);

	boolean endsLaterThan(
			final Interval interval);

	boolean endsSameAs(
			final Interval interval);
}
