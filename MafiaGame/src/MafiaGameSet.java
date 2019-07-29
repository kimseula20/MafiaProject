import java.io.DataOutputStream;
import java.util.HashMap;

public class MafiaGameSet {
    final static int MAX_PERSON = 5; // 게임 진행 플레이어 수
    static HashMap<String, Job> jobs = new HashMap<String, Job>(); // 플레이어 닉네임(key), 직업 객체(value)
    // 플레이어 name(key), 서버 -> 클라이언트 통료(Value)
    static HashMap<String, DataOutputStream> clients = new HashMap<String, DataOutputStream>();
    // 현재 남아있는 생존자 수
    static HashMap<String, Job> liveHashMap;
    static int[] voteArr = new int[MAX_PERSON]; // 시민 투표 - 생존자 투표 배열 (맨 처음 초기 값) 
    static int voteCnt = 0; // 시민 투표 - 생존자 투표 카운트
    static int pickNum = -1; // 마피아 투표 - 상대 지목
    static boolean gamePlay = true; // 게임플레이
    static boolean debugMode = true; // 디버그 모드
    static int today = 1; // 토론 날짜
}
