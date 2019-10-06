/* ==================================================================
 * CanbusDatumDataSource.java - 24/09/2019 8:48:39 pm
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

package net.solarnetwork.node.datum.canbus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.io.canbus.CanbusFrame;
import net.solarnetwork.node.io.canbus.CanbusFrameListener;
import net.solarnetwork.node.io.canbus.support.CanbusDatumDataSourceSupport;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicGroupSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.SettingsUtil;
import net.solarnetwork.util.ArrayUtils;

/**
 * Generic CAN bus datum data source.
 * 
 * 
 * @author matt
 * @version 1.0
 */
public class CanbusDatumDataSource extends CanbusDatumDataSourceSupport
		implements DatumDataSource<GeneralNodeDatum>, SettingSpecifierProvider, CanbusFrameListener {

	/** The setting UID value. */
	public static final String SETTING_UID = "net.solarnetwork.node.datum.canbus";

	private String sourceId;
	private CanbusMessageConfig[] msgConfigs;

	@Override
	public Class<? extends GeneralNodeDatum> getDatumType() {
		return GeneralNodeDatum.class;
	}

	@Override
	public GeneralNodeDatum readCurrentDatum() {
		// TODO Auto-generated method stub
		return null;
	}

	// SettingsSpecifierProvider

	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		super.configurationChanged(properties);
		// TODO apply subscriptions based on propConfigs
	}

	@Override
	public void canbusFrameReceived(CanbusFrame frame) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSettingUID() {
		return SETTING_UID;
	}

	@Override
	public String getDisplayName() {
		return "CAN Bus Datum Data Source";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = getIdentifiableSettingSpecifiers();
		results.addAll(canbusDatumDataSourceSettingSpecifiers(""));
		results.add(new BasicTextFieldSettingSpecifier("sourceId", ""));

		CanbusMessageConfig[] confs = getMsgConfigs();
		List<CanbusMessageConfig> confsList = (confs != null ? Arrays.asList(confs)
				: Collections.<CanbusMessageConfig> emptyList());
		results.add(SettingsUtil.dynamicListSettingSpecifier("msgConfigs", confsList,
				new SettingsUtil.KeyedListCallback<CanbusMessageConfig>() {

					@Override
					public Collection<SettingSpecifier> mapListSettingKey(CanbusMessageConfig value,
							int index, String key) {
						BasicGroupSettingSpecifier configGroup = new BasicGroupSettingSpecifier(
								value.settings(key + "."));
						return Collections.<SettingSpecifier> singletonList(configGroup);
					}
				}));

		return results;
	}

	// Accessors

	/**
	 * Get the message configurations.
	 * 
	 * @return the message configurations
	 */
	public CanbusMessageConfig[] getMsgConfigs() {
		return msgConfigs;
	}

	/**
	 * Set the message configurations to use.
	 * 
	 * @param msgConfigs
	 *        the configs to use
	 */
	public void setMsgConfigs(CanbusMessageConfig[] msgConfigs) {
		this.msgConfigs = msgConfigs;
	}

	/**
	 * Get the number of configured {@code msgConfigs} elements.
	 * 
	 * @return the number of {@code msgConfigs} elements
	 */
	public int getMsgConfigsCount() {
		CanbusMessageConfig[] confs = this.msgConfigs;
		return (confs == null ? 0 : confs.length);
	}

	/**
	 * Adjust the number of configured {@code msgConfigs} elements.
	 * 
	 * <p>
	 * Any newly added element values will be set to new
	 * {@link CanbusMessageConfig} instances.
	 * </p>
	 * 
	 * @param count
	 *        The desired number of {@code msgConfigs} elements.
	 */
	public void setMsgConfigsCount(int count) {
		this.msgConfigs = ArrayUtils.arrayWithLength(this.msgConfigs, count, CanbusMessageConfig.class,
				null);
	}

	/**
	 * Get the source ID to use for returned datum.
	 * 
	 * @return the source ID to use
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Set the source ID to use for returned datum.
	 * 
	 * @param soruceId
	 *        the source ID to use
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

}
