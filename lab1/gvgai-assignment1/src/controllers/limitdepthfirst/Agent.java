package controllers.limitdepthfirst;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
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
    protected ArrayList<Integer> stateDepth = new ArrayList<>();
    protected final int MAX_DEPTH  = 6;
    /**
    * The answer of DepthFisrt actions.
    */
    protected ArrayList<Types.ACTIONS> limitDepthFirstAction = new ArrayList<>();

    /**
    *  The number of now Step
    */
    protected double dist = Double.POSITIVE_INFINITY;
    protected ArrayList<Types.ACTIONS> bestAction = new ArrayList<>();
    Vector2d goalpos = null; //目标位置
    Vector2d keypos = null; //钥匙的位置
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
    protected int isInOldStateObs(StateObservation obs){
        int i=0;
        for(StateObservation tmp:hasStateObs){
            if(tmp.equalPosition(obs)){
                return i;
            }
            i++;
        }
        return -1;
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

        Vector2d avatarpos = stateObs.getAvatarPosition();
        if(goalpos == null || keypos == null){
            ArrayList[] fixedPositions = stateObs.getImmovablePositions();
            ArrayList[] movingPositions = stateObs.getMovablePositions();
            goalpos = ((Observation)(fixedPositions[1].get(0))).position; //目标位置
            keypos = ((Observation)(movingPositions[0].get(0))).position; //钥匙的位置
        }

//        System.out.println(stateObs.getAvatarType()); // 没拿到钥匙是1
//        debugPos(avatarpos,"精灵");
//        debugPos(goalpos,"门");
//        debugPos(keypos,"钥匙");
        if(avatarGetKey(stateObs)){
//            System.out.println("Goal - Avatar : " + getDistance(goalpos,avatarpos));
            return getDistance(goalpos,avatarpos);
        }
        else{
//            System.out.println("Key - Avatar : " + getDistance(keypos,avatarpos));
            return getDistance(keypos,avatarpos) + getDistance(goalpos,keypos);
        }
    }

    public void debugPos(Vector2d vec, String head){
        System.out.println(head + vec.toString());
    }

    protected void initAgent(){
        dist = Double.POSITIVE_INFINITY;
        // grid = so.getObservationGrid();
        hasStateObs.clear();
        stateDepth.clear();
        bestAction.clear();
        limitDepthFirstAction.clear();
    }
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        initAgent();
        block_size = so.getBlockSize();
    }

    /**
    * Recursive computing depth first path.
    */
    boolean getLimitDepthFirst(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 5;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

//            if(limitDepthFirst(stateObs,elapsedTimer,numIters))
//                return true;

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
        return false;
    }

    protected void limitDepthFirst(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int depth){
//        System.out.println("depth: "+ depth);
//        System.out.println("score: " + dist);
        if(depth-- <= 0){
            double temp = getStateObsScore(stateObs);

            if(temp < dist){
                dist = temp - (depth+1);
                bestAction.clear();
                bestAction.addAll(limitDepthFirstAction);
                System.out.println("best score: "+ dist);
                debugPrintAllAction(bestAction);
            }
            return;
        }
        else if(depth != MAX_DEPTH){
            if(isInOldStateObs(stateObs)!=-1 && depth == stateDepth.get(isInOldStateObs(stateObs))){
                return;
            }
            else{
                hasStateObs.add(stateObs);
                stateDepth.add(depth);
            }
        }
        else{
            hasStateObs.clear();
            stateDepth.clear();
        }


        Types.ACTIONS action = null; // 动作
        StateObservation stCopy = stateObs.copy(); //局面
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        Collections.shuffle(actions);
        for(Types.ACTIONS tmp:actions){
            action = tmp;
            stCopy.advance(action);
            if(stCopy.equalPosition(stateObs))
                continue;
//            debugPrint(action);
//            System.out.println("try it");
            limitDepthFirstAction.add(action);
            if(stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS) {
                System.out.println("Found it! Score: " + (-depth-1) + " now is "+dist);

                if(- (depth+1) < dist){
                    dist =  - (depth+1);
                    bestAction.clear();
                    bestAction.addAll(limitDepthFirstAction);
                    debugPrintAllAction(limitDepthFirstAction);
                }
                stCopy = stateObs.copy();
                limitDepthFirstAction.remove(limitDepthFirstAction.size()-1);
                continue;
            }
/*            else if(isInCloseList(stCopy) || stCopy.isGameOver()){
                System.out.println("can't do it");
                stCopy = stateObs.copy();
                limitDepthFirstAction.remove(limitDepthFirstAction.size()-1);
                continue;
            }*/
            else{
//                System.out.println("next in depth " + depth);
                limitDepthFirst(stCopy,elapsedTimer,depth);
                stCopy = stateObs.copy();
                limitDepthFirstAction.remove(limitDepthFirstAction.size()-1);
                continue;

            }
        }
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
//        if(isCalculated && nowStep > -1)
//            return limitDepthFirstAction.get(nowStep++);
        initAgent();
        limitDepthFirst(stateObs,elapsedTimer,MAX_DEPTH);
//        debugPrintAllAction(bestAction);
        System.out.print("Size: " + bestAction.size() + "   ACTION: ");
        debugPrint(bestAction.get(0));
        System.out.println("END\n____________________________");
        return bestAction.get(0);
//        if(limitDepthFirst(stateObs,elapsedTimer,3))
//            return bestAction.get(0);
//        else {
//            System.out.println("no");
////            if(isCalculated)
////                System.out.print("ERROR: NO ACTIONS, STATE IS Calculated\n");
////            else{
////                if(limitDepthFirstAction.size()==0)
////                    System.out.print("ERROR: NO ACTIONS, STATE IS NOT Calculated\n");
////            }
//            return Types.ACTIONS.ACTION_NIL;
//        }

    }

    public void debugPrint(Types.ACTIONS act){
        controllers.Astar.Agent.debugActionPrint(act);
    }

    protected void debugPrintAllAction(ArrayList<Types.ACTIONS> actions){
        System.out.println("now action num: " + actions.size());
        for(Types.ACTIONS tmp:actions)
            debugPrint(tmp);
        System.out.println("END");

    }
}
