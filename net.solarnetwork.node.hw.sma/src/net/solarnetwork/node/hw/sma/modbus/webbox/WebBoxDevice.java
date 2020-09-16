/* ==================================================================
 * WebBoxDevice.java - 14/09/2020 9:52:00 AM
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.node.hw.sma.modbus.webbox;

import java.io.IOException;
import net.solarnetwork.node.hw.sma.domain.SmaDeviceDataAccessor;
import net.solarnetwork.node.hw.sma.domain.SmaDeviceKind;

/**
 * API for a device connected to a WebBox.
 * 
 * @author matt
 * @version 1.0
 */
public interface WebBoxDevice {

	/**
	 * Get the Modbus unit ID of this device.
	 * 
	 * @return the unit ID
	 */
	int getUnitId();

	/**
	 * Get the device serial number.
	 * 
	 * @return the serial number
	 */
	Long getSerialNumber();

	/**
	 * Get the kind of device this accessor provides access to.
	 * 
	 * @return the device kind
	 */
	SmaDeviceKind getDeviceKind();

	/**
	 * Get a data accessor for this device.
	 * 
	 * @return the data accessor
	 */
	SmaDeviceDataAccessor getDeviceDataAccessor();

	/**
	 * Refresh the device data.
	 * 
	 * @param maxAge
	 *        if greater than {@literal 0} then only refresh the data if it is
	 *        older than this many milliseconds (or never refreshed before)
	 * @return a <b>read-only</b> snapshot of the current sample data
	 * @throws IOException
	 *         if any error occurs
	 */
	SmaDeviceDataAccessor refreshData(long maxAge) throws IOException;

	/**
	 * Get a description of a device, that includes the device type, device ID,
	 * unit ID, and serial number.
	 * 
	 * @return the description, never {@literal null}
	 */
	default String getDeviceDescription() {
		return String.format("%s (%d) @ %d - %d",
				getDeviceKind() != null ? getDeviceKind().getDescription() : "N/A",
				getDeviceKind() != null ? getDeviceKind().getCode() : -1, getUnitId(),
				getSerialNumber());
	}

}
