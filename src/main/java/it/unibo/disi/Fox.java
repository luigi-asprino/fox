package it.unibo.disi;

import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.disi.features.SENECA;
import it.unibo.disi.features.Tipalo;

public class Fox {

	private static Logger logger = LoggerFactory.getLogger(Fox.class);

	public static void main(String[] args) {
		try {
			Configurations configs = new Configurations();
			Configuration config = configs.properties(args[0]);

			SENECA seneca = SENECA.getInstance(config.getString("SENECA_classes"), config.getString("SENECA_PhysicalObjects"));
			Tipalo tipalo = Tipalo.getInstance(config.getString("Tipalo_classes"), config.getString("Tipalo_PhysicalObjects"));

			logger.info("{}", seneca.isPhysicalObject("http://dbpedia.org/resource/Xanthophyll"));
			logger.info("{}", tipalo.isPhysicalObject("http://dbpedia.org/resource/Xanthophyll"));

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
