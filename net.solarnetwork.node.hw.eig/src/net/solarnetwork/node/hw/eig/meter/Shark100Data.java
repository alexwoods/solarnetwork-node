/* ==================================================================
 * Shark100Data.java - 26/07/2018 2:53:25 PM
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

package net.solarnetwork.node.hw.eig.meter;

import static net.solarnetwork.node.io.modbus.IntRangeSetUtils.combineToReduceSize;
import bak.pcj.set.IntRange;
import bak.pcj.set.IntRangeSet;
import net.solarnetwork.node.domain.ACPhase;
import net.solarnetwork.node.io.modbus.ModbusConnection;
import net.solarnetwork.node.io.modbus.ModbusData;
import net.solarnetwork.node.io.modbus.ModbusReadFunction;

/**
 * Data object for the Shark 100 series meter.
 * 
 * @author matt
 * @version 1.0
 */
public class Shark100Data extends ModbusData implements Shark100DataAccessor {

	private static final int MAX_RESULTS = 64;

	/**
	 * Constructor.
	 */
	public Shark100Data() {
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the meter data to copy
	 */
	public Shark100Data(ModbusData other) {
		super(other);
	}

	@Override
	public ModbusData copy() {
		return new Shark100Data(this);
	}

	/**
	 * Get a snapshot copy of the data.
	 * 
	 * @return a copy of the data
	 * @see Shark100Data#copy()
	 */
	public Shark100Data getSnapshot() {
		return (Shark100Data) copy();
	}

	/**
	 * Read the configuration and information registers from the device.
	 * 
	 * @param conn
	 *        the connection
	 */
	public final void readConfigurationData(final ModbusConnection conn) {
		performUpdates(new ModbusDataUpdateAction() {

			@Override
			public boolean updateModbusData(MutableModbusData m) {
				// we actually read ALL registers here, so our snapshot timestamp includes everything
				updateData(conn, m,
						combineToReduceSize(Shark100Register.getRegisterAddressSet(), MAX_RESULTS));
				return true;
			}
		});
	}

	/**
	 * Read the meter registers from the device.
	 * 
	 * @param conn
	 *        the connection
	 */
	public final void readMeterData(final ModbusConnection conn) {
		performUpdates(new ModbusDataUpdateAction() {

			@Override
			public boolean updateModbusData(MutableModbusData m) {
				updateData(conn, m,
						combineToReduceSize(Shark100Register.getMeterRegisterAddressSet(), MAX_RESULTS));
				return true;
			}
		});
	}

	private void updateData(ModbusConnection conn, MutableModbusData m, IntRangeSet rangeSet) {
		IntRange[] ranges = rangeSet.ranges();
		for ( IntRange r : ranges ) {
			int[] data = conn.readUnsignedShorts(ModbusReadFunction.ReadHoldingRegister, r.first(),
					r.length());
			m.saveDataArray(data, r.first());
		}
	}

	/**
	 * Get an accessor for a specific phase.
	 * 
	 * <p>
	 * This class implements {@link Shark100DataAccessor} for the {@code Total}
	 * phase. Call this method to get an accessor for a different phase.
	 * </p>
	 * 
	 * @param phase
	 *        the phase to get an accessor for
	 * @return the accessor
	 */
	public Shark100DataAccessor dataAccessorForPhase(ACPhase phase) {
		if ( phase == ACPhase.Total ) {
			return this;
		}
		// TODO
		throw new UnsupportedOperationException("Phase measurements not supported yet.");
	}

	private Long getEnergyValue(Shark100Register reg) {
		Number n = getNumber(reg);
		if ( n == null ) {
			return null;
		}
		SharkPowerEnergyFormat pef = getPowerEnergyFormat();
		if ( pef != null ) {
			n = pef.energyValue(n);
		}
		return (n != null ? n.longValue() : null);
	}

	@Override
	public SharkPowerEnergyFormat getPowerEnergyFormat() {
		Number n = getNumber(Shark100Register.ConfigPowerEnergyFormats);
		return (n != null ? SharkPowerEnergyFormat.forRegisterValue(n.intValue()) : null);
	}

	@Override
	public String getName() {
		return getAsciiString(Shark100Register.InfoMeterName, true);
	}

	@Override
	public String getSerialNumber() {
		return getAsciiString(Shark100Register.InfoSerialNumber, true);
	}

	@Override
	public String getFirmwareRevision() {
		return getAsciiString(Shark100Register.InfoFirmwareVersion, true);
	}

	@Override
	public SharkPowerSystem getPowerSystem() {
		Number n = getNumber(Shark100Register.ConfigPtMultiplierAndPowerSystem);
		SharkPowerSystem m = null;
		if ( n != null ) {
			try {
				m = SharkPowerSystem.forRegisterValue(n.intValue());
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
		}
		return m;
	}

	@Override
	public Float getFrequency() {
		Number v = getNumber(Shark100Register.MeterFrequency);
		return (v != null ? v.floatValue() : null);
	}

	@Override
	public Float getPowerFactor() {
		Number v = getNumber(Shark100Register.MeterPowerFactorTotal);
		return (v != null ? v.floatValue() : null);
	}

	@Override
	public Integer getActivePower() {
		Number v = getNumber(Shark100Register.MeterActivePowerTotal);
		return (v != null ? v.intValue() : null);
	}

	@Override
	public Integer getApparentPower() {
		Number v = getNumber(Shark100Register.MeterApparentPowerTotal);
		return (v != null ? v.intValue() : null);
	}

	@Override
	public Integer getReactivePower() {
		Number v = getNumber(Shark100Register.MeterReactivePowerTotal);
		return (v != null ? v.intValue() : null);
	}

	@Override
	public Float getCurrent() {
		Number a = getNumber(Shark100Register.MeterCurrentPhaseA);
		Number b = getNumber(Shark100Register.MeterCurrentPhaseB);
		Number c = getNumber(Shark100Register.MeterCurrentPhaseC);
		return (a != null && b != null && c != null ? a.floatValue() + b.floatValue() + c.floatValue()
				: null);
	}

	@Override
	public Float getVoltage() {
		Number a = getNumber(Shark100Register.MeterVoltageLineNeutralPhaseA);
		Number b = getNumber(Shark100Register.MeterVoltageLineNeutralPhaseB);
		Number c = getNumber(Shark100Register.MeterVoltageLineNeutralPhaseC);
		return (a != null && b != null && c != null
				? (a.floatValue() + b.floatValue() + c.floatValue()) / 3.0f
				: null);
	}

	@Override
	public Long getActiveEnergyDelivered() {
		return getEnergyValue(Shark100Register.MeterActiveEnergyDelivered);
	}

	@Override
	public Long getActiveEnergyReceived() {
		return getEnergyValue(Shark100Register.MeterActiveEnergyReceived);
	}

	@Override
	public Long getReactiveEnergyDelivered() {
		return getEnergyValue(Shark100Register.MeterReactiveEnergyDelivered);
	}

	@Override
	public Long getReactiveEnergyReceived() {
		return getEnergyValue(Shark100Register.MeterReactiveEnergyReceived);
	}

}
