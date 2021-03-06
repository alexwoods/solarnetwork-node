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

package net.solarnetwork.node.weather.ibm.wc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.MultiDatumDataSource;
import net.solarnetwork.node.domain.GeneralDayDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicMultiValueSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;

/**
 * The WCDayDatumDataSource class provides GUI settings and an entry point for
 * retrieving daily datum.
 * 
 * @author matt frost
 *
 */
public class WCDayDatumDataSource extends WCSupport<GeneralDayDatum>
		implements DatumDataSource<GeneralDayDatum>, MultiDatumDataSource<GeneralDayDatum>,
		SettingSpecifierProvider {

	public WCDayDatumDataSource() {
		super();
		setDatumPeriod(DailyDatumPeriod.SEVENDAY.getPeriod());
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> result = new ArrayList<SettingSpecifier>(1);
		result.add(new BasicTextFieldSettingSpecifier("uid", null));
		result.add(new BasicTextFieldSettingSpecifier("apiKey", null));
		BasicMultiValueSettingSpecifier menuSpec = new BasicMultiValueSettingSpecifier("datumPeriod",
				DailyDatumPeriod.SEVENDAY.getPeriod());
		Map<String, String> menuValues = new LinkedHashMap<String, String>(
				DailyDatumPeriod.values().length);
		for ( DailyDatumPeriod p : DailyDatumPeriod.values() ) {
			menuValues.put(p.getPeriod(), p.getPeriod());
		}
		menuSpec.setValueTitles(menuValues);
		result.add(menuSpec);
		result.add(new BasicTextFieldSettingSpecifier("locationIdentifier", null));
		return result;
	}

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.weather.ibm.wc.day";
	}

	@Override
	public String getDisplayName() {
		return "IBM daily weather information";
	}

	@Override
	public Class<? extends GeneralDayDatum> getDatumType() {
		return GeneralDayDatum.class;
	}

	@Override
	public GeneralDayDatum readCurrentDatum() {
		Collection<GeneralDayDatum> results = readMultipleDatum();
		return (results.isEmpty() ? null : results.iterator().next());
	}

	@Override
	public Class<? extends GeneralDayDatum> getMultiDatumType() {
		return GeneralDayDatum.class;
	}

	@Override
	public Collection<GeneralDayDatum> readMultipleDatum() {
		return this.getClient().readDailyForecast(this.getLocationIdentifier(), this.getApiKey(),
				DailyDatumPeriod.forPeriod(this.getDatumPeriod()));
	}

}
