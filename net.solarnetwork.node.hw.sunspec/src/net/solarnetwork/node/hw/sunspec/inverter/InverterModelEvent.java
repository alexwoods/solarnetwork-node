/* ==================================================================
 * InverterModelEvent.java - 5/10/2018 4:24:50 PM
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

package net.solarnetwork.node.hw.sunspec.inverter;

import java.util.LinkedHashSet;
import java.util.Set;
import net.solarnetwork.node.hw.sunspec.ModelEvent;

/**
 * Inverter type events
 * 
 * @author matt
 * @version 1.0
 */
public enum InverterModelEvent implements ModelEvent {

	GroundFault(0, "Ground fault"),

	DcOverVoltage(1, "DC over voltage"),

	AcDisconnect(2, "AC disconnect open"),

	DcDisconnect(3, "DC disconnect open"),

	GridDisconnect(4, "Grid disconnect"),

	CabinetOpen(5, "Cabinet open"),

	ManualShutdown(6, "Manual shutdown"),

	OverTemperature(7, "Over temperature"),

	OverFrequency(8, "Frequency above limit"),

	UnderFrequency(9, "Frequency under limit"),

	AcOverVoltage(10, "AC voltage above limit"),

	AcUnderVoltage(11, "AC voltage under limit"),

	BlownStringFuse(12, "Blown string fuse on input"),

	UnderTemperature(13, "Under temperature"),

	MemoryLoss(14, "Generic memory or communication error (internal)"),

	HwTestFailure(15, "Hardware test failure"),

	OEM_01(16, "OEM 1"),

	OEM_02(17, "OEM 2"),

	OEM_03(18, "OEM 3"),

	OEM_04(19, "OEM 4"),

	OEM_05(20, "OEM 5"),

	OEM_06(21, "OEM 6"),

	OEM_07(22, "OEM 7"),

	OEM_08(23, "OEM 8"),

	OEM_09(24, "OEM 9"),

	OEM_10(25, "OEM 10"),

	OEM_11(26, "OEM 11"),

	OEM_12(27, "OEM 12"),

	OEM_13(28, "OEM 13"),

	OEM_14(29, "OEM 14"),

	OEM_15(30, "OEM 15"),

	OEM_16(31, "OEM 16");

	final private int index;
	final private String description;

	private InverterModelEvent(int index, String description) {
		this.index = index;
		this.description = description;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Get an enumeration for an index value.
	 * 
	 * @param index
	 *        the ID to get the enum value for
	 * @return the enumeration value
	 * @throws IllegalArgumentException
	 *         if {@code index} is not supported
	 */
	public static InverterModelEvent forIndex(int index) {
		for ( InverterModelEvent e : InverterModelEvent.values() ) {
			if ( e.index == index ) {
				return e;
			}
		}
		throw new IllegalArgumentException("Index [" + index + "] not supported");
	}

	/**
	 * Get a set of events from a bitmask.
	 * 
	 * @param bitmask
	 *        the bitmask
	 * @return the active events
	 */
	public static Set<ModelEvent> forBitmask(long bitmask) {
		Set<ModelEvent> result = new LinkedHashSet<>(32);
		for ( InverterModelEvent e : InverterModelEvent.values() ) {
			int index = e.getIndex();
			if ( ((bitmask >> index) & 0x1) == 1 ) {
				result.add(e);
			}
		}
		return result;
	}

}
