import java.util.ArrayList;

public class Follower {
	
	double comfort,peerScore,strat;
	
	ArrayList<Double>  discomforts = new ArrayList<Double>();
	
	
	int leader=0;
	
	Follower(double comfort)
	{
		this.comfort = comfort;
	}
	
	void setPeerScore(double peerScore)
	{
		this.peerScore = peerScore;
	}
	
	void setStrat(double strat)
	{
		this.strat=strat;
	}
	
	void setDiscomforts(ArrayList discomforts)
	{
		this.discomforts=discomforts;
	}
	
    double getComfort()
	{
		return comfort;
	}
    
    double getPeerScore()
    {
    	return peerScore;
    }
    double getStrat()
	{
		return strat;
	}
    
    
    void setLeader()
    {
    	leader=1;
    }

    Boolean isLeader()
    {
    	if(leader==1)
    		return true;
    	else
    		return false;
    }
}
