/* ==================================================================
 * StaticDataMapModbusConnection.java - 8/11/2019 11:28:16 am
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

package net.solarnetwork.node.io.modbus.support;

import java.util.BitSet;
import java.util.Map;
import net.solarnetwork.node.io.modbus.ModbusConnection;
import net.solarnetwork.node.io.modbus.ModbusWriteFunction;

/**
 * {@link ModbusConnection} for reading/writing static data.
 * 
 * <p>
 * This class can be useful in tests working with Modbus connections.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 2.16
 */
public class StaticDataMapModbusConnection extends StaticDataMapReadonlyModbusConnection {

	/**
	 * Constructor.
	 * 
	 * @param data
	 *        the starting data
	 */
	public StaticDataMapModbusConnection(Map<Integer, Integer> data) {
		super(data);
	}

	@Override
	public void writeUnsignedShorts(ModbusWriteFunction function, Integer address, int[] values) {
		Map<Integer, Integer> data = getData();
		for ( int i = 0, len = values.length; i < len; i++ ) {
			data.put(address + i, values[i]);
		}
	}

	@Override
	public void writeString(ModbusWriteFunction function, Integer address, String value,
			String charsetName) {
		// TODO Auto-generated method stub
		super.writeString(function, address, value, charsetName);
	}

	@Override
	public void writeSignedShorts(ModbusWriteFunction function, Integer address, short[] values) {
		Map<Integer, Integer> data = getData();
		for ( int i = 0, len = values.length; i < len; i++ ) {
			data.put(address + i, (int) values[i]);
		}
	}

	@Override
	public Boolean writeDiscreetValues(Integer[] addresses, BitSet bits) {
		// TODO Auto-generated method stub
		return super.writeDiscreetValues(addresses, bits);
	}

	@Override
	public void writeBytes(ModbusWriteFunction function, Integer address, byte[] values) {
		int[] unsigned = new int[(int) Math.ceil(values.length / 2.0)];
		for ( int i = 0; i < values.length; i += 2 ) {
			int v = ((values[i] & 0xFF) << 8);
			if ( i + 1 < values.length ) {
				v |= (values[i + 1] & 0xFF);
			}
			unsigned[i / 2] = v;
		}
		writeUnsignedShorts(function, address, unsigned);
	}

}