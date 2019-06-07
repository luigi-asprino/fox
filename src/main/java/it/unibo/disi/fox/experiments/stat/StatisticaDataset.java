package it.unibo.disi.fox.experiments.stat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.disi.fox.experiments.fdistinctions.WekaUtils;
import it.unibo.disi.fox.utils.Utils;
import weka.core.Attribute;
import weka.core.Instances;

public class StatisticaDataset {

	static Configuration config;
	private static Logger logger = LoggerFactory.getLogger(StatisticaDataset.class);
	public static final int CHECKPOINT = 100000, QUERY_ATTEMPTS = 3, SLEEP = 60000;
	private static HashMap<String, Integer> mappa = new HashMap<String, Integer>();
	public static final String TOKEN_PREFIX_ATTRIBUTE = "_t_";
	public static final String NUMBER_URI_TOKENS = "__numberOfTokensInURI";
	public static final String NUMBER_URI_CAPITAL_TOKENS = "__numberOfTokensInURIStartingWithCapitalLetter";
	public static final String NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT = "__numberOfURITokensFoundInAbstract";
	public static final String __isDetectedBySENECA = "__isDetectedBySENECA";
	public static final String __isDetectedByORA = "__isDetectedByORA";
	public static final String ENTITY_URI_COLUMN_LABEL = "entity_uri";
	public static final String CLASS_COLUMN_LABEL = "__class";

	public static void main(String args[]) {

		try {
			Configurations configs = new Configurations();
			Configuration config;
			if (args.length > 0) {
				config = configs.properties(args[0]);
			} else {

				config = configs.properties("config.properties");
			}
			String inputFile_classInstance = config.getString("output_folder") + "/classinstance_features.tsv";
			String outputFile_classInstance = config.getString("output_folder") + "/classinstance_for_stats.tsv";
			createTableStructureFromTraining(config.getString("training_classinstance"), outputFile_classInstance);
			addRows(inputFile_classInstance, outputFile_classInstance, 8, 9);

			String inputFile_physicalObject = config.getString("output_folder") + "/physicalObject_features.tsv";
			String outputFile_physicalObject = config.getString("output_folder") + "/physicalObject_for_stats.tsv";
			createTableStructureFromTraining(config.getString("training_physisicalobject"), outputFile_physicalObject);
			addRows(inputFile_physicalObject, outputFile_physicalObject, 8, 9);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void createTableStructureFromTraining(String trainingDataset, String outputFile) {
		try {
			Instances instances = WekaUtils.loadXRFFInstances(trainingDataset);

			// remove the attribute id from instances
			Attribute idAtt = instances.attribute("ID");
			instances.deleteAttributeAt(idAtt.index());

			List<String> headers = new ArrayList<>();
			headers.add(ENTITY_URI_COLUMN_LABEL);
			headers.add(CLASS_COLUMN_LABEL);
			headers.add(NUMBER_URI_TOKENS);
			headers.add(NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT);
			headers.add(NUMBER_URI_CAPITAL_TOKENS);
			headers.add(__isDetectedBySENECA);
			headers.add(__isDetectedByORA);

			Enumeration<Attribute> atts = instances.enumerateAttributes();
			while (atts.hasMoreElements()) {
				Attribute attribute = (Attribute) atts.nextElement();
				if (headers.contains(attribute.name())) {
					continue;
				} else {
					headers.add(attribute.name());
				}

			}
			int colum_number = 0;
			FileOutputStream fos = new FileOutputStream(new File(outputFile));
			Iterator<String> it = headers.iterator();
			while (it.hasNext()) {
				String f = (String) it.next();
				mappa.put(f, colum_number++);
				if (it.hasNext()) {
					fos.write((f + "\t").getBytes());
				} else {
					fos.write((f + "\n").getBytes());
				}
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("Table structure created (number of column {})", mappa.keySet().size());
//		logger.info("Attributes {}", mappa.keySet());
	}

	@SuppressWarnings("unused")
	private static void createTableStructure(String entities, String outputFile, int propertiesColum,
			int abstractCoulum) throws IOException {

		logger.info("Creating table structure");

		FileOutputStream fos = new FileOutputStream(new File(outputFile));

		int colum_number = 0;
		mappa.put(ENTITY_URI_COLUMN_LABEL, colum_number++);
		fos.write((ENTITY_URI_COLUMN_LABEL + "\t").getBytes());

		mappa.put(CLASS_COLUMN_LABEL, colum_number++);
		fos.write((CLASS_COLUMN_LABEL + "\t").getBytes());

		mappa.put(NUMBER_URI_TOKENS, colum_number++);
		fos.write((NUMBER_URI_TOKENS + "\t").getBytes());

		mappa.put(NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT, colum_number++);
		fos.write((NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT + "\t").getBytes());

		mappa.put(NUMBER_URI_CAPITAL_TOKENS, colum_number++);
		fos.write((NUMBER_URI_CAPITAL_TOKENS + "\t").getBytes());

		mappa.put(__isDetectedBySENECA, colum_number++);
		fos.write((__isDetectedBySENECA + "\t").getBytes());

		mappa.put(__isDetectedByORA, colum_number++);
		fos.write((__isDetectedByORA + "\t").getBytes());

		int c = 0;

		BufferedReader br = new BufferedReader(new FileReader(new File(entities)));
		String line;
//		while ((line = br.readLine()) != null) {
//
//			if (!line.startsWith("http://dbpedia.org/resource/"))
//				continue;
//
//		}

//		List<String> tokensInDictionary = d.getTokens(c.getDictionarySize());

		Set<String> features = new HashSet<>();
//		br = new BufferedReader(new FileReader(new File(entities)));

		while ((line = br.readLine()) != null) {
			if (c > 0 && c % CHECKPOINT == 0) {
				logger.info("Analysed {} entities", c);
			}

			c++;

			if (!line.startsWith("http://dbpedia.org/resource/"))
				continue;

			try {
				String[] splitta = line.split("\t");
				// Add column for properties

				JSONObject obj = new JSONObject(splitta[propertiesColum]);
				for (String k : obj.keySet()) {
					features.add(k);

				}
				// Add column for abstract tokens
				if (splitta.length > abstractCoulum) {
					for (String token : Utils.tokenize(splitta[abstractCoulum])) {
						features.add(TOKEN_PREFIX_ATTRIBUTE + token);
					}
				}
			} catch (Exception e) {
				logger.error(line);
				logger.error("{}:line:{}", e.getMessage(), c);
			}
		}
		// scrivo HEADER su file
		Iterator<String> it = features.iterator();
		while (it.hasNext()) {
			String f = (String) it.next();
			mappa.put(f, colum_number++);
			if (it.hasNext()) {
				fos.write((f + "\t").getBytes());
			} else {
				fos.write((f + "\n").getBytes());
			}
		}

		br.close();
		logger.info("Table structure created (number of columsn {})", mappa.keySet().size());
		fos.close();

	}

	private static void addRows(String input, String outputFile, int propertiesColum, int abstractCoulum)
			throws Exception {

		logger.info("Adding Rows!");

		FileOutputStream fos = new FileOutputStream(new File(outputFile), true);

		BufferedReader br = new BufferedReader(new FileReader(new File(input)));

		// skip header
		br.readLine();
		String line;

		int skipped = 0;
		int lineNumber = 0;

		// LEGGO il FILE dataset.tsv
		while ((line = br.readLine()) != null) {

			if (lineNumber > 0 && lineNumber % CHECKPOINT == 0) {
				logger.info("Analysed {} entities", lineNumber);
			}

			lineNumber++;

			if (!line.startsWith("http://dbpedia.org/resource/")) {
				skipped++;
				continue;
			}

			String[] splitta2 = line.split("\t");
			String[] rigaCompleta = new String[mappa.keySet().size()];

			rigaCompleta[mappa.get(ENTITY_URI_COLUMN_LABEL)] = splitta2[0];
			rigaCompleta[mappa.get(CLASS_COLUMN_LABEL)] = splitta2[1];

			List<String> tokens = new ArrayList<>();
			if (splitta2.length > abstractCoulum) {
				tokens = Utils.tokenize(splitta2[abstractCoulum]);
			}

			String[] uriToken = Utils.getUriTokens(splitta2[0]);

			// getNUMBER URI TOKENS
			rigaCompleta[mappa.get(NUMBER_URI_TOKENS)] = Utils.getNumberOfURITokens(uriToken) + "";

			// getNUMBER_OF_TOKENS_FOUND_IN_ABSTRACT
			rigaCompleta[mappa.get(NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT)] = Utils.getNumberOfURITokensInAbstract(uriToken,
					tokens) + "";

			// getNUMBER_URI_CAPITAL_TOKENS
			rigaCompleta[mappa.get(NUMBER_URI_CAPITAL_TOKENS)] = Utils
					.getNumberOfURITokensStartingWithCapitalCharacters(uriToken) + "";

			rigaCompleta[mappa.get(__isDetectedByORA)] = splitta2[4];
			rigaCompleta[mappa.get(__isDetectedBySENECA)] = splitta2[3];

			JSONObject obj_properties = new JSONObject(splitta2[propertiesColum]);
			for (String k : obj_properties.keySet()) {
				if (mappa.containsKey(k))
					rigaCompleta[mappa.get(k)] = obj_properties.getInt(k) + "";
			}

			for (String token : tokens) {
				if (mappa.containsKey(TOKEN_PREFIX_ATTRIBUTE + token))
					rigaCompleta[mappa.get(TOKEN_PREFIX_ATTRIBUTE + token)] = "1.0";
			}

			for (int i = 0; i < rigaCompleta.length - 1; i++) {
				if (rigaCompleta[i] == null) {
					fos.write("0.0".getBytes());
				} else {
					fos.write(rigaCompleta[i].getBytes());
				}
				fos.write('\t');
			}
			if (rigaCompleta[rigaCompleta.length - 1] != null) {
				fos.write(rigaCompleta[rigaCompleta.length - 1].getBytes());
			} else {
				fos.write("0.0".getBytes());
			}
			fos.write('\n');
		}
		logger.info("Number of skipped lines {}", skipped);
		br.close();
		fos.close();
	}

}
