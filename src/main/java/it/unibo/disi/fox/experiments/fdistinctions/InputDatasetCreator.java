package it.unibo.disi.fox.experiments.fdistinctions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONObject;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputDatasetCreator {

	private static Logger logger = LoggerFactory.getLogger(InputDatasetCreator.class);
	public static final int CHECKPOINT = 100, QUERY_ATTEMPTS = 3, SLEEP = 60000;

	public static void main(String args[]) throws IOException {

		String type = args[0];
		String endpoint = args[1];
		String entityFile = args[2];
		String featureFile = args[3];
		boolean excludeSelection = args.length > 4 ? Boolean.parseBoolean(args[4]) : false;

		logger.info("Parameters\nSPARQL endpoint: {}\nEntity file: {}\nOutput file: {}\nExclude selection: {}", endpoint, entityFile, featureFile, excludeSelection);
		System.out.println(TripleID.size());

		if (type.equalsIgnoreCase("sparql")) {
			createDatasetUsingSPARQLEndpoint(entityFile, featureFile, endpoint, excludeSelection);
		} else if (type.equalsIgnoreCase("dump")) {
			createDatasetUsingDump(entityFile, featureFile, endpoint, excludeSelection);
		}

	}

	private static void createDatasetUsingDump(String entityFile, String featureFile, String dump, boolean excludeSelection) throws IOException {
		if (!excludeSelection)
			selectEntitiesUsingDump(entityFile, dump);
		createInputDatasetUsingDump(entityFile, featureFile, dump);
	}

	private static void selectEntitiesUsingDump(String entityFile, String dump) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(entityFile));
		HDT hdt = HDTManager.mapIndexedHDT(dump);
		int c = 0;
		try {
			IteratorTripleString it = hdt.search("", "http://dbpedia.org/ontology/wikiPageID", "");
			while (it.hasNext()) {
				if (c > 0 && c % 10000 == 0) {
					logger.info("{} entities dumped!", c);
				}
				c++;
				TripleString tripleString = (TripleString) it.next();
				fos.write((tripleString.getSubject() + "\n").getBytes());
			}
		} catch (org.rdfhdt.hdt.exceptions.NotFoundException e) {
			logger.error(e.getMessage());
		}
		fos.flush();
		fos.close();
	}

	private static void createInputDatasetUsingDump(String entityFile, String featureFile, String dump) throws IOException {
		logger.info("Creating input dataset!");
		//FIXME to complete
		// In order to reuse the results of precedent runs, take the uri of the last entity found in an existent output file.
		String lastEntityAnalysed = null;
		BufferedReader br = new BufferedReader(new FileReader(featureFile));
		String line;
		while ((line = br.readLine()) != null) {
			lastEntityAnalysed = line.split("\t")[0];
		}
		br.close();

		logger.info("Found a run to resume: {}", lastEntityAnalysed);

		FileOutputStream fos = new FileOutputStream(new File(featureFile), true);
		Scanner inputStream = null;
		try {
			inputStream = new Scanner(new File(entityFile));
		} catch (FileNotFoundException e) {
			logger.error("Error while reading {} {}", e.getMessage(), entityFile);
		}
		int c = 0;

		// if lastEntityAnalysed is null we do not search for the last URI
		boolean foundLastURI = lastEntityAnalysed == null;

		while (inputStream.hasNextLine()) {

			if (c > 0 && c % CHECKPOINT == 0) {
				logger.info("Dumped {} entities", c);
			}
			c++;

			String currentEntityURI = inputStream.nextLine().toString();

			if (!foundLastURI) {
				if (currentEntityURI.equals(lastEntityAnalysed)) {
					foundLastURI = true;
					logger.info("Resuming from {}", currentEntityURI);
				} else {
					continue;
				}
			}

			JSONObject proprieta = new JSONObject();
			String astratto = null;

			fos.write((currentEntityURI + "\t" + proprieta.toString() + "\t" + astratto + "\n").getBytes());
			fos.flush();
		}
		inputStream.close();
		fos.close();

	}

	/**
	 * This method creates both an entity list file and a feature file.
	 * 
	 * @param entityFile
	 * @param featureFile
	 * @param dbpediaSparqlEndpoint
	 * @param excludeSelection
	 * @throws IOException
	 */
	private static void createDatasetUsingSPARQLEndpoint(String entityFile, String featureFile, String dbpediaSparqlEndpoint, boolean excludeSelection) throws IOException {
		if (!excludeSelection)
			selectEntitiesUsingSPARQLEndpoint(entityFile, dbpediaSparqlEndpoint);
		createInputDatasetUsingSPARQLEndpoint(entityFile, featureFile, dbpediaSparqlEndpoint);
	}

	/**
	 * This method writes on tmpFile the list of DBPedia entities. The method assumes that the set of DBPedia entities corresponds to the domain of the property dbo:wikiPageID.
	 * 
	 * @param tmpFile
	 *            absolute path of the file where the list of entities is dumped
	 * @param dbpediaSparqlEndpoint
	 *            the URL of the DBPedia SPARQL endpoint
	 */
	private static void selectEntitiesUsingSPARQLEndpoint(String tmpFile, String dbpediaSparqlEndpoint) {
		PrintWriter outputStream = null;
		try {
			outputStream = new PrintWriter(tmpFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		boolean doQuery = true;
		int i = 0;

		logger.info("Gathering entity URIs started!");
		while (doQuery) {
			//@f:off
			String queryStringSubjects = "PREFIX dbo: <http://dbpedia.org/ontology/> "
					+ "select ?s {"
					+ "?s dbo:wikiPageID ?o "
					+ "} limit 10000 "
					+ "offset " + i * 10000;
			//@f:on
			QueryExecution qExec0 = QueryExecutionFactory.sparqlService(dbpediaSparqlEndpoint, QueryFactory.create(queryStringSubjects));
			try {
				logger.trace("Executing \n{}\n on {}", queryStringSubjects, dbpediaSparqlEndpoint);
				ResultSet results = qExec0.execSelect();
				if (!(results.hasNext())) {
					logger.trace("No entities are returned!");
					doQuery = false;
					outputStream.close();
				}
				while (results.hasNext()) {
					QuerySolution sol0 = results.nextSolution();
					outputStream.print(sol0.get("?s").asResource().getURI() + "\n");
				}
			} finally {
				qExec0.close();
			}
			i++;
		}
		logger.info("Gathering entity URIs ended!");
	}

	/**
	 * This method writes on outputFile features of the entities in the entity list of entities file. The format of the output file is: <entityURI>\t<Features>\t<Abstract> The features are store using a JSON Object {"featureURI":numberOfDifferentValuesForThatFeature}.
	 * 
	 * @param entities
	 *            a tsv file with the list of entities
	 * @param outputFile
	 * @param dbpediaSPARQLEndpoint
	 * @throws IOException
	 */
	private static void createInputDatasetUsingSPARQLEndpoint(String entities, String outputFile, String dbpediaSPARQLEndpoint) throws IOException {

		logger.info("Creating input dataset!");

		// In order to reuse the results of precedent runs, take the uri of the last entity found in an existent output file.
		String lastEntityAnalysed = null;

		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		String line;
		while ((line = br.readLine()) != null) {
			lastEntityAnalysed = line.split("\t")[0];
		}
		br.close();

		logger.info("Found a run to resume: {}", lastEntityAnalysed);

		FileOutputStream fos = new FileOutputStream(new File(outputFile), true);

		Scanner inputStream = null;

		try {
			inputStream = new Scanner(new File(entities));
		} catch (FileNotFoundException e) {
			logger.error("Error while reading {} {}", e.getMessage(), entities);
		}

		//@f:off
		String queryStringProperties = "select ?p (count(DISTINCT ?o) AS ?c){ "
				+ "?s ?p ?o "
				+ "} "
				+ "GROUP BY ?p ";
		
		String queryStringAbstract = "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "select distinct ?abstract  "
				+ "{ ?s dbo:abstract ?abstract "
				+ "FILTER (lang(?abstract) = 'en')}";
		//@f:on

		ParameterizedSparqlString pss = new ParameterizedSparqlString(queryStringProperties);

		int c = 0;

		// if lastEntityAnalysed is null we do not search for the last URI
		boolean foundLastURI = lastEntityAnalysed == null;

		boolean blocked = false;

		while (inputStream.hasNextLine()) {

			if (c > 0 && c % CHECKPOINT == 0) {
				logger.info("Dumped {} entities", c);
			}
			c++;

			String currentEntityURI = inputStream.nextLine().toString();

			if (!foundLastURI) {
				if (currentEntityURI.equals(lastEntityAnalysed)) {
					foundLastURI = true;
					logger.info("Resuming from {}", currentEntityURI);
				} else {
					continue;
				}
			}

			pss.setIri("s", currentEntityURI);

			QueryExecution qExec = QueryExecutionFactory.sparqlService(dbpediaSPARQLEndpoint, pss.asQuery());

			JSONObject proprieta = new JSONObject();
			boolean propertiesRetrieved = false;
			String astratto = null;

			for (int i = 0; i < QUERY_ATTEMPTS && !propertiesRetrieved; i++) {
				try {
					ResultSet results = qExec.execSelect();
					while (results.hasNext()) {
						QuerySolution sol = results.nextSolution();
						String prop = sol.getResource("p").getURI();
						int num = sol.getLiteral("c").getInt();
						proprieta.put(prop, num);
					}
					propertiesRetrieved = true;
				} catch (Exception e) {
					e.printStackTrace();
					try {
						logger.info("SLEEP");
						Thread.sleep(SLEEP);
						logger.info("RESUME");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} finally {
					qExec.close();
				}
			}

			if (!propertiesRetrieved) {
				logger.info("Could not retrieve properties for entity {}", currentEntityURI);
				blocked = true;
				break;
			}

			ParameterizedSparqlString pssAbstract = new ParameterizedSparqlString(queryStringAbstract);
			pssAbstract.setIri("s", currentEntityURI);
			QueryExecution qExec2 = QueryExecutionFactory.sparqlService(dbpediaSPARQLEndpoint, pssAbstract.asQuery());
			boolean abstractRetrieved = false;
			for (int i = 0; i < QUERY_ATTEMPTS && !abstractRetrieved; i++) {
				try {
					ResultSet results = qExec2.execSelect();
					while (results.hasNext()) {
						QuerySolution sol = results.nextSolution();
						astratto = sol.getLiteral("abstract").getString().replace('\n', ' ').replace('\t', ' ');
					}
					abstractRetrieved = true;
				} catch (Exception e) {
					e.printStackTrace();
					try {
						logger.info("SLEEP");
						Thread.sleep(SLEEP);
						logger.info("RESUME");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} finally {
					qExec.close();
				}
			}

			if (!abstractRetrieved) {
				logger.info("Could not retrieve abstract for entity {}", currentEntityURI);
				blocked = true;
				break;
			}

			fos.write((currentEntityURI + "\t" + proprieta.toString() + "\t" + astratto + "\n").getBytes());
			fos.flush();
		}
		inputStream.close();
		fos.close();

		if (!blocked)
			logger.info("Input dataset created!");

	}

}
