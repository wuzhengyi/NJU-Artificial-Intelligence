package controllers.depthfirst;

import java.awt.Graphics2D;
import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: Zhengyi Wu
 * Date: 2018.3.14
 * Time: 20:14
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    protected ArrayList<StateObservation> hasStateObs = null;

    /**
    * Whether the results have been calculated.
    */
    protected boolean isCalculated;

    /**
    * The answer of DepthFisrt actions.
    */
    protected ArrayList<Types.ACTIONS> depthFirstAction = null;

    /**
    *  The number of now Step
    */
    protected int nowStep;

    /**
     * Observation grid.
     */
    protected ArrayList<Observation> grid[][];

    /**
     * block size
     */
    protected int block_size;

    protected boolean isInOldStateObs(StateObservation obs){
        for(StateObservation tmp:hasStateObs){
            if(tmp.equalPosition(obs)){
                System.out.println("Old Action");
                return true;
            }
        }
        return false;
    }


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        isCalculated = false;
        nowStep = -1;
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        hasStateObs = new ArrayList<>();
    }

    /**
    * Recursive computing depth first path.
    */
    boolean getDepthFirst(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
        Types.ACTIONS action = null; // 动作
        StateObservation stCopy = stateObs.copy(); //局面
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        for(Types.ACTIONS tmp:actions){
            action = tmp;
            stCopy.advance(action);
            if(/*stCopy.isGameOver()*/stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS) {
                System.out.println("Found it!");
                isCalculated = true;
                depthFirstAction.add(action);
                nowStep = depthFirstAction.size()-1;
                return true;
            }
            else if(isInOldStateObs(stCopy)){
                stCopy = stateObs.copy();
                continue;
            }
            else{
                hasStateObs.add(stCopy);
                if(getDepthFirst(stCopy,elapsedTimer)){
                    isCalculated = true;
                    depthFirstAction.add(action);
                    nowStep = depthFirstAction.size()-1;
                    return true;
                }
                else {
                    stCopy = stateObs.copy();
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        // return action directly if answer has been calculated.
        grid = stateObs.getObservationGrid();
        if(isCalculated && nowStep > -1)
                return depthFirstAction.get(nowStep--);
        if(getDepthFirst(stateObs,elapsedTimer))
            return depthFirstAction.get(nowStep--);
        else {
            if(isCalculated)
                System.out.print("ERROR: NO ACTIONS, STATE IS Calculated\n");
            else
                System.out.print("ERROR: NO ACTIONS, STATE IS NOT Calculated\n");
            return Types.ACTIONS.ACTION_NIL;
        }
    }


}
