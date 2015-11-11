import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.HashMap;
public class GameSec {

	@SuppressWarnings("resource")
	
	static double lamda=1;
	public static void main(String args[]) throws FileNotFoundException
	{
		/*int noUsers = 34;
		
		FileReader fr = new FileReader(new File("/Users/sushamakarumanchi/Documents/workspace/GameSec/ComfortValuesUsers.rtf"));
		//File file = new File("/Users/sushamakarumanchi/Documents/workspace/GameSec/inputComfValues.txt");
		//Scanner scanner = new Scanner(file);
		
		String comfortHolder;
		
		while(scanner.hasNextLine())
		{
			Follower follower = new Follower(Float.parseFloat(scanner.nextLine().trim()));
			followersList.add(follower);
		}
		
		for(int i = 0;i< noUsers; i++)
		{
			System.out.println(followersList.get(i).getComfort());
		}
		scanner.close();*/
		double w=1;
		double epsilon = 0.1; //for checking difference among all the expected values
		ArrayList<Double> privacySettings =new ArrayList<Double>();
		ArrayList<Double> comfortValues = new ArrayList<Double>();
		ArrayList<Double> peerScores = new ArrayList<Double>();
		ArrayList<Double> strats = new ArrayList<Double>();
		ArrayList<Double> discretes = new ArrayList<Double>();		
		ArrayList<ArrayList<Double>> discomfortsEachUser =new ArrayList<ArrayList<Double>>();
		ArrayList<Follower> followersList = new ArrayList<Follower>();
		ArrayList<ArrayList<Double>> probabilities =new ArrayList<ArrayList<Double>>();
		ArrayList<Double> peerScoresNew =new ArrayList<Double>();
		ArrayList<Double> expectedValuesAllUsers = new ArrayList<Double>();	
		ArrayList<Double> choicesUsers = new ArrayList<Double>();	


		int countIterator = 0;
		
		//initialize privacy settings
		privacySettings.add(0.0);
		privacySettings.add(0.2);
		privacySettings.add(0.4);
		privacySettings.add(0.6);
		privacySettings.add(0.8);
		privacySettings.add(1.0);
		

		Random random = new Random();
		int noofUsers;
		Scanner in = new Scanner(System.in);
		System.out.println("Enter no of users:");
		noofUsers=in.nextInt();
		
		for(int i=0;i<noofUsers;i++)
		{
			comfortValues.add(random.nextDouble());
			System.out.println(comfortValues.get(i));

			Follower follower = new Follower(comfortValues.get(i));
			followersList.add(follower);
			followersList.get(0).setLeader();
		}
		System.out.println("\n\n");

		
		long startTime = System.nanoTime();
	    
		//compute peer scores
		for(int i=1;i<noofUsers;i++)  //not considering leader
		{
			peerScores.add(getPeerScore(i,comfortValues,noofUsers));
			followersList.get(i).setPeerScore(getPeerScore(i,comfortValues,noofUsers));
  			//System.out.println("PS="+getPeerScore(i,comfortValues,noofUsers));

		}
		/*for(int i=0;i<noofUsers-1;i++)
 	    {
		    System.out.println("PS="+peerScores.get(i));
 	    }*/

		//compute strats
		for(int i=1;i<noofUsers;i++)  //not considering leader
		{
			strats.add(getStrat(i,followersList.get(i).getComfort(),followersList.get(i).getPeerScore(),w));
			followersList.get(i).setStrat(getStrat(i,followersList.get(i).getComfort(),followersList.get(i).getPeerScore(),w));
		}
	
		/*for(int i=0;i<noofUsers-1;i++)
 	    {
		    System.out.println("Strats="+strats.get(i));
 	    }*/
		
		//Compute discomforts
		for(int i=0;i<noofUsers-1;i++)  //not considering leader
		{
			ArrayList<Double> discomforts = new ArrayList<Double>();
			for(int j=0;j<privacySettings.size();j++)
				discomforts.add(Math.abs(strats.get(i)-privacySettings.get(j)));
			discomfortsEachUser.add(discomforts);
		}
		
		/*
		System.out.println("discomfort values=");
		for(int i=0;i<noofUsers-1;i++)
 	    {
    		for(int j=0;j<discomfortsEachUser.get(i).size();j++)
    		{
    			System.out.print("<"+discomfortsEachUser.get(i).get(j)+">");
    		}
    		System.out.println("");
 	    } */
		
		//compute discretes ( prob vector of choosing each option for each user)
		for(int i=0;i<noofUsers-1;i++)  //not considering leader
		{
			probabilities.add(getProbabilityVector(discomfortsEachUser.get(i)));
		}
		
		/*
		System.out.println("probabilities/discretes=");
		for(int i=0;i<noofUsers-1;i++)
 	    {
    		for(int j=0;j<probabilities.get(i).size();j++)
    		{
    			System.out.print("<"+probabilities.get(i).get(j)+">");
    		}
    		System.out.println("");
 	    } */
		
		//compute expected values for all users
		for(int i=0;i<noofUsers-1;i++)  //not considering leader
		{
			expectedValuesAllUsers.add(getExpectedValueUser(probabilities.get(i),privacySettings));
		}
		/*
		System.out.println("Expected Values:");
		for(int i=0;i<noofUsers-1;i++)
 	    {
		    System.out.println(expectedValuesAllUsers.get(i));
 	    }*/
		countIterator++;
		
		//compute choices of all users
		choicesUsers = getChoicesUsers(noofUsers,probabilities);
		
		if(expectedValuesWithinRange(expectedValuesAllUsers,epsilon))
		{
			//System.out.println("Within Range");
			long estimatedTime = System.nanoTime()-startTime;
			 //print final privacy settings  
		      for(int i=0;i<choicesUsers.size();i++)
		      {
		    	  System.out.println(choicesUsers.get(i));
		      }
		    //print final privacy settings  
		      for(int i=0;i<probabilities.size();i++)
		      {
		    	  System.out.println("Probability vector "+i);

		          for(int j=0;j<probabilities.get(i).size();j++)

		        	  System.out.print(" "+probabilities.get(i).get(j));
		      }
		    System.out.println("Time="+estimatedTime);
		    System.out.println("Iterations="+countIterator);

			return;
		}
		
		probabilities.clear();
		strats.clear();
		discomfortsEachUser.clear();
		
		
	  //while expected values are within episilon = 0.1 range
        while(!expectedValuesWithinRange(expectedValuesAllUsers,epsilon))
		{
        	probabilities.clear();
    	   
    	  //compute peer scores for all users
    	   for(int i=0;i<noofUsers-1;i++)  //not considering leader
      		{ 
		       peerScoresNew.add(getPeerScoreUserfromChoices(i,noofUsers,choicesUsers));
		       followersList.get(i+1).setPeerScore(getPeerScoreUserfromChoices(i,noofUsers,choicesUsers));
      		      
      		}
    	    
   			expectedValuesAllUsers.clear();

   		   /*	
    	   for(int i=0;i<noofUsers-1;i++)
    	   {
 		        System.out.println("PS="+peerScoresNew.get(i));
    	   }*/
    	   

    	    // compute strats for all users
      		for(int i=1;i<noofUsers;i++)  //not considering leader
      		{
      			strats.add(getStratNewUser(peerScoresNew.get(i-1),w, comfortValues.get(i)));
      			followersList.get(i).setStrat(getStratNewUser(peerScoresNew.get(i-1),w, comfortValues.get(i)));
      		}
      		/*
      		for(int i=0;i<noofUsers-1;i++)
     	    {
  		        System.out.println("strat="+strats.get(i));

     	    } */
      		peerScoresNew.clear();
      		
      		//Compute discomforts
    		for(int i=0;i<noofUsers-1;i++)  //not considering leader
    		{
    			ArrayList<Double> discomforts = new ArrayList<Double>();
    			for(int j=0;j<privacySettings.size();j++)
    				discomforts.add(Math.abs(strats.get(i)-privacySettings.get(j)));
    			discomfortsEachUser.add(discomforts);
    		}
    		
    		/*
    		System.out.println("discomfort values=");
    		for(int i=0;i<noofUsers-1;i++)
     	    {
        		for(int j=0;j<discomfortsEachUser.get(i).size();j++)
        		{
        			System.out.print("<"+discomfortsEachUser.get(i).get(j)+">");
        		}
        		System.out.println("");

     	    } */
    		
    		
    		strats.clear();
    		
    		//compute discretes ( prob vector of choosing each option for each user)
    		for(int i=0;i<noofUsers-1;i++)  //not considering leader
    		{
    			probabilities.add(getProbabilityVector(discomfortsEachUser.get(i)));
    		}
    		
    		/*
    		System.out.println("probabilities=");
    		for(int i=0;i<noofUsers-1;i++)
     	    {
        		for(int j=0;j<probabilities.get(i).size();j++)
        		{
        			System.out.print("<"+probabilities.get(i).get(j)+">");
        		}
        		System.out.println("");

     	    } */
    		
    		discomfortsEachUser.clear();
    		//compute expected values for all users
    		for(int i=0;i<noofUsers-1;i++)  //not considering leader
    		{
    			expectedValuesAllUsers.add(getExpectedValueUser(probabilities.get(i),privacySettings));
    		}
    		
    		/*
    		System.out.println("expected values=");

    		for(int i=0;i<expectedValuesAllUsers.size();i++)
    		{
    			System.out.println(expectedValuesAllUsers.get(i));
    		}*/
    		
    		//compute choices of all users
    		choicesUsers = getChoicesUsers(noofUsers,probabilities);
      		
      		//probabilities.clear(); //change it to this
      		w++;
      		//w = w + 2.1;
            countIterator++;
  
      }
      
      //print final privacy settings  
      for(int i=0;i<choicesUsers.size();i++)
      {
    	  System.out.println(choicesUsers.get(i));
      }
      
    //print final privacy settings  
      for(int i=0;i<probabilities.size();i++)
      {
    	  System.out.println("Probability vector "+i);

          for(int j=0;j<probabilities.get(i).size();j++)

        	  System.out.print(" "+probabilities.get(i).get(j));
      }
        
      long estimatedTime = System.nanoTime()-startTime;
        
      System.out.println("Time="+estimatedTime);
      System.out.println("Iterations="+countIterator);
  
	}	
	
	static boolean expectedValuesWithinRange(ArrayList<Double> expectedValuesAllUsers, double epsilon)
	{
		boolean isWithinRange = true;
		for(int i=0;i<expectedValuesAllUsers.size();i++)
		{
			for(int j=0;j<expectedValuesAllUsers.size();j++)
			{
				if(Math.abs(expectedValuesAllUsers.get(i)-expectedValuesAllUsers.get(j))>epsilon)
				{
					isWithinRange = false;
					return isWithinRange;
				}
			}

		}
		System.out.println("Expected values:");
		for(int i=0;i<expectedValuesAllUsers.size();i++)
		{
			System.out.println(expectedValuesAllUsers.get(i));
		}
		return isWithinRange;
	}
	
	static double getPeerScore(int userID, ArrayList<Double> comfortValues, int noofUsers)
	{
		ArrayList<Double> peerScoresLocal =  new ArrayList<Double>();
		double peerScoreUser = 0.0;
		
		for(int i=0; i <comfortValues.size(); i++)
		{
			if(i==userID||i==0)
				continue;
			peerScoresLocal.add((Double)comfortValues.get(i));
		}

		//compute avg
		for(int i=0;i<peerScoresLocal.size();i++)
		{
			peerScoreUser = peerScoreUser + peerScoresLocal.get(i);
		}
		
	    peerScoreUser = peerScoreUser/(noofUsers-2);
		
	    return peerScoreUser;
		
	}
	
	static double getStrat(int userID, double comfortValue, double peerScore, double w)
	{
		double strat;
		strat = (comfortValue+w*peerScore)/(w+1);
		return strat;
	}
	
	
	static ArrayList<Double> getProbabilityVector(ArrayList<Double> discomfortEachUser)
	{
		ArrayList<Double> probabilityVector = new ArrayList<Double>();
		
		for(int i=0;i<discomfortEachUser.size();i++)
		{
			probabilityVector.add(getProbability(discomfortEachUser.get(i),discomfortEachUser));
			//check sum of probabilities equal to 1
		}
		
		return probabilityVector;
	}
	
	static Double getProbability(double discomfort, ArrayList<Double> discomfortEachUser)
	{
		double probability;
		probability = Math.pow(Math.E,lamda*(-discomfort));
		probability = probability/sumMulLamdaUtility(discomfortEachUser);
		return probability;
	}
	
	static double sumMulLamdaUtility(ArrayList<Double> discomfortEachUser)
	{
		double sum = 0;
		for(int i=0;i<discomfortEachUser.size();i++)
		{
			sum = sum + Math.pow(Math.E,lamda*(-discomfortEachUser.get(i)));
		}
		return sum;
	}
	
	static double getExpectedValueUser(ArrayList<Double> discreteVector, ArrayList<Double> privacySettings)
	{
		double expectedValueUser = 0;
		for(int i=0;i<discreteVector.size();i++)
		{
			expectedValueUser = expectedValueUser + discreteVector.get(i)*privacySettings.get(i);
			
		}
		
		return expectedValueUser;
	}
	
	//compute new peer scores for one user
	/*static double getPeerScoreUserfromExpectedValues(int userID,ArrayList<Double> expectedValuesAllUsers,int noofUsers)
	{
		ArrayList<Double> peerScoresLocal =  new ArrayList<Double>();
		double peerScoreUser = 0.0;
		
		for(int i=0; i <expectedValuesAllUsers.size(); i++)
		{
			if(i==userID)
				continue;
			peerScoresLocal.add((Double)expectedValuesAllUsers.get(i));
		}

		//compute avg
		for(int i=0;i<peerScoresLocal.size();i++)
		{
			peerScoreUser = peerScoreUser + peerScoresLocal.get(i);
		}
		
	    peerScoreUser = peerScoreUser/(noofUsers-2);
		
	    return peerScoreUser;
	}*/
	
	static double getPeerScoreUserfromChoices(int userID, int noofUsers, ArrayList<Double> choicesUsers)
	{
		ArrayList<Double> peerScoresLocal =  new ArrayList<Double>();
		double peerScoreUser = 0.0;
		
		for(int i=0; i<noofUsers-1; i++)
		{
			if(i==userID)
				continue;
			peerScoresLocal.add(choicesUsers.get(i));
		}

		//compute avg
		for(int i=0;i<peerScoresLocal.size();i++)
		{
			peerScoreUser = peerScoreUser + peerScoresLocal.get(i);
		}
		
	    peerScoreUser = peerScoreUser/(noofUsers-2);
		
	    return peerScoreUser;
	}
	
	static ArrayList<Double> getChoicesUsers(int noofUsers,ArrayList<ArrayList<Double>> probabilities)
	{
		double randomNumber;
		ArrayList<Double> choicesUsers = new ArrayList<Double>();
		Random randomGenerator = new Random();
		ArrayList<Double> probUser = new ArrayList<Double>();
		ArrayList<Double> intervals = new ArrayList<Double>();
		
		for(int i=0;i<noofUsers-1;i++)  // not considering the leader
		{
			int intervalsIndex = 0;
			intervals = computeIntervals(probabilities.get(i));
			randomNumber = randomGenerator.nextDouble();
			//System.out.println("Random number="+randomNumber);
			if(intervals.get(intervalsIndex)<=randomNumber&&randomNumber<=intervals.get(intervalsIndex+1))
			{
				choicesUsers.add(0.0);
			}
			
			else if(intervals.get(intervalsIndex+2)<=randomNumber&&randomNumber<=intervals.get(intervalsIndex+3))
			{
				choicesUsers.add(0.2);
			}
			
			else if(intervals.get(intervalsIndex+4)<randomNumber&&randomNumber<=intervals.get(intervalsIndex+5))
			{
				choicesUsers.add(0.4);
			}
			
			else if(intervals.get(intervalsIndex+6)<randomNumber&&randomNumber<=intervals.get(intervalsIndex+7))
			{
				choicesUsers.add(0.6);
			}
			
			else if(intervals.get(intervalsIndex+8)<randomNumber&&randomNumber<=intervals.get(intervalsIndex+9))
			{
				choicesUsers.add(0.8);
			}
			
			else if(intervals.get(intervalsIndex+10)<randomNumber&&randomNumber<=intervals.get(intervalsIndex+11))
			{
				choicesUsers.add(1.0);
			}
		}
		
		return choicesUsers;
		
	}
	
	static ArrayList<Double> computeIntervals(ArrayList<Double> probHolder)
	{
		ArrayList<Double> intervals = new ArrayList<Double>();
		int intervalsIndex=0;
		int probIndex=0;
		intervals.add(0.0);
		intervals.add(probHolder.get(0));
		intervalsIndex = intervalsIndex + 2;
		probIndex = 1;
		for(int i=0;i<5;i++)
		{
			intervals.add(intervals.get(intervalsIndex-1));
			intervals.add(intervals.get(intervalsIndex)+probHolder.get(probIndex));
			intervalsIndex = intervalsIndex + 2;
			probIndex++;
		}
		
		return intervals;
	}
	
	static double getStratNewUser(double peerScore,double w,double comfortValue)
	{
		double strat;
		
		strat = (comfortValue + (w*peerScore)) / (w+1);		
		return strat;
	}
	/*
	ArrayList<Double> getPeerScorefromProb(int userID,ArrayList<ArrayList<Double>> probabilities,int noofUsers)
	{
		ArrayList<Double> peerScoresLocal =  new ArrayList<Double>();
		double peerScoreElement = 0.0;
		
		for(int j=0;j<noofUsers-1;j++)
		{
			if(j==userID)
				continue;
			for(int i=0; i <probabilities.size(); i++)
				peerScoresLocal.add(probabilities.get(i).get(j));
			//compute avg
			for(int i=0;i<peerScoresLocal.size();i++)
			{
				peerScoreElement = peerScoreElement + peerScoresLocal.get(i);
			}
			
			
		}
		
		
		
	    peerScoreUser = peerScoreUser/(noofUsers-2);
		
	    return peerScoreUser;
	}*/
}
