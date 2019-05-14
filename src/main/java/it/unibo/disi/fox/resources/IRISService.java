package it.unibo.disi.fox.resources;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.disi.fox.experiments.WekaUtils;
import it.unibo.disi.fox.model.Classification;
import it.unibo.disi.fox.model.Klass;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;

@Path("/iris")
public class IRISService {

	private static Logger logger = LoggerFactory.getLogger(IRISService.class);
	private static SMO svm;
	private static Instances instances;
	private static ArrayList<Attribute> attributes;
	private static Classification classification;

	public static void init(String pathToTrainingDataset, String nameOfClassAttribute) throws Exception {
		// Loading annotations
		logger.info("Init IRIS Annotator Service");
		logger.info("Reading {}", pathToTrainingDataset);

		if (FilenameUtils.getExtension(pathToTrainingDataset).equals("arff")) {
			instances = WekaUtils.loadARFFInstances(pathToTrainingDataset);
		} else {
			instances = WekaUtils.loadXRFFInstances(pathToTrainingDataset);
		}
		logger.info("Class index:{}", instances.classIndex());
		instances.setClassIndex(instances.numAttributes() - 1);
		logger.info("Class index:{}", instances.classIndex());

		attributes = WekaUtils.getAttributes(instances);
		attributes.add(instances.classAttribute());

		Set<Klass> klassesInTheClassification = new HashSet<>();
		Enumeration<Object> values = instances.classAttribute().enumerateValues();
		while (values.hasMoreElements()) {
			klassesInTheClassification.add(new Klass((String) values.nextElement()));
		}
		classification = new Classification(instances.relationName(), klassesInTheClassification, instances.relationName());

		svm = new SMO();
		svm.buildClassifier(instances);

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/classify")
	public String classify(String text) {
		try {
			return classifyObject(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}

	private static String classifyObject(String text) throws Exception {

		JSONObject obj = new JSONObject(text);

		logger.info("Classifying entity {}", obj.getString("entityURI"));

		JSONObject features = obj.getJSONObject("features");
		Iterator<String> featureNames = features.keys();
		SparseInstance si = new SparseInstance(instances.numAttributes());
		while (featureNames.hasNext()) {
			String featureName = (String) featureNames.next();
			JSONArray values = features.getJSONArray(featureName);
			double value = values.getDouble(0);
			si.setValue(instances.attribute(featureName), value);
		}

		// Classifying entity
		Instances dataset = new Instances("Test", attributes, 1);
		dataset.add(si);
		dataset.setClassIndex(dataset.numAttributes() - 1);
		double res = svm.classifyInstance(dataset.firstInstance());
		logger.info("Result: {} ({})", instances.classAttribute().value((int) res), svm.distributionForInstance(dataset.firstInstance())[(int) res]);

		JSONObject classificationObject = new JSONObject();
		classificationObject.put("name", classification.getName());
		JSONArray classes = new JSONArray();
		classification.getClasses().forEach(c -> {
			classes.put(c.getClassName());
		});
		classificationObject.put("classes", classes);

		JSONArray annotations = new JSONArray();
		JSONObject jsonCB = new JSONObject();
		jsonCB.put("classification", classificationObject);
		jsonCB.put("method", "Support Vector Machine trained on " + instances.relationName());
		jsonCB.put("label", instances.classAttribute().value((int) res));
		jsonCB.put("confidence", svm.distributionForInstance(dataset.firstInstance())[(int) res]);
		annotations.put(jsonCB);

		obj.put("annotations", annotations);

		return obj.toString();
	}

}
