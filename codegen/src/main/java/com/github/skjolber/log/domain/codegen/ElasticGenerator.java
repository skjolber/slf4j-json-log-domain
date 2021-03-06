package com.github.skjolber.log.domain.codegen;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

import com.github.skjolber.log.domain.model.Domain;
import com.github.skjolber.log.domain.model.Key;

/**
 * 
 * Generator for adding mapping to Elastic Search. Generates properties.
 * 
 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-field-mapping.html">Elastic field mapping</a>
 * 
 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html">Elastic templates</a>
 */

public class ElasticGenerator {

	public static void generate(Path file, Path outputDirectory) throws IOException {
		Domain domain = DomainFactory.parse(Files.newBufferedReader(file, StandardCharsets.UTF_8));

		generate(domain, outputDirectory);
	}

	public static void generate(Domain domain, Path outputFile) throws IOException {
		Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
		try {
			writer.write(generateProperties(domain).toString());
		} finally {
			writer.close();
		}
	}

	public static JSONObject generateProperties(Domain domain) {
		JSONObject properties = new JSONObject();

		for(Key key : domain.getKeys()) {

			JSONObject property = new JSONObject();

			property.put("type", parseTypeFormat(key.getType(), key.getFormat()));
			
			properties.put(key.getId(), property);
		}
		
		if(domain.hasTags()) {
			// https://www.elastic.co/guide/en/elasticsearch/reference/current/array.html
			JSONObject property = new JSONObject();
			property.put("type", "text");
			properties.put("tags", property);
		}
		
		if(!domain.hasQualifier()) {
			return properties;
		}

		return new JSONObject().put(domain.getQualifier(), new JSONObject().put("properties", properties)); 
	}
	
	/**
	 * @see https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html
	 */
	
	private static String parseTypeFormat(String type, String format) {
		switch(type) {
			case "integer" : {
				if(format != null) {
					if(format.equals("int32")) {
						return "integer";
					} else if(format.equals("int64")) {
						return "long";
					}
				}
				break;
			}
			case "string" : {
				if(format == null) {
					return "text";
				} else if(format.equals("date")) {
					return "date";
				} else if(format.equals("date-time")) {
					return "date";
				} else if(format.equals("password")) {
					return "text";
				} else if(format.equals("byte")) {
					return "byte";
				} else if(format.equals("binary")) {
					return "binary";
				}
				break;
			}
			case "number" : {
				if(format != null) {
					if(format.equals("float")) {
						return "float";
					} else if(format.equals("double")) {
						return "double";
					}
				}
				break;
			}
		}
		throw new IllegalArgumentException("Unknown type " + type + " format " + format);
	}

}
