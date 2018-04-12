import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Vector;

// Класс действий над базой данных
public class DialogWin extends JFrame{
    public JButton insert, del_btn;
    public String ch_col;
    public String USQL;
    public int index_type;

    DialogWin(){
        setBounds(500, 300, 220, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public void INSERT(Connection conn) {
        setTitle("Вставка");
        String SQL = "INSERT INTO " + Admin.ch_elem_t;
        if(Admin.info_col_def.get(0) == 1) {
            SQL += " VALUES(DEFAULT";
            for (int i = 1; i < Admin.t_columns; i++) {
                SQL += ",?";
            }
        }else {
            SQL += " VALUES(?";
            for (int i = 1; i < Admin.t_columns; i++) {
                SQL += ",?";
            }
        }
        SQL += ");";
        System.out.println(SQL);



        try {
            JPanel input_panels = new JPanel();
            Vector<JTextField> fields = new Vector<>();

            Box mainbox = Box.createVerticalBox();


            for (int i = 0; i < Admin.t_columns; i++) {
                Box box_cn = Box.createHorizontalBox();
                JLabel templ = new JLabel(Admin.column_name.get(i));
                templ.setPreferredSize(new Dimension(65,10));
                box_cn.add(templ);
                //input_panels.add(new JLabel((String) Admin.column_name.get(i)));
                fields.add(new JTextField(10));
                box_cn.add(Box.createHorizontalStrut(6));
                box_cn.add(fields.get(i));
                //input_panels.add((Component) fields.get(i));
                mainbox.add(box_cn);
                mainbox.add(Box.createVerticalStrut(10));
                //add(input_panels);
            }
            input_panels.add(mainbox);
            add(input_panels);
            /*
            for (int i = 0; i < Admin.t_columns; i++) {
                input_panels.add(new JLabel((String) Admin.column_name.get(i)));
                fields.add(new JTextField(10));
                input_panels.add((Component) fields.get(i));
                add(input_panels);
            }*/
            JPanel btn = new JPanel();
            insert = new JButton("Вставить");
            btn.add(insert);
            add(btn, BorderLayout.SOUTH);
            if(Admin.info_col_def.get(0) == 1){
                (fields.get(0)).setText("default");
                (fields.get(0)).setEditable(false);
            }
            Vector<String> fields_text = new Vector<>();

            String finalSQL = SQL;
            insert.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JTextField in_field = new JTextField();
                    fields_text.removeAllElements();
                    for (int i = 0; i < Admin.t_columns; i++) {
                        in_field = fields.get(i);
                        fields_text.add(in_field.getText());
                    }
                    System.out.println(fields_text);
                    try {
                        PreparedStatement pstmt = conn.prepareStatement(finalSQL);
                        //pstmt.setInt(1, Admin.data.size() + 1);
                        for (int i = 2; i <= Admin.t_columns; i++) {
                            if(Admin.c_types.get(i-1) == 4) {
                                pstmt.setInt(i-1, Integer.valueOf(fields_text.get(i - 1)));
                            }else if (Admin.c_types.get(i-1) == 12){
                                pstmt.setString(i-1, fields_text.get(i - 1));
                            }else {
                               // Converting string to date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                java.util.Date date = null;
                                try {
                                    date = dateFormat.parse(fields_text.get(i - 1));
                                    System.out.println(date);
                                } catch (ParseException ep) {
                                    ep.printStackTrace();
                                }
                                pstmt.setDate(i-1, new java.sql.Date(date.getTime()));
                            }
                        }
                        pstmt.executeUpdate();
                        Admin ins = new Admin(1);
                        ins.dispose();
                        dispose();
                        System.out.println(finalSQL);
                    }catch (SQLException exc){
                        System.out.println(exc.getMessage());
                        JOptionPane.showMessageDialog(null, exc.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            setPreferredSize(new Dimension(300, 300));
            pack();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void DELETE(Connection conn) {
        setTitle("Удаление");
        String SQL = "DELETE FROM " + Admin.ch_elem_t + " WHERE " + Admin.column_name.get(0) + " = ?;";

        try {

            JPanel input_panels = new JPanel();
            JTextField del_id = new JTextField(10);
            input_panels.add(new JLabel(Admin.column_name.get(0)));
            input_panels.add(del_id);

            JPanel btn = new JPanel();
            del_btn = new JButton("Удалить");
            btn.add(del_btn);
            add(btn, BorderLayout.SOUTH);
            add(input_panels);
            String finalSQL = SQL;
            del_btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int get_id = Integer.valueOf(del_id.getText());
                    try {
                        PreparedStatement pstmt = conn.prepareStatement(finalSQL);
                        pstmt.setInt(1, get_id);
                        pstmt.executeUpdate();
                        System.out.println(finalSQL);
                    } catch (SQLException exc) {
                        System.out.println(exc.getMessage());
                        JOptionPane.showMessageDialog(null, exc.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                    Admin ins = new Admin(1);
                    ins.dispose();
                    dispose();
                }
            });
            setPreferredSize(new Dimension(250, 150));
            pack();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void UPDATE(Connection conn) {
        setTitle("Обновление");
        JPanel contents = new JPanel(new FlowLayout());

        JList<String> spisok_column = new JList<>(Admin.column_name);
        spisok_column.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton choose = new JButton("Обновить");
        JTextField var = new JTextField(10);
        JTextField id_field = new JTextField(10);

        JLabel set = new JLabel("Значение:");
        JLabel id = new JLabel("id:");

        contents.add(spisok_column, BorderLayout.EAST);
        contents.add(new JScrollPane(spisok_column));

        Box box = Box.createVerticalBox();

        Box bxu = Box.createHorizontalBox();
        Box bxp = Box.createHorizontalBox();

        bxu.add(set);
        bxu.add(Box.createHorizontalStrut(6));
        bxu.add(var);
        box.add(bxu);
        box.add(Box.createVerticalStrut(10));

        bxp.add(id);
        bxp.add(Box.createHorizontalStrut(6));
        bxp.add(id_field);
        box.add(bxp);
        box.add(Box.createVerticalStrut(10));

        id.setPreferredSize(set.getPreferredSize());
        var.setPreferredSize(id_field.getPreferredSize());

        setPreferredSize(new Dimension(300,250));
        box.add(choose);

        contents.add(box);
        add(contents);
        pack();

        choose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int get_id = Integer.valueOf(id_field.getText());
                System.out.println(USQL);
                try {
                    PreparedStatement pstmt = conn.prepareStatement(USQL);
                    String eq = String.valueOf(Admin.info_col_type.get(index_type));
                    if (Objects.equals(eq, "integer")){
                        pstmt.setInt(1,Integer.valueOf(var.getText()));
                    }else if (Objects.equals(eq, "character varying")){
                        pstmt.setString(1,var.getText());
                    }else if (Objects.equals(eq, "timestamp without time zone")){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date date = null;
                        try {
                            date = dateFormat.parse((String) var.getText());
                            System.out.println(date);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        pstmt.setDate(1, new java.sql.Date(date.getTime()));
                    }
                    pstmt.setInt(2, get_id);
                    pstmt.executeUpdate();
                    System.out.println(USQL);
                } catch (SQLException exc) {
                    System.out.println(exc.getMessage());
                    JOptionPane.showMessageDialog(null, exc.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                Admin ins = new Admin(1);
                ins.dispose();
                dispose();
            }
        });
        spisok_column.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        try{
                            String element = spisok_column.getSelectedValue();
                            ch_col = element.toString();
                            System.out.println(ch_col);
                            USQL = "UPDATE " + Admin.ch_elem_t + " SET " + ch_col + " = ? WHERE " + Admin.column_name.get(0) + " = ?;";
                            index_type = Admin.column_name.indexOf(ch_col);
                            System.out.println("index " + index_type);
                        }catch (Exception ex){
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
    }
}
