/* ==================================================================
 * Stabiliti30cAcPortType.java - 27/08/2019 4:07:01 pm
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
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

package net.solarnetwork.node.hw.idealpower.pc;

/**
 * Enumeration of AC port types.
 * 
 * @author matt
 * @version 1.0
 */
public enum Stabiliti30cAcPortType {

	Delta3Wire(0x0103, "Delta 3-Wire");

	private final int code;
	private final String description;

	private Stabiliti30cAcPortType(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * Get the code for this condition.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get a description.
	 * 
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Get an enum for a code value.
	 * 
	 * @param code
	 *        the code to get an enum for
	 * @return the enum with the given {@code code}
	 * @throws IllegalArgumentException
	 *         if {@code code} is not supported
	 */
	public static Stabiliti30cAcPortType forCode(int code) {
		for ( Stabiliti30cAcPortType c : values() ) {
			if ( code == c.code ) {
				return c;
			}
		}
		throw new IllegalArgumentException("Stabiliti30cAcPortType code [" + code + "] not supported");
	}

}
