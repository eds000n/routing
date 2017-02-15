package projects.GA.utils;

public class Vertex{
	public int ID;			//Node ID 1-indexed 
	public int terminal;
	public double x, y;
	public double battery;	//Current used battery
	public double pbattery;	//Previous battery level, indicates the previous level of energy that the algorithm run with
	public Vertex(int ID, int terminal, double xCoord, double yCoord, double battery){
		this.ID = ID;
		this.terminal = terminal;
		this.x = xCoord;
		this.y = yCoord;
		this.battery = battery;
		this.pbattery = battery;
	}
}