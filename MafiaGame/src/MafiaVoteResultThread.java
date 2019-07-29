import java.util.Arrays;

public class MafiaVoteResultThread extends Thread {
    Utility ut = new Utility();
    int voteNum;    
    
    MafiaVoteResultThread(int voteNum) {
        this.voteNum = voteNum;
    }
    
    MafiaVoteResultThread() {}
    
    public int sendVoteResult() {
        MafiaGameSet.voteCnt++; // 투표 한 사람 수
        int selIdx = 0; // 선택 된 사람 번호
        int[] tempArr = Arrays.copyOf(MafiaGameSet.voteArr, MafiaGameSet.voteArr.length); // 투표 번호
        Arrays.sort(tempArr); // 오름차순 정렬
        
        int max = tempArr[tempArr.length-1]; // 최다 득표 수
        int maxCnt = 0; // 최다 득표자 수
        for (int i = 0; i < MafiaGameSet.voteArr.length; i++) {
            if(MafiaGameSet.voteArr[i] == max) {
                System.out.println("최다 득표자: " + (i+1) + "번 플레이어");
                selIdx = i;
                maxCnt++;
            }
        }

        if (maxCnt >= 2) // 최다 득표자가 2명 이상일 경우 무효 표
            return -1;
        else
            return selIdx;
    }
    
    public void run() {
        MafiaGameSet.voteArr[voteNum-1] += 1;
        System.out.println("[MafiaVoteResult] 현재 투표 현황 : " + Arrays.toString(MafiaGameSet.voteArr));
    }
}
