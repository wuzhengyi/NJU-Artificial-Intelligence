package controllers.limitdepthfirst;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Vector;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: Zhengyi Wu
 * Date: 2018.3.16
 * Time: 19:50
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    protected ArrayList<StateObservation> hasStateObs = new ArrayList<>();

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

    /** 
     * Judge whether the State obs has occurred
     */
    protected boolean isInOldStateObs(StateObservation obs){
        for(StateObservation tmp:hasStateObs){
            if(tmp.equalPosition(obs)){
//                System.out.println("Old Action");
                return true;
            }
        }
        return false;
    }

    /** 
     * get the distance between 2 things
    */    
    protected int getDistance(Vector2d x, Vector2d y) {
        
    }

    /**
     *  get now State Observation score 
     */
    protected int getStateObsScore(StateObservation so) {
        ArrayList[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList[] movingPositions = stateObs.getMovablePositions();
        Vector2d goalpos = fixedPositions[1].get(0).position; //目标位置
        Vector2d keypos = movingPositions[0].get(0).position; //钥匙的位置
        /** TODO: get avatar position , 
         * if(get key)
         *      return distance between door and avatar
         * else
         *      return distance between key and avatar
         */
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
        hasStateObs.clear();
        depthFirstAction.clear();
    }

    /**
    * Recursive computing depth first path.
    */
    boolean getDepthFirst(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
//        debugPrintAllAction(depthFirstAction);
        if(isInOldStateObs(stateObs)){
            return false;
        }
        else{
            hasStateObs.add(stateObs);
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
            else if(isInOldStateObs(stCopy) || stCopy.isGameOver()){
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
        switch (act){

            case ACTION_NIL:System.out.print("NIL->");
                break;
            case ACTION_UP:System.out.print("UP->");
                break;
            case ACTION_LEFT:System.out.print("LEFT->");
                break;
            case ACTION_DOWN:System.out.print("DOWN->");
                break;
            case ACTION_RIGHT:System.out.print("RIGHT->");
                break;
            case ACTION_USE:System.out.print("USE->");
                break;
            case ACTION_ESCAPE:System.out.print("ESCAPE->");
                break;
        }
    }

    protected void debugPrintAllAction(ArrayList<Types.ACTIONS> actions){
        System.out.println("now action num: " + depthFirstAction.size());
        for(Types.ACTIONS tmp:actions)
            debugPrint(tmp);
        System.out.println("END");

    }
}
