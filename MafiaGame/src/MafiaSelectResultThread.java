
public class MafiaSelectResultThread extends Thread{    
    MafiaSelectResultThread(int pickNum){
        MafiaGameSet.pickNum = pickNum;
    }
    
    MafiaSelectResultThread(){}
    
    public void run() {}
    
    public int idxResult() {
        if (MafiaGameSet.liveHashMap.size() < MafiaGameSet.pickNum) {
            return -1;
        } else {
            return MafiaGameSet.pickNum-1;   
        }
    }
}
