package dat;

public class RefreshMoviesTask implements Runnable {

    @Override
    public void run() {
        System.out.println("Running the background task at: " + java.time.LocalDateTime.now());
        // Add your logic here
        BuildMain.main(new String[]{});
    }

}
