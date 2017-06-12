package projects.HCCRFD.models.distributionModels;

import java.util.Vector;

import sinalgo.configuration.Configuration;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;
import sinalgo.tools.statistics.Distribution;

public class MiddleSinkDistribution extends DistributionModel {

	private java.util.Random rand = Distribution.getRandom();
//	private Vector<Position> positions = new Vector<>();
	private int returnNum = 0;

	@Override
	public Position getNextPosition() {
		if ( returnNum == 0 ){
			int w = Configuration.dimX;
			int h = Configuration.dimY;
			returnNum++;
			return new Position(w/2, h/2, 0);
		}else
			return new Position(rand.nextDouble() * Configuration.dimX, rand.nextDouble() * Configuration.dimY, 0);
			
	}

}
