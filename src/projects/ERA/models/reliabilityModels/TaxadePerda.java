package projects.ERA.models.reliabilityModels;

import projects.ERA.nodes.messages.ERADataMessage;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.ReliabilityModel;
import sinalgo.nodes.messages.Packet;
import sinalgo.runtime.Main;
import sinalgo.tools.statistics.Distribution;

public class TaxadePerda extends ReliabilityModel {
	java.util.Random rand = Distribution.getRandom();
	private double dropRate = 0;
	//public static double dropRate = 0;
	/* (non-Javadoc)
	 * @see sinalgo.models.ReliabilityModel#reachesDestination(sinalgo.nodes.messages.Packet)
	 */
	public boolean reachesDestination(Packet p){ 
		if(p.message instanceof ERADataMessage){
			double r = rand.nextDouble();
			return(r > dropRate);
		}else{
			return(true);
		}
	}
	
	/**
	 * Creates a new Drop Rate Reliability Model instance.
	 */
	public TaxadePerda() {
		try {
			dropRate = Configuration.getDoubleParameter("TaxadePerda/dropRate");
		} catch (CorruptConfigurationEntryException e) {
			Main.fatalError("Missing configuration entry for the Message Transmission Model:\n" + e.getMessage());
		}
	}
	

}
