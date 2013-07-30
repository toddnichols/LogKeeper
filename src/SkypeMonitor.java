
import com.skype.*;

public class SkypeMonitor implements Runnable {

    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void run() {
        TalkAbroadLogKeeper.debug("SkypeMonitor thread instantiated.");
        while (!running) {
            try {
                running = Skype.isRunning();
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        TalkAbroadLogKeeper.debug("SkypeMonitor thread closing.");
    }
}


