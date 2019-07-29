class MafiaTimeThread extends Thread{
    private int totalTime;
    private int runTime;

    MafiaTimeThread(int second){
        totalTime = second;
        runTime = second;
    }
    public int getLeftTime() {
        return runTime;
    }

    public void run() {
        for (int i = 1; i <= totalTime;i++) {
            try {
                Thread.sleep(1000); 
                runTime--;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Timer {
    private MafiaTimeThread at;
    
    Timer(int timer) {
        at = new MafiaTimeThread(timer);
        
        at.start();
        try {
            at.join();
        } catch (Exception e) {}
    }
    
    int getTime() { 
        return at.getLeftTime();
    }
}