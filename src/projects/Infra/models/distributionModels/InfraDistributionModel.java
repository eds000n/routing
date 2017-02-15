package projects.Infra.models.distributionModels;

import sinalgo.configuration.Configuration;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;
import sinalgo.runtime.Main;
import sinalgo.tools.statistics.Distribution;
import sinalgo.tools.statistics.UniformDistribution;

public class InfraDistributionModel extends DistributionModel {

	private UniformDistribution rand = new UniformDistribution(0,Configuration.dimX);
	private int nnodes=1; 
	/* (non-Javadoc)
	 * @see distributionModels.DistributionModelInterface#getOnePosition()
	 */
	public Position getNextPosition() {
		if (nnodes == 1){
			nnodes++;
			return new Position(0.001*Configuration.dimX,0.001*Configuration.dimY,0);
		}else {	
			double randomPosX = rand.nextSample();// * Configuration.dimX;
			double randomPosY = rand.nextSample();// * Configuration.dimY;
			double randomPosZ = 0;
			if(Main.getRuntime().getTransformator().getNumberOfDimensions() == 3) {
				randomPosZ = rand.nextSample();// * Configuration.dimZ;
			}
//			System.out.println("Posicao "+ nnodes +" "+ randomPosX + "\t" + randomPosY);
//			nnodes++;
			return new Position(randomPosX, randomPosY, randomPosZ);
		}
	}

}
