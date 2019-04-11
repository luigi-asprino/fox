package it.unibo.disi.experiments;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.XRFFLoader;
import weka.core.converters.XRFFSaver;

/**
 * 
 * @author Luigi Asprino
 *
 */
public class WekaUtils {

	
	/**
	 * Save a set of instances in XRFF format
	 * 
	 * @param instances is the set of instances to save
	 * @param fileOut is absolute filepath of the file where instances will be saved
	 * @throws IOException
	 */
	public static void saveInstancesToXRFF(Instances instances, String fileOut) throws IOException {
		XRFFSaver saver = new XRFFSaver();
		saver.setFile(new File(fileOut));
		saver.setInstances(instances);
		saver.writeBatch();
	}
	
	/**
	 * Load a set of instances from an input file.
	 * 
	 * @param fileIn the absolute filepath of the input file.
	 * @return
	 * @throws IOException
	 */
	public static Instances loadXRFFInstances(String fileIn) throws IOException {
		XRFFLoader loader = new XRFFLoader();
		loader.setFile(new File(fileIn));
		Instances result = loader.getDataSet();
		return result;
	}

}
