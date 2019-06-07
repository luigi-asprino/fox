package it.unibo.disi.fox.experiments.fdistinctions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.disi.fox.services.SENECA;
import it.unibo.disi.fox.services.Tipalo;
import it.unibo.disi.fox.utils.Utils;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.unsupervised.attribute.Remove;

public class TestClassInstance {

	public static final String TOKEN_PREFIX_ATTRIBUTE = "_t_";
	public static final String NUMBER_URI_TOKENS = "__numberOfTokensInURI";
	public static final String NUMBER_URI_CAPITAL_TOKENS = "__numberOfTokensInURIStartingWithCapitalLetter";
	public static final String NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT = "__numberOfURITokensFoundInAbstract";
	public static final String __isDetectedBySENECA = "__isDetectedBySENECA";
	public static final String __isDetectedByORA = "__isDetectedByORA";
	private static Logger logger = LoggerFactory.getLogger(TestClassInstance.class);
	
	private static final String FILE_HEADER = "Entity_URI\tClass\tConfidence\tSENECA\tTipalo\tNumber_of_URI_tokens\tNUMBER_OF_TOKENS_FOUND_IN_ABSTRACT\tNUMBER_URI_CAPITAL_TOKENS\tProperties\tAbstract\n";

	private static final int CHECKPOINT = 10000;

	public static void main(String[] args) {
		try {

			// file di configurazione
			Configurations configs = new Configurations();
			Configuration config;
			if (args.length > 0) {
				config = configs.properties(args[0]);
			} else {

				config = configs.properties("config.properties");
			}

			// Load instances
			// Note: pass the filepath of the XRFF as main parameter class_instance (dataset
			// for training)
			Instances instances = WekaUtils.loadXRFFInstances(config.getString("training_classinstance"));
			new File(config.getString("output_folder")).mkdirs();

			FileWriter fileWriter = new FileWriter(config.getString("output_folder") + "/classinstance_features.tsv");
			PrintWriter printWriter = new PrintWriter(fileWriter);

			FileWriter fileWriter_noFeatures = new FileWriter(config.getString("output_folder") + "/classinstance.tsv");
			PrintWriter printWriter_noFeatures = new PrintWriter(fileWriter_noFeatures);
			logger.info("caricamento file e instances ");

			printWriter.print(FILE_HEADER);
			// Create a new classifier
			SMO svm = new SMO();

			// remove the attribute id from instances
			Attribute idAtt = instances.attribute("ID");
			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(svm);
			logger.info("nuovo classificatore");
			Remove rm = new Remove();
			rm.setAttributeIndicesArray(new int[] { idAtt.index() });
			fc.setFilter(rm);

			svm.buildClassifier(instances);
			logger.info("build classifier");

			// caricamento del file TSV
			String nomeFile = config.getString("to_classify");
			String line = "";
			int line_number = 0;
			try (BufferedReader br = new BufferedReader(new FileReader(nomeFile))) {

				SENECA seneca = SENECA.getInstance(config.getString("SENECA_classes"),
						config.getString("SENECA_PhysicalObjects"));
				Tipalo tipalo = Tipalo.getInstance(config.getString("Tipalo_classes"),
						config.getString("Tipalo_PhysicalObjects"));

				ArrayList<Attribute> atts = new ArrayList<>();
				for (int i = 0; i < instances.numAttributes(); i++) {
					atts.add(instances.attribute(i));
				}
				logger.info("Number of attributes: " + atts.size());

				while ((line = br.readLine()) != null) {

					if (line_number > 0 && line_number % CHECKPOINT == 0) {
						logger.info("{} lines processed", line_number);
					}
					line_number++;

					String[] array = line.split("\t");

					String uriEntity = array[0];

					if (!uriEntity.startsWith("http://dbpedia.org/resource")) {
						throw new RuntimeException("Format error");
					}

					JSONObject propriet = new JSONObject(array[1]);

					String _abstaract = "";
					if (array.length > 2) {
						_abstaract = array[2];
					}

					SparseInstance si = new SparseInstance(instances.numAttributes());

					propriet.keys().forEachRemaining(k -> {
						Attribute a = instances.attribute(k);

						if (a != null) {
							si.setValue(a, propriet.getInt(k));
							logger.trace("setto il token per ogni attributo");
						}
					});

					List<String> tokens = Utils.tokenize(_abstaract);

					// TOKEN PREFIX ATTRIBUTE
					for (String token : tokens) {
						Attribute a = instances.attribute(TOKEN_PREFIX_ATTRIBUTE + token);

						if (a != null) {
							si.setValue(a, 1.0);
							logger.trace("setto il token TOKEN_PREFIX_ATTRIBUTE");
						}
					}

					String[] uriToken = Utils.getUriTokens(uriEntity);
					logger.debug("Number of uri tokens {}", uriToken.length);

					// getNUMBER URI TOKENS
					int n1 = Utils.getNumberOfURITokens(uriToken);

					// getNUMBER_OF_TOKENS_FOUND_IN_ABSTRACT
					int n2 = Utils.getNumberOfURITokensInAbstract(uriToken, tokens);

					// getNUMBER_URI_CAPITAL_TOKENS
					int n3 = Utils.getNumberOfURITokensStartingWithCapitalCharacters(uriToken);

					// NUMBER URI TOKENS
					Attribute b = instances.attribute(NUMBER_URI_TOKENS);
					if (b != null) {
						si.setValue(b, n1);
						logger.trace("setto il token NUMBER_URI_TOKENST");
					}

					// NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT
					Attribute d = instances.attribute(NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT);
					if (d != null) {
						si.setValue(d, n2);
						logger.trace("setto il token NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT");
					}

					// NUMBER_URI_CAPITAL_TOKENS
					Attribute c = instances.attribute(NUMBER_URI_CAPITAL_TOKENS);
					if (c != null) {
						si.setValue(c, n3);
						logger.trace("setto il token NUMBER_URI_CAPITAL_TOKENS");
					}

					// SENECA classe
					String seneca_output;
					Attribute a = instances.attribute(__isDetectedBySENECA);
					if (seneca.isClass(uriEntity)) {
						seneca_output = "YES";
						si.setValue(a, a.indexOfValue("YES"));
						logger.trace("è una classe");
					} else {
						seneca_output = "NO";
						si.setValue(a, a.indexOfValue("NO"));
						logger.trace("non è una classe");
					}

					// TIPALO
					String tipalo_output;
					Attribute at = instances.attribute(__isDetectedByORA);
					if (tipalo.isClass(uriEntity)) {
						tipalo_output = "YES";
						si.setValue(at, at.indexOfValue("YES"));
						logger.trace("è una classe");
					} else {
						tipalo_output = "NO";
						si.setValue(at, at.indexOfValue("NO"));
						logger.trace("non è una classe");
					}

					Instances dataUnlabeled = new Instances("TestInstances", atts, 0);
					dataUnlabeled.add(si);
					dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

					double classe = svm.classifyInstance(dataUnlabeled.firstInstance());
					double confindence = svm.distributionForInstance(dataUnlabeled.firstInstance())[(int) classe];

					// associare il double all'etichetta
					String nomeClasse = instances.classAttribute().value((int) classe);

					// scrittura su file
					printWriter.print(uriEntity + "\t" + nomeClasse + "\t" + confindence + "\t" + seneca_output + "\t"
							+ tipalo_output + "\t" + n1 + "\t" + n2 + "\t" + n3 + "\t" + array[1] + "\t" + _abstaract
							+ "\n");

					printWriter_noFeatures.print(uriEntity + "\t" + nomeClasse + "\n");

				}
				printWriter.close();
				printWriter_noFeatures.close();
				logger.info("scrittura su file conclusa");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
