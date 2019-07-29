public class MafiaTimeClient extends Thread{
    Utility ut = new Utility();
    
    private int time1;
    private int time2;
    private String str = "";
    private String mafiaName = "";

    MafiaTimeClient(int time1, int time2, int flag) {
        this.time1 = time1;
        this.time2 = time2;
        
        // 0 : 토론, 1 : 시민 투표, 2 : 마피아 지목
        if (flag == 0) str = "토론";
        else if (flag == 1) str = "투표";
        else if (flag == 2) str = "지목";
    }
    
    MafiaTimeClient(int time1, int time2, int flag, String mafiaName) {
        this(time1, time2, flag);
        this.mafiaName = mafiaName;
    }

    public void run() {
        try {
            if (mafiaName.equals("")) {
                MafiaTimeThread mts1 = new MafiaTimeThread(time1);
                mts1.start();
                mts1.join();
                ut.sendToAll("------------------" + str + "시간 " + time2 + "초 남았습니다------------------");
                MafiaTimeThread mts2 = new MafiaTimeThread(time2);
                mts2.start();
                mts2.join();
                ut.sendToAll(str + "시간이 종료되었습니다.");
            } else {
                MafiaTimeThread mts1 = new MafiaTimeThread(time1);
                mts1.start();
                mts1.join();
                ut.sendToClient(mafiaName, "------------------" + str + "시간 " + time2 + "초 남았습니다------------------");
                MafiaTimeThread mts2 = new MafiaTimeThread(time2);
                mts2.start();
                mts2.join();
                ut.sendToClient(mafiaName, str + "시간이 종료되었습니다.");                
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}