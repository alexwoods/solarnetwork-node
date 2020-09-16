/* ==================================================================
 * WebBoxDeviceDataSource.java - 15/09/2020 2:57:24 PM
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

package net.solarnetwork.node.datum.sma.webbox;

import static net.solarnetwork.util.OptionalService.service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.hw.sma.domain.SmaDeviceDataAccessor;
import net.solarnetwork.node.hw.sma.modbus.webbox.WebBoxDevice;
import net.solarnetwork.node.hw.sma.modbus.webbox.WebBoxOperations;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTitleSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.FilterableService;
import net.solarnetwork.util.OptionalService;

/**
 * {@link DatumDataSource} for a {@link WebBoxDevice}.
 * 
 * @author matt
 * @version 1.0
 */
public class WebBoxDeviceDataSource extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeDatum>, SettingSpecifierProvider {

	/** The {@code sourceId} property default value. */
	public static final String DEFAULT_SOURCE_ID = "WebBox Device";

	/** The WebBox service {@literal UID} property filter default value. */
	public static final String DEFAULT_WEBBOX_UID = "WebBox";

	/** The {@code sampleCacheMs} property default value. */
	public static final long DEFAULT_SAMPLE_CACHE_MS = 5000L;

	private final OptionalService<WebBoxOperations> webBox; // also FilterableService

	private String sourceId = DEFAULT_SOURCE_ID;
	private long sampleCacheMs = 5000;
	private Integer unitId;

	private SmaDeviceDataAccessor lastSample;

	/**
	 * Constructor.
	 * 
	 * @param webBox
	 *        the {@link WebBoxOperations} to use; must also implement
	 *        {@link FilterableService}
	 */
	public WebBoxDeviceDataSource(OptionalService<WebBoxOperations> webBox) {
		super();
		if ( !(webBox instanceof FilterableService) ) {
			throw new IllegalArgumentException(
					"The provided OptionalService must also implement FilterableService.");
		}
		this.webBox = webBox;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WebBoxDeviceDataSource{webBox=");
		builder.append(service(webBox));
		builder.append(", sourceId=");
		builder.append(sourceId);
		builder.append(", unitId=");
		builder.append(unitId);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.datum.sma.webbox.ds";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = new ArrayList<>(8);

		results.add(new BasicTitleSettingSpecifier("info", infoMessage(), true));
		results.add(new BasicTitleSettingSpecifier("sample", sampleMessage(lastSample), true));

		results.addAll(getIdentifiableSettingSpecifiers());
		results.add(
				new BasicTextFieldSettingSpecifier("webBox.propertyFilters['UID']", DEFAULT_WEBBOX_UID));
		results.add(new BasicTextFieldSettingSpecifier("unitId", null));
		results.add(new BasicTextFieldSettingSpecifier("sourceId", DEFAULT_SOURCE_ID));
		results.add(new BasicTextFieldSettingSpecifier("samplesTransformService.propertyFilters['UID']",
				null));

		return results;
	}

	private String infoMessage() {
		WebBoxDevice device = webBoxDevice();
		if ( device == null ) {
			return "N/A";
		}
		return device.getDeviceDescription();
	}

	private String sampleMessage(SmaDeviceDataAccessor data) {
		if ( data == null || data.getDataTimestamp() < 1 ) {
			return "N/A";
		}
		return data.getDataDescription();
	}

	@Override
	public Class<? extends GeneralNodeDatum> getDatumType() {
		return GeneralNodeDatum.class;
	}

	private WebBoxDevice webBoxDevice() {
		final int uId = (unitId != null ? unitId.intValue() : -1);
		if ( uId < 3 ) {
			return null;
		}
		WebBoxOperations ops = service(webBox);
		if ( ops == null ) {
			return null;
		}
		WebBoxDevice device = ops.availableDevices().stream().filter(e -> e.getUnitId() == uId).findAny()
				.orElse(null);
		if ( device == null ) {
			log.warn("No WebBox device with unit ID {} available from WebBox {}", uId, ops);
		}
		return device;
	}

	@Override
	public GeneralNodeDatum readCurrentDatum() {
		WebBoxDevice device = webBoxDevice();
		if ( device == null ) {
			return null;
		}
		final long now = System.currentTimeMillis();
		try {
			SmaDeviceDataAccessor sample = device.refreshData(getSampleCacheMs());
			if ( sample != null && sample.getDataTimestamp() >= now ) {
				lastSample = sample;
				GeneralNodeDatum d = new GeneralNodeDatum();
				d.setCreated(new Date(sample.getDataTimestamp()));
				d.setSourceId(resolvePlaceholders(getSourceId()));
				if ( d.getSourceId() != null ) {
					sample.populateDatumSamples(d.asMutableSampleOperations(), null);
					d = applySamplesTransformer(d, null);
					postDatumCapturedEvent(d);
					return d;
				}
			}
		} catch ( IOException e ) {
			log.warn("Communication error reading data from WebBox device {}: {}", device, e.toString());
		}
		return null;
	}

	/**
	 * Get the optional {@link WebBoxOperations} instance.
	 * 
	 * @return the optional service, never {@literal null}
	 */
	public OptionalService<WebBoxOperations> getWebBox() {
		return webBox;
	}

	/**
	 * Get the source ID.
	 * 
	 * @return the source ID
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Set the source ID.
	 * 
	 * @param sourceId
	 *        the source ID
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Get the sample cache maximum age, in milliseconds.
	 * 
	 * @return the cache milliseconds
	 */
	public long getSampleCacheMs() {
		return sampleCacheMs;
	}

	/**
	 * Set the sample cache maximum age, in milliseconds.
	 * 
	 * @param sampleCacheMs
	 *        the cache milliseconds
	 */
	public void setSampleCacheMs(long sampleCacheMs) {
		this.sampleCacheMs = sampleCacheMs;
	}

	/**
	 * Get the WebBox device unit ID to collect data from.
	 * 
	 * @return the unit ID
	 */
	public Integer getUnitId() {
		return unitId;
	}

	/**
	 * Set the WebBox device unit ID to collect data from.
	 * 
	 * @param unitId
	 *        the unit ID; must be &gt; 2
	 */
	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}

}
