import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

// 클라이언트
public class MafiaClient {
    static boolean isChat = true; // 채팅 가능여부
    static boolean isVote = false; // 투표 가능여부
    static boolean isLive = true; // 생존 여부
    static boolean isSelct = false; // 밤(마피아 전용) 투표 시간

    public static void main(String[] args) {
        // 클라이언트는 닉네임을 가지고 입장하여야 한다. (인자 1개 필수)
        if (args.length != 1) {
            System.out.println("USAGE : java 클라이언트 대화명");
            System.exit(0);
        }

        try {
            String serverIp = "203.236.209.164"; // 서버 아이피
            Socket socket = new Socket(serverIp, 7777); // 서버와 소켓 생성
            System.out.println("서버에 연결되었습니다.");

            Thread sender = new Thread(new ClientSender(socket, args[0])); // 서버로 보내는 통로 생성
            Thread receiver = new Thread(new ClientReceiver(socket)); // 서버에서 온 데이터를 받는 통로 생성

            // 쓰레드 시작
            sender.start();
            receiver.start(); 
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (Exception e) {}
    }

    // 클라이언트 -> 서버 통신
    static class ClientSender extends Thread {
        Socket socket;
        DataOutputStream out;
        String name;

        ClientSender(Socket socket, String name) {
            this.socket = socket;

            try {
                out = new DataOutputStream(socket.getOutputStream()); // 출력 스트림 생성
                this.name = name; // 닉네임 설정
            } catch (Exception e) {}
        }

        public void run() {
            Scanner scanner = new Scanner(System.in);

            try {
                if (out != null) {
                    out.writeUTF(name);
                }

                while (out != null) {
                    String strSend = scanner.nextLine();
                    if (isSelct) {
                        // 예외처리 (문자열이 들어왔을경우와 게임시작 인원보다 높은 수의 수를 입력 했을경우 예외 발생
                        while (true) {
                            try {
                                int tempNum = Integer.parseInt(strSend);
                                if (tempNum <= 0 || tempNum > MafiaGameSet.MAX_PERSON) {
                                    throw new NumberFormatException();
                                } else {
                                    out.writeUTF("/pick " + strSend);
                                    System.out.println(strSend + "번 투표하셨습니다.");
                                    out.flush();
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("잘못 된 값을 입력하셨습니다.");
                                System.out.println("다시 입력 해 주세요");
                                strSend = scanner.nextLine();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        isSelct = false;
                    } else {
                        if (isLive) {
                            if (isChat && !isVote) { // 토론 중
                                // 일반적인 채팅이 가능하다
                                out.writeUTF("[" + name + "] : " + strSend);
                                out.flush();
                            } else if (!isChat && isVote) { // 투표 중
                                while (true) {
                                    // 예외처리 (문자열이 들어왔을경우와 현재 생존인원보다 높은 수의 수를 입력 했을경우 예외 발생
                                    try {
                                        int tempNum = Integer.parseInt(strSend);
                                        if (tempNum <= 0 || tempNum > MafiaGameSet.voteArr.length) {
                                            throw new NumberFormatException();
                                        } else {
                                            // 정상적으로 투표했다면 무한루프를 빠져나옴
                                            out.writeUTF("/vote " + strSend);
                                            System.out.println(strSend + "번 투표하셨습니다.");
                                            out.flush();
                                            break;
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("잘못 된 값을 입력하셨습니다.");
                                        System.out.println("다시 입력 해 주세요");
                                        strSend = scanner.nextLine();
                                    }
                                }
                                isVote = false;
                            } else { // 채팅이 불가능한 상황일 때 채팅을 하려고 한 경우
                                System.out.println("지금은 채팅이 불가합니다.");
                            }
                        } else { // 플에이어가 죽었을 경우
                            System.out.println("사망한 플레이어는 채팅이 불가합니다.");
                        }
                    }
                }
            } catch (Exception e) {}
        }
    }

    // 서버 -> 클라이언트
    static class ClientReceiver extends Thread {
        Socket socket;
        DataInputStream in;

        ClientReceiver(Socket socket) {
            this.socket = socket;
            try {
                in = new DataInputStream(socket.getInputStream()); // 입력스트림 생성
            } catch (Exception e) {}
        }

        public synchronized void run() {
            while (in != null) {
                try {
                    String str = in.readUTF();

                    // 서버에서 온 메시지를 받아 클라이언트에서 출력 동작을 제한함
                    if (str.equals("/voteOn")) {
                        isChat = false;
                        isVote = true;
                    } else if(str.equals("/chatOff")) {
                        isChat = false;
                        isVote = false;
                    } else if(str.equals("/chatOn")) {
                        isChat = true;
                        isVote = false;                        
                    } else if(str.equals("/dead")) {
                        isLive = false;
                    } else if(str.equals("/select")) {
                        isSelct = true;
                    } else {
                        System.out.println(str);
                    }
                } catch (Exception e) {}
            }
        }
    }
}
