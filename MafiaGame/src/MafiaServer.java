import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;

// 클라이언트와 통신하는 서버
public class MafiaServer {
    Socket socket;
    
    MafiaServer() {
        // 현재 서버에 접속한 플레이어 정보는 어떠한 환경에서도 정확해야 한다.
        // HashMap은 동기화를 지원하지 않기때문에 synchronizedMap을 이용해 동기화 한다.
        Collections.synchronizedMap(MafiaGameSet.clients);
    }

    public void start() {
        ServerSocket serverSocket = null;
        socket = null;

        try {
            serverSocket = new ServerSocket(7777);
            if (MafiaGameSet.debugMode == true) System.out.println("[MafiaServer] 서버가 시작되었습니다.");
            while (true) {
                socket = serverSocket.accept(); // 서버와 클라이언트가 연결이 되면 값을 반환
                if (MafiaGameSet.debugMode == true) System.out.println("[MafiaServer] [" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하였습니다");
                ServerReceiver thread = new ServerReceiver(socket);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {        
        new MafiaServer().start();
    }

    class ServerReceiver extends Thread {
        Socket socket;
        DataInputStream in;
        DataOutputStream out;

        ServerReceiver(Socket socket) {
            this.socket = socket;
            try { // 데이터 입출력 스트림 생성
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {}
        }

        @Override
        public void run() {
            String name = "";
            Utility ut = new Utility();

            try {
                name = in.readUTF();
                if (MafiaGameSet.clients.size()+1 == MafiaGameSet.MAX_PERSON) { // 인원 충족 시 게임을 시작한다
                    MafiaGameSet.clients.put(name, out);
                    ut.sendToAll(name + "플레이어가 참여했습니다.");
                    ut.sendToAll("현재 참가인원 (" + MafiaGameSet.clients.size() + "/" + MafiaGameSet.MAX_PERSON + ")");
                    
                    ut.storyTempo();
                    ut.sendToAll("\n게임 시작 조건이 충족되었습니다.");
                    MafiaGameSet.gamePlay = true;
                    
                    if (MafiaGameSet.debugMode == true) System.out.println("[MafiaServer] 현재 서버 접속자 수는 " + MafiaGameSet.clients.size() + "입니다.");
                    
                    if (MafiaGameSet.gamePlay) {
                        // 직업 선정 및 시나리오 페이지로 이동
                        new MafiaJobSelectThread().start();   
                    } else {
                        ut.sendToAll("게임 플레이 도중 플레이어 한명이 탈주했습니다.\n무승부로 게임을 종료합니다.");
                    }
                } else { // 인원이 부족 한 경우
                    MafiaGameSet.clients.put(name, out);
                    ut.sendToAll(name + "플레이어가 참여했습니다.");
                    ut.sendToAll("현재 참가인원 (" + MafiaGameSet.clients.size() + "/" + MafiaGameSet.MAX_PERSON + ") 현재 대기중....");
                    
                    if (MafiaGameSet.debugMode == true) System.out.println("[MafiaServer] 현재 서버 접속자 수는 " + MafiaGameSet.clients.size() + "입니다.");
                }

                while (in != null) {
                    String str = in.readUTF();
                    // 클라이언트한테 /vote 1과 같은 형태로 데이터가 온다.
                    if (str.indexOf("/vote") >= 0) {
                        int voteNum = Integer.parseInt(str.substring(str.indexOf(" ")+1));
                        new MafiaVoteResultThread(voteNum).start();
                    } else if (str.indexOf("/pick") >= 0) {
                        int pickNum = Integer.parseInt(str.substring(str.indexOf(" ")+1));
                        new MafiaSelectResultThread(pickNum).start();
                    } else {
                        // 클라이언트로부터 명령어가 오지 않는 경우
                        ut.sendToAll(str);
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
            } finally { // 플레이어가 퇴장 or 오류가 발생한 경우
                ut.sendToAll(name + "플레이어가 나갔습니다.");
                MafiaGameSet.clients.remove(name);
                
                if (MafiaGameSet.clients.size()+1 == MafiaGameSet.MAX_PERSON) MafiaGameSet.gamePlay = false;
                
                ut.sendToAll("현재 참가인원 (" + MafiaGameSet.clients.size() + "/" + MafiaGameSet.MAX_PERSON +") 현재 대기중....");
                if (MafiaGameSet.debugMode == true) {
                    System.out.println("[MafiaServer] [" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하였습니다.");
                    System.out.println("[MafiaServer] 현재 서버접속자 수는 " + MafiaGameSet.clients.size() + "입니다.");
                }
            }
        }
    }
}