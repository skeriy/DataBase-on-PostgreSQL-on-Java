import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Objects;

public class Win_Set extends JFrame {
    private JLabel win_text, userl, passwordl;
    private JTextField input_user;
    private JPasswordField input_pass;
    private JButton button_1;
    private JButton button_2;
    private static String user, pass;
    private static String url = "jdbc:postgresql://localhost/postgres";
    static Connection connecting;
    static boolean isSuper;


    public Win_Set() {
        super("Окно авторизации");
        setBounds(500, 300, 400, 200);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        win_text = new JLabel("ВХОД");
        userl = new JLabel("   Логин:");
        userl.setBounds(0, 0, 20, 10);
        passwordl = new JLabel("Пароль:");
        button_1 = new JButton("Войти");
        button_2 = new JButton("Выйти");
        input_user = new JTextField(10);
        input_user.setBounds(12, 12, 12, 12);
        input_pass = new JPasswordField(10);


        JPanel title = new JPanel();
        title.add(win_text);
        add(title, BorderLayout.NORTH);

        JPanel input_panel = new JPanel();
        Box box = Box.createVerticalBox();
        Box bxu = Box.createHorizontalBox();
        Box bxp = Box.createHorizontalBox();
        box.add(bxu);
        box.add(Box.createVerticalStrut(10));
        box.add(bxp);
        input_panel.add(box);

        bxu.add(userl);
        bxu.add(Box.createHorizontalStrut(6));
        bxu.add(input_user);
        bxp.add(passwordl);
        bxp.add(Box.createHorizontalStrut(6));
        bxp.add(input_pass);
        userl.setPreferredSize(passwordl.getPreferredSize());
        input_user.setPreferredSize(input_pass.getPreferredSize());
        add(input_panel);
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(button_1);
        buttonsPanel.add(button_2);
        add(buttonsPanel, BorderLayout.SOUTH);

        initListeners();
    }
//  Метод на события кнопок
    private void initListeners() {
        button_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
        button_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    public void connect() {
        user = input_user.getText();
        pass = input_pass.getText();
        System.out.println(user + " " + pass);
        connecting = Connect.connect(url, user, pass);
        if (connecting != null) {
            isSuperUser(connecting);
            Win_Set.this.dispose();
            Admin win_admin = new Admin();
            win_admin.setVisible(true);

        } else {
            JOptionPane.showMessageDialog(null, "Неверно введен логин или пароль", "Ошибка", JOptionPane.ERROR_MESSAGE);
            initListeners();
        }
    }

//    Геттеры/сеттеры для получения/для установления значений переменных
    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPass() {
        return pass;
    }

    public static void setUrl(String new_url) {
        url = new_url;
    }

//  Проверка не является ли пользователь Администратором имеющим все привелегии
    public void isSuperUser(Connection conn) {
        String SQL = "SELECT usename,usesuper FROM pg_shadow WHERE usesuper ='t';";
        isSuper = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            rs.next();
            System.out.println(rs.getString(1) + " " + rs.getString(2));
            if ((Objects.equals(rs.getString(1), user)) & Objects.equals(rs.getString(2), "t")) isSuper = true;
        } catch (SQLException esql) {
            System.out.println(esql.getMessage());
        }
        System.out.println(user + " " + isSuper);
    }

//  Точка входа в программу начинается с создания экземпляра класса Win_Set
    public static void main(String[] args) {
        Win_Set window = new Win_Set();
        window.setResizable(false);
        window.setVisible(true);
    }


}
