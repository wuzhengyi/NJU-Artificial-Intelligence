package controllers.Astar;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.PriorityQueue;


/**
 * Created with IntelliJ IDEA.
 * User: Zhengyi Wu
 * Date: 2018.3.18
 * Time: 20:00
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */

public class Agent extends AbstractPlayer {
    protected ArrayList<StateObservation> closeList = new ArrayList<>();
    protected ArrayList<Types.ACTIONS> aStarAction = new ArrayList<>();
    protected PriorityQueue<Node> openList = new PriorityQueue<>();
    Vector2d goalpos = null; //目标位置
    Vector2d keypos = null; //钥匙的位置
    protected ArrayList<Observation> grid[][];
    protected int block_size;
    protected boolean getAnswer;

    protected void initAgent(){
        openList.clear();
        closeList.clear();
        aStarAction.clear();
        getAnswer = false;
    }

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        initAgent();
        block_size = so.getBlockSize();
        aStar(so,elapsedTimer);
    }

    /**
     * Judge whether the State obs has occurred
     */
    protected int isInCloseList(StateObservation obs){
        int i=0;
        for(StateObservation tmp: closeList){
            if(tmp.equalPosition(obs)){
                return i;
            }
            i++;
        }
        return -1;
    }

    protected int isInOpenList(StateObservation obs){
        int i=0;
        for(Node tmp: openList){
            if(tmp.stateObs.equalPosition(obs)){
                return i;
            }
            i++;
        }
        return -1;
    }

    protected double getDistance(Vector2d vec1, Vector2d vec2) {
        return Math.abs(vec1.x - vec2.x) + Math.abs(vec1.y - vec2.y);
    }

    public boolean avatarGetKey(StateObservation stateObs){
        return stateObs.getAvatarType() != 1;
    }

    /**
     *  get now State Observation score
     */
    public double heuristic(StateObservation stateObs){
        int radio = -500;
        int cost = 1;
        Vector2d avatarpos = stateObs.getAvatarPosition();
        ArrayList[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList[] movingPositions = stateObs.getMovablePositions();
        if(goalpos == null)
            goalpos = ((Observation)(fixedPositions[1].get(0))).position; //目标位置
        if(keypos == null)
            keypos = ((Observation)(movingPositions[0].get(0))).position; //钥匙的位置

        if(movingPositions == null){
            //System.out.println("null");
            return radio*stateObs.getGameScore()+cost*getDistance(goalpos,avatarpos);
        }
        if(avatarGetKey(stateObs)){
            return radio*stateObs.getGameScore()+cost*getDistance(goalpos,avatarpos);
        }
        else{
            return radio*stateObs.getGameScore()+cost*getDistance(keypos,avatarpos) + cost*getDistance(goalpos,keypos);
        }
    }


    protected void aStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        initAgent();
        Node startNode = new Node(stateObs,heuristic(stateObs),aStarAction);
        openList.add(startNode);

        long remaining = elapsedTimer.remainingTimeMillis();
        int remainingLimit = 15;
        while(remaining > remainingLimit && !openList.isEmpty() /*&& myStep<30*/)
        {
//            System.out.println("_______________________________");
//            System.out.println("number of openlist:" + openList.size());
//            debugOpenList();
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            Node tmp = openList.poll();
            aStarAction.clear();
            aStarAction.addAll(tmp.actions);
//            debugPrintAllAction(aStarAction);
            ArrayList<Types.ACTIONS> actions = tmp.stateObs.getAvailableActions();
            closeList.add(tmp.stateObs.copy());
            for(Types.ACTIONS act:actions){
                StateObservation stCopy = tmp.stateObs.copy();
                stCopy.advance(act);
//                closeList.add(stCopy.copy());
                aStarAction.add(act);
                if(stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS) {
                    getAnswer = true;
                    return ;
                }
                else if(stCopy.isGameOver() || isInCloseList(stCopy) != -1) {
                    aStarAction.remove(aStarAction.size()-1);
                    continue;
                }

                else if(isInOpenList(stCopy) != -1){
                    for(Node old: openList){
                        if(old.stateObs.equalPosition(stCopy)){
                            if(old.actions.size()>aStarAction.size()){
                                openList.remove(old);
                                openList.add(new Node(stCopy,heuristic(stCopy),aStarAction));

                                break;
                            }

                        }
                    }
                    aStarAction.remove(aStarAction.size()-1);
                }
                else{
                    openList.add(new Node(stCopy,heuristic(stCopy),aStarAction));
                    aStarAction.remove(aStarAction.size()-1);
                }

            }

            remaining = elapsedTimer.remainingTimeMillis();
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
        if(getAnswer){
            Types.ACTIONS act = aStarAction.get(0);
            aStarAction.remove(0);
            return act;
        }

        aStar(stateObs,elapsedTimer);
//        System.out.println("number of openlist:" + openList.size());
        if(openList.isEmpty() || openList.peek().actions.isEmpty()) {
//            System.out.println("[Error] no action");
            return Types.ACTIONS.ACTION_NIL;
        }
//        System.out.println("_______________________________");
//        System.out.println("number of openlist:" + openList.size());
//        debugOpenList();
//        debugActionPrint(openList.peek().actions.get(0));
        return openList.peek().actions.get(0);
    }

    public static void debugActionPrint(Types.ACTIONS act) {
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

    private void debugOpenList(){
//        int i= 1;
        for(Node t:openList){
//            if(i++ > 5)
//                break;
            Vector2d pos = t.stateObs.getAvatarPosition();
            System.out.println(pos.toString() + " f: " + t.getfValue() + " h: " + t.gethValue());
        }
    }

    public static void debugPrintAllAction(ArrayList<Types.ACTIONS> actions){
        System.out.println("now action num: " + actions.size());
        for(Types.ACTIONS tmp:actions)
            debugActionPrint(tmp);
        System.out.println("END");

    }
}
