
public class MafiaDiscussionThread extends Thread {
    Utility ut = new Utility();
    
    MafiaDiscussionThread() {}
    MafiaDiscussionThread(int cnt) {
        MafiaGameSet.today += cnt; 
    }
    
    public void run() {
        ut.sendToAll("지금부터 채팅이 가능합니다!");
        ut.sendToAll("/chatOn"); // 모든 플레이어가 채팅이 가능해짐
        if (MafiaGameSet.debugMode == true) System.out.println("[MafiaDiscussionThread] 모든 클라이언트에 /chatOn 명령어 전송");
        
        String mc = MafiaGameSet.today + "일차 낮 시민들은 모두 나와 토론을 진행해 주세요. (1분)";
        System.out.println("[MafiaJobSelectThread] liveHashMap.size() : " + MafiaGameSet.liveHashMap.size());
        ut.sendToAll(mc);
        
        // 60초의 타이머 시작 (45초 경과후 "15초남았습니다...." 메시지 출력)
        MafiaTimeClient mdtc = new MafiaTimeClient(45, 15, 0);
        mdtc.start();
        if (MafiaGameSet.debugMode == true) System.out.println("[MafiaDiscussionThread] MafiaTimeClient 생성 성공!");
        
        // mdtc 쓰레드가 종료되었는지 확인
        while (true) {
            if (!mdtc.isAlive()) {
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaDiscussionThread] MafiaTimeClient 종료 완료!");
                break;
            }
        }
        
        if (MafiaGameSet.gamePlay) {
            // 투표 쓰레드 시작
            new MafiaVoteThread().start();
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaDiscussionThread] MafiaVoteThread 생성 완료!");
        } else {
            ut.sendToAll("게임 플레이 도중 플레이어 한명이 탈주했습니다.\n무승부로 게임을 종료합니다.");
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaDiscussionThread] 플레이어가 게임을 나감.");
        }
    }
}
