/* ==================================================================
 * ModbusTransactionUtils.java - 3/02/2018 8:53:29 AM
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

package net.solarnetwork.node.io.modbus.jamod;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.node.io.modbus.ModbusDataUtils;
import net.solarnetwork.node.io.modbus.ModbusReadFunction;
import net.solarnetwork.node.io.modbus.ModbusWriteFunction;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteCoilResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.BitVector;

/**
 * Utility methods for Modbus actions.
 * 
 * @author matt
 * @version 2.0
 */
public class ModbusTransactionUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ModbusTransactionUtils.class);

	/**
	 * Get the values of a set of "coil" type registers, as a BitSet.
	 * 
	 * <p>
	 * This uses a Modbus function code {@literal 1} request.
	 * </p>
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param address
	 *        the Modbus register addresses to start reading from
	 * @param count
	 *        the count of 16-bit registers to read
	 * @param unitId
	 *        the Modbus unit ID to use in the read request
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @return BitSet, with indexes set from {@literal 0} to a {@code count - 1}
	 */
	public static BitSet readDiscreteValues(final ModbusTransaction trans, final int address,
			final int count, final int unitId, final boolean headless) {
		BitSet result = new BitSet(count);
		ReadCoilsRequest req = new ReadCoilsRequest(address, count);
		req.setUnitID(unitId);
		req.setHeadless();
		trans.setRequest(req);
		try {
			trans.execute();
		} catch ( ModbusException e ) {
			throw new RuntimeException(e);
		}
		ReadCoilsResponse res = (ReadCoilsResponse) trans.getResponse();
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("Got Modbus read coil {} response [{}]", address, res.getCoils());
		}
		for ( int i = 0; i < res.getBitCount(); i++ ) {
			result.set(i, res.getCoilStatus(i));
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read {} Modbus coil {} values: {}", count, address, result);
		}
		return result;
	}

	/**
	 * Get the values of a set of "coil" type registers, as a BitSet.
	 * 
	 * <p>
	 * This uses a Modbus function code {@literal 1} request. The returned set
	 * will have a size equal to {@code addresses.length * count}.
	 * </p>
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param addresses
	 *        the Modbus register addresses to read
	 * @param count
	 *        the count of registers to read with each address
	 * @param unitId
	 *        the Modbus unit ID to use in the read request
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @return BitSet, with each {@code count} indexes for each index in the
	 *         {@code addresses} parameter
	 */
	public static BitSet readDiscreetValues(final ModbusTransaction trans, final int[] addresses,
			final int count, final int unitId, final boolean headless) {
		BitSet result = new BitSet(addresses.length);
		for ( int i = 0, w = 0; i < addresses.length; i++ ) {
			BitSet set = readDiscreteValues(trans, addresses[i], count, unitId, true);
			for ( int j = 0; j < count; j++ ) {
				// map individual bitset index to overall output bitset index
				result.set(w++, set.get(j));
			}
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read Modbus coil {} x {} values: {}", addresses, count, result);
		}
		return result;
	}

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
	 * @param trans
	 *        the Modbus transaction to use
	 * @param addresses
	 *        the Modbus register addresses to start writing to
	 * @param bits
	 *        the bits to write, each index corresponding to an index in
	 *        {@code addresses}
	 * @param unitId
	 *        the Modbus unit ID to use in the read request
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 */
	public static void writeDiscreetValues(final ModbusTransaction trans, final int[] addresses,
			final BitSet bits, final int unitId, final boolean headless) {
		for ( int i = 0; i < addresses.length; i++ ) {
			WriteCoilRequest req = new WriteCoilRequest(addresses[i], bits.get(i));
			req.setUnitID(unitId);
			if ( headless ) {
				req.setHeadless();
			}
			trans.setRequest(req);
			try {
				trans.execute();
			} catch ( ModbusException e ) {
				throw new RuntimeException(e);
			}
			WriteCoilResponse res = (WriteCoilResponse) trans.getResponse();
			if ( LOG.isTraceEnabled() ) {
				LOG.trace("Got write {} response [{}]", addresses[i], res.getCoil());
			}
		}
	}

	/**
	 * Get the values of a set of "input discrete" type registers, as a BitSet.
	 * 
	 * <p>
	 * This uses a Modbus function code {@literal 2} request. The returned
	 * bitset will have {@code count} values set, from {@literal 0} to
	 * {@code count - 1}.
	 * </p>
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param address
	 *        the Modbus register addresses to start reading from
	 * @param count
	 *        the count of registers to read
	 * @param unitId
	 *        the Modbus unit ID to use in the read request
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @return BitSet, with each {@literal 0} to {@code count} indexes
	 */
	public static BitSet readInputDiscreteValues(final ModbusTransaction trans, final int address,
			final int count, final int unitId, final boolean headless) {
		BitSet result = new BitSet(count);
		ReadInputDiscretesRequest req = new ReadInputDiscretesRequest(address, count);
		req.setUnitID(unitId);
		req.setHeadless();
		trans.setRequest(req);
		try {
			trans.execute();
		} catch ( ModbusException e ) {
			throw new RuntimeException(e);
		}
		ReadInputDiscretesResponse res = (ReadInputDiscretesResponse) trans.getResponse();
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("Got Modbus read input discretes {} response [{}]", address, res.getDiscretes());
		}
		for ( int i = 0; i < res.getBitCount(); i++ ) {
			result.set(i, res.getDiscreteStatus(i));
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read {} Modbus input discrete {} values: {}", count, address, result);
		}
		return result;
	}

	/**
	 * Get the values of specific "input" type registers.
	 * 
	 * <p>
	 * This uses a Modbus function code {@code 4} request.
	 * </p>
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param addresses
	 *        the Modbus register addresses to read
	 * @param count
	 *        the number of Modbus "words" to read from each address
	 * @param unitId
	 *        the Modbus unit ID to use in the read request
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @return map of integer addresses to corresponding integer values, there
	 *         should be {@code count} values for each {@code address} read
	 * @see #readUnsignedShorts(ModbusTransaction, int, ModbusReadFunction,
	 *      Integer, int)
	 */
	public static Map<Integer, Integer> readInputValues(ModbusTransaction trans, Integer[] addresses,
			int count, int unitId, boolean headless) {
		Map<Integer, Integer> result = new LinkedHashMap<Integer, Integer>(
				(addresses == null ? 0 : addresses.length) * count);
		for ( int i = 0; i < addresses.length; i++ ) {
			int[] data = readUnsignedShorts(trans, unitId, headless,
					ModbusReadFunction.ReadInputRegister, addresses[i], count);
			if ( data != null ) {
				for ( int j = 0; j < data.length; j++ ) {
					result.put(addresses[i] + j, data[j]);
				}
			}
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read Modbus input registers {} values: {}", addresses, result);
		}
		return result;
	}

	/**
	 * Read a set of "holding" type registers and interpret as a US-ASCII
	 * encoded string.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param address
	 *        the Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus "words" to read
	 * @param unitId
	 *        the Modbus unit ID to use in the read request
	 * @param trim
	 *        if <em>true</em> then remove leading/trailing whitespace from the
	 *        resulting string
	 * @return String from interpreting raw bytes as a US-ASCII encoded string
	 * @see #readString(ModbusTransaction, int, boolean, ModbusReadFunction,
	 *      Integer, int, boolean, String)
	 */
	public static String readASCIIString(final ModbusTransaction trans, final Integer address,
			final int count, final int unitId, final boolean trim) {
		return readString(trans, unitId, true, ModbusReadFunction.ReadHoldingRegister, address, count,
				trim, ModbusDataUtils.ASCII_CHARSET);
	}

	/**
	 * Read a set of "holding" type registers and interpret as a UTF-8 encoded
	 * string.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param address
	 *        the Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus "words" to read
	 * @param unitId
	 *        the Modbus unit ID to use in the read request
	 * @param trim
	 *        if <em>true</em> then remove leading/trailing whitespace from the
	 *        resulting string
	 * @return String from interpreting raw bytes as a UTF-8 encoded string
	 * @see #readString(ModbusTransaction, int, boolean, ModbusReadFunction,
	 *      Integer, int, boolean, String)
	 */
	public static String readUTF8String(final ModbusTransaction trans, final Integer address,
			final int count, final int unitId, final boolean trim) {
		return readString(trans, unitId, true, ModbusReadFunction.ReadHoldingRegister, address, count,
				trim, ModbusDataUtils.UTF8_CHARSET);
	}

	/**
	 * Create a new {@link ModbusRequest} instance appropriate for a given
	 * function, unit ID, address, and count.
	 * 
	 * @param function
	 *        the function to use
	 * @param unitId
	 *        the unit ID
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param address
	 *        the register address to start reading from
	 * @param count
	 *        the count of registers to read
	 * @return a newly created request instance
	 * @throws UnsupportedOperationException
	 *         if the function is not supported
	 */
	public static ModbusRequest modbusReadRequest(ModbusReadFunction function, int unitId,
			boolean headless, int address, int count) {
		ModbusRequest req;
		switch (function) {
			case ReadCoil:
				req = new ReadCoilsRequest(address, count);
				break;

			case ReadDiscreteInput:
				req = new ReadInputDiscretesRequest(address, count);
				break;

			case ReadHoldingRegister:
				req = new ReadMultipleRegistersRequest(address, count);
				break;

			case ReadInputRegister:
				req = new ReadInputRegistersRequest(address, count);
				break;

			default:
				throw new UnsupportedOperationException("Function " + function + " is not supported");

		}
		if ( headless ) {
			req.setHeadless();
		}
		req.setUnitID(unitId);
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("Modbus {} {} @ {} x {}", unitId, function, address, count);
		}
		return req;
	}

	/**
	 * Create a new {@link ModbusRequest} suitable for writing to non-discrete
	 * registers.
	 * 
	 * @param function
	 *        the function to use
	 * @param unitId
	 *        the unit ID
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param address
	 *        the address to start writing to
	 * @return a newly created request instance
	 * @throws UnsupportedOperationException
	 *         if the function is not supported
	 */
	public static ModbusRequest modbusWriteRequest(ModbusWriteFunction function, int unitId,
			boolean headless, int address, int count) {
		ModbusRequest req;
		switch (function) {
			case WriteHoldingRegister:
				req = new WriteSingleRegisterRequest(address, null);
				break;

			case WriteMultipleHoldingRegisters:
				req = new WriteMultipleRegistersRequest(address, null);
				break;

			default:
				throw new UnsupportedOperationException("Function " + function + " is not supported");

		}
		if ( headless ) {
			req.setHeadless();
		}
		req.setUnitID(unitId);
		if ( LOG.isTraceEnabled() ) {
			LOG.trace("Modbus {} {} @ {} x {}", unitId, function, address, count);
		}
		return req;
	}

	/**
	 * Get the values of specific registers as an array of signed 16-bit shorts.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the unit ID
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param function
	 *        the function to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @return array of register values; the result will have a length equal to
	 *         {@code count}
	 */
	public static short[] readSignedShorts(ModbusTransaction trans, int unitId, boolean headless,
			ModbusReadFunction function, Integer address, int count) {
		ModbusRequest req = modbusReadRequest(function, unitId, headless, address, count);
		trans.setRequest(req);
		try {
			trans.execute();
		} catch ( ModbusException e ) {
			throw new RuntimeException(e);
		}
		ModbusResponse response = trans.getResponse();
		short[] result = new short[count];
		if ( response instanceof ReadMultipleRegistersResponse ) {
			ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) response;
			for ( int w = 0, len = res.getWordCount(); w < len; w += 1 ) {
				result[w] = res.getRegister(w).toShort();
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		} else if ( response instanceof ReadInputRegistersResponse ) {
			ReadInputRegistersResponse res = (ReadInputRegistersResponse) response;
			for ( int w = 0, len = res.getWordCount(); w < len; w += 1 ) {
				result[w] = res.getRegister(w).toShort();
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		} else if ( response instanceof ReadInputDiscretesResponse ) {
			ReadInputDiscretesResponse res = (ReadInputDiscretesResponse) response;
			BitVector bv = res.getDiscretes();
			for ( int w = 0; w < count; w += 1 ) {
				result[w] = bv.getBit(w) ? (short) 1 : (short) 0;
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		} else if ( response instanceof ReadCoilsResponse ) {
			ReadCoilsResponse res = (ReadCoilsResponse) response;
			BitVector bv = res.getCoils();
			for ( int w = 0; w < count; w += 1 ) {
				result[w] = bv.getBit(w) ? (short) 1 : (short) 0;
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read Modbus register {} count {} values: {}",
					new Object[] { address, count, result });
		}
		return result;
	}

	/**
	 * Write signed 16-bit short values to registers.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the unit ID
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param function
	 *        the function to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @param values
	 *        the signed 16-bit values to write
	 */
	public static void writeSignedShorts(ModbusTransaction trans, int unitId, boolean headless,
			ModbusWriteFunction function, Integer address, short[] values) {
		int len = values.length;
		int[] unsigned = new int[len];
		for ( int i = 0; i < len; i += 1 ) {
			unsigned[i] = values[i] & 0xFFFF;
		}
		writeUnsignedShorts(trans, unitId, headless, function, address, unsigned);
	}

	/**
	 * Get the values of specific registers as an array of unsigned 16-bit
	 * shorts.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the Modbus unit ID to direct the request to
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @return array of register values; the result will have a length equal to
	 *         {@code count}
	 */
	public static int[] readUnsignedShorts(ModbusTransaction trans, int unitId, boolean headless,
			ModbusReadFunction function, Integer address, int count) {
		ModbusRequest req = modbusReadRequest(function, unitId, headless, address, count);
		trans.setRequest(req);
		try {
			trans.execute();
		} catch ( ModbusException e ) {
			throw new RuntimeException(e);
		}
		ModbusResponse response = trans.getResponse();
		int[] result = new int[count];
		if ( response instanceof ReadMultipleRegistersResponse ) {
			ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) response;
			for ( int w = 0, len = res.getWordCount(); w < len; w += 1 ) {
				result[w] = res.getRegisterValue(w);
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		} else if ( response instanceof ReadInputRegistersResponse ) {
			ReadInputRegistersResponse res = (ReadInputRegistersResponse) response;
			for ( int w = 0, len = res.getWordCount(); w < len; w += 1 ) {
				result[w] = res.getRegisterValue(w);
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		} else if ( response instanceof ReadInputDiscretesResponse ) {
			ReadInputDiscretesResponse res = (ReadInputDiscretesResponse) response;
			BitVector bv = res.getDiscretes();
			for ( int w = 0; w < count; w += 1 ) {
				result[w] = bv.getBit(w) ? 1 : 0;
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		} else if ( response instanceof ReadCoilsResponse ) {
			ReadCoilsResponse res = (ReadCoilsResponse) response;
			BitVector bv = res.getCoils();
			for ( int w = 0; w < count; w += 1 ) {
				result[w] = bv.getBit(w) ? 1 : 0;
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + w, result[w]);
				}
			}
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read Modbus register {} count {} values: {}",
					new Object[] { address, count, result });
		}
		return result;
	}

	/**
	 * Write unsigned 16-bit short values to registers.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the Modbus unit ID to direct the request to
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start writing to
	 * @param values
	 *        the unsigned 16-bit values to write
	 */
	public static void writeUnsignedShorts(ModbusTransaction trans, int unitId, boolean headless,
			ModbusWriteFunction function, Integer address, int[] values) {
		ModbusRequest request = modbusWriteRequest(function, unitId, headless, address,
				(values != null ? values.length : 0));
		if ( request instanceof WriteMultipleRegistersRequest ) {
			WriteMultipleRegistersRequest req = (WriteMultipleRegistersRequest) request;
			int len = values.length;
			Register[] regs = new Register[len];
			for ( int i = 0; i < len; i += 1 ) {
				regs[i] = new SimpleRegister(values[i]);
			}
			req.setRegisters(regs);
		} else if ( request instanceof WriteSingleRegisterRequest ) {
			WriteSingleRegisterRequest req = (WriteSingleRegisterRequest) request;
			req.setRegister(new SimpleRegister(values[0]));
		} else {
			throw new UnsupportedOperationException("Funciton " + function + " not supported");
		}

		trans.setRequest(request);
		try {
			trans.execute();
		} catch ( ModbusException e ) {
			throw new RuntimeException(e);
		}

		if ( LOG.isTraceEnabled() ) {
			ModbusResponse response = trans.getResponse();
			if ( response instanceof WriteMultipleRegistersResponse ) {
				WriteMultipleRegistersResponse res = (WriteMultipleRegistersResponse) response;
				LOG.trace("Got write {} response count {}", address, res.getWordCount());
			} else if ( response instanceof WriteSingleRegisterResponse ) {
				WriteSingleRegisterResponse res = (WriteSingleRegisterResponse) response;
				LOG.trace("Got write {} response [{}]", address, res.getRegisterValue());
			}
		}
	}

	/**
	 * Get the raw bytes of specific registers.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the Modbus unit ID to direct the request to
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start reading from
	 * @param count
	 *        the number of Modbus 16-bit registers to read
	 * @return array of register bytes; the result will have a length equal to
	 *         {@code count * 2}
	 */
	public static byte[] readBytes(ModbusTransaction trans, int unitId, boolean headless,
			ModbusReadFunction function, Integer address, int count) {
		byte[] result = new byte[count * 2];
		ModbusRequest req = modbusReadRequest(function, unitId, headless, address, count);
		trans.setRequest(req);
		try {
			trans.execute();
		} catch ( ModbusException e ) {
			throw new RuntimeException(e);
		}
		ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
		InputRegister[] registers = res.getRegisters();
		if ( registers != null ) {

			for ( int i = 0; i < registers.length; i++ ) {
				if ( LOG.isTraceEnabled() ) {
					LOG.trace("Got Modbus read {} response {}", address + i, res.getRegisterValue(i));
				}
				System.arraycopy(registers[i].toBytes(), 0, result, i * 2, 2);
			}
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read Modbus register {} count {} bytes: {}",
					new Object[] { address, count, result });
		}
		return result;
	}

	/**
	 * Write raw byte values to registers.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the Modbus unit ID to direct the request to
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start writing to
	 * @param values
	 *        the byte values to write
	 */
	public static void writeBytes(ModbusTransaction trans, int unitId, boolean headless,
			ModbusWriteFunction function, Integer address, byte[] values) {
		int[] unsigned = new int[(int) Math.ceil(values.length / 2.0)];
		for ( int i = 0; i < values.length; i += 2 ) {
			int v = ((values[i] & 0xFF) << 8);
			if ( i + 1 < values.length ) {
				v |= (values[i + 1] & 0xFF);
			}
			unsigned[i / 2] = v;
		}
		writeUnsignedShorts(trans, unitId, headless, function, address, unsigned);
	}

	/**
	 * Read a set of registers as bytes and interpret as a string.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the Modbus unit ID to direct the request to
	 * @param headless
	 *        {@literal true} for headless (serial) mode
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
	 * @see #readBytes(ModbusTransaction, int, boolean, ModbusReadFunction,
	 *      Integer, int)
	 */
	public static String readString(ModbusTransaction trans, int unitId, boolean headless,
			ModbusReadFunction function, Integer address, int count, boolean trim, String charsetName) {
		final byte[] bytes = readBytes(trans, unitId, headless, function, address, count);
		String result = null;
		if ( bytes != null ) {
			try {
				result = new String(bytes, charsetName);
				if ( trim ) {
					result = result.trim();
				}
			} catch ( UnsupportedEncodingException e ) {
				throw new RuntimeException(e);
			}
		}
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Read Modbus input register {} count {} string: {}",
					new Object[] { address, count, result });
		}
		return result;
	}

	/**
	 * Write a string as raw byte values to registers.
	 * 
	 * @param trans
	 *        the Modbus transaction to use
	 * @param unitId
	 *        the Modbus unit ID to direct the request to
	 * @param headless
	 *        {@literal true} for headless (serial) mode
	 * @param function
	 *        the Modbus function code to use
	 * @param address
	 *        the 0-based Modbus register address to start writing to
	 * @param value
	 *        the string value to write
	 * @param charsetName
	 *        the character set to interpret the bytes as
	 */
	public static void writeString(ModbusTransaction trans, int unitId, boolean headless,
			ModbusWriteFunction function, Integer address, String value, String charsetName) {
		try {
			byte[] bytes = value.getBytes(charsetName);
			writeBytes(trans, unitId, headless, function, address, bytes);
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}

	}
}
