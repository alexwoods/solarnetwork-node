/* ==================================================================
 * ControlConfig.java - 22/02/2019 5:21:41 pm
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.solarnetwork.domain.NodeControlInfo;
import net.solarnetwork.node.NodeControlProvider;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;

/**
 * A configuration for a DNP3 control integration with a
 * {@link net.solarnetwork.node.NodeControlProvider} control value.
 * 
 * <p>
 * This configuration maps a control value to a DNP3 measurement.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class ControlConfig {

	/** The default control type. */
	public static final ControlType DEFAULT_TYPE = ControlType.Analog;

	private String controlProviderUid;
	private String controlId;
	private ControlType type = DEFAULT_TYPE;

	/**
	 * Default constructor.
	 */
	public ControlConfig() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param controlProviderUid
	 *        the {@link NodeControlProvider#getUID()} to collect from
	 * @param controlId
	 *        the control ID a {@link NodeControlInfo#getControlId()} to collect
	 *        from
	 * @param type
	 *        the DNP3 control type
	 */
	public ControlConfig(String dataSourceUid, String controlId, ControlType type) {
		super();
		this.controlProviderUid = dataSourceUid;
		this.controlId = controlId;
		this.type = type;
	}

	/**
	 * Get settings suitable for configuring an instance of this class.
	 * 
	 * @param prefix
	 *        a setting key prefix to use
	 * @return the settings, never {@literal null}
	 */
	public static List<SettingSpecifier> settings(String prefix) {
		List<SettingSpecifier> results = new ArrayList<>(3);

		results.add(new BasicTextFieldSettingSpecifier(prefix + "controlProviderUid", ""));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "controlId", ""));

		// drop-down menu for control type
		BasicMultiValueSettingSpecifier propTypeSpec = new BasicMultiValueSettingSpecifier(
				prefix + "typeKey", Character.toString(DEFAULT_TYPE.getCode()));
		Map<String, String> propTypeTitles = new LinkedHashMap<>(3);
		for ( ControlType e : ControlType.values() ) {
			propTypeTitles.put(Character.toString(e.getCode()), e.getTitle());
		}
		propTypeSpec.setValueTitles(propTypeTitles);
		results.add(propTypeSpec);

		return results;
	}

	public String getControlProviderUid() {
		return controlProviderUid;
	}

	public void setControlProviderUid(String dataSourceUid) {
		this.controlProviderUid = dataSourceUid;
	}

	public String getControlId() {
		return controlId;
	}

	public void setControlId(String sourceId) {
		this.controlId = sourceId;
	}

	public ControlType getType() {
		return type;
	}

	public void setType(ControlType type) {
		this.type = type;
	}

	/**
	 * Get the control type key.
	 * 
	 * <p>
	 * This returns the configured {@link #getType()}
	 * {@link ControlType#getCode()} value as a string. If the type is not
	 * available, {@link ControlType#AnalogInput} will be returned.
	 * </p>
	 * 
	 * @return the control type key
	 */
	public String getTypeKey() {
		ControlType type = getType();
		if ( type == null ) {
			type = DEFAULT_TYPE;
		}
		return Character.toString(type.getCode());
	}

	/**
	 * Set the control type via a key value.
	 * 
	 * <p>
	 * This uses the first character of {@code key} as a {@link ControlType}
	 * code value to call {@link #setType(ControlType)}. If there is any problem
	 * parsing the type, {@link ControlType#AnalogInput} is set.
	 * </p>
	 * 
	 * @param key
	 *        the control type key to set
	 */
	public void setTypeKey(String key) {
		ControlType type = null;
		if ( key != null && key.length() > 0 ) {
			try {
				type = ControlType.forCode(key.charAt(0));
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
		}
		if ( type == null ) {
			type = DEFAULT_TYPE;
		}
		setType(type);
	}

}
