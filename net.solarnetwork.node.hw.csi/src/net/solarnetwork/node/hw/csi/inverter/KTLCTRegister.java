/* ==================================================================
 * KTLCTRegister.java - 1/08/2018 1:26:59 PM
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

package net.solarnetwork.node.hw.csi.inverter;

import static net.solarnetwork.node.io.modbus.ModbusDataType.StringAscii;
import static net.solarnetwork.node.io.modbus.ModbusDataType.UInt16;
import static net.solarnetwork.node.io.modbus.ModbusDataType.UInt32;
import static net.solarnetwork.node.io.modbus.ModbusDataType.UInt64;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import bak.pcj.set.IntRangeSet;
import net.solarnetwork.node.io.modbus.ModbusDataType;
import net.solarnetwork.node.io.modbus.ModbusReadFunction;
import net.solarnetwork.node.io.modbus.ModbusReference;

/**
 * Enumeration of Modbus register mappings for the CSI 50KTL-CT series inverter.
 * 
 * @author matt
 * @version 1.1
 */
public enum KTLCTRegister implements ModbusReference {

	/** Inverter model number. */
	InfoInverterModel(0, UInt16),

	/** Firmware version, as in {@link KTLCTFirmwareVersion}. */
	InfoFirmwareVersion(0x5, UInt16),

	/** Serial number. */
	InfoSerialNumber(0x6, UInt64),

	/** Inverter model name. */
	InfoInverterModelName(0xA, 20, StringAscii),

	/** Total active energy delivered, in kWh. */
	InverterActiveEnergyDelivered(0x16, UInt32),

	/** Active energy delivered today, in 0.1 kWh. */
	InverterActiveEnergyDeliveredToday(0x18, UInt16),

	/** Efficiency, in 0.0001 %. */
	InverterEfficiency(0x19, UInt16),

	/** Power factor from 0 - 1, in 0.001 increment. */
	InverterPowerFactor(0x1A, UInt16),

	/** AC active power total, in 0.1 kW (100 W). */
	InverterActivePowerTotal(0x1D, UInt16),

	/** AC active power total, in 0.1 kVA (100 VA). */
	InverterApparentPowerTotal(0x1E, UInt16),

	/** AC line voltage with phase A and B, in 0.1 V. */
	InverterVoltageLineLinePhaseAPhaseB(0x1F, UInt16),

	/** AC line voltage with phase B and C, in 0.1 V. */
	InverterVoltageLineLinePhaseBPhaseC(0x20, UInt16),

	/** AC line voltage with phase C and A, in 0.1 V. */
	InverterVoltageLineLinePhaseCPhaseA(0x21, UInt16),

	/** AC current, phase A, in 0.1 A. */
	InverterCurrentPhaseA(0x22, UInt16),

	/** AC current, phase B, in 0.1 A. */
	InverterCurrentPhaseB(0x23, UInt16),

	/** AC current, phase C, in 0.1 A. */
	InverterCurrentPhaseC(0x24, UInt16),

	/** PV 1 input voltage, in 0.1 V. */
	InverterPv1Voltage(0x25, UInt16),

	/** PV 1 input current, in 0.1 A. */
	InverterPv1Current(0x26, UInt16),

	/** PV 2 input voltage, in 0.1 V. */
	InverterPv2Voltage(0x27, UInt16),

	/** PV 2 input current, in 0.1 A. */
	InverterPv2Current(0x28, UInt16),

	/** PV 3 input voltage, in 0.1 V. */
	InverterPv3Voltage(0x29, UInt16),

	/** PV 3 input current, in 0.1 A. */
	InverterPv3Current(0x2A, UInt16),

	/** AC line frequency, in 0.1 Hz. */
	InverterFrequency(0x2B, UInt16),

	/** Module temperature, in 0.1 C. */
	InverterModuleTemperature(0x2C, UInt16),

	/** Internal temperature, in 0.1 C. */
	InverterInternalTemperature(0x2D, UInt16),

	/** Transformer temperature, in 0.1 C. */
	InverterTransformerTemperature(0x2E, UInt16),

	/** Status mode code, see {@link KTLCTInverterWorkMode}. */
	StatusMode(0x2F, UInt16),

	/** Permanent fault codes. */
	StatusPermanentFault(0x34, UInt16),

	/** Warning codes. */
	StatusWarn(0x35, UInt16),

	/** Fault 0 codes. */
	StatusFault0(0x36, UInt16),

	/** Fault 1 codes. */
	StatusFault1(0x376, UInt16),

	/** Fault 2 codes. */
	StatusFault2(0x38, UInt16),

	/** Fault 3 codes. */
	StatusFault3(0x39, UInt16),

	/** Fault 4 codes. */
	StatusFault4(0x3A, UInt16);

	private static final IntRangeSet CONFIG_REGISTER_ADDRESS_SET = createConfigRegisterAddressSet();
	private static final IntRangeSet INVERTER_REGISTER_ADDRESS_SET = createInverterRegisterAddressSet();

	private final int address;
	private final int length;
	private final ModbusDataType dataType;

	private KTLCTRegister(int address, ModbusDataType dataType) {
		this(address, 0, dataType);
	}

	private KTLCTRegister(int address, int length, ModbusDataType dataType) {
		this.address = address;
		this.length = length;
		this.dataType = dataType;
	}

	private static IntRangeSet createRegisterAddressSet(Set<String> prefixes) {
		IntRangeSet set = new IntRangeSet();
		for ( KTLCTRegister r : KTLCTRegister.values() ) {
			for ( String prefix : prefixes ) {
				if ( r.name().startsWith(prefix) ) {
					int len = r.getWordLength();
					if ( len > 0 ) {
						set.addAll(r.getAddress(), r.getAddress() + len - 1);
					}
					break;
				}
			}
		}
		return set;
	}

	private static IntRangeSet createInverterRegisterAddressSet() {
		return createRegisterAddressSet(new HashSet<>(Arrays.asList("Inverter", "Status")));
	}

	private static IntRangeSet createConfigRegisterAddressSet() {
		return createRegisterAddressSet(new HashSet<>(Arrays.asList("Info", "Config")));
	}

	@Override
	public int getAddress() {
		return address;
	}

	@Override
	public ModbusDataType getDataType() {
		return dataType;
	}

	@Override
	public ModbusReadFunction getFunction() {
		return ModbusReadFunction.ReadInputRegister;
	}

	@Override
	public int getWordLength() {
		return (this.length > 0 ? this.length : dataType.getWordLength());
	}

	/**
	 * Get an address range set that covers all the registers defined in this
	 * enumeration.
	 * 
	 * @return the range set
	 */
	public static IntRangeSet getRegisterAddressSet() {
		IntRangeSet s = new IntRangeSet(CONFIG_REGISTER_ADDRESS_SET);
		s.addAll(INVERTER_REGISTER_ADDRESS_SET);
		return s;
	}

	/**
	 * Get an address range set that covers all the configuration and info
	 * registers defined in this enumeration.
	 * 
	 * <p>
	 * Note the ranges in this set represent <i>inclusive</i> starting addresses
	 * and ending addresses.
	 * </p>
	 * 
	 * @return the range set
	 */
	public static IntRangeSet getConfigRegisterAddressSet() {
		return (IntRangeSet) CONFIG_REGISTER_ADDRESS_SET.clone();
	}

	/**
	 * Get an address range set that covers all the inverter registers defined
	 * in this enumeration.
	 * 
	 * <p>
	 * Note the ranges in this set represent <i>inclusive</i> starting addresses
	 * and ending addresses.
	 * </p>
	 * 
	 * @return the range set
	 */
	public static IntRangeSet getInverterRegisterAddressSet() {
		return (IntRangeSet) INVERTER_REGISTER_ADDRESS_SET.clone();
	}

}
