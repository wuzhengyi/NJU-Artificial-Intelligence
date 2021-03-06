package controllers.Astar;
import core.game.StateObservation;
import ontology.Types;
import java.util.ArrayList;

public class Node implements Comparable<Node>{
    private double hValue;
    private int fValue;
    private double sum;
    public StateObservation stateObs;
    public ArrayList<Types.ACTIONS> actions = new ArrayList<>();;

    public Node(StateObservation stateObs, double heuristic, ArrayList<Types.ACTIONS> actions){
        this.hValue = heuristic;
        this.stateObs = stateObs.copy();
        this.actions.clear();
        this.actions.addAll(actions);
        fValue = actions.size() * 20;
        sum = fValue + hValue;
    }
    public double getfValue(){return fValue;}
    public double gethValue(){return hValue;}
    public ArrayList<Types.ACTIONS> getActions(){
        ArrayList<Types.ACTIONS> tmp = new ArrayList<> ();
        tmp.clear();
        tmp.addAll(actions);
        return tmp;
    }

    public int compareTo(Node n){
        if(this.sum > n.sum)
            return 1;
        else if(this.sum  < n.sum)
            return -1;
        return 0;
    }
}
