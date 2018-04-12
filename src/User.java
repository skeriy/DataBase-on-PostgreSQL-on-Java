import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User extends JFrame {
    private Connection user_connect;
    private Vector users;
    private String ch_elem_user = "", patern;
    private String name_text;
    private String pass_text;
    private JList spisok_users;
    String find_in_this = "";

    User() {
        setBounds(500, 300, 220, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public void select_user() {
        this.setTitle("Пользователи");
        String url = Win_Set.getUrl();
        String user = Win_Set.getUser();
        String pass = Win_Set.getPass();
        user_connect = Connect.connect(url, user, pass);

        String sql_users = "select * from pg_shadow";
        users = new Vector();
        try (Statement stmt_user = user_connect.createStatement();
             ResultSet rs_user = stmt_user.executeQuery(sql_users)) {
            while (rs_user.next()) {
                users.add(rs_user.getString(1));
                System.out.println(rs_user.getString(1));
            }
        } catch (SQLException e) {

        }
        JPanel contents = new JPanel();
        spisok_users = new JList(users);
        spisok_users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton choose = new JButton("Добавить");
        JButton del_btn = new JButton("Удалить");
        JButton priv_btn = new JButton("Привелегии");
        choose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add_user();
            }
        });
        del_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                del_user();
                select_user();
                JList spisok_users_new = new JList(users);
                ListModel new_list = spisok_users_new.getModel();
                spisok_users.setModel(new_list);

            }
        });
        priv_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add_privelegue();
            }
        });
        spisok_users.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        Object element = spisok_users.getSelectedValue();
                        if (ch_elem_user != null) {
                            ch_elem_user = element.toString();
                            System.out.println(ch_elem_user);
                        }
                    }
                });
        contents.add(spisok_users, BorderLayout.EAST);
        contents.add(new JScrollPane(spisok_users));
        Box box = Box.createVerticalBox();
        box.add(choose);
        box.add(Box.createVerticalStrut(6));
        box.add(del_btn);
        box.add(Box.createVerticalStrut(6));
        box.add(priv_btn);
        contents.add(box);
        /*
        contents.add(choose, BorderLayout.EAST);
        contents.add(del_btn, BorderLayout.EAST);
        contents.add(priv_btn, BorderLayout.EAST);
        */
        setContentPane(contents);
        setPreferredSize(new Dimension(300, 250));
        setVisible(true);
        pack();
    }

    public void add_user() {
        JPanel contents = new JPanel();
        JTextField name_f = new JTextField(10);
        JTextField pass_f = new JTextField(10);
        JLabel name_l = new JLabel("Имя:");
        JLabel pass_l = new JLabel("Пароль:");
        JButton add_btn = new JButton("Добавить");

        Box box = Box.createVerticalBox();

        Box bxu = Box.createHorizontalBox();
        Box bxp = Box.createHorizontalBox();

        bxu.add(name_l);
        bxu.add(Box.createHorizontalStrut(6));
        bxu.add(name_f);
        box.add(bxu);
        box.add(Box.createVerticalStrut(10));

        bxp.add(pass_l);
        bxp.add(Box.createHorizontalStrut(6));
        bxp.add(pass_f);
        box.add(bxp);
        box.add(Box.createVerticalStrut(10));

        name_l.setPreferredSize(pass_l.getPreferredSize());
        name_f.setPreferredSize(pass_f.getPreferredSize());
        box.add(add_btn);
        contents.add(box);


        add_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                name_text = name_f.getText();
                pass_text = pass_f.getText();
                if ((Objects.equals(name_text, "")) || (Objects.equals(pass_text, ""))) {
                    JOptionPane.showMessageDialog(null, "Введено пустое значение для логина или пароля", "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    String sql_add = "CREATE USER " + name_text + " WITH PASSWORD '" + pass_text + "';";
                    try (Statement stmt_user = user_connect.createStatement();
                         ResultSet rs_user = stmt_user.executeQuery(sql_add)) {

                    } catch (SQLException ex) {

                    }
                    select_user();
                }
            }
        });

        setContentPane(contents);
        setPreferredSize(new Dimension(300, 250));
        setVisible(true);
        pack();

    }

    public void del_user() {
        String sql_del = "DROP USER " + ch_elem_user + ";";
        String sql_rev = "REVOKE ALL privileges ON ALL TABLES IN SCHEMA public FROM " + ch_elem_user + ";";
        try (Statement stmt_user = user_connect.createStatement()) {
                stmt_user.executeQuery(sql_rev);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        try (Statement stmt_user = user_connect.createStatement()) {
            stmt_user.executeQuery(sql_del);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            String er = ex.getMessage().toString();
            System.out.println("er "+er);
            if (!Objects.equals(er, "Запрос не вернул результатов.")) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        select_user();
    }

//    Добавление и отображение привелегий выбранного пользователя
    public void add_privelegue() {
        setTitle("Привелегии");
        JPanel contents = new JPanel();
        JButton accept = new JButton("Запомнить");

        JCheckBox select = new JCheckBox("SELECT");
        JCheckBox delete = new JCheckBox("DELETE");
        JCheckBox update = new JCheckBox("UPDATE");
        JCheckBox insert = new JCheckBox("INSERT");

        Box box = Box.createVerticalBox();

        box.add(select);
        box.add(Box.createVerticalStrut(10));
        box.add(delete);
        box.add(Box.createVerticalStrut(10));
        box.add(insert);
        box.add(Box.createVerticalStrut(10));
        box.add(update);
        box.add(Box.createVerticalStrut(10));
        box.add(accept);
        contents.add(box);
        select.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

            }
        });
        delete.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

            }
        });
        insert.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

            }
        });
        update.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

            }
        });
        String find_priv = "select relname,relacl from pg_catalog.pg_class WHERE relacl NOTNULL AND relname IN (select table_name from information_schema.columns where table_schema='public');";
        try (Statement stmt_find = user_connect.createStatement();
             ResultSet rs_find = stmt_find.executeQuery(find_priv)) {
            rs_find.next();
            System.out.println(rs_find.getString(2));
            find_in_this = rs_find.getString(2);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        patern = "(.*)(" + ch_elem_user + ")(.*)";

        boolean isFind = find_priv(patern, find_in_this);
            patern = "(.*)(" + ch_elem_user + ")(.*)[r](.*)(/)(.*)";
            boolean r_find = find_priv(patern,find_in_this);
            System.out.println(r_find);
            if(r_find) select.setSelected(true);
            patern = "(.*)(" + ch_elem_user + ")(.*)[w](.*)(/)(.*)";
            boolean w_find = find_priv(patern,find_in_this);
            System.out.println(w_find);
            if (w_find) update.setSelected(true);
            patern = "(.*)(" + ch_elem_user + ")(.*)[a](.*)(/)(.*)";
            boolean a_find = find_priv(patern,find_in_this);
            System.out.println(a_find);
            if (a_find) insert.setSelected(true);
            patern = "(.*)(" + ch_elem_user + ")(.*)[d](.*)(/)(.*)";
            boolean d_find = find_priv(patern,find_in_this);
            System.out.println(d_find);
            if(d_find) delete.setSelected(true);



        accept.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Vector privelegies = new Vector();
                if (select.isSelected()) privelegies.add("SELECT");
                if (delete.isSelected()) privelegies.add("DELETE");
                if (insert.isSelected()) privelegies.add("INSERT");
                if (update.isSelected()) privelegies.add("UPDATE");
                if (privelegies.isEmpty()) {
                    dispose();
                } else {
                    String sql_add_priv = "GRANT ";
                    if (privelegies.size() == 1) {
                        sql_add_priv += privelegies.get(0).toString();
                        sql_add_priv += " ON ALL TABLES IN SCHEMA public TO " + ch_elem_user + ";";
                    } else {
                        for (int i = 0; i < privelegies.size() - 1; i++) {
                            sql_add_priv += privelegies.get(i).toString() + ",";
                        }
                        sql_add_priv += privelegies.get(privelegies.size() - 1).toString() + " ON ALL TABLES IN SCHEMA public TO " + ch_elem_user + ";";
                    }
                    try (Statement stmt_user = user_connect.createStatement();
                         ResultSet rs_user = stmt_user.executeQuery(sql_add_priv)) {

                    } catch (SQLException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
                    String sql_revoke_priv = "REVOKE SELECT ON ALL TABLES IN SCHEMA public FROM " + ch_elem_user + ";";
                    Vector rev_privelegies = new Vector();
                    if (!select.isSelected()) rev_privelegies.add("SELECT");
                    if (!delete.isSelected()) rev_privelegies.add("DELETE");
                    if (!insert.isSelected()) rev_privelegies.add("INSERT");
                    if (!update.isSelected()) rev_privelegies.add("UPDATE");
                    if (rev_privelegies.isEmpty()) {
                        dispose();
                    } else {
                        String sql_rev_priv = "REVOKE ";
                        if (rev_privelegies.size() == 1) {
                            sql_rev_priv += rev_privelegies.get(0).toString();
                            sql_rev_priv += " ON ALL TABLES IN SCHEMA public FROM " + ch_elem_user + ";";
                        } else {
                            for (int i = 0; i < rev_privelegies.size() - 1; i++) {
                                sql_rev_priv += rev_privelegies.get(i).toString() + ",";
                            }
                            sql_rev_priv += rev_privelegies.get(rev_privelegies.size() - 1).toString() + " ON ALL TABLES IN SCHEMA public FROM " + ch_elem_user + ";";
                        }
                        try (Statement stmt_user = user_connect.createStatement();
                             ResultSet rs_user_rev = stmt_user.executeQuery(sql_rev_priv)) {

                        } catch (SQLException ex) {
                            System.out.println(ex.getMessage());
                        }
                }
                try (Statement stmt_find = user_connect.createStatement();
                     ResultSet rs_find = stmt_find.executeQuery(find_priv)) {
                    rs_find.next();
                    System.out.println(rs_find.getString(2));
                    find_in_this = rs_find.getString(2);
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
                patern = "(.*)(" + ch_elem_user + ")(.*)";

                boolean isFind = find_priv(patern, find_in_this);
                    patern = "(.*)(" + ch_elem_user + ")(.*)[r](.*)(/)(.*)";
                    boolean r_find = find_priv(patern,find_in_this);
                    System.out.println(r_find);
                    if(r_find) select.setSelected(true);
                    patern = "(.*)(" + ch_elem_user + ")(.*)[w](.*)(/)(.*)";
                    boolean w_find = find_priv(patern,find_in_this);
                    System.out.println(w_find);
                    if (w_find) update.setSelected(true);
                    patern = "(.*)(" + ch_elem_user + ")(.*)[a](.*)(/)(.*)";
                    boolean a_find = find_priv(patern,find_in_this);
                    System.out.println(a_find);
                    if (a_find) insert.setSelected(true);
                    patern = "(.*)(" + ch_elem_user + ")(.*)[d](.*)(/)(.*)";
                    boolean d_find = find_priv(patern,find_in_this);
                    System.out.println(d_find);
                    if(d_find) delete.setSelected(true);
            select_user();
            }

        });

        setContentPane(contents);
        setPreferredSize(new Dimension(300, 250));
        setVisible(true);
        pack();

    }

    public boolean find_priv(String patern, String find_in_this) {
        System.out.println(patern);
        Pattern p = Pattern.compile(patern);
        Matcher m = p.matcher(find_in_this);
        return m.matches();
    }

}
