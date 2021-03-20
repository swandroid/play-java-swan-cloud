package optimization.algortihmsConditions;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public class PopulationSizeCalculator
{
	public PopulationSizeCalculator() {
	}
	
	public int calculateNumberIndividualsGMOA(long time, int skeletonPathLength) {
		int numIndividuals;
		long deadline = time;
		long individualCreationTime = 0;
		if(AlgorithmSettings.geneticAlgorithmOption==GeneticAlgorithmOption.GENETICMULTIOBJECTIVEALGORITM)
			individualCreationTime = retrieveSingleIndividualCreationTimeDA(skeletonPathLength);
		else
			retrieveSingleIndividualCreationTimeDRA(skeletonPathLength);
		numIndividuals = (int)Math.floorDiv(deadline,individualCreationTime);
		if(numIndividuals>AlgorithmSettings.maxInitialPopulationSize)
			return AlgorithmSettings.maxInitialPopulationSize;
		else
			return numIndividuals -1;
		
	}
	
	
	public static long retrieveSingleIndividualCreationTimeDA(int length) {
		//example for 20 clusters. Time is in ms
		if(length==1)
			return 500;
		else if(length==2)
			return 1000;
		else if(length==3)
			return 1500;
		else if(length==4)
			return 1700;
		else if(length==5)
			return 1800;
		else if(length==6)
			return 1900;
		else if(length==7)
			return 2000;
		else if(length==8)
			return 3000;
		else if(length==9)
			return 3100;
		else if(length==10)
			return 3200;
		else if(length==11)
			return 3700;
		else if(length==12)
			return 3700;
		else if(length==13)
			return 4500;
		else if(length==14)
			return 5000;
		else if(length==15)
			return 5400;
		else if(length==16)
			return 5300;
		else if(length==17)
			return 5500;
		else if(length==18)
			return 6200;
		else if(length==19)
			return 6500;
		else
			return 7000;
	}
	
	public static long retrieveSingleIndividualCreationTimeDRA(int length) {
		//example for 20 clusters. Time is in ms
		if(length==1)
			return 600;
		else if(length==2)
			return 700;
		else if(length==3)
			return 800;
		else if(length==4)
			return 900;
		else if(length==5)
			return 1000;
		else if(length==6)
			return 1600;
		else if(length==7)
			return 1700;
		else if(length==8)
			return 2000;
		else if(length==9)
			return 2500;
		else if(length==10)
			return 2600;
		else if(length==11)
			return 2700;
		else if(length==12)
			return 3000;
		else if(length==13)
			return 3100;
		else if(length==14)
			return 3200;
		else if(length==15)
			return 3300;
		else if(length==16)
			return 3500;
		else if(length==17)
			return 3700;
		else if(length==18)
			return 3800;
		else if(length==19)
			return 3900;
		else
			return 4000;
	}
	
}
