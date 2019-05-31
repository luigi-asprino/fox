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

public class TestPhysicalObject {

	public static final String TOKEN_PREFIX_ATTRIBUTE = "_t_";
	public static final String __isDetectedBySENECA = "__isDetectedBySENECA";
	public static final String __isDetectedByORA = "__isDetectedByORA";
	private static final String FILE_HEADER = "Entity URI\tClass\tConfidence\tSENECA\tTìpalo\tProperties\tAbstract\t";
	private static final int CHECKPOINT = 10000;
	private static Logger logger = LoggerFactory.getLogger(TestClassInstance.class);

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
			// Note: pass the filepath of the XRFF as main parameter
			new File(config.getString("output_folder")).mkdirs();
			Instances instances = WekaUtils.loadXRFFInstances(config.getString("training_physisicalobject"));
			FileWriter fileWriter = new FileWriter(config.getString("output_folder") + "/physicalObject_features.tsv");
			PrintWriter printWriter = new PrintWriter(fileWriter);

			FileWriter fileWriterNoFeature = new FileWriter(config.getString("output_folder") + "/physicalObject.tsv");
			PrintWriter printWriterNoFeature = new PrintWriter(fileWriterNoFeature);

			printWriter.print(FILE_HEADER);
			logger.info("carico file e instances ");

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

				SENECA seneca = SENECA.getInstance(config.getString("SENECA_classes"), config.getString("SENECA_PhysicalObjects"));
				Tipalo tipalo = Tipalo.getInstance(config.getString("Tipalo_classes"), config.getString("Tipalo_PhysicalObjects"));

				ArrayList<Attribute> atts = new ArrayList<>();
				for (int i = 0; i < instances.numAttributes(); i++) {
					atts.add(instances.attribute(i));
				}

				while ((line = br.readLine()) != null) {
					if (line_number > 0 && line_number % CHECKPOINT == 0) {
						logger.info("{} lines processed", line_number);
					}
					line_number++;

					String[] array = line.split("\t");

					String uriEntity = array[0];
					JSONObject propriet = new JSONObject(array[1]);

					logger.trace("mappa di proprieta");
					String _abstaract = "";
					if (array.length > 2)
						_abstaract = array[2];

					SparseInstance si = new SparseInstance(instances.numAttributes());

					propriet.keys().forEachRemaining(k -> {
						Attribute a = instances.attribute(k);
						if (a != null) {
							si.setValue(a, propriet.getInt(k));

						}
					});

					List<String> tokens = Utils.tokenize(_abstaract);

					for (String token : tokens) {
						Attribute a = instances.attribute(TOKEN_PREFIX_ATTRIBUTE + token);

						if (a != null) {
							si.setValue(a, 1.0);

						}
					}

					// SENECA physical object
					Attribute a = instances.attribute(__isDetectedBySENECA);
					String seneca_output;
					if (seneca.isPhysicalObject(uriEntity)) {
						seneca_output = "YES";
						si.setValue(a, a.indexOfValue("YES"));
						logger.trace("è un oggetto fisico");
					} else {
						seneca_output = "NO";
						si.setValue(a, a.indexOfValue("NO"));
						logger.trace("non è un oggetto fisico");
					}

					// TIPALO
					Attribute at = instances.attribute(__isDetectedByORA);
					String tipalo_output;
					if (tipalo.isPhysicalObject(uriEntity)) {
						si.setValue(at, at.indexOfValue("YES"));
						tipalo_output = "YES";
						logger.trace("è un oggetto fisico");
					} else {
						tipalo_output = "NO";
						si.setValue(at, at.indexOfValue("NO"));
						logger.trace("non è un oggetto fisico");
					}

					Instances dataUnlabeled = new Instances("TestInstances", atts, 0);
					dataUnlabeled.add(si);
					dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

					double classe = svm.classifyInstance(dataUnlabeled.firstInstance());
					double confindence = svm.distributionForInstance(dataUnlabeled.firstInstance())[(int) classe];

					// associare il double all'etichetta
					String nomeClasse = instances.classAttribute().value((int) classe);

					// scrittura su file
					printWriter.print(uriEntity + "\t" + nomeClasse + "\t" + confindence + "\t" + seneca_output + "\t" + tipalo_output + "\t" + array[1] + "\t" + _abstaract + "\n");

					printWriterNoFeature.print(uriEntity + "\t" + nomeClasse + "\n");

				}
				printWriter.close();
				printWriterNoFeature.close();
				logger.info("scrittura su file conclusa");

			}

		} catch (Exception e) {
			e.printStackTrace();

		}

	}
}
