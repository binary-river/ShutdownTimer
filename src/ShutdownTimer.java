import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShutdownTimer extends JFrame {

    private JTextField timeField;
    private JLabel timeLabel;
    private JLabel timerLable;
    private JButton startButton;
    private JButton cancelButton;
    private Thread shutdownThread;

    public ShutdownTimer() {
        setTitle("Auto Shutdown Timer");
        setSize(300,140);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        timeField = new JTextField(5);
        timeLabel = new JLabel("(min)");
        startButton = new JButton("START");
        cancelButton = new JButton("CANCEL");
        timerLable = new JLabel("00:00 lefts");

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
        panel3.add(timerLable);

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
            int milliseconds = minutes * 60 * 1000;

            shutdownThread = new Thread(new ShutdownJob(milliseconds));
            shutdownThread.start();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "숫자만 입력 가능 합니다.");
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }


    private class ShutdownJob implements Runnable{
        private int milliseconds;

        public ShutdownJob(int milliseconds) {
            this.milliseconds = milliseconds;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(milliseconds);
                JOptionPane.showMessageDialog(null, "System Shutdown!");
                //Runtime.getRuntime().exec("shutdown -s -t " + milliseconds);
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "타이머가 종료되었습니다.");
            } catch (Exception e) {
                e.printStackTrace();
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
        }
    }

    private class OnCancelButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if( validShutdownButton() ) {
                JOptionPane.showMessageDialog(null, "현재 타이머 실행 중이 아닙니다.");
                return ;
            }

            shutdownThread.interrupt();
            shutdownThread = null;
        }
    }

}
