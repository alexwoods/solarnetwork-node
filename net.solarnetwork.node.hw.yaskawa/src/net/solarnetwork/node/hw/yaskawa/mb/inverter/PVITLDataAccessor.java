/* ==================================================================
 * PVITLDataAccessor.java - 21/08/2018 1:25:31 PM
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

package net.solarnetwork.node.hw.yaskawa.mb.inverter;

import net.solarnetwork.node.domain.ACEnergyDataAccessor;
import net.solarnetwork.node.domain.PVEnergyDataAccessor;

/**
 * API for reading CSI 50KTL-CT inverter series data.
 * 
 * @author matt
 * @version 1.0
 */
public interface PVITLDataAccessor extends PVEnergyDataAccessor, ACEnergyDataAccessor {

	/**
	 * Get the inverter device type.
	 * 
	 * @return the device type
	 */
	PVITLInverterType getInverterType();

	/**
	 * Get the DSP firmware version.
	 * 
	 * @return the DSP firmware version
	 */
	String getDspFirmwareVersion();

	/**
	 * Get the LCD firmware version.
	 * 
	 * @return the LCD firmware version
	 */
	String getLcdFirmwareVersion();

	/**
	 * Get the device model name.
	 * 
	 * @return the model name
	 */
	String getModelName();

	/**
	 * Get the device serial number.
	 * 
	 * @return the serial number
	 */
	String getSerialNumber();

	/**
	 * Get the module (heat sink) temperature, in degrees Celsius.
	 * 
	 * @return the module temperature
	 */
	Float getModuleTemperature();

	/**
	 * Get the internal (ambient) temperature, in degrees Celsius.
	 * 
	 * @return the internal temperature
	 */
	Float getInternalTemperature();

	/**
	 * Get the active energy delivered today, in Wh.
	 * 
	 * @return the delivered active energy today only
	 */
	Long getActiveEnergyDeliveredToday();

	/**
	 * Get the PV 1 string voltage.
	 * 
	 * @return the voltage for PV string 1
	 */
	Float getPv1Voltage();

	/**
	 * Get the PV 1 string power, in W.
	 * 
	 * @return the power for PV string 1
	 */
	Integer getPv1Power();

	/**
	 * Get the PV 2 string voltage.
	 * 
	 * @return the voltage for PV string 2
	 */
	Float getPv2Voltage();

	/**
	 * Get the PV 2 string power, in W.
	 * 
	 * @return the power for PV string 2
	 */
	Integer getPv2Power();

}
