package jp.seraphyware.smplejarapp;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 単純な実行可能jarとしてのJavaアプリケーション例
 *
 * @author seraphy
 */
public class SimpleJarApp extends JFrame {

    private static final long serialVersionUID = -417644051755960867L;

    public SimpleJarApp() {
        try {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            initComponent();

        } catch (RuntimeException ex) {
            dispose();
            throw ex;
        }
    }

    /**
     * レジストリキーにあるPreferencesへのアクセス用
     */
    Preferences pref = Preferences.systemNodeForPackage(this.getClass());

    /**
     * メッセージ用
     */
    private JLabel lblMessage;

    /**
     * キー値
     */
    private JTextField txtKey;

    /**
     * 設定値
     */
    private JTextField txtValue;

    private void initComponent() {
        setTitle("SimpleJarApp");

        Container contentPane = getContentPane();
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);

        // アイコンのロード
        try {
            Image iconImage = ImageIO.read(getClass().getResource("icon.png"));
            setIconImage(iconImage);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel lblKey = new JLabel("Key");
        JLabel lblValue = new JLabel("Value");

        this.lblMessage = new JLabel();
        this.txtKey = new JTextField("sample-key");
        this.txtValue = new JTextField("sample-val");

        this.lblMessage.setText(pref.absolutePath());

        JButton btnInfo = new JButton(new AbstractAction("Info") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                showInfo();
            }
        });
        JButton btnLoad = new JButton(new AbstractAction("Load") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                load();
            }
        });
        JButton btnRegister = new JButton(new AbstractAction("Register") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        Dimension dim = txtKey.getPreferredSize();
        dim.width = 200;
        txtKey.setPreferredSize(dim);

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        hGroup.addGroup(layout.createParallelGroup().addComponent(lblKey).addComponent(lblValue));
        hGroup.addGroup(layout
                .createParallelGroup(Alignment.TRAILING)
                .addComponent(lblMessage, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                .addComponent(txtKey)
                .addComponent(txtValue)
                .addGroup(
                        layout.createSequentialGroup().addComponent(btnInfo).addComponent(btnLoad)
                                .addComponent(btnRegister)));
        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(lblMessage));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(lblKey).addComponent(txtKey));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(lblValue).addComponent(txtValue));
        vGroup.addGroup(layout.createParallelGroup().addComponent(btnInfo).addComponent(btnLoad)
                .addComponent(btnRegister));
        layout.setVerticalGroup(vGroup);

        // レイアウト確定
        pack();

        // レジストリを読み取る
        load();
    }

    /**
     * システムの情報を表示する.
     */
    protected void showInfo() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("[System Properties]");
        System.getProperties().list(pw);

        pw.println();
        pw.println("[Environment]");
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            pw.println(key + "=" + val);
        }

        JTextArea textArea = new JTextArea();
        textArea.setText(sw.toString());

        JScrollPane scr = new JScrollPane(textArea);
        scr.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scr);
    }

    /**
     * レジストリから指定されたキーの値を読み取りテキストフィールドに設定する.
     */
    protected void load() {
        String key = txtKey.getText();
        String val = pref.get(key, "");
        // 以下のレジストリキーの読み込み
        // \HKLM\Software\JavaSoft\Prefs\jp\seraphyware\simplejarapp
        txtValue.setText(val);
    }

    /**
     * レジストリの指定されたキーにテキストフィールドの値を設定する.
     */
    protected void register() {
        String key = txtKey.getText();
        String val = txtValue.getText();
        // 以下のレジストリキーの書き込み
        // \HKLM\Software\JavaSoft\Prefs\jp\seraphyware\simplejarapp
        // Windows Vista以降の場合、管理者権限があってもUACが有効でなければ動作しない
        // (この場合は、失敗しても例外は発生せず、コンソール上に警告が表示されるのみ。)
        try {
            pref.put(key, val);

        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            JOptionPane.showMessageDialog(this, ex.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * エントリポイント
     *
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (args.length > 0) {
                    // 起動引数があれば、それをメッセージボックスに表示する.
                    StringBuilder buf = new StringBuilder();
                    for (String arg : args) {
                        buf.append(arg);
                        buf.append("\r\n");
                    }
                    JOptionPane.showMessageDialog(null, buf.toString());
                }

                // フレームを作成して表示する.
                SimpleJarApp app = new SimpleJarApp();
                app.setLocationByPlatform(true);
                app.setVisible(true);
            }
        });
    }
}
