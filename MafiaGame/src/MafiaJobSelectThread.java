import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

// 직업 선정 쓰레드
public class MafiaJobSelectThread extends Thread {
    Utility ut = new Utility();
    Random rd = new Random();
    
    MafiaJobSelectThread() {}
    
    /**
     * 직업 지정 메서드
     * @param userAry : 유저목록
     * @param jobName : 직업명
     * @return 직업이 선정되지 않은 유저 목록
     */
    ArrayList<String> jobSelected(ArrayList<String> userAry, String jobName) {
        String strMsg = "";
        String strMsg2 = "";
        int rdIdx = rd.nextInt(userAry.size());
        Job job = null;
        
        if (jobName.equals("Mafia")) {
            job = new Mafia();
            strMsg = "마피아";
            strMsg2 ="당신은 밤에 전부터 마음에 안들었던 시민한명을 죽일 수 있습니다\r\n" + 
                    "낮에 투표로 죽을수도있으니 최대한 정치 당하지 않도록 잘 살아보세요^^*";
        } else if (jobName.equals("Police")) {
            job = new Police();
            strMsg = "경찰";
            strMsg2 = "당신은 아무런 능력이없습니다 이름만 경찰이죠 "+ 
                    "하지만 낮에 당신의 투표로 마피아를 죽일 수 있습니다\r\n" + 
                    "투표는 꼭 합시다 ^^*  ";
        } else if (jobName.equals("Citizen")) {
            job = new Citizen();
            strMsg = "시민";
            strMsg2 = "아무런 능력이없어 슬프죠 T.T\r\n" + 
                    "하지만 낮에 당신의 투표로 마피아를 죽일 수 있습니다\r\n" + 
                    "투표는 꼭 합시다 ^^*\r\n";
        }
        
        for (int i = 0; i < userAry.size(); i++) {
            if (!jobName.equals("Citizen")) {
                MafiaGameSet.jobs.put(userAry.get(rdIdx), job);
                ut.sendToClient(userAry.get(rdIdx), "\n당신은 <<" + strMsg + ">> 입니다.");
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaJobSelectThread] " + userAry.get(rdIdx) + "플레이어가 " + strMsg + "로 지정 됨.");
                ut.sendToClient(userAry.get(rdIdx), strMsg2);
                break;
            } else {
                MafiaGameSet.jobs.put(userAry.get(i), job);
                ut.sendToClient(userAry.get(i), "\n당신은 <<" + strMsg + ">> 입니다.");
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaJobSelectThread] " + userAry.get(i) + "플레이어가 " + strMsg + "로 지정 됨.");
                ut.sendToClient(userAry.get(i), strMsg2);
            }
        }
        
        userAry.remove(rdIdx);
        ut.sendToAll(strMsg + " 지정 완료 !");
        
        return userAry;
    }
    
    public void run() {
        ut.sendToAll("지금부터 잠시 채팅을 할 수 없습니다!");
        ut.sendToAll("/chatOff"); // 모든 플레이어가 채팅을 칠 수 없게 됨
        if (MafiaGameSet.debugMode == true) System.out.println("[MafiaJobSelectThread] 모든 클라이언트에 /chatOff 명령어 전송");
        
        try {
            ut.storyTempo();
            ut.sendToAll("각 플레이어에게 직업을 할당 중 입니다\n");
            ut.storyTempo();
            ut.sendToAll("두근두근두근\n");
            ArrayList<String> userAryList = ut.userList();

            ut.storyTempo();
            // 마피아 선정
            userAryList = jobSelected(userAryList, "Mafia");
            ut.storyTempo();
            // 경찰 선정
            userAryList = jobSelected(userAryList, "Police");
            ut.storyTempo();
            // 남은 인원 모두 시민으로
            userAryList = jobSelected(userAryList, "Citizen");
            userAryList.clear(); // 직업선정 배열 클리어
            ut.storyTempo();
            
            // 현재 생존자 HashMap<String, Job>형태로 보관
            MafiaGameSet.liveHashMap = (HashMap<String, Job>) MafiaGameSet.jobs.clone();
            
            /* 직업선정 기능 종료 */
            ut.sendToAll("직업선정 완료 ");

            ut.sendToAll("\n");
            ut.sendToAll("모든 플레이어의 직업 선정이 완료되었습니다.");
            ut.storyTempo();
            ut.sendToAll("게임을 시작합니다.\n\n");

            ut.storyTempo(2000);
            ut.sendToAll("평화로운 비트 마을.....\n\n");

            ut.storyTempo();
            ut.sendToAll("어느날 살인 사건이 발생했다.");
            ut.storyTempo();
            ut.sendToAll("사람들은 아직 이 마을에 범인이 있을거라고 확신하였고");
            ut.storyTempo();
            ut.sendToAll("낮에 투표를 통해 한 명씩 사람들을 처형시키기로 한다.....\n");
            ut.storyTempo();
            
            System.out.println("[MafiaJobSelectThread] liveHashMap.size() : " + MafiaGameSet.liveHashMap.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (MafiaGameSet.gamePlay) {
            // 토론 서버 쓰레드 시작
            new MafiaDiscussionThread().start();
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaJobSelectThread] 토론쓰레드 정상 생성");
        } else {
            ut.sendToAll("게임 플레이 도중 플레이어 한명이 탈주했습니다.\n무승부로 게임을 종료합니다.");
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaJobSelectThread] 플레이어가 게임을 나감.");
        }
    }
}
