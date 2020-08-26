/* ==================================================================
 * AE500NxDatumDataSource.java - 23/04/2020 2:36:49 pm
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

package net.solarnetwork.node.datum.ae.ae500nx;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.MultiDatumDataSource;
import net.solarnetwork.node.domain.GeneralNodePVEnergyDatum;
import net.solarnetwork.node.hw.ae.inverter.nx.AE500NxData;
import net.solarnetwork.node.hw.ae.inverter.nx.AE500NxDataAccessor;
import net.solarnetwork.node.hw.ae.inverter.nx.AE500NxFault;
import net.solarnetwork.node.hw.ae.inverter.nx.AE500NxSystemStatus;
import net.solarnetwork.node.io.modbus.ModbusConnection;
import net.solarnetwork.node.io.modbus.support.ModbusDataDatumDataSourceSupport;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.util.StringUtils;

/**
 * {@link DatumDataSource} for the AE 500NX series inverter.
 * 
 * @author matt
 * @version 1.1
 */
public class AE500NxDatumDataSource extends ModbusDataDatumDataSourceSupport<AE500NxData>
		implements DatumDataSource<GeneralNodePVEnergyDatum>,
		MultiDatumDataSource<GeneralNodePVEnergyDatum>, SettingSpecifierProvider {

	private String sourceId = "AE 500NX";

	/**
	 * Default constructor.
	 */
	public AE500NxDatumDataSource() {
		this(new AE500NxData());
	}

	/**
	 * Construct with a specific sample data instance.
	 * 
	 * @param sample
	 *        the sample data to use
	 */
	public AE500NxDatumDataSource(AE500NxData sample) {
		super(sample);
	}

	@Override
	protected void refreshDeviceInfo(ModbusConnection connection, AE500NxData sample) {
		sample.readConfigurationData(connection);
	}

	@Override
	protected void refreshDeviceData(ModbusConnection connection, AE500NxData sample) {
		sample.readInverterData(connection);
	}

	@Override
	public Class<? extends GeneralNodePVEnergyDatum> getDatumType() {
		return GeneralNodePVEnergyDatum.class;
	}

	@Override
	public GeneralNodePVEnergyDatum readCurrentDatum() {
		final long start = System.currentTimeMillis();
		try {
			final AE500NxData currSample = getCurrentSample();
			if ( currSample == null ) {
				return null;
			}
			AE500NxDatum d = new AE500NxDatum(currSample);
			d.setSourceId(resolvePlaceholders(sourceId));
			if ( currSample.getDataTimestamp() >= start ) {
				// we read from the device
				postDatumCapturedEvent(d);
			}
			return d;
		} catch ( IOException e ) {
			log.error("Communication problem reading source {} from AE 500NX device {}: {}",
					this.sourceId, modbusDeviceName(), e.getMessage());
			return null;
		}
	}

	@Override
	public Class<? extends GeneralNodePVEnergyDatum> getMultiDatumType() {
		return GeneralNodePVEnergyDatum.class;
	}

	@Override
	public Collection<GeneralNodePVEnergyDatum> readMultipleDatum() {
		GeneralNodePVEnergyDatum datum = readCurrentDatum();
		if ( datum != null ) {
			return Collections.singletonList(datum);
		}
		return Collections.emptyList();
	}

	// SettingSpecifierProvider

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.datum.ae.ae500nx";
	}

	@Override
	public String getDisplayName() {
		return "Advanced Energy 500NX Meter";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = new ArrayList<>(12);
		results.add(new BasicTitleSettingSpecifier("info", getInfoMessage(), true));
		results.add(new BasicTitleSettingSpecifier("sample", getSampleMessage(getSample()), true));

		results.addAll(getIdentifiableSettingSpecifiers());
		results.addAll(getModbusNetworkSettingSpecifiers());

		AE500NxDatumDataSource defaults = new AE500NxDatumDataSource();
		results.add(new BasicTextFieldSettingSpecifier("sampleCacheMs",
				String.valueOf(defaults.getSampleCacheMs())));
		results.add(new BasicTextFieldSettingSpecifier("sourceId", defaults.sourceId));

		return results;
	}

	private String getInfoMessage() {
		String msg = null;
		try {
			msg = getDeviceInfoMessage();
		} catch ( RuntimeException e ) {
			log.debug("Error reading info: {}", e.getMessage());
		}
		return (msg == null ? "N/A" : msg);
	}

	private String getSampleMessage(AE500NxDataAccessor data) {
		if ( data.getDataTimestamp() < 1 ) {
			return "N/A";
		}
		StringBuilder buf = new StringBuilder();
		buf.append("W = ").append(data.getActivePower());
		buf.append("; Wh = ").append(data.getActiveEnergyDelivered());

		Set<AE500NxSystemStatus> status = data.getSystemStatus();
		if ( status != null && !status.isEmpty() ) {
			buf.append("; status = ").append(StringUtils.commaDelimitedStringFromCollection(status));
		}

		Set<AE500NxFault> faults = data.getFaults();
		if ( faults != null && !faults.isEmpty() ) {
			buf.append("; faults = ").append(StringUtils.commaDelimitedStringFromCollection(faults));
		}

		buf.append("; sampled at ").append(
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT).format(
						Instant.ofEpochMilli(data.getDataTimestamp()).atZone(ZoneId.systemDefault())));
		return buf.toString();
	}

	/**
	 * Set the source ID to use for returned datum.
	 * 
	 * @param sourceId
	 *        the source ID to use; defaults to {@literal modbus}
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
}
