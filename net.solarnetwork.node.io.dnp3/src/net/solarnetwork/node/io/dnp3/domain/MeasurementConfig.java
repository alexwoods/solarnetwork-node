/* ==================================================================
 * MeasurementConfig.java - 21/02/2019 4:39:11 pm
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

package net.solarnetwork.node.io.dnp3.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;

/**
 * A configuration for a DNP3 measurement integration with a
 * {@link net.solarnetwork.node.DatumDataSource} property.
 * 
 * <p>
 * This configuration maps a datum property to a DNP3 measurement.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class MeasurementConfig {

	/** The default measurement type: {@code AnalogInput}. */
	public static final MeasurementType DEFAULT_TYPE = MeasurementType.AnalogInput;

	/** The default unit multiplier: {@literal 1}. */
	public static final BigDecimal DEFAULT_UNIT_MULTIPLIER = BigDecimal.ONE;

	/** The default decimal scale: {@literal 0}. */
	public static final int DEFAULT_DECIMAL_SCALE = 0;

	private String dataSourceUid;
	private String sourceId;
	private String propertyName;
	private MeasurementType type;
	private BigDecimal unitMultiplier = DEFAULT_UNIT_MULTIPLIER;
	private int decimalScale;

	/**
	 * Default constructor.
	 */
	public MeasurementConfig() {
		super();
		setType(DEFAULT_TYPE);
		setUnitMultiplier(DEFAULT_UNIT_MULTIPLIER);
		setDecimalScale(DEFAULT_DECIMAL_SCALE);
	}

	/**
	 * Constructor.
	 * 
	 * @param dataSourceUid
	 *        the {@link DatumDataSource#getUID()} to collect from
	 * @param sourceId
	 *        the source ID a
	 *        {@link net.solarnetwork.node.domain.Datum#getSourceId()} to
	 *        collect from
	 * @param propertyName
	 *        the datum property name to collect
	 * @param type
	 *        the DNP3 measurement type
	 */
	public MeasurementConfig(String dataSourceUid, String sourceId, String propertyName,
			MeasurementType type) {
		super();
		setDataSourceUid(dataSourceUid);
		setSourceId(sourceId);
		setPropertyName(propertyName);
		setType(type);
		setUnitMultiplier(DEFAULT_UNIT_MULTIPLIER);
		setDecimalScale(DEFAULT_DECIMAL_SCALE);
	}

	/**
	 * Get settings suitable for configuring an instance of this class.
	 * 
	 * @param prefix
	 *        a setting key prefix to use
	 * @return the settings, never {@literal null}
	 */
	public static List<SettingSpecifier> settings(String prefix) {
		List<SettingSpecifier> results = new ArrayList<>(6);

		results.add(new BasicTextFieldSettingSpecifier(prefix + "dataSourceUid", ""));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "sourceId", ""));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "propertyName", ""));

		// drop-down menu for measurement type
		BasicMultiValueSettingSpecifier propTypeSpec = new BasicMultiValueSettingSpecifier(
				prefix + "typeKey", Character.toString(DEFAULT_TYPE.getCode()));
		Map<String, String> propTypeTitles = new LinkedHashMap<>(3);
		for ( MeasurementType e : MeasurementType.values() ) {
			propTypeTitles.put(Character.toString(e.getCode()), e.toString());
		}
		propTypeSpec.setValueTitles(propTypeTitles);
		results.add(propTypeSpec);

		results.add(new BasicTextFieldSettingSpecifier(prefix + "unitMultiplier",
				DEFAULT_UNIT_MULTIPLIER.toString()));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "decimalScale",
				String.valueOf(DEFAULT_DECIMAL_SCALE)));

		return results;
	}

	public String getDataSourceUid() {
		return dataSourceUid;
	}

	public void setDataSourceUid(String dataSourceUid) {
		this.dataSourceUid = dataSourceUid;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public MeasurementType getType() {
		return type;
	}

	public void setType(MeasurementType type) {
		this.type = type;
	}

	/**
	 * Get the measurement type key.
	 * 
	 * <p>
	 * This returns the configured {@link #getType()}
	 * {@link MeasurementType#getCode()} value as a string. If the type is not
	 * available, {@link MeasurementType#AnalogInput} will be returned.
	 * </p>
	 * 
	 * @return the measurement type key
	 */
	public String getTypeKey() {
		MeasurementType type = getType();
		if ( type == null ) {
			type = DEFAULT_TYPE;
		}
		return Character.toString(type.getCode());
	}

	/**
	 * Set the measurement type via a key value.
	 * 
	 * <p>
	 * This uses the first character of {@code key} as a {@link MeasurementType}
	 * code value to call {@link #setType(MeasurementType)}. If there is any
	 * problem parsing the type, {@link MeasurementType#AnalogInput} is set.
	 * </p>
	 * 
	 * @param key
	 *        the measurement type key to set
	 */
	public void setTypeKey(String key) {
		MeasurementType type = null;
		if ( key != null && key.length() > 0 ) {
			try {
				type = MeasurementType.forCode(key.charAt(0));
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
		}
		if ( type == null ) {
			type = DEFAULT_TYPE;
		}
		setType(type);
	}

	/**
	 * Get the unit multiplier.
	 * 
	 * @return the multiplier; defaults to {@link #DEFAULT_UNIT_MULTIPLIER}
	 */
	public BigDecimal getUnitMultiplier() {
		return unitMultiplier;
	}

	/**
	 * Set the unit multiplier.
	 * 
	 * <p>
	 * This value represents a multiplication factor to apply to values
	 * collected for this property so that a standardized unit is captured. For
	 * example, a power meter might report power as <i>killowatts</i>, in which
	 * case {@code multiplier} can be configured as {@literal .001} to convert
	 * the value to <i>watts</i>.
	 * </p>
	 * 
	 * @param unitMultiplier
	 *        the mutliplier to set
	 */
	public void setUnitMultiplier(BigDecimal unitMultiplier) {
		this.unitMultiplier = unitMultiplier;
	}

	/**
	 * Get the decimal scale to round decimal numbers to.
	 * 
	 * @return the decimal scale; defaults to {@link #DEFAULT_DECIMAL_SCALE}
	 */
	public int getDecimalScale() {
		return decimalScale;
	}

	/**
	 * Set the decimal scale to round decimal numbers to.
	 * 
	 * <p>
	 * This is a <i>maximum</i> scale value that decimal values should be
	 * rounded to. This is applied <i>after</i> any {@code unitMultiplier} is
	 * applied. A scale of {@literal 0} would round all decimals to integer
	 * values.
	 * </p>
	 * 
	 * @param decimalScale
	 *        the scale to set, or {@literal -1} to disable rounding completely
	 */
	public void setDecimalScale(int decimalScale) {
		this.decimalScale = decimalScale;
	}
}
