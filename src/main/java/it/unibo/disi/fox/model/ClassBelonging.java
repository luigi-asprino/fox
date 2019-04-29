package it.unibo.disi.fox.model;

public class ClassBelonging {

	private Klass klass;
	private double confidence;
	private Classification classification;

	public ClassBelonging(Klass klass, double confidence, Classification classification) {
		super();
		this.klass = klass;
		this.confidence = confidence;
		this.setClassification(classification);
	}

	public Klass getKlass() {
		return klass;
	}

	public void setKlass(Klass klass) {
		this.klass = klass;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public Classification getClassification() {
		return classification;
	}

	public void setClassification(Classification classification) {
		this.classification = classification;
	}

}
