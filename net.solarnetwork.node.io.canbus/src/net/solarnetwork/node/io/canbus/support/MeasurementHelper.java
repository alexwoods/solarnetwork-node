/* ==================================================================
 * MeasurementHelper.java - 15/09/2019 9:53:05 am
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

package net.solarnetwork.node.io.canbus.support;

import java.util.Set;
import javax.measure.MeasurementException;
import javax.measure.Unit;
import javax.measure.format.UnitFormat;
import javax.measure.spi.FormatService;
import javax.measure.spi.FormatService.FormatType;
import javax.measure.spi.UnitFormatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.javax.measure.MeasurementServiceProvider;
import net.solarnetwork.util.OptionalServiceCollection;

/**
 * Helper for dealing with KCD units of measurement, using the
 * {@code javax.measure} API.
 * 
 * @author matt
 * @version 1.0
 */
public class MeasurementHelper {

	private final OptionalServiceCollection<MeasurementServiceProvider> measurementProviders;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 * 
	 * @param measurementProvider
	 *        the measurement service provider to use
	 * @throws IllegalArgumentException
	 *         if {@code measurementProvider} is {@literal null}
	 */
	public MeasurementHelper(
			OptionalServiceCollection<MeasurementServiceProvider> measurementProviders) {
		super();
		if ( measurementProviders == null ) {
			throw new IllegalArgumentException("Measurement provider collection must be provided.");
		}
		this.measurementProviders = measurementProviders;
	}

	/**
	 * Get a unit for a unit string value.
	 * 
	 * <p>
	 * This method will attempt to parse the unit string using all available
	 * measurement providers, returning the first successfully parsed unit.
	 * </p>
	 * 
	 * @param unitString
	 *        the unit string value
	 * @return the unit, or {@literal null} if {@code unitString} is
	 *         {@literal null} or the unit cannot be determined
	 */
	public Unit<?> unitValue(String unitString) {
		if ( unitString == null || unitString.isEmpty() ) {
			return null;
		}
		for ( MeasurementServiceProvider measurementProvider : measurementProviders.services() ) {
			// try the UnitFormatService first, for cases when this returns a different implementation from FormatService
			UnitFormatService ufs = measurementProvider.getUnitFormatService();
			if ( ufs != null ) {
				Unit<?> unit = unitValueFromUnitFormatService(unitString, ufs);
				if ( unit != null ) {
					return unit;
				}
			}

			FormatService fs = measurementProvider.getFormatService();
			if ( fs != null && fs != ufs ) {
				Unit<?> unit = unitValueFromFormatService(unitString, fs);
				if ( unit != null ) {
					return unit;
				}
			}
		}
		log.debug("Unit not found for unit [{}]", unitString);
		return null;
	}

	private Unit<?> unitValueFromUnitFormatService(String unitString, UnitFormatService formatService) {
		if ( formatService == null ) {
			return null;
		}
		@SuppressWarnings("deprecation")
		Set<String> names = formatService.getAvailableFormatNames();
		return unitValue(unitString, formatService, names);
	}

	private Unit<?> unitValueFromFormatService(String unitString, FormatService formatService) {
		if ( formatService == null ) {
			return null;
		}
		Set<String> names = formatService.getAvailableFormatNames(FormatType.UNIT_FORMAT);
		return unitValue(unitString, formatService, names);
	}

	private Unit<?> unitValue(String unitString, UnitFormatService formatService,
			Set<String> unitFormatNames) {
		for ( String formatName : unitFormatNames ) {
			UnitFormat fmt = formatService.getUnitFormat(formatName);
			try {
				Unit<?> unit = fmt.parse(unitString);
				if ( unit != null ) {
					return unit;
				}
			} catch ( MeasurementException | UnsupportedOperationException e ) {
				log.trace("Error parsing unit [{}]: {}", unitString, e.toString());
			}
		}
		return null;
	}

}
