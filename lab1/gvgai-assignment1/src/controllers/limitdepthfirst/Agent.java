package controllers.limitdepthfirst;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Vector;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

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
                return true;
            }
        }
        return false;
    }

    /** 
     * get the distance between 2 things
    */    
    protected double getDistance(Vector2d vec1, Vector2d vec2) {
        return Math.abs(vec1.x - vec2.x) + Math.abs(vec1.y - vec2.y);
    }

    public boolean avatarGetKey(StateObservation stateObs){
        return stateObs.getAvatarType() != 1;
    }

    /**
     *  get now State Observation score 
     */
    protected double getStateObsScore(StateObservation stateObs) {
        ArrayList[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList[] movingPositions = stateObs.getMovablePositions();
        Vector2d avatarpos = stateObs.getAvatarPosition();
        Vector2d goalpos = ((Observation)(fixedPositions[1].get(0))).position; //目标位置
        Vector2d keypos = ((Observation)(movingPositions[0].get(0))).position; //钥匙的位置
//        System.out.println(stateObs.getAvatarType()); // 没拿到钥匙是1
//        debugPos(avatarpos,"精灵");
//        debugPos(goalpos,"门");
//        debugPos(keypos,"钥匙");
        if(avatarGetKey(stateObs)){
            return getDistance(goalpos,avatarpos);
        }
        else{
            return getDistance(keypos,avatarpos);
        }
    }

    public void debugPos(Vector2d vec, String head){
        System.out.println(head + vec.toString());
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
    boolean getLimitDepthFirst(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
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
        getStateObsScore(stateObs);
        return Types.ACTIONS.ACTION_DOWN;
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
