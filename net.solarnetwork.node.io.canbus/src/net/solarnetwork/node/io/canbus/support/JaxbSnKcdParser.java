/* ==================================================================
 * JaxbSnKcdParser.java - 13/09/2019 6:08:07 pm
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import net.solarnetwork.node.io.canbus.KcdParser;
import net.solarnetwork.node.io.canbus.kcd.NetworkDefinitionType;

/**
 * Implementation of {@link KcdParser} that uses JAXB to parse the
 * SolarNetwork-extended <i>SN-Definition-Datum.xsd</i> KDC XML.
 * 
 * @author matt
 * @version 1.0
 */
public class JaxbSnKcdParser implements KcdParser {

	private static final int GZIP_MAGIC = 0x1f8b;
	private static final Logger logger = LoggerFactory.getLogger(JaxbSnKcdParser.class);

	private boolean validating;
	private Schema schema;
	private JAXBContext context;

	/**
	 * Constructor.
	 * 
	 * @throws RuntimeException
	 *         if the KCD JAXB context or XML Schema cannot be loaded
	 */
	public JaxbSnKcdParser() {
		this(true);
	}

	/**
	 * Constructor.
	 * 
	 * @param validating
	 *        {@literal true} to enable schema validation
	 * @throws RuntimeException
	 *         if the KCD JAXB context or XML Schema cannot be loaded
	 */
	public JaxbSnKcdParser(boolean validating) {
		this.validating = validating;
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(new LSResourceResolver() {

			@Override
			public LSInput resolveResource(String type, String namespaceURI, String publicId,
					String systemId, String baseURI) {
				if ( ("http://kayak.2codeornot2code.org/1.0".equals(namespaceURI)
						|| "urn:solarnetwork:datum:1.0".equals(namespaceURI)
						|| "http://www.w3.org/XML/1998/namespace".equals(namespaceURI))
						&& systemId != null ) {
					return new LSInput() {

						@Override
						public void setSystemId(String systemId) {

						}

						@Override
						public void setStringData(String stringData) {

						}

						@Override
						public void setPublicId(String publicId) {

						}

						@Override
						public void setEncoding(String encoding) {

						}

						@Override
						public void setCharacterStream(Reader characterStream) {

						}

						@Override
						public void setCertifiedText(boolean certifiedText) {

						}

						@Override
						public void setByteStream(InputStream byteStream) {

						}

						@Override
						public void setBaseURI(String baseURI) {

						}

						@Override
						public String getSystemId() {
							return systemId;
						}

						@Override
						public String getStringData() {
							return null;
						}

						@Override
						public String getPublicId() {
							return publicId;
						}

						@Override
						public String getEncoding() {
							return null;
						}

						@Override
						public Reader getCharacterStream() {
							return null;
						}

						@Override
						public boolean getCertifiedText() {
							return false;
						}

						@Override
						public InputStream getByteStream() {
							String fileName = systemId;
							int lastSlash = systemId.lastIndexOf('/');
							if ( lastSlash >= 0 && lastSlash + 1 < systemId.length() ) {
								fileName = systemId.substring(lastSlash + 1);
							}
							return JaxbSnKcdParser.this.getClass().getClassLoader().getResourceAsStream(
									"net/solarnetwork/node/io/canbus/schema/" + fileName);
						}

						@Override
						public String getBaseURI() {
							return baseURI;
						}
					};
				}
				return null;
			}
		});
		InputStream resourceAsStream = getClass().getClassLoader()
				.getResourceAsStream("net/solarnetwork/node/io/canbus/schema/SN-Definition-Datum.xsd");
		Source s = new StreamSource(resourceAsStream);
		try {
			schema = schemaFactory.newSchema(s);
			context = JAXBContext.newInstance(new Class[] { NetworkDefinitionType.class });
		} catch ( JAXBException | SAXException ex ) {
			logger.error("Could not create KCD XML context: ", ex);
			throw new RuntimeException(ex);
		}
	}

	private InputStream inputStreamForStream(InputStream in) throws IOException {
		// checking for GZIP
		if ( in instanceof GZIPInputStream ) {
			return in;
		}
		PushbackInputStream s = new PushbackInputStream(in, 2);
		int count = 0;
		byte[] magic = new byte[] { -1, -1 };
		while ( count < 2 ) {
			int readCount = s.read(magic, count, 2 - count);
			if ( readCount < 0 ) {
				break;
			}
			count += readCount;
		}
		s.unread(magic, 0, count);
		// GZIP magic bytes: 0x1F 0x8B
		if ( magic[0] == (byte) (GZIP_MAGIC >> 8) && magic[1] == (byte) (GZIP_MAGIC) ) {
			return new GZIPInputStream(s);
		}
		return s;
	}

	/**
	 * Parse KCD data from an input stream, using a given file name.
	 * 
	 * <p>
	 * GZIP encoded streams are supported and will be automatically detected.
	 * </p>
	 * 
	 */
	@Override
	public NetworkDefinitionType parseKcd(InputStream in, boolean validate) throws IOException {
		try (InputStream input = inputStreamForStream(in)) {
			Unmarshaller umarshall = context.createUnmarshaller();
			if ( validating && validate ) {
				umarshall.setSchema(schema);
			}
			JAXBElement<NetworkDefinitionType> object = umarshall.unmarshal(new StreamSource(input),
					NetworkDefinitionType.class);
			return (object != null ? object.getValue() : null);
		} catch ( IOException e ) {
			throw e;
		} catch ( UnmarshalException e ) {
			logger.warn("Invalid KCD data.", e);
			throw new IOException(e);
		} catch ( Exception e ) {
			logger.warn("Error loading KCD data.", e);
			throw new IOException(e);
		}
	}

	/**
	 * Get the validating flag.
	 * 
	 * @return {@literal true} if the XML should be validated against the
	 *         <i>SN-Definition-Datum.xsd</i> schema
	 */
	public boolean isValidating() {
		return validating;
	}

	/**
	 * Set the validating flag.
	 * 
	 * @param validating
	 *        {@literal true} if the XML should be validated against the
	 *        <i>SN-Definition-Datum.xsd</i> schema
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

}
