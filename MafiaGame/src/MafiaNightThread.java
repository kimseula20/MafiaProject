import java.util.ArrayList;

public class MafiaNightThread extends Thread {
    Utility ut = new Utility();
    boolean isMafia = false;
    
    MafiaNightThread() {}
    
    public void run() {
        ut.sendToAll("밤이 되었습니다.");
        
        ArrayList<String> liveList = ut.liveAryList();
        /* 마피아 이름 출력 로직 */
        String mafiaName = "";
        
        for (int i = 0; i < liveList.size(); i++) {
            String[] jobSplit = MafiaGameSet.liveHashMap.get(liveList.get(i)).toString().split("@");
            System.out.println(jobSplit[0]);
            if (jobSplit[0].equals("Mafia")) {
                isMafia = true;
                mafiaName = liveList.get(i);
                break;
            }
        }
        
        ut.sendToClient(mafiaName, "마피아 데스노트작성시간이 되었습니다(20초)\n"); //찾은 마피아에게 보냄
        ut.sendToClient(mafiaName, "전부터 마음에 들지않아서 죽이고싶은 시민 한명을 선택해주세요"); //찾은 마피아에게 보냄
        ut.sendToClient(mafiaName, "/select");  // /select 명령어를 받은 마피아는 지목을 시작
        if (MafiaGameSet.debugMode == true) System.out.println("[MafiaNightThread] " + mafiaName + "플레이어(마피아)에게만 /select 명령어 전송");
        
        /* 현재 생존자 리스트 출력 */
        for (int i = 0; i < liveList.size(); i++) {
            ut.sendToClient(mafiaName, (i+1) + ". " + liveList.get(i));
        }
        
        // 밤 타이머 시작 (15초 후 "5초 남았습니다..." 출력 (마피아에게만))
        MafiaTimeClient mntc = new MafiaTimeClient(15, 5, 2, mafiaName);
        mntc.start();
        if (MafiaGameSet.debugMode == true) System.out.println("[MafiaNightThread] MafiaTimeClient 생성 성공!");
        
        // 타이머 쓰레드가 끝날 때 까지 대기
        while (true) {
            if (!mntc.isAlive()) {
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaNightThread] MafiaTimeClient 종료 완료!");
                break;
            }
        }
        
        // 지목 결과 리턴 (지목당한 index 번호)
        MafiaSelectResultThread msrs = new MafiaSelectResultThread();
        int MafiaSelectKillNum = msrs.idxResult();
        ut.storyTempo();

        if (MafiaSelectKillNum <= -1) { // 아무도 지목하지 않았거나, 유효하지 않은 번호를 입력했을 경우 
            ut.sendToAll("오늘밤엔 아무일도 일어나지 않았습니다.");
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaNightThread] 마피아가 선택을 하지 않았음, 아무도 죽이지 않음");
        } else {
            ArrayList<String> liveListAry = ut.liveAryList();
            String MafiaSelectKill = liveListAry.get(MafiaSelectKillNum);
            
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaNightThread] 마피아가 " + MafiaSelectKill + "플레이어 지목");

            if (mafiaName.equals(MafiaSelectKill)) { // 마피아 자결 엔딩
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaNightThread] 마피아가 본인을 지목");
                MafiaGameSet.liveHashMap.remove(MafiaSelectKill);
                
                ut.sendToAll("밤 사이 " + MafiaSelectKill + "가 죄책감에 못이겨 자결했습니다.");
                
                ut.sendToClient(MafiaSelectKill, "당신은 스스로 죽었습니다.");
                ut.sendToClient(MafiaSelectKill, "채팅은 할 수 없고, 관전만 가능합니다.");
                ut.sendToClient(MafiaSelectKill, "/dead");
            } else {
                ut.sendToClient(mafiaName, MafiaSelectKill + "가 선택되었습니다.");
                ut.storyTempo();
                ut.sendToClient(mafiaName, MafiaSelectKill + "를 살해 합니다.");
                ut.storyTempo();

                MafiaGameSet.liveHashMap.remove(MafiaSelectKill);
                
                ut.sendToAll("밤 사이 " + MafiaSelectKill + "가 마피아의 습격을 받아 살해되었습니다.");
                
                ut.sendToClient(MafiaSelectKill, "============================================");
                ut.sendToClient(MafiaSelectKill, "저런.. 마피아의 선택을받아 당신은 죽었습니다");
                ut.sendToClient(MafiaSelectKill, "채팅은 할 수 없고, 관전만 가능합니다.");
                ut.sendToClient(MafiaSelectKill, "유령이되어 마피아를 저주해보세요.(그렇다고 마피아가죽는건아닙니다)");
                ut.sendToClient(MafiaSelectKill, "카톡으로 마피아가 누군지 알려주지마세요 -_-+");
                ut.sendToClient(MafiaSelectKill, "/dead");
                ut.sendToClient(MafiaSelectKill, "============================================");
            }
            
        }

        // 게임 종료조건 비교
        boolean isEnd = ut.checkEndGame();
        
        if (isEnd) {
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] 게임종료조건 달성, 게임종료");
        } else {
            ut.sendToAll("아침이 되었습니다.");
            /* static 데이터 초기화 */
            MafiaGameSet.voteCnt = 0; // 투표 초기화
            MafiaGameSet.voteArr = new int[MafiaGameSet.liveHashMap.size()];
            MafiaGameSet.pickNum = -1;
            if (MafiaGameSet.gamePlay) {
                new MafiaDiscussionThread(1).start(); // 다시 낮으로 이동한다
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaVoteThread] 게임종료조건 미달성, MafiaDiscussionThread 생성 성공!");
            } else {
                ut.sendToAll("게임 플레이 도중 플레이어 한명이 탈주했습니다.\n무승부로 게임을 종료합니다.");
            }
        }
    }
}
