/* ===================================================================
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
 * ===================================================================
 */

package net.solarnetwork.node.weather.ibm.wc.test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.node.weather.ibm.wc.BasicWCClient;
import net.solarnetwork.node.weather.ibm.wc.WCClient;
import net.solarnetwork.node.weather.ibm.wc.WCHourlyDatum;
import net.solarnetwork.node.weather.ibm.wc.WCHourlyDatumDataSource;

/**
 * Tests the parsing for hourly forecasts using downloaded JSON.
 * 
 * @author matt frost
 *
 */
public class WCHourlyDatumDataSourceTests {

	private static final String TEST_SOURCE_ID = "src.test";
	private static final String TEST_PERIOD = "1day";
	private static final String TEST_KEY = "key.test";
	private static final String TEST_LOC = "loc.test";

	private WCHourlyDatumDataSource hourlyService;

	private WCClient client;

	@Before
	public void additionalSetup() {

		client = EasyMock.createMock(WCClient.class);

		hourlyService = new WCHourlyDatumDataSource();
		hourlyService.setClient(client);
		hourlyService.setDatumPeriod(TEST_PERIOD);
		hourlyService.setApiKey(TEST_KEY);
		hourlyService.setLocationIdentifier(TEST_LOC);

	}

	@After
	public void finish() {
		EasyMock.verify(client);
	}

	private void replayAll() {
		EasyMock.replay(client);
	}

	@Test
	public void defaultValues() {
		replayAll();
		WCHourlyDatumDataSource ds = new WCHourlyDatumDataSource();
		assertTrue("Client available", ds.getClient() instanceof BasicWCClient);
	}

	@Test
	public void readMultipleDatum() {

		final WCHourlyDatum datum = new WCHourlyDatum();
		datum.setCreated(new Date(System.currentTimeMillis()));
		datum.setSourceId(TEST_SOURCE_ID);
		final Collection<WCHourlyDatum> datumList = Arrays.asList(datum);

		// followed by forecast
		expect(hourlyService.readMultipleDatum()).andReturn(datumList);

		replayAll();

		Collection<WCHourlyDatum> result = hourlyService.readMultipleDatum();
		assertEquals("Forecast list", Arrays.asList(datum), result);
	}

}
