package controllers.depthfirst;

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

    protected ArrayList<StateObservation> closeList = new ArrayList<>();

    /**
    * Whether the results have been calculated.
    */
    protected boolean isCalculated;

    /**
    * The answer of DepthFisrt actions.
    */
    protected ArrayList<Types.ACTIONS> depthFirstAction = new ArrayList<>();

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

    protected boolean isInCloseList(StateObservation obs){
        for(StateObservation tmp: closeList){
            if(tmp.equalPosition(obs)){
//                System.out.println("Old Action");
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
        // grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        closeList.clear();
        depthFirstAction.clear();
    }

    /**
    * Recursive computing depth first path.
    */
    boolean getDepthFirst(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
//        debugPrintAllAction(depthFirstAction);
        if(isInCloseList(stateObs)){
            return false;
        }
        else{
            closeList.add(stateObs);
        }
        Types.ACTIONS action = null; // 动作
        StateObservation stCopy = stateObs.copy(); //局面
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        for(Types.ACTIONS tmp:actions){
            action = tmp;
            stCopy.advance(action);
            depthFirstAction.add(action);
            if(stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS) {
                System.out.println("Found it!");
                debugPrintAllAction(depthFirstAction);
                nowStep = 0;
                isCalculated = true;
                return true;
            }
            else if(isInCloseList(stCopy) || stCopy.isGameOver()){
                stCopy = stateObs.copy();
                depthFirstAction.remove(depthFirstAction.size()-1);
                continue;
            }
            else{
                if(getDepthFirst(stCopy,elapsedTimer)){
                    return true;
                }
                else {
                    stCopy = stateObs.copy();
                    depthFirstAction.remove(depthFirstAction.size()-1);
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
        // grid = stateObs.getObservationGrid();
        if(isCalculated && nowStep > -1)
                return depthFirstAction.get(nowStep++);
        if(getDepthFirst(stateObs,elapsedTimer))
            return depthFirstAction.get(nowStep++);
        else {
            if(isCalculated)
                System.out.print("ERROR: NO ACTIONS, STATE IS Calculated\n");
            else{
                if(depthFirstAction.size()==0)
                    System.out.print("ERROR: NO ACTIONS, STATE IS NOT Calculated\n");
            }
            return Types.ACTIONS.ACTION_NIL;
        }
    }

    public void debugPrint(Types.ACTIONS act){
        controllers.Astar.Agent.debugActionPrint(act);
    }

    protected void debugPrintAllAction(ArrayList<Types.ACTIONS> actions){
        System.out.println("now action num: " + depthFirstAction.size());
        for(Types.ACTIONS tmp:actions)
            debugPrint(tmp);
        System.out.println("END");

    }
}
