package it.unibo.disi.fox.experiments.fdistinctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputDatasetCreator {

	private static Logger logger = LoggerFactory.getLogger(InputDatasetCreator.class);

	private static PrintWriter printWriter;

	public static void main(String args[]) throws IOException {

		String tmpFile = "/Users/lgu/Desktop/entities.tsv";
		String output = "/Users/lgu/Desktop/out.tsv";
		String dbpediaSparqlEndpoint = "https://dbpedia.org/sparql";

//		selectEntities(tmpFile, dbpediaSparqlEndpoint);
		createInputDataset(tmpFile, output, dbpediaSparqlEndpoint);
	}

	/**
	 * This method write on a tmpFile the list of DBPedia entities. The method assumes that the set of DBPedia entities corresponds to the domain of the property dbo:wikiPageID.
	 * 
	 * @param tmpFile
	 *            absolute path of the file where the list of entities is dumped
	 * @param dbpediaSparqlEndpoint
	 *            the URL of the DBPedia SPARQL endpoint
	 */
	static void selectEntities(String tmpFile, String dbpediaSparqlEndpoint) {
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

	static void createInputDataset(String entities, String outputFile, String dbpediaSPARQLEndpoint) throws IOException {

		FileWriter fileWriter = new FileWriter(outputFile);
		printWriter = new PrintWriter(fileWriter);

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

		while (inputStream.hasNextLine()) {

			String currentEntityURI = inputStream.nextLine().toString();
			pss.setIri("s", currentEntityURI);

			// passo la query allo sparqlendpoint
			QueryExecution qExec = QueryExecutionFactory.sparqlService(dbpediaSPARQLEndpoint, pss.asQuery());

			JSONObject proprieta = new JSONObject();
			String astratto = null;

			try {
				ResultSet results = qExec.execSelect();
				while (results.hasNext()) {
					QuerySolution sol = results.nextSolution();
					String prop = sol.getResource("p").getURI();
					int num = sol.getLiteral("c").getInt();
					proprieta.put(prop, num);
				}
			} finally {
				qExec.close();
			}

			ParameterizedSparqlString pssAbstract = new ParameterizedSparqlString(queryStringAbstract);
			pssAbstract.setIri("s", currentEntityURI);
			QueryExecution qExec2 = QueryExecutionFactory.sparqlService(dbpediaSPARQLEndpoint, pssAbstract.asQuery());

			try {

				ResultSet results = qExec2.execSelect();
				while (results.hasNext()) {
					QuerySolution sol = results.nextSolution();
					astratto = sol.getLiteral("abstract").getString().replace('\n', ' ').replace('\t', ' ');
				}
			} finally {
				qExec.close();

			}
			printWriter.print(currentEntityURI + "\t" + proprieta.toString() + "\t" + astratto + "\n");

		}
		printWriter.close();
		inputStream.close();

	}

}
