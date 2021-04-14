package CellularAutomata.CarMoving1D;

import GenCol.entity;

public class inputEntity extends entity{
	private String input;
	private double sigma;

	public inputEntity(String input, double sig) {
		super(input);
		this.input = input;
		this.sigma = sig;

	}

	public String getInput() {
		return input;
	}
	
	public double getSigma() {
		return sigma;
	}

	public void setInput(String input) {
		this.input = input;
	}
	
	public void setSigma(double sig) {
		this.sigma = sig;
	}

}
