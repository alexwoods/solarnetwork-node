/* ==================================================================
 * LocationDatumDataSource.java - Feb 21, 2011 5:23:28 PM
 * 
 * Copyright 2007-2011 SolarNetwork.net Dev Team
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

package net.solarnetwork.node.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import net.solarnetwork.domain.GeneralLocationSourceMetadata;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.LocationService;
import net.solarnetwork.node.MultiDatumDataSource;
import net.solarnetwork.node.domain.BasicGeneralLocation;
import net.solarnetwork.node.domain.Datum;
import net.solarnetwork.node.domain.GeneralLocation;
import net.solarnetwork.node.domain.GeneralLocationDatum;
import net.solarnetwork.node.domain.GeneralNodeDatum;
import net.solarnetwork.node.domain.Location;
import net.solarnetwork.node.domain.PriceLocation;
import net.solarnetwork.node.domain.PricedDatum;
import net.solarnetwork.node.settings.KeyedSettingSpecifier;
import net.solarnetwork.node.settings.LocationLookupSettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicLocationLookupSettingSpecifier;
import net.solarnetwork.node.util.PrefixedMessageSource;
import net.solarnetwork.util.OptionalService;
import net.solarnetwork.util.OptionalServiceTracker;

/**
 * {@link DatumDataSource} that augments some other data source's datum values
 * with location IDs.
 * 
 * <p>
 * This is to be used to easily augment various datum that relate to a location
 * with the necessary {@link Location#getLocationId()} ID. This class also
 * implements the {@link MultiDatumDataSource} API, and will call the methods of
 * that API on the configured {@code delegate} if that also implements
 * {@link MultiDatumDataSource}. If the {@code delegate} does not implement
 * {@link MultiDatumDataSource} this class will "fake" that API by calling
 * {@link DatumDataSource#readCurrentDatum()} and returning that object in a
 * Collection.
 * </p>
 * 
 * @author matt
 * @version 1.6
 */
public class LocationDatumDataSource<T extends Datum>
		implements DatumDataSource<T>, MultiDatumDataSource<T>, SettingSpecifierProvider {

	/** Default value for the {@code locationIdPropertyName} property. */
	public static final String DEFAULT_LOCATION_ID_PROP_NAME = "locationId";

	/** Default value for the {@code sourceIdPropertyName} property. */
	public static final String DEFAULT_SOURCE_ID_PROP_NAME = "locationSourceId";

	/** Bundle name for price location lookup messages. */
	public static final String PRICE_LOCATION_MESSAGE_BUNDLE = "net.solarnetwork.node.support.PriceLocationDatumDataSource";

	private DatumDataSource<T> delegate;
	private OptionalService<LocationService> locationService;
	private String locationType = Location.PRICE_TYPE;
	private String locationIdPropertyName = DEFAULT_LOCATION_ID_PROP_NAME;
	private String sourceIdPropertyName = DEFAULT_SOURCE_ID_PROP_NAME;
	private boolean requireLocationService = false;
	private String messageBundleBasename = PRICE_LOCATION_MESSAGE_BUNDLE;
	private Long locationId = null;
	private String sourceId = null;
	private Set<String> datumClassNameIgnore;

	private GeneralLocation location = null;
	private MessageSource messageSource;

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Factory method.
	 * 
	 * <p>
	 * This method exists to work around issues with wiring this class via
	 * Gemini Blueprint 2.2. It throws a
	 * {@code SpringBlueprintConverterService$BlueprintConverterException} if
	 * the delegate parameter is defined as {@code DatumDataSource}.
	 * </p>
	 * 
	 * @param delegate
	 *        the delegate, must implement
	 *        {@code DatumDataSource<? extends Datum>}
	 * @param locationService
	 *        the location service
	 * @return the data source
	 */
	public static LocationDatumDataSource<? extends Datum> getInstance(Object delegate,
			OptionalServiceTracker<LocationService> locationService) {
		LocationDatumDataSource<? extends Datum> ds = new LocationDatumDataSource<Datum>();
		ds.setDelegate(delegate);
		ds.setLocationService(locationService);
		return ds;
	}

	@Override
	public Class<? extends T> getDatumType() {
		return delegate.getDatumType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> getMultiDatumType() {
		if ( delegate instanceof MultiDatumDataSource ) {
			return ((MultiDatumDataSource<T>) delegate).getMultiDatumType();
		}
		return delegate.getDatumType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<T> readMultipleDatum() {
		Collection<T> results = null;
		if ( delegate instanceof MultiDatumDataSource ) {
			results = ((MultiDatumDataSource<T>) delegate).readMultipleDatum();
		} else {
			// fake multi API
			results = new ArrayList<T>(1);
			T datum = delegate.readCurrentDatum();
			if ( datum != null ) {
				results.add(datum);
			}
		}
		if ( results != null && locationId != null ) {
			for ( T datum : results ) {
				populateLocation(datum);
			}
		} else if ( results != null && results.size() > 0 && locationId == null
				&& requireLocationService ) {
			log.warn("Location required but not available, discarding datum: {}", results);
			results = Collections.emptyList();
		}
		return results;
	}

	@Override
	public T readCurrentDatum() {
		T datum = delegate.readCurrentDatum();
		if ( datum != null && locationId != null ) {
			populateLocation(datum);
		} else if ( datum != null && locationId == null && requireLocationService ) {
			log.warn("LocationService required but not available, discarding datum: {}", datum);
			datum = null;
		}
		return datum;
	}

	private void populateLocation(T datum) {
		if ( locationId != null && sourceId != null && !shouldIgnoreDatum(datum) ) {
			log.debug("Augmenting datum {} with locaiton ID {} ({})", datum, locationId, sourceId);
			if ( datum instanceof GeneralLocationDatum ) {
				GeneralLocationDatum gDatum = (GeneralLocationDatum) datum;
				gDatum.setLocationId(locationId);
				gDatum.setSourceId(sourceId);
			} else if ( datum instanceof GeneralNodeDatum ) {
				GeneralNodeDatum gDatum = (GeneralNodeDatum) datum;
				gDatum.putStatusSampleValue(PricedDatum.PRICE_LOCATION_KEY, locationId);
				gDatum.putStatusSampleValue(PricedDatum.PRICE_SOURCE_KEY, sourceId);
			} else {
				BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(datum);
				if ( bean.isWritableProperty(locationIdPropertyName)
						&& bean.isWritableProperty(sourceIdPropertyName) ) {
					bean.setPropertyValue(locationIdPropertyName, locationId);
					bean.setPropertyValue(sourceIdPropertyName, sourceId);
				}
			}
		}
	}

	private boolean shouldIgnoreDatum(T datum) {
		return (datumClassNameIgnore != null
				&& datumClassNameIgnore.contains(datum.getClass().getName()));
	}

	@Override
	public String toString() {
		return delegate != null ? delegate.toString() + "[LocationDatumDataSource proxy]"
				: "LocationDatumDataSource";
	}

	@Override
	public String getUID() {
		return delegate.getUID();
	}

	@Override
	public String getGroupUID() {
		return delegate.getGroupUID();
	}

	@Override
	public String getSettingUID() {
		if ( delegate instanceof SettingSpecifierProvider ) {
			return ((SettingSpecifierProvider) delegate).getSettingUID();
		}
		return getClass().getName();
	}

	@Override
	public String getDisplayName() {
		if ( delegate instanceof SettingSpecifierProvider ) {
			return ((SettingSpecifierProvider) delegate).getDisplayName();
		}
		return null;
	}

	@Override
	public synchronized MessageSource getMessageSource() {
		if ( messageSource == null ) {
			MessageSource other = null;
			if ( delegate instanceof SettingSpecifierProvider ) {
				other = ((SettingSpecifierProvider) delegate).getMessageSource();
			}
			PrefixedMessageSource delegateSource = null;
			if ( other != null ) {
				delegateSource = new PrefixedMessageSource();
				delegateSource.setDelegate(other);
				delegateSource.setPrefix("delegate.");
			}

			ResourceBundleMessageSource proxySource = new ResourceBundleMessageSource();
			proxySource.setBundleClassLoader(getClass().getClassLoader());
			proxySource.setBasename(messageBundleBasename);
			if ( delegateSource != null ) {
				proxySource.setParentMessageSource(delegateSource);
			}

			messageSource = proxySource;
		}
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> result = new ArrayList<SettingSpecifier>();
		result.add(getLocationSettingSpecifier());
		if ( delegate instanceof SettingSpecifierProvider ) {
			List<SettingSpecifier> delegateResult = ((SettingSpecifierProvider) delegate)
					.getSettingSpecifiers();
			if ( delegateResult != null ) {
				for ( SettingSpecifier spec : delegateResult ) {
					if ( spec instanceof KeyedSettingSpecifier<?> ) {
						KeyedSettingSpecifier<?> keyedSpec = (KeyedSettingSpecifier<?>) spec;
						result.add(keyedSpec.mappedTo("delegate."));
					} else {
						result.add(spec);
					}
				}
			}
		}
		return result;
	}

	private LocationLookupSettingSpecifier getLocationSettingSpecifier() {
		if ( location == null && locationService != null && locationId != null && sourceId != null ) {
			LocationService service = locationService.service();
			if ( service != null ) {
				GeneralLocationSourceMetadata meta = service.getLocationMetadata(locationId, sourceId);
				BasicGeneralLocation loc = new BasicGeneralLocation();
				loc.setLocationId(locationId);
				loc.setSourceId(sourceId);
				loc.setSourceMetadata(meta);
				location = loc;
			}
		}
		return new BasicLocationLookupSettingSpecifier("locationKey", locationType, location);
	}

	/**
	 * Get the delegate.
	 * 
	 * <p>
	 * This getter is used to work around a <i>No conversion found for generic
	 * argument(s) for reified type</i> bug with Spring/Gemini Blueprint wiring.
	 * </p>
	 * 
	 * @return the delegate
	 * @see #getDelegateDatumDataSource()
	 */
	public Object getDelegate() {
		return getDelegateDatumDataSource();
	}

	/**
	 * Set the delegate.
	 * 
	 * <p>
	 * This getter is used to work around a <i>No conversion found for generic
	 * argument(s) for reified type</i> bug with Spring/Gemini Blueprint wiring.
	 * </p>
	 * 
	 * @param delegate
	 *        the delegate to set; must implement {@link DatumDataSource}
	 * @see #setDelegateDatumDataSource(DatumDataSource)
	 */
	@SuppressWarnings("unchecked")
	public void setDelegate(Object delegate) {
		setDelegateDatumDataSource((DatumDataSource<T>) delegate);
	}

	/**
	 * Get the delegate {@link DatumDataSource}.
	 * 
	 * @return the delegate
	 * @since 1.6
	 */
	public Object getDelegateDatumDataSource() {
		return delegate;
	}

	/**
	 * Set the delegate {@link DatumDataSource}.
	 * 
	 * @param delegate
	 *        the delegate
	 * @since 1.6
	 */
	public void setDelegateDatumDataSource(DatumDataSource<T> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Get the {@link LocationService} to use to lookup {@link Location}
	 * instances via the configured {@code locationId} property.
	 * 
	 * @return the location service
	 */
	public OptionalService<LocationService> getLocationService() {
		return locationService;
	}

	/**
	 * Set the {@link LocationService} to use to lookup {@link Location}
	 * instances via the configured {@code locationId} property.
	 * 
	 * @param locationService
	 *        the service to use
	 */
	public void setLocationService(OptionalService<LocationService> locationService) {
		this.locationService = locationService;
	}

	/**
	 * Get the JavaBean property name to set the found
	 * {@link Location#getLocationId()} to on the {@link Datum} returned from
	 * the configured {@code delegate}.
	 * 
	 * @return the location ID property name; defaults to
	 *         {@link #DEFAULT_LOCATION_ID_PROP_NAME}
	 */
	public String getLocationIdPropertyName() {
		return locationIdPropertyName;
	}

	/**
	 * Set the JavaBean property name to set the found
	 * {@link Location#getLocationId()} to on the {@link Datum} returned from
	 * the configured {@code delegate}.
	 * 
	 * <p>
	 * The object must support a JavaBean setter method for this property.
	 * </p>
	 * 
	 * @param locationIdPropertyName
	 *        the property name to use
	 */
	public void setLocationIdPropertyName(String locationIdPropertyName) {
		this.locationIdPropertyName = locationIdPropertyName;
	}

	/**
	 * Get the "location service required" flag.
	 * 
	 * @return the location service reqiured flag; defaults to {@literal false}
	 */
	public boolean isRequireLocationService() {
		return requireLocationService;
	}

	/**
	 * Get the "location service required" flag.
	 * 
	 * <p>
	 * If configured as {@literal true} then return {@literal null} data only
	 * instead of calling the {@code delegate}. This is designed for services
	 * that require a location ID to be set, for example a Location Datum
	 * logger.
	 * </p>
	 * 
	 * @param requireLocationService
	 *        the required setting to use
	 */
	public void setRequireLocationService(boolean requireLocationService) {
		this.requireLocationService = requireLocationService;
	}

	/**
	 * Get the type of location to search for.
	 * 
	 * @return the type; defaults to {@link PriceLocation}
	 */
	public String getLocationType() {
		return locationType;
	}

	/**
	 * Set the type of location to search for.
	 * 
	 * @param locationType
	 *        the location type
	 */
	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	/**
	 * Get the message bundle basename to use.
	 * 
	 * @return the basename; defaults to {@link #PRICE_LOCATION_MESSAGE_BUNDLE}
	 */
	public String getMessageBundleBasename() {
		return messageBundleBasename;
	}

	/**
	 * Set the message bundle basename to use.
	 * 
	 * <p>
	 * This can be customized so different messages can be shown for different
	 * uses of this proxy.
	 * </p>
	 * 
	 * @param messageBundleBaseName
	 *        the basename to use
	 */
	public void setMessageBundleBasename(String messageBundleBaseName) {
		this.messageBundleBasename = messageBundleBaseName;
	}

	/**
	 * Set the location ID and source ID as a single string value. The format of
	 * the key is {@code locationId:sourceId}.
	 * 
	 * @param key
	 *        the location and source ID key
	 */
	public void setLocationKey(String key) {
		Long newLocationId = null;
		String newSourceId = null;
		if ( key != null ) {
			int idx = key.indexOf(':');
			if ( idx > 0 && idx + 1 < key.length() ) {
				newLocationId = Long.valueOf(key.substring(0, idx));
				newSourceId = key.substring(idx + 1);
			}
		}
		setLocationId(newLocationId);
		setSourceId(newSourceId);
	}

	/**
	 * Get the {@link Location} ID to assign to datum.
	 * 
	 * @return the location ID
	 */
	public Long getLocationId() {
		return locationId;
	}

	/**
	 * Set the {@link Location} ID to assign to datum.
	 * 
	 * @param locationId
	 *        the location ID
	 */
	public void setLocationId(Long locationId) {
		if ( this.location != null && locationId != null
				&& !locationId.equals(this.location.getLocationId()) ) {
			this.location = null; // set to null so we re-fetch from server
		}
		this.locationId = locationId;
	}

	public GeneralLocation getLocation() {
		return location;
	}

	public Set<String> getDatumClassNameIgnore() {
		return datumClassNameIgnore;
	}

	public void setDatumClassNameIgnore(Set<String> datumClassNameIgnore) {
		this.datumClassNameIgnore = datumClassNameIgnore;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		if ( this.location != null && sourceId != null
				&& !sourceId.equals(this.location.getSourceId()) ) {
			this.location = null; // set to null so we re-fetch from server
		}
		this.sourceId = sourceId;
	}

	/**
	 * Get the JavaBean property name to set the found
	 * {@link Location#getSourceId()} to on the {@link Datum} returned from the
	 * configured {@code delegate}.
	 * 
	 * @return the source ID property name; defaults to
	 *         {@link #DEFAULT_SOURCE_ID_PROP_NAME}
	 */
	public String getSourceIdPropertyName() {
		return sourceIdPropertyName;
	}

	/**
	 * Set the JavaBean property name to set the found
	 * {@link Location#getSourceId()} to on the {@link Datum} returned from the
	 * configured {@code delegate}.
	 * 
	 * <p>
	 * The object must support a JavaBean setter method for this property.
	 * </p>
	 * 
	 * @param sourceIdPropertyName
	 *        the source ID property name to use
	 */
	public void setSourceIdPropertyName(String sourceIdPropertyName) {
		this.sourceIdPropertyName = sourceIdPropertyName;
	}

}
