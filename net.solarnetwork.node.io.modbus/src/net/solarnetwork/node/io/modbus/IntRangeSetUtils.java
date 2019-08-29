/* ==================================================================
 * IntRangeSetUtils.java - 14/05/2018 6:37:25 PM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.io.modbus;

import java.util.Set;
import bak.pcj.set.IntRange;
import bak.pcj.set.IntRangeSet;

/**
 * Utilities for the {@link IntRangeSet} class.
 * 
 * @author matt
 * @version 1.1
 * @since 2.8
 */
public final class IntRangeSetUtils {

	/**
	 * Combine ranges within an {@link IntRangeSet} to reduce the size of the
	 * set.
	 * 
	 * <p>
	 * This can be useful when querying for Modbus data to reduce the number of
	 * transactions required to read a set of registers. Instead of reading many
	 * small ranges of registers, fewer large ranges of registers can be
	 * requested. For example, if the set passed to this method contained these
	 * ranges:
	 * </p>
	 * 
	 * <ul>
	 * <li>0-1</li>
	 * <li>3-5</li>
	 * <li>20-28</li>
	 * <li>404-406</li>
	 * <li>412-418</li>
	 * <ul>
	 * 
	 * <p>
	 * then calling this method like <code>combineToReduceSize(set, 32)</code>
	 * would return a new set with these ranges:
	 * </p>
	 * 
	 * <ul>
	 * <li>0-28</li>
	 * <li>404-418</li>
	 * <ul>
	 * 
	 * @param set
	 *        the set to reduce
	 * @param maxRangeLength
	 *        the maximum length of any combined range in the resulting set
	 * @return a new set with ranges possibly combined, or {@code set} if there
	 *         are less than two ranges to start with, or {@literal null} if
	 *         {@code set} is {@literal null}
	 */
	public static IntRangeSet combineToReduceSize(IntRangeSet set, int maxRangeLength) {
		if ( set == null ) {
			return null;
		}
		if ( set.size() < 2 ) {
			return set;
		}
		IntRangeSet result = new IntRangeSet();
		IntRange[] ranges = set.ranges();
		IntRange currRange = ranges[0];
		for ( int i = 1; i < ranges.length; i++ ) {
			IntRange r = ranges[i];
			if ( r.last() - currRange.first() < maxRangeLength ) {
				// combine
				currRange = new IntRange(currRange.first(), r.last());
			} else {
				result.addAll(currRange);
				currRange = r;
			}
		}
		if ( !result.containsAll(currRange) ) {
			result.addAll(currRange);
		}
		return result;
	}

	/**
	 * Create a Modbus register address set from enum {@link ModbusReference}
	 * values.
	 * 
	 * @param <T>
	 *        the enum type that also implements {@link ModbusReference}
	 * @param clazz
	 *        the enum class to extract the register set from
	 * @param prefixes
	 *        an optional set of enum prefixes to restrict the result to; if not
	 *        provided then all enum values will be included
	 * @return the range set, never {@literal null}
	 * @see #createRegisterAddressSet(ModbusReference[], Set)
	 * @since 1.1
	 */
	public static <T extends Enum<?> & ModbusReference> IntRangeSet createRegisterAddressSet(
			Class<T> clazz, Set<String> prefixes) {
		return createRegisterAddressSet(clazz.getEnumConstants(), prefixes);
	}

	/**
	 * Create a Modbus register address set from enum {@link ModbusReference}
	 * values.
	 * 
	 * @param <T>
	 *        the enum type that also implements {@link ModbusReference}
	 * @param clazz
	 *        the enum class to extract the register set from
	 * @param prefixes
	 *        an optional set of enum prefixes to restrict the result to, which
	 *        are compared to the {@link Object#toString()} value of each
	 *        {@link ModbusReference} in {@code refs}; if not provided then all
	 *        values will be included
	 * @return the range set, never {@literal null}
	 * @since 1.1
	 */
	public static IntRangeSet createRegisterAddressSet(ModbusReference[] refs, Set<String> prefixes) {
		IntRangeSet set = new IntRangeSet();
		for ( ModbusReference r : refs ) {
			if ( prefixes != null ) {
				String name = r.toString();
				boolean found = false;
				for ( String prefix : prefixes ) {
					if ( name.startsWith(prefix) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					continue;
				}
			}

			int len = r.getWordLength();
			if ( len > 0 ) {
				set.addAll(r.getAddress(), r.getAddress() + len - 1);
			}
		}
		return set;
	}

}
