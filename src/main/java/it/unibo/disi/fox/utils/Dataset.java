package it.unibo.disi.fox.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.lgu.commons.spreadsheets.XLS;
import it.unibo.disi.fox.fdistinctions.experiments.WekaUtils;
import it.unibo.disi.fox.model.ClassBelonging;
import it.unibo.disi.fox.model.Classification;
import it.unibo.disi.fox.model.Klass;
import it.unibo.disi.fox.model.MulticlassEntity;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class Dataset {

	private static Logger logger = LoggerFactory.getLogger(Dataset.class);

	private Map<String, MulticlassEntity> idMap = new HashMap<>();
	private static final int CHECKPOINT = 100;
	private Map<String, String> mapClassName = new HashMap<>();

	/**
	 * Load a dataset of entity from a spreadhsheet complying with the following format:
	 * 
	 * First row: (headings) URI Class Confidence Feature1 ... FeatureN Abstract
	 * 
	 * The entities within the spreadheet belong to the same class.
	 * 
	 * @param xlsPath
	 * @param klass
	 * @param classification
	 * @throws Exception
	 */
	public void loadEdgeFile(String xlsPath, Klass klass, Classification classification) throws Exception {
		logger.info("Loading dataset from {}", xlsPath);
		XLS xls = new XLS(xlsPath);
		addEntitiesFromRowList(xls.getRowsOfSheet(0), classification, true, klass, -1, 0, 2);
	}

	/**
	 * Load a dataset of entity from a spreadhsheet complying with the following format:
	 * 
	 * First row (headings) URI Class Feature1 ... FeatureN Abstract
	 * 
	 * @param xlsPath
	 * @param klass
	 * @param classification
	 * @throws Exception
	 */
	public void loadEdgeFileWithClasses(String fileIn, int colClass, int colUri, Classification classification) throws Exception {
		logger.info("Loading dataset from {}", fileIn);
		XLS xlsIn = new XLS(fileIn);
		addEntitiesFromRowList(xlsIn.getRowsOfSheet(0), classification, false, null, colClass, colUri, Integer.MIN_VALUE);
	}

	/**
	 *
	 * Load a list of entities from a list rows (a row is an array of strings). Each row (i.e. string array) is associated with an entity of the dataset and stores: the URI of the entity, the class of the entity, the confidence of the classification, and the features of the entity. The method assumes
	 * that the first row of the list contains the headings of the columns. It also assumes that columns after colConfidence (or after colClass in the case that the confidence is not provided) until the second to last column stores numeric features.
	 * 
	 * @param rows
	 *            A list of arrays of strings storing the entities
	 * @param sameKlass
	 *            true if entities belong to the same class, false otherwise
	 * @param klass
	 *            if sameKlass is true, it represents the class of the entities
	 * @param colClass
	 *            if sameKlass is false, it contains the column that specifies the class of the entity
	 * @param colUri
	 *            the column specifying the IRI of the entity
	 * @param colConfidence
	 *            the column specifying the confidence of classification
	 * @throws Exception
	 */
	private void addEntitiesFromRowList(List<String[]> rows, Classification classification, boolean sameKlass, Klass klass, int colClass, int colUri, int colConfidence) throws Exception {
		String[] firstRow = rows.get(0);

		logger.info("\nClassification: {}\ncolClass: {}\ncolURI: {}", classification.getName(), colClass, colUri);

		for (int i = 1; i < rows.size(); i++) {
			if (i % CHECKPOINT == 0) {
				logger.info("Loaded {} entities", i);
			}
			String[] row = rows.get(i);
			Klass c = klass;
			if (!sameKlass) {
				// It allows to change the name of the loaded classes by setting mapClassName.
				String klassNameNormalised = mapClassName.containsKey(row[colClass]) ? mapClassName.get(row[colClass]) : row[colClass];
				c = classification.getClass(klassNameNormalised);
				if (c == null) {
					throw new Exception("Could not find class " + klassNameNormalised + " for " + row[colUri]);
				}
			}

			MulticlassEntity e = idMap.get(row[colUri]);
			if (e == null) {
				e = new MulticlassEntity(row[colUri], row[row.length - 1]);
			}
			idMap.put(row[colUri], e);

			if (colConfidence < 0) {
				e.addKlassBelonging(c, 1.0, classification);
			} else {
				e.addKlassBelonging(c, Double.parseDouble(row[colConfidence]), classification);
			}

			// adding feature
			if (colConfidence < 0) {
				for (int f = colClass + 1; f < row.length - 1; f++) {
					e.addFeatureValue(firstRow[f], Double.parseDouble(row[f]));
				}
			} else {
				for (int f = colConfidence + 1; f < row.length - 1; f++) {
					e.addFeatureValue(firstRow[f], Double.parseDouble(row[f]));
				}
			}
		}
	}

	/**
	 * Export the dataset to a JSON file
	 * 
	 * @param pathJSON
	 * @throws IOException
	 */
	public void exportToJSON(String pathJSON) throws IOException {
		logger.info("Exporting dataset to {}", pathJSON);

		JSONArray output = new JSONArray();
		idMap.forEach((k, v) -> {

			JSONObject entityObject = new JSONObject();
			entityObject.put("entityURI", k);
			entityObject.put("features", v.features());
			entityObject.put("abstract", v.get_abstract());

			JSONArray annotations = new JSONArray();
			for (ClassBelonging cb : v.getClassBelonging()) {
				JSONObject jsonCB = new JSONObject();
				JSONObject classificationObject = new JSONObject();
				classificationObject.put("name", cb.getClassification().getName());
				JSONArray classes = new JSONArray();
				cb.getClassification().getClasses().forEach(c -> {
					classes.put(c.getClassName());
				});
				classificationObject.put("classes", classes);

				jsonCB.put("classification", classificationObject);
				jsonCB.put("method", cb.getClassification().getMethodDescription());
				jsonCB.put("label", cb.getKlass().getClassName());
				jsonCB.put("confidence", cb.getConfidence());
				annotations.put(jsonCB);
			}

			entityObject.putOnce("annotations", annotations);

			output.put(entityObject);

		});

		FileOutputStream fos = new FileOutputStream(new File(pathJSON));
		fos.write(output.toString(4).getBytes());
		fos.flush();
		fos.close();

	}

	public void setMapClassName(Map<String, String> mapClassName) {
		this.mapClassName = mapClassName;
	}

	public void loadInstances(Instances instances, String nameOfClassAttribute) {
		Attribute classAttribute = instances.attribute(nameOfClassAttribute);
		instances.setClass(classAttribute);

		Set<Klass> klassesInTheClassification = new HashSet<>();
		Enumeration<Object> values = classAttribute.enumerateValues();
		while (values.hasMoreElements()) {
			klassesInTheClassification.add(new Klass((String) values.nextElement()));
		}
		Classification classification = new Classification(instances.relationName(), klassesInTheClassification, instances.relationName());

		ArrayList<Attribute> attributes = WekaUtils.getAttributes(instances);
		ListIterator<Instance> li = instances.listIterator();
		int identifier = 0;
		while (li.hasNext()) {
			Instance i = (Instance) li.next();
			String uriInstance = identifier++ + "";
			MulticlassEntity me = new MulticlassEntity(uriInstance, "");
			for (Attribute a : attributes) {
				me.addFeatureValue(a.name(), i.value(a));
			}
			me.addKlassBelonging(new Klass(classAttribute.value((int) i.classValue())), i.weight(), classification);
			idMap.put(uriInstance, me);
		}

	}


}
