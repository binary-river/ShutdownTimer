import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class ShutdownTimer extends JFrame {

    private JTextField timeField;
    private JLabel timeLabel;
    private JLabel clockLabel;
    private JButton startButton;
    private JButton cancelButton;
    private Thread shutdownThread;
    private Timer clockTimer;
    private DecimalFormat df;

    public ShutdownTimer() {
        setTitle("Auto Shutdown Timer");
        setSize(320,140);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        df = new DecimalFormat("00");

        timeField = new JTextField(5);
        timeLabel = new JLabel("(min)");
        startButton = new JButton("START");
        cancelButton = new JButton("CANCEL");
        clockLabel = new JLabel("00:00 left");

        startButton.addActionListener(new OnTimerButtonClicked());
        cancelButton.addActionListener(new OnCancelButtonClicked());

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridLayout(0,1));

        JPanel panel1 = new JPanel();
        panel1.add(timeField);
        panel1.add(timeLabel);

        JPanel panel2 = new JPanel();
        panel2.add(startButton);
        panel2.add(cancelButton);

        JPanel panel3 = new JPanel();
        panel3.add(clockLabel);

        contentPane.add(panel1);
        contentPane.add(panel2);
        contentPane.add(panel3);
    }

    private boolean validShutdownButton(){
        return shutdownThread == null;
    }

    private void startShutdownTimer() {
        String inputTime = timeField.getText();
        try{
            int minutes = Integer.parseInt(inputTime);
            long milliseconds = minutes * 60 * 1000;

            shutdownThread = new Thread(new ShutdownJob(milliseconds));
            shutdownThread.start();
            timeField.setEditable(false);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "숫자만 입력 가능 합니다.");
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    private void cancelShutdownTimer() {
        shutdownThread.interrupt();
        shutdownThread = null;
        timeField.setEditable(true);
    }

    private void startClock(){
        String inputTime = timeField.getText();
        try{
            int minutes = Integer.parseInt(inputTime);
            long milliseconds = minutes * 60 * 1000;

            if( clockTimer != null ) clockTimer.stop();
            clockTimer = new Timer(1000, new ActionListener() {
                private long mills = milliseconds;

                @Override
                public void actionPerformed(ActionEvent e) {
                    mills -= 1000;
                    if (mills <= 0) setClockDefault();
                    else setClockMinutes(mills);
                }
            });
            clockTimer.start();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "숫자만 입력 가능 합니다.");
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    private void stopClock() {
        if( clockTimer != null ) clockTimer.stop();
        setClockDefault();
    }

    private void setClockDefault() {
        clockLabel.setText("00:00 left");
    }

    private void setClockMinutes(long milliseconds) {
        long seconds = milliseconds / 1000;
        long min = seconds / 60;
        long sec = seconds % 60;
        clockLabel.setText(df.format(min) + ":" + df.format(sec) + " lefts");
    }

    private void shutdownImmediate() {
        String osName = System.getProperty("os.name").toLowerCase();

        try{
            if( osName.contains("win")) Runtime.getRuntime().exec("shutdown -s").waitFor();
            else if( osName.contains("mac")) Runtime.getRuntime().exec("osascript -e 'tell app \"System Events\" to shut down'").waitFor();
            else if( osName.contains("nux") || osName.contains("aix")) Runtime.getRuntime().exec("shutdown -s").waitFor();
            else throw new RuntimeException("Unsupported OS");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "시스템 종료중 문제가 발생했습니다.");
        }
    }


    private class ShutdownJob implements Runnable{
        private long milliseconds;

        public ShutdownJob(long milliseconds) {
            this.milliseconds = milliseconds;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(milliseconds);
//                JOptionPane.showMessageDialog(null, "System Shutdown!");
                shutdownImmediate();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "타이머가 종료되었습니다.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                shutdownThread = null;
            }
        }
    }

    private class OnTimerButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if( !validShutdownButton() ) {
                JOptionPane.showMessageDialog(null, "이미 타이머가 실행 중입니다.");
            }

            startShutdownTimer();
            startClock();
        }
    }

    private class OnCancelButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if( validShutdownButton() ) {
                JOptionPane.showMessageDialog(null, "현재 타이머 실행 중이 아닙니다.");
                return ;
            }

            cancelShutdownTimer();
            stopClock();
        }
    }

}
