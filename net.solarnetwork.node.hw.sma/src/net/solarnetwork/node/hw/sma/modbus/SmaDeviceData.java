/* ==================================================================
 * SmaDeviceData.java - 14/09/2020 10:09:10 AM
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

package net.solarnetwork.node.hw.sma.modbus;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import net.solarnetwork.node.hw.sma.domain.SmaCommonStatusCode;
import net.solarnetwork.node.hw.sma.domain.SmaDeviceDataAccessor;
import net.solarnetwork.node.hw.sma.domain.SmaDeviceKind;
import net.solarnetwork.node.io.modbus.ModbusConnection;
import net.solarnetwork.node.io.modbus.ModbusData;
import net.solarnetwork.node.io.modbus.ModbusReference;
import net.solarnetwork.util.NumberUtils;

/**
 * Base {@link ModbusData} for SMA devices.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class SmaDeviceData extends ModbusData implements SmaDeviceDataAccessor {

	/**
	 * A default maximum number of Modbus registers to attempt to read in one
	 * transaction.
	 */
	public static final int MAX_RESULTS = 64;

	/**
	 * Constructor.
	 */
	public SmaDeviceData() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param data
	 *        some initial data to use
	 * @param addr
	 *        the starting Modbus register address of {@code data}
	 */
	public SmaDeviceData(short[] data, int addr) {
		super();
		performUpdates(new ModbusDataUpdateAction() {

			@Override
			public boolean updateModbusData(MutableModbusData m) {
				m.saveDataArray(data, addr);
				return true;
			}
		});
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *        the meter data to copy
	 */
	public SmaDeviceData(ModbusData other) {
		super(other);
	}

	@Override
	public abstract SmaDeviceData copy();

	@Override
	public Map<String, Object> getDeviceInfo() {
		SmaDeviceData data = copy();
		Map<String, Object> result = new LinkedHashMap<>(4);
		SmaDeviceKind type = data.getDeviceKind();
		if ( type != null ) {
			result.put("Device ID", type.getCode());
			result.put(INFO_KEY_DEVICE_MODEL, type.getDescription());
		}
		Long n = data.getSerialNumber();
		if ( n != null ) {
			result.put(INFO_KEY_DEVICE_SERIAL_NUMBER, n.toString());
		}
		return result;
	}

	@Override
	public boolean hasCommonDataAccessorSupport() {
		return false;
	}

	@Override
	public Long getSerialNumber() {
		return getUnsignedInt32(SmaCommonDeviceRegister.SerialNumber.getAddress());
	}

	/**
	 * Read the informational registers from the device.
	 * 
	 * @param conn
	 *        the connection
	 */
	public abstract void readInformationData(final ModbusConnection conn);

	/**
	 * Read the data registers from the device.
	 * 
	 * @param conn
	 *        the connection
	 */
	public abstract void readDeviceData(final ModbusConnection conn);

	/**
	 * Get a status code register value.
	 * 
	 * @param ref
	 *        the status register to get the value of
	 * @return the code, or {@link SmaCommonStatusCode#Unknown} if not available
	 */
	public SmaCommonStatusCode getStatusCode(ModbusReference ref) {
		return getStatusCode(ref, SmaCommonStatusCode.Unknown);
	}

	/**
	 * Get a status code register value.
	 * 
	 * @param ref
	 *        the status register to get the value of
	 * @param unknownValue
	 *        the value to use if the register has no value or is the
	 *        {@literal Unknown} value
	 * @param treatAsUnknown
	 *        optional values to also treat as "unknown" and return
	 *        {@code unknownValue} for
	 * @return the code
	 */
	public SmaCommonStatusCode getStatusCode(ModbusReference ref, SmaCommonStatusCode unknownValue,
			SmaCommonStatusCode... treatAsUnknown) {
		Number n = filterNotNumber(getNumber(ref), ref);
		if ( n == null ) {
			return unknownValue;
		}
		SmaCommonStatusCode c = SmaCommonStatusCode.forCode(n.intValue());
		if ( treatAsUnknown != null ) {
			for ( SmaCommonStatusCode t : treatAsUnknown ) {
				if ( t == c ) {
					c = SmaCommonStatusCode.Unknown;
					break;
				}
			}
		}
		return (c != SmaCommonStatusCode.Unknown ? c : unknownValue);
	}

	/**
	 * Filter out "not a number" values.
	 * 
	 * @param n
	 *        the number to filter
	 * @param ref
	 *        the register being filtered
	 * @return either {@code n} or {@literal null} if {@code n} is
	 *         {@literal null} or represents "not a number"
	 */
	public static Number filterNotNumber(Number n, ModbusReference ref) {
		if ( n == null ) {
			return n;
		}
		return SmaModbusConstants.isNaN(n, ref.getDataType()) ? null : n;
	}

	/**
	 * Get a temperature register value.
	 * 
	 * @param ref
	 *        the register to get the value from
	 * @return the temperature, or {@literal null}
	 */
	public BigDecimal getTemperatureValue(ModbusReference ref) {
		return getTemperatureValue(ref, null);
	}

	/**
	 * Get a temperature register value.
	 * 
	 * @param ref
	 *        the register to get the value from
	 * @param unknownValue
	 *        the value to use if the register has no value
	 * @return the temperature, or {@literal null}
	 */
	public BigDecimal getTemperatureValue(ModbusReference ref, BigDecimal unknownValue) {
		return getFixedScaleValue(ref, 1, unknownValue);
	}

	/**
	 * Get a temperature register value.
	 * 
	 * @param ref
	 *        the register to get the value from
	 * @param scale
	 *        the fix scale (i.e. 0..3)
	 * @return the fixed scale, or {@literal null}
	 */
	public BigDecimal getFixedScaleValue(ModbusReference ref, int scale) {
		return getFixedScaleValue(ref, scale, null);
	}

	/**
	 * Get a temperature register value.
	 * 
	 * @param ref
	 *        the register to get the value from
	 * @param scale
	 *        the fix scale (i.e. 0..3)
	 * @param unknownValue
	 *        the value to use if the register has no value
	 * @return the temperature, or {@literal null}
	 */
	public BigDecimal getFixedScaleValue(ModbusReference ref, int scale, BigDecimal unknownValue) {
		Number n = filterNotNumber(getNumber(ref), ref);
		BigDecimal d = NumberUtils.bigDecimalForNumber(n);
		if ( d == null ) {
			return unknownValue;
		}
		return d.movePointLeft(scale);
	}

}
