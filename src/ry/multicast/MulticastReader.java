package ry.multicast;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 *
 * @author ry
 */
public class MulticastReader {

    private static InetAddress IA;
    private static MulticastSocket MSOC;

    static {
        try {
            IA = InetAddress.getByName("224.0.1.20");
            MSOC = new MulticastSocket(4242);
            MSOC.joinGroup(IA);
            MSOC.setLoopbackMode(false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public MulticastReader() {
        JFrame frame = new JFrame("Multicast Listener");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setWrapStyleWord(true);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        final JTextField textField = new JTextField();
        final JButton send = new JButton("Senden");
        send.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (MSOC != null && !MSOC.isClosed()) {
                    byte[] buf = textField.getText().getBytes();
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, IA, 4242);
                    try {
                        MSOC.send(dp);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        try {
                            MSOC.leaveGroup(IA);
                        } catch (IOException ex1) {

                        }
                    }
                }

            }
        });
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0f;
        c.weighty = 0.9f;
        frame.getContentPane().add(new JScrollPane(area), c);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.9f;
        c.weighty = 0.1f;
        frame.getContentPane().add(textField, c);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.1f;
        frame.getContentPane().add(send, c);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        new MCASTWorker(MSOC, area).execute();
    }

    private static class MCASTWorker extends SwingWorker<Void, String> {

        private final MulticastSocket socket;
        private final JTextArea area;

        public MCASTWorker(MulticastSocket soc, JTextArea tArea) {
            area = tArea;
            socket = soc;
        }

        @Override
        protected Void doInBackground() throws Exception {
            byte[] buffer = new byte[8192];
            while (true) {
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                socket.receive(dp);
                String s = new String(dp.getData(), "8859_1");
                publish(s);
            }
        }

        @Override
        protected void process(List<String> chunks) {
            for (String msg : chunks) {
                area.append(msg + "\n");
            }
        }
    }

    public static void main(String[] args) {
        new MulticastReader();
    }
}
