package sinalgo.models.EnergyModel;

public interface IEnergy {
	
	public void spend(EnergyMode mode);
    public void spend(EnergyMode mode, float time);
    
    public Float getTotalSpentEnergy();
    
    public Float getEnergy();
    
    /**
     * This method returns the initial energy of the node.
     * @return InitialEnergy
     */
    public Float getInitialEnergy();
}
