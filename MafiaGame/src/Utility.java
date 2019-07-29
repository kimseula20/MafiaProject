import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Utility {
    ArrayList<String> userList() {
        ArrayList<String> userAry = new ArrayList<String>(); // 플레이어 아이디를 List애 담기위해 ArrayList 객체 생성
        Iterator<String> it = MafiaGameSet.clients.keySet().iterator();

        while (it.hasNext()) {
            userAry.add(it.next()); // 리스트에 담는다.
        }

        return userAry;
    }
    
    ArrayList<String> liveAryList() {
        ArrayList<String> liveAry = new ArrayList<String>(); // 플레이어 아이디를 List애 담기위해 ArrayList 객체 생성
        Iterator<String> it = MafiaGameSet.liveHashMap.keySet().iterator();

        while (it.hasNext()) {
            liveAry.add(it.next()); // 리스트에 담는다.
        }

        return liveAry;
    }
    
    void storyTempo() {
        try {
            Thread.sleep(2000);   
        } catch(Exception e) {}
    }
    
    void storyTempo(int speed) {
        try {
            Thread.sleep(speed);   
        } catch(Exception e) {}
    }

    void sendToAll(String msg) {
        Iterator<String> it = MafiaGameSet.clients.keySet().iterator();

        while (it.hasNext()) {
            try {
                DataOutputStream out = (DataOutputStream) MafiaGameSet.clients.get(it.next());
                out.writeUTF(msg);
                out.flush();
            } catch (Exception e) {}
        }
    }

    void sendToClient(String key, String msg) {
        try {
            DataOutputStream out = (DataOutputStream) MafiaGameSet.clients.get(key);
            out.writeUTF(msg);
        } catch (Exception e) {}
    }
    
    public boolean checkEndGame() {
        boolean isEnd = false;
        
        //처형 뒤 생존자 리스트
        ArrayList<String> liveList = liveAryList();
        String[] jobSplit = null;
        String[] temp = new String[liveList.size()];
        int citizenCnt = 0;
        int mafiaCnt = 0;
        
        for (int i = 0; i < liveList.size(); i++) {
            jobSplit = MafiaGameSet.liveHashMap.get(liveList.get(i)).toString().split("@");
            temp[i] = jobSplit[0]; //살아남은 사람의 직업들을 temp라는 String배열에 집어넣음
        }
        
        for (int i = 0; i < temp.length; i++) {
            if(temp[i].equals("Citizen"))
                citizenCnt++;
            else if(temp[i].equals("Police"))
                citizenCnt++;
            else if(temp[i].equals("Mafia"))
                mafiaCnt++;
        }

        if(mafiaCnt == 0) { // 마피아가 다 죽었으면
            isEnd = true; //시민 WIN 종료
            sendToAll("게임종료. 시민팀 승리!");
            
            String tempArr[] = new String[userList().size()];
            sendToAll("[각 플레이어의 직업]");
            for (int i = 0; i < userList().size(); i++) {
                tempArr = MafiaGameSet.jobs.get(userList().get(i)).toString().split("@");
                sendToAll((i+1) + ". " + userList().get(i) + " : " + tempArr[0]);
            }
            
        } else if (mafiaCnt >= citizenCnt) { //마피아 숫자가 시민과 같아지면
            isEnd = true; //마피아 WIN 종료
            sendToAll("게임종료. 마피아팀 승리!");
            
            String tempArr[] = new String[userList().size()];
            sendToAll("[각 플레이어의 직업]");
            for (int i = 0; i < userList().size(); i++) {
                tempArr = MafiaGameSet.jobs.get(userList().get(i)).toString().split("@");
                sendToAll((i+1) + ". " + userList().get(i) + " : " + tempArr[0]);
            }
        } else {
            isEnd = false; //아직 승부가 안남
        }
        
        return isEnd;
    }
}
