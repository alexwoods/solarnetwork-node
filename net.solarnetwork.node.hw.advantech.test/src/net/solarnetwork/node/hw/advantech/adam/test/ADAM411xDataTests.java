/* ==================================================================
 * ADAM411xDataTests.java - 22/11/2018 7:16:47 AM
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

package net.solarnetwork.node.hw.advantech.adam.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.node.hw.advantech.adam.ADAM411xData;
import net.solarnetwork.node.hw.advantech.adam.InputRangeType;
import net.solarnetwork.node.io.modbus.ModbusData.ModbusDataUpdateAction;
import net.solarnetwork.node.io.modbus.ModbusData.MutableModbusData;
import net.solarnetwork.node.test.DataUtils;

/**
 * Test cases for the {@link ADAM411xData} class.
 * 
 * @author matt
 * @version 1.0
 */
public class ADAM411xDataTests {

	private static final Logger log = LoggerFactory.getLogger(ADAM411xDataTests.class);

	private static Map<Integer, Integer> parseTestData(String resource) {
		try {
			return DataUtils.parseModbusHexRegisterMappingLines(new BufferedReader(
					new InputStreamReader(ADAM411xDataTests.class.getResourceAsStream(resource))));
		} catch ( IOException e ) {
			log.error("Error reading modbus data resource [{}]", resource, e);
			return Collections.emptyMap();
		}
	}

	private ADAM411xData getDataInstance(String resource) {
		Map<Integer, Integer> registers = parseTestData(resource);
		ADAM411xData data = new ADAM411xData();
		data.performUpdates(new ModbusDataUpdateAction() {

			@Override
			public boolean updateModbusData(MutableModbusData m) {
				m.saveDataMap(registers);
				return true;
			}
		});
		return data;
	}

	@Test
	public void adam4117_01() {
		ADAM411xData data = getDataInstance("test-4117-01.txt");
		assertThat("Model", data.getModelName(), equalTo("4117"));
		assertThat("Firmware revision", data.getFirmwareRevision(), equalTo("A104"));
		assertThat("Enabled channels", data.getEnabledChannelNumbers(),
				contains(0, 1, 2, 3, 4, 5, 6, 7));
		for ( int i = 0; i < 8; i++ ) {
			assertThat("Channel " + i + " type", data.getChannelType(i),
					equalTo(InputRangeType.ZeroToOneHundredFiftyMilliVolts));
		}
		BigDecimal[] expected = new BigDecimal[] {
			// @formatter:off
			new BigDecimal("0.07657"), // 684; (684 + 32768) / 65535 * 150
			new BigDecimal("0.07666"), // 724
			new BigDecimal("0.07678"), // 778
			// @formatter:on
		};
		for ( int i = 0; i < expected.length; i++ ) {
			BigDecimal val = data.getChannelValue(i).setScale(5, RoundingMode.HALF_UP);
			assertThat("Channel " + i + " value", val, equalTo(expected[i]));
		}
	}

	@Test
	public void adam4117_02() {
		ADAM411xData data = getDataInstance("test-4117-02.txt");
		assertThat("Model", data.getModelName(), equalTo("4117"));
		assertThat("Firmware revision", data.getFirmwareRevision(), equalTo("A104"));
		assertThat("Enabled channels", data.getEnabledChannelNumbers(), contains(0, 1, 2, 3));
		for ( int i = 0; i < 8; i++ ) {
			assertThat("Channel " + i + " type", data.getChannelType(i),
					equalTo(InputRangeType.PlusMinusOneHundredFiftyMilliVolts));
		}
		BigDecimal[] expected = new BigDecimal[] {
			// @formatter:off
			new BigDecimal("-0.14770"), // -32265; (-32265 + 32768) / 65535 * 300 - 150
			new BigDecimal("-0.14694"), // -32099
			new BigDecimal("-0.14710"), // -32134
			new BigDecimal("-0.14743"), // -32206
			// @formatter:on
		};
		for ( int i = 0; i < expected.length; i++ ) {
			BigDecimal val = data.getChannelValue(i).setScale(5, RoundingMode.HALF_UP);
			assertThat("Channel " + i + " value", val, equalTo(expected[i]));
		}
	}

	@Test
	public void adam4118_01() {
		ADAM411xData data = getDataInstance("test-4118-01.txt");
		assertThat("Model", data.getModelName(), equalTo("4118"));
		assertThat("Firmware revision", data.getFirmwareRevision(), equalTo("A106"));
		assertThat("Enabled channels", data.getEnabledChannelNumbers(),
				contains(0, 1, 2, 3, 4, 5, 6, 7));
		InputRangeType[] expectedTypes = new InputRangeType[] {
				// @formatter:off
				InputRangeType.TypeTThermocouple,
				InputRangeType.TypeTThermocouple,
				InputRangeType.PlusMinusFifteenMilliVolts,
				InputRangeType.FourToTwentyMilliAmps,
				InputRangeType.FourToTwentyMilliAmps,
				InputRangeType.PlusMinusFifteenMilliVolts,
				InputRangeType.PlusMinusFifteenMilliVolts,
				InputRangeType.PlusMinusFifteenMilliVolts,
				// @formatter:on
		};
		for ( int i = 0; i < 8; i++ ) {
			assertThat("Channel " + i + " type", data.getChannelType(i), equalTo(expectedTypes[i]));
		}
		BigDecimal[] expected = new BigDecimal[] {
				// @formatter:off
				new BigDecimal("171.99011"), // 14089; (14089 / 32767) * 400
				new BigDecimal("172.62489"), // 14141; (14141 / 32767) * 400
				new BigDecimal("-0.01451"), // -31698; (-31698 + 32768) / 65535 * 30 - 15
				new BigDecimal("0.01200"), // -1; (-1 + 32768) / 65535 * (20 - 4) + 4
				new BigDecimal("0.01200"), // -1 (-1 + 32768) / 65535 * (20 - 4) + 4
				new BigDecimal("-0.01444"), // -31550; (-31550 + 32768) / 65535 * 30 - 15
				new BigDecimal("-0.01119"), // -24445
				new BigDecimal("-0.01165"), // -25458
				// @formatter:on
		};
		for ( int i = 0; i < expected.length; i++ ) {
			BigDecimal val = data.getChannelValue(i).setScale(5, RoundingMode.HALF_UP);
			assertThat("Channel " + i + " value", val, equalTo(expected[i]));
		}
	}

}