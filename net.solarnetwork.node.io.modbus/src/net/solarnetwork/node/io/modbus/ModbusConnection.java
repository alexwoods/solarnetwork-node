/* ==================================================================
 * ModbusConnection.java - Jul 29, 2014 11:19:18 AM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
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

import java.io.IOException;
import java.util.BitSet;
import net.solarnetwork.node.LockTimeoutException;

/**
 * High level Modbus connection API.
 * 
 * <p>
 * This API aims to simplify accessing Modbus capable devices without having any
 * direct dependency on Jamod (or any other Modbus implementation).
 * </p>
 * 
 * @author matt
 * @version 2.0
 * @since 2.0
 */
public interface ModbusConnection {

	/**
	 * Get the Modbus Unit ID this device represents.
	 * 
	 * @return the unit ID
	 */
	int getUnitId();

	/**
	 * Open the connection, if it is not already open. The connection must be
	 * opened before calling any of the other methods in this API.
	 * 
	 * @throws IOException
	 *         if the connection cannot be opened
	 */
	void open() throws IOException, LockTimeoutException;

	/**
	 * Close the connection, if it is open.
	 */
	void close();

	/**
	 * Get the values of a set of "coil" type registers, as a BitSet.
	 * 
	 * <p>
	 * This uses a Modbus function code {@literal 1} request.
	 * </p>
	 * 
	 * @param address
	 *        the 0-based Modbus register address to read
	 * @param count
	 *        the count of discreet registers to read
	 * @return BitSet, with indexes set from {@literal 0} to a {@code count - 1}
	 * @since 1.1
	 */
	BitSet readDiscreetValues(int address, int count);

	/**
	 * Get the values of a set of "coil" type registers, as a BitSet.
	 * 
	 * <p>
	 * This uses a Modbus function code {@literal 1} request. The returned set
	 * will have a size equal to {@code addresses.length * count}.
	 * </p>
	 * 
	 * @param addresses
	 *        the 0-based Modbus register addresses to read
	 * @param count
	 *        the count of coils to read with each address
	 * @return BitSet, with each {@code count} indexes for each index in the
	 *         {@code addresses} parameter
	 */
	BitSet readDiscreetValues(int[] addresses, int count);

	/**
	 * Write values of a set of "coil" type registers, via a BitSet.
	 * 
	 * <p>
	 * This uses a Modbus function code {@literal 5} request, once for each
	 * address in {@code addresses}. Each address at index <em>i</em>
	 * corresponds to the value of bit at index <em>i</em>. Thus bits
	 * {@literal 0} to {@code addresses.length - 1} are used.
	 * </p>
	 * 
	 * @param addresses
	 *        the Modbus register addresses to start writing to
	 * @param bits
	 *        the bits to write, each index corresponding to an index in
	 *        {@code addresses}
	 * @return {@literal true} if the write succeeded
	 */
	void writeDiscreetValues(int[] addresses, BitSet bits);

	/**
	 * Get the values of a set of "input discrete" type registers, as a BitSet.
	 * 
	 * <p>
	 * This uses a Modbus function code {@literal 2} request. The returned
	 * bitset will have {@code count} values set, from {@literal 0} to
	 * {@code count - 1}.
	 * </p>
	 * 
	 * @param address
	 *        the Modbus register addresses to start reading from
	 * @param count
	 *        the count of registers to read
	 * @return BitSet, with each {@literal 0} to {@code count} indexes
	 */
	BitSet readInputDiscreteValues(int address, int count);

	/**
	 * Get the values of specific 16-bit Modbus registers as an array of 16-bit
	 * words.
	 * 
	 * <p>
	 * Note that the raw short values can be treated as unsigned shorts by
	 * converting them to integers, like
	 * {@code int unsigned = ((int)s) && 0xFFFF}, or by calling
	 * {@link Short#toUnsignedInt(short)}. Thus the values returned by this
	 * method are technically the same as those returned by
	 * {@link #readUnsignedShorts(ModbusReadFunction, Integer, int)}, without
	 * having been cast to ints.
	 * </p>
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @return array of register values; the result will have a length equal to
	 *         {@code count}
	 * @since 2.0
	 */
	short[] readWords(ModbusReadFunction function, int address, int count);

	/**
	 * Write 16-bit word values to 16-bit Modbus registers.
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start writing to
	 * @param values
	 *        the 16-bit values to write
	 * @since 2.0
	 */
	void writeWords(ModbusWriteFunction function, int address, short[] values);

	/**
	 * Get the values of specific registers as an array of unsigned 16-bit
	 * shorts.
	 * 
	 * <p>
	 * Note that the raw int values can be treated as signed shorts by casting
	 * them to shorts, like {@code short signed = (short)s}. Thus the values
	 * returned by this method are technically the same as those returned by
	 * {@link #readWords(ModbusReadFunction, int, int)}, having been cast to
	 * ints.
	 * </p>
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @return array of register values; the result will have a length equal to
	 *         {@code count}
	 * @since 1.2
	 */
	int[] readUnsignedShorts(ModbusReadFunction function, Integer address, int count);

	/**
	 * Write unsigned 16-bit short values to registers.
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start writing to
	 * @param values
	 *        the unsigned 16-bit values to write
	 * @since 1.2
	 */
	void writeUnsignedShorts(ModbusWriteFunction function, Integer address, int[] values);

	/**
	 * Get the raw bytes of specific registers.
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @return array of register bytes; the result will have a length equal to
	 *         {@code count * 2}
	 * @since 1.2
	 */
	byte[] readBytes(ModbusReadFunction function, Integer address, int count);

	/**
	 * Write raw byte values to registers.
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start writing to;
	 *        {@code values.length * 2} 16-bit registers will be written
	 * @param values
	 *        the byte values to write
	 * @since 1.2
	 */
	void writeBytes(ModbusWriteFunction function, Integer address, byte[] values);

	/**
	 * Read a set of registers as bytes and interpret as a string.
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @param trim
	 *        if <em>true</em> then remove leading/trailing whitespace from the
	 *        resulting string
	 * @param charsetName
	 *        the character set to interpret the bytes as
	 * @return String from interpreting raw bytes as a string
	 * @see #readBytes(ModbusReadFunction, Integer, int)
	 * @since 1.2
	 */
	String readString(ModbusReadFunction function, Integer address, int count, boolean trim,
			String charsetName);

	/**
	 * Write a string as raw byte values to registers.
	 * 
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start writing to
	 * @param value
	 *        the string value to write
	 * @param charsetName
	 *        the character set to interpret the bytes as
	 * @since 1.2
	 */
	void writeString(ModbusWriteFunction function, Integer address, String value, String charsetName);

}
