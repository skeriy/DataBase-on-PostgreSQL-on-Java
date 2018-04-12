import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Admin extends JFrame {
    private JLabel chose_data;
    public static Vector<Vector<String>> data;
    public static Vector<String> column_name;
    public static Vector<Integer> info_col_def;
    public static Vector<String> info_col_type;
    public static Vector<Integer> ord_position;
    public String ch_elem_bd, new_url;
    public static Connection new_connect;
    public static JTable table = null;
    public static int t_columns;
    public static String ch_elem_t;
    public static Vector<String> bd_c;
    public static Vector<Integer> c_types;
    public JMenu action_Menu,user_Menu;

    Admin() {
        setBounds(400, 300, 600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Font font = new Font("Verdana", Font.PLAIN, 11);
        JMenuBar menuBar = new JMenuBar();

        JMenu bd_Menu = new JMenu("БД");
        bd_Menu.setFont(font);

        JMenuItem chose_bd = new JMenuItem("Выбор БД");
        chose_bd.setFont(font);
        bd_Menu.add(chose_bd);

        JMenu sign_out = new JMenu("Выход");
        sign_out.setFont(font);

        JMenuItem unsign = new JMenuItem("Выйти из БД");
        unsign.setFont(font);
        sign_out.add(unsign);

        user_Menu = new JMenu("Пользователи");
        user_Menu.setFont(font);

        JMenuItem chose_user = new JMenuItem("Выбор пользователя");
        chose_user.setFont(font);
        user_Menu.add(chose_user);

        action_Menu = new JMenu("Действия");
        action_Menu.setFont(font);

        JMenuItem added = new JMenuItem("Вставка");
        added.setFont(font);
        action_Menu.add(added);

        JMenuItem delete = new JMenuItem("Удаление");
        delete.setFont(font);
        action_Menu.add(delete);

        JMenuItem update = new JMenuItem("Обновление");
        update.setFont(font);
        action_Menu.add(update);


        chose_bd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table = null;
                setTitle(ch_elem_bd + ".Выбор БД");
                show_bd(new_connect);
                pack();
            }
        });

        chose_user.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                User user = new User();
                user.select_user();
            }
        });
        added.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogWin add_dialog = new DialogWin();
                add_dialog.INSERT(Admin.new_connect);
            }
        });
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogWin dell = new DialogWin();
                dell.DELETE(Admin.new_connect);
            }
        });

        update.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogWin upd = new DialogWin();
                upd.UPDATE(Admin.new_connect);
            }
        });
        unsign.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                try {
                    if(new_connect != null) new_connect.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                Win_Set new_win = new Win_Set();
                new_win.setResizable(false);
                new_win.setVisible(true);
            }
        });

        menuBar.add(bd_Menu);
        menuBar.add(user_Menu);
        menuBar.add(action_Menu);
        menuBar.add(sign_out);
        sign_out.setVisible(false);

        this.setJMenuBar(menuBar);
        this.setPreferredSize(new Dimension(270, 225));

        chose_data = new JLabel("Выбор БД");
        JPanel title = new JPanel(new FlowLayout());
        title.add(chose_data);
        add(title, BorderLayout.NORTH);
        show_bd(Win_Set.connecting);
        pack();
    }
    Admin(int i){
        SELECT(new_connect, ch_elem_t);
    }

    public void show_bd(Connection conn) {
        action_Menu.setVisible(false);
        user_Menu.setVisible(false);
        if(ch_elem_bd == null){
            this.setTitle("Выбор БД");
        }
        String SQL = "SELECT datname FROM pg_database WHERE datistemplate = false;";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            Vector<String> bd_c = new Vector<String>();
            int i = 0;
            while (rs.next()) {
                bd_c.add(rs.getString(1));
                System.out.println(bd_c.get(i));
                i++;
            }
            JPanel contents = new JPanel();
            JList<String> spisok_bd = new JList<>(bd_c);
            spisok_bd.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JButton choose = new JButton("Выбрать");
            choose.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    validate();
                    try {
                        Win_Set.connecting.close();
                        Win_Set.setUrl(new_url);
                        String url = Win_Set.getUrl();
                        String user = Win_Set.getUser();
                        String pass = Win_Set.getPass();
                        new_connect = Connect.connect(url, user, pass);
                    } catch (SQLException ex) {
                        System.out.println(ex.getMessage());
                    }
                    show_t(new_connect);
                    pack();
                }
            });
            spisok_bd.addListSelectionListener(
                    new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e) {
                            String element = spisok_bd.getSelectedValue();
                            ch_elem_bd = element;
                            System.out.println(ch_elem_bd);
                            new_url = "jdbc:postgresql://localhost/" + ch_elem_bd;
                        }
                    });
            contents.add(spisok_bd, BorderLayout.EAST);
            contents.add(new JScrollPane(spisok_bd));
            contents.add(choose, BorderLayout.EAST);
            setContentPane(contents);
            setPreferredSize(new Dimension(300, 250));
            pack();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void show_t(Connection conn) {
        if (Win_Set.isSuper){
            user_Menu.setVisible(true);
            action_Menu.setVisible(false);
        }else {
            user_Menu.setVisible(false);
            action_Menu.setVisible(false);
        }

        this.setTitle(ch_elem_bd + ".Выбор таблицы");
        String SQL = "SELECT table_name FROM information_schema.tables WHERE table_schema='public';";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            bd_c = new Vector<>();
            int i = 0;
            while (rs.next()) {
                bd_c.add(rs.getString(1));
                System.out.println(bd_c.get(i));
                i++;
            }
            JPanel contents = new JPanel(new FlowLayout());
            JList<String> spisok_tables = new JList<String>(bd_c);
            spisok_tables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JButton choose = new JButton("Выбрать");
            choose.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SELECT(new_connect, ch_elem_t);
                    repaint();
                    validate();
                    pack();
                }
            });
            spisok_tables.addListSelectionListener(
                    new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e) {
                            String element = spisok_tables.getSelectedValue();
                            ch_elem_t = element;
                            try (Statement stmt = conn.createStatement();
                                 ResultSet rs = stmt.executeQuery("select * from " + ch_elem_t)) {
                                t_columns = rs.getMetaData().getColumnCount();
                                System.out.println(t_columns);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            System.out.println(ch_elem_t);
                        }
                    });
            contents.add(spisok_tables, BorderLayout.EAST);
            contents.add(new JScrollPane(spisok_tables));
            contents.add(choose, BorderLayout.EAST);
            setContentPane(contents);
            setVisible(true);
            pack();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void SELECT(Connection conn, String selected) {
        if(action_Menu != null) action_Menu.setVisible(true);
        String SQL = "SELECT * FROM " + selected;
        String sql_info = "SELECT data_type,column_default,ordinal_position from information_schema.columns " +
                            "WHERE (table_schema = 'public') AND (table_name = '" + selected + "');";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            column_name = new Vector<>();
            data = new Vector<>();
            info_col_def = new Vector<>();
            info_col_type = new Vector<>();
            ord_position = new Vector<Integer>();

            int counts = rs.getMetaData().getColumnCount();
            c_types = new Vector<Integer>();
            for (int i = 1; i <= counts; i++) {
                column_name.add(rs.getMetaData().getColumnName(i));
                System.out.print(column_name.get(i - 1) + "\t");
                c_types.add(rs.getMetaData().getColumnType(i));
            }
            System.out.println(c_types);
            System.out.println();
            int line = 0;
            while (rs.next()) {
                Vector<String> v = new Vector<>();
                data.add(v);
                for (int i = 1; i <= counts; i++) {
                    v.add(rs.getString(i));
                }
                System.out.println(data.get(line));
                line++;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        try (Statement stmt_info = conn.createStatement();
             ResultSet rs_info = stmt_info.executeQuery(sql_info)) {
            boolean ma = false;
            while (rs_info.next()) {
                try {
                    info_col_type.add(rs_info.getString(1));
                    ord_position.add(rs_info.getInt(3));
                    String s = rs_info.getString(2);
                    Pattern p = Pattern.compile("nextval*");
                    Matcher m = p.matcher(s);
                    ma = m.lookingAt();
                } catch (Exception eee) {
                    System.out.println(eee.getMessage());
                    ma = false;

                }
                if (ma) {
                    info_col_def.add(1);
                } else {
                    info_col_def.add(0);
                }
            }
        } catch (SQLException ee) {
            ee.printStackTrace();
        }
        System.out.println(ord_position);
        System.out.println(info_col_type);
        ;
        System.out.println(info_col_def);
        if (table == null) {
            table = new JTable(data, column_name) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            getContentPane().add(new JScrollPane(table));
            setPreferredSize(new Dimension(900, 400));
            setLocationRelativeTo(null);
            pack();
        } else {
            JTable new_table = new JTable(data, column_name) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            TableModel new_t = new_table.getModel();
            table.setModel(new_t);
        }
    }

}

