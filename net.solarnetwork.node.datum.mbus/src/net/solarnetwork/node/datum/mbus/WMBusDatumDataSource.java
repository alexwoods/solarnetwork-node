/* ==================================================================
 * WMBusDatumDataSource.java - 06/07/2020 13:09:29 pm
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

package net.solarnetwork.node.datum.mbus;

import java.util.Date;
import java.util.List;
import java.util.Map;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.io.mbus.MBusData;
import net.solarnetwork.node.io.mbus.MBusMessage;
import net.solarnetwork.node.io.mbus.MBusMessageHandler;
import net.solarnetwork.node.io.mbus.support.WMBusDeviceDatumDataSourceSupport;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.util.StringUtils;

public class WMBusDatumDataSource extends WMBusDeviceDatumDataSourceSupport
		implements DatumDataSource<GeneralNodeDatum>, SettingSpecifierProvider, MBusMessageHandler {

	private String sourceId;
	private MBusPropertyConfig[] propConfigs;

	// Partial message, awaiting more messages
	private MBusData partialData = null;
	// Latest complete data
	private MBusData latestData = null;
	private final Object dataLock = new Object();

	public WMBusDatumDataSource() {
		super();
		sourceId = "wmbus";
	}

	/**
	 * Set the property configurations to use.
	 * 
	 * @param propConfigs
	 *        the configs to use
	 */
	public void setPropConfigs(MBusPropertyConfig[] propConfigs) {
		this.propConfigs = propConfigs;
	}

	@Override
	public GeneralNodeDatum readCurrentDatum() {
		final MBusData currSample = getCurrentSample();
		if ( currSample == null ) {
			return null;
		}
		GeneralNodeDatum d = new GeneralNodeDatum();
		d.setCreated(new Date(currSample.getDataTimestamp()));
		d.setSourceId(sourceId);
		//populateDatumProperties(currSample, d, propConfigs);
		//populateDatumProperties(currSample, d, virtualMeterConfigs);
		//populateDatumProperties(currSample, d, expressionConfigs);
		return d;
	}

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.datum.mbus";
	}

	@Override
	public String getDisplayName() {
		return "Generic Wireless M-Bus Device";
	}

	@Override
	public void handleMessage(MBusMessage message) {
		synchronized ( dataLock ) {
			if ( message.moreRecordsFollow ) {
				if ( partialData == null ) {
					partialData = new MBusData(message);
				} else {
					partialData.addRecordsFrom(message);
				}
			} else {
				if ( partialData == null ) {
					latestData = new MBusData(message);
				} else {
					latestData = partialData;
					latestData.addRecordsFrom(message);
					partialData = null;
				}
			}
		}
	}

	private MBusData getCurrentSample() {
		synchronized ( dataLock ) {
			if ( latestData == null ) {
				return null;
			}
			return new MBusData(latestData);
		}
	}

	private String getSampleMessage(MBusData sample) {
		if ( sample.getDataTimestamp() < 1 ) {
			return "N/A";
		}

		GeneralNodeDatum d = new GeneralNodeDatum();
		//		populateDatumProperties(sample, d, propConfigs);

		Map<String, ?> data = d.getSampleData();
		if ( data == null || data.isEmpty() ) {
			return "No data.";
		}

		StringBuilder buf = new StringBuilder();
		buf.append(StringUtils.delimitedStringFromMap(data));
		//		buf.append("; sampled at ")
		//				.append(DateTimeFormat.forStyle("LS").print(new DateTime(sample.getDataTimestamp())));
		return buf.toString();
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = getIdentifiableSettingSpecifiers();

		results.add(0,
				new BasicTitleSettingSpecifier("sample", getSampleMessage(getCurrentSample()), true));

		results.addAll(getWMBusNetworkSettingSpecifiers());

		/*
		 * WMBusDatumDataSource defaults = new WMBusDatumDataSource();
		 * results.add(new BasicTextFieldSettingSpecifier("sourceId",
		 * defaults.sourceId)); results.add(new
		 * BasicTextFieldSettingSpecifier("sampleCacheMs",
		 * String.valueOf(defaults.sampleCacheMs))); results.add(new
		 * BasicTextFieldSettingSpecifier("maxReadWordCount",
		 * String.valueOf(defaults.maxReadWordCount)));
		 * 
		 * // drop-down menu for word order BasicMultiValueSettingSpecifier
		 * wordOrderSpec = new BasicMultiValueSettingSpecifier( "wordOrderKey",
		 * String.valueOf(defaults.getWordOrder().getKey())); Map<String,
		 * String> wordOrderTitles = new LinkedHashMap<String, String>(2); for (
		 * ModbusWordOrder e : ModbusWordOrder.values() ) {
		 * wordOrderTitles.put(String.valueOf(e.getKey()), e.toDisplayString());
		 * } wordOrderSpec.setValueTitles(wordOrderTitles);
		 * results.add(wordOrderSpec);
		 */

		return results;
	}

	@Override
	public Class<? extends GeneralNodeDatum> getDatumType() {
		return GeneralNodeDatum.class;
	}

}