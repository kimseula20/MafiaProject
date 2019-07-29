import java.util.ArrayList;

public class MafiaVoteThread extends Thread {
    Utility ut = new Utility();

    MafiaVoteThread() {}
    
    public void run() {      
        ut.sendToAll("\n투표시간에는 전체 채팅이 불가합니다.");
        ut.sendToAll("마피아 일 것 같은 시민을 투표해 주세요 (20초)");
        ut.sendToAll("당신의 선택은?");
        ut.sendToAll("/voteOn"); // 모든 플레이어가 마피아 투표를 진행한다.
        if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] 모든 클라이언트에 /voteOn 명령어 전송");
        
        /* 현재 생존자 리스트 출력 */
        ArrayList<String> liveList = ut.liveAryList();
        for (int i = 0; i < liveList.size(); i++) {
            ut.sendToAll((i+1) + ". " + liveList.get(i));    
        }

        // 20초의 타이머 시작 (15초 경과후 "5초남았습니다...." 메시지 출력)
        MafiaTimeClient mvtc = new MafiaTimeClient(15, 5, 1);
        mvtc.start();
        if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] MafiaTimeClient 생성 성공!");
        
        // mvtc 쓰레드가 종료되었는지 확인
        while (true) {
            if (!mvtc.isAlive()) {
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] MafiaTimeClient 종료 완료!");
                break;
            }
        }

        // 투표 결과 리턴 (최다 득표자 index 번호)
        MafiaVoteResultThread mvrs = new MafiaVoteResultThread();
        int killPlayerNum = mvrs.sendVoteResult();
        if (MafiaGameSet.debugMode == true) {
            System.out.println("[MafiaVoteThread] MafiaVoteResultThread 생성 완료!");
            System.out.println("[MafiaVoteThread] sendVoteResult의 리턴 값 : " + killPlayerNum);
        }
        
        if (killPlayerNum == -1) { // 최다 득표자가 2명 이상일 경우
            ut.sendToAll("최다 득표자가 존재하지 않습니다.");
            ut.storyTempo();
            ut.sendToAll("이번 낮에는 아무도 처형당하지 않았습니다.");
            ut.storyTempo();
        } else { // 아닐 경우
            ArrayList<String> userListAry = ut.liveAryList();
            String killPlayer = userListAry.get(killPlayerNum);
            
            ut.sendToAll(killPlayer + "가 최다 득표자가 되었습니다.");
            ut.storyTempo();
            ut.sendToAll(killPlayer + "를 처형합니다.");
            ut.storyTempo();
            
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] liveHashMap 삭제 전 크기: " + MafiaGameSet.liveHashMap.size());
            MafiaGameSet.liveHashMap.remove(killPlayer); // 생존자 리스트에서 제거
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] liveHashMap 삭제 후 크기: " + MafiaGameSet.liveHashMap.size());
            
            ut.sendToClient(killPlayer, "============================================");
            ut.sendToClient(killPlayer, "투표에 의해 처형당하셨습니다. T.T 저런");
            ut.sendToClient(killPlayer, "억울하지만 어쩔수없네요.. 무엇때문에 선택당했는지 잘 생각해보세요");
            ut.sendToClient(killPlayer, "당신은 채팅은 할 수 없고, 관전만 가능합니다.");
            ut.sendToClient(killPlayer, "/dead"); // /dead 메시지를 받은 유저는 게임에서 제외된다.
            ut.sendToClient(killPlayer, "============================================");
        }
        
        // 게임 승리조건 비교
        boolean isEnd = ut.checkEndGame();
        
        if (isEnd) {
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] 게임종료조건 달성, 게임종료");
        } else {
            // 게임이 끝나지 않았을 경우, 마피아 전용 투표 쓰레드 시작
            new MafiaNightThread().start();
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] 게임 종료조건 미달성, MafiaNightThread 생성 완료!");
        }
    }
}
