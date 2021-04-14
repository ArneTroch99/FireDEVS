package CellularAutomata.Diffusion;

import GenCol.entity;

public class inputEntity extends entity{
	private String input;
	private int i, j;

	public inputEntity(int i, int j, String input) {
		super(input);
		this.i = i;
		this.j = j;
		this.input = input;

	}

	public String getInput() {
		return input;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setI(int i) {
		this.i = i;
	}

	public void setJ(int j) {
		this.j = j;
	}

}
