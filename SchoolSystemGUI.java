package sql;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import java.awt.*;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String fileName) {
        try {
            backgroundImage = new ImageIcon(fileName).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

public class SchoolSystemGUI extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/workstudyprogram?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "hzy720929";

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    private JTextField studentIDField, nameField, departmentField, serviceAbilityField, serviceCategoryField;
    private JTextField projectNameField, demandUnitField, jobDescriptionField, workTimeField, payField;
    private JTextField assignmentStudentIDField, assignmentProjectIDField;

    private JTable studentTable, projectTable;
    private DefaultTableModel studentTableModel, projectTableModel;

    private String selectedRole;

    public SchoolSystemGUI() {
        super("勤工俭学数据库系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 添加登录选择界面
        JPanel loginPanel = createLoginPanel();
        mainPanel.add(loginPanel, "login");

        // Admin functionality setup
        JPanel adminPanel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel studentManagementPanel = createStudentManagementPanel();
        JPanel projectManagementPanel = addprojectPanel();
        JPanel viewStudentsPanel = createViewStudentsPanel();
        JPanel viewProjectsPanel = createViewProjectsPanel();
        JPanel creditManagementPanel = createCreditManagementPanel();
        JPanel viewCreditRecordsPanel = createViewCreditRecordsPanel();
         JPanel assign=createAssignmentManagementPanel();
        JPanel canyu=createStudentStatusPanel();
         JPanel status=  createProjectStatusPanel();
        tabbedPane.add("添加学生", studentManagementPanel);
        tabbedPane.add("添加项目", projectManagementPanel);
        tabbedPane.add("查看学生", viewStudentsPanel);
        tabbedPane.add("查看项目", viewProjectsPanel);
        tabbedPane.add("分配项目", assign);
        tabbedPane.add("添加信誉",creditManagementPanel);
        tabbedPane.add("查看信誉", viewCreditRecordsPanel);
        tabbedPane.add("查询参与学生", canyu);
        tabbedPane.add("项目状态", status);


        adminPanel.add(tabbedPane);

        mainPanel.add(adminPanel, "admin");
        JPanel studentViewPanel = createStudentViewPanel();
        mainPanel.add(studentViewPanel, "student");
        // 添加其他角色面板的占位符
       // mainPanel.add(new JLabel("学生功能界面"), "student");
        JPanel cViewPanel = createCompanyViewPanel();
        mainPanel.add(cViewPanel,"customer" );
        JButton backButton = new JButton("返回登录界面");

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "login");
            }
        });
        adminPanel.add(backButton, BorderLayout.EAST);
        add(mainPanel);
    }
    private JPanel createRevenueStatisticsPanel() {
        JPanel panel = new JPanel();
        JButton revenueButton = new JButton("统计院系收入");

        // 添加按钮到面板
        panel.add(revenueButton);

        // 按钮事件监听器
        revenueButton.addActionListener(e -> calculateDepartmentRevenue());

        return panel;
    }
    private void calculateDepartmentRevenue() {
        String query = "SELECT Department, SUM(Pay) AS TotalRevenue " +
                "FROM Projects " +
                "JOIN Departments ON Projects.DepartmentID = Departments.DepartmentID " +
                "WHERE ProjectStatus = '已完成' " +
                "GROUP BY Department";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // 清空之前的数据
            DefaultTableModel model = new DefaultTableModel(new String[]{"院系", "总收入"}, 0);

            while (rs.next()) {
                String department = rs.getString("Department");
                double totalRevenue = rs.getDouble("TotalRevenue");
                model.addRow(new Object[]{department, totalRevenue});
            }

            // 展示数据
            JTable table = new JTable(model);
            JOptionPane.showMessageDialog(null, new JScrollPane(table), "院系收入统计", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createProjectStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建下拉框选择项目状态
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"正在进行", "已完成"});
        panel.add(statusComboBox, BorderLayout.NORTH);

        // 创建表格显示项目信息
        String[] columnNames = {"项目ID", "项目名称", "需求单位", "状态"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 下拉框选择事件处理
        statusComboBox.addActionListener(e -> updateProjectStatusTable((String) statusComboBox.getSelectedItem(), model));

        return panel;
    }
    private void updateProjectStatusTable(String status, DefaultTableModel model) {
        model.setRowCount(0); // 清空表格数据

        String query = "";
        if ("正在进行".equals(status)) {
            query = "SELECT projects.ProjectID, projects.ProjectName, projects.DemandUnit, servicearrangements.IsCompleted  FROM projects JOIN servicearrangements ON projects.ProjectID = servicearrangements.xiangmuid WHERE servicearrangements.IsCompleted = '正在进行'";

        } else if ("已完成".equals(status)) {
            query = "SELECT projects.ProjectID, projects.ProjectName, projects.DemandUnit, servicearrangements.IsCompleted  FROM projects JOIN servicearrangements ON projects.ProjectID = servicearrangements.xiangmuid WHERE servicearrangements.IsCompleted = '已完成'";
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String projectID = rs.getString("ProjectID");
                String projectName = rs.getString("ProjectName");
                String demandUnit = rs.getString("DemandUnit");
                model.addRow(new Object[]{projectID, projectName, demandUnit, status});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStudentStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建下拉框选择项目参与状态
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"已参与项目", "未参与项目", "正在参与项目"});
        panel.add(statusComboBox, BorderLayout.NORTH);

        // 创建表格显示学生信息
        String[] columnNames = {"学生ID", "姓名", "系别", "状态"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 下拉框选择事件处理
        statusComboBox.addActionListener(e -> updateStudentStatusTable((String) statusComboBox.getSelectedItem(), model));

        return panel;
    }
    private void updateStudentStatusTable(String status, DefaultTableModel model) {
        model.setRowCount(0); // 清空表格数据

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "";
            if ("已参与项目".equals(status)) {
                query = "SELECT DISTINCT Students.StudentID, Students.Name, Students.Department ,Students.iscanyu FROM Students WHERE Students.iscanyu='已参与'";
            } else if ("未参与项目".equals(status)) {
                query = "SELECT DISTINCT Students.StudentID, Students.Name, Students.Department ,Students.iscanyu FROM Students WHERE Students.iscanyu='未参与'";
            } else if ("正在参与项目".equals(status)) {
                query = "SELECT DISTINCT Students.StudentID, Students.Name, Students.Department ,Students.iscanyu FROM Students WHERE Students.iscanyu='正在参与'";
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String studentID = rs.getString("StudentID");
                    String name = rs.getString("Name");
                    String department = rs.getString("Department");
                    model.addRow(new Object[]{studentID, name, department, status});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createCompanyViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建上方输入企业名称的面板
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("企业名称:"));
        JTextField companyNameInputField = new JTextField(10);
        inputPanel.add(companyNameInputField);

        JButton loadButton = new JButton("加载学生");
        inputPanel.add(loadButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        // 初始化表格模型和表格
        companyStudentTableModel = new DefaultTableModel(new String[]{"学生学号", "学生姓名", "项目名称", "岗位描述","学生能力"}, 0);
        companyStudentTable = new JTable(companyStudentTableModel);
        JScrollPane scrollPane = new JScrollPane(companyStudentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton confirmButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");
        JButton backButton = new JButton("返回登录界面");

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "login");
            }
        });
        panel.add(backButton, BorderLayout.EAST);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(confirmButton);


        panel.add(buttonPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadCompanyProjectsStudents(companyNameInputField.getText()));
        confirmButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "操作确认"));


        return panel;
    }
    private DefaultTableModel companyStudentTableModel;
    private JTable companyStudentTable;
    private void loadCompanyProjectsStudents(String companyName) {
        // 清空当前表格数据
        companyStudentTableModel.setRowCount(0);

        String projectQuery = "SELECT ProjectID FROM Projects WHERE DemandUnit = ?";
        String participationQuery = "SELECT StudentID FROM 参与 WHERE xiangmuid = ?";
        String studentQuery = "SELECT StudentID, Name ,ServiceAbility FROM Students WHERE StudentID = ?";
        String projectNameQuery = "SELECT ProjectName, JobDescription FROM Projects WHERE ProjectID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement projectStmt = conn.prepareStatement(projectQuery)) {

            projectStmt.setString(1, companyName);
            ResultSet projectRs = projectStmt.executeQuery();

            while (projectRs.next()) {
                String projectID = projectRs.getString("ProjectID");

                String projectName = "";
                String jobDescription = "";

                try (PreparedStatement projectNameStmt = conn.prepareStatement(projectNameQuery)) {
                    projectNameStmt.setString(1, projectID);
                    ResultSet projectNameRs = projectNameStmt.executeQuery();
                    if (projectNameRs.next()) {
                        projectName = projectNameRs.getString("ProjectName");
                        jobDescription = projectNameRs.getString("JobDescription");
                    }
                }

                try (PreparedStatement participationStmt = conn.prepareStatement(participationQuery)) {
                    participationStmt.setString(1, projectID);
                    ResultSet participationRs = participationStmt.executeQuery();

                    while (participationRs.next()) {
                        String studentID = participationRs.getString("StudentID");

                        try (PreparedStatement studentStmt = conn.prepareStatement(studentQuery)) {
                            studentStmt.setString(1, studentID);
                            ResultSet studentRs = studentStmt.executeQuery();

                            if (studentRs.next()) {
                                String studentName = studentRs.getString("Name");
                                String a = studentRs.getString("ServiceAbility");
                                companyStudentTableModel.addRow(new Object[]{studentID, studentName, projectName, jobDescription,a});
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据加载失败: " + e.getMessage());
        }
    }

    private JTextField creditStudentIDField, serviceQualityField, workAttitudeField, creditScoreField;
    private JTable creditRecordTable;
    private DefaultTableModel creditRecordTableModel;
// 在 SchoolSystemGUI 类中添加以下代码

    private JTable studentProjectTable;
    private DefaultTableModel studentProjectTableModel;

    private JPanel createStudentViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建上方输入学号的面板
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("学号:"));
        JTextField studentIDInputField = new JTextField(10);
        inputPanel.add(studentIDInputField);

        JButton loadButton = new JButton("加载项目");
        inputPanel.add(loadButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        studentProjectTableModel = new DefaultTableModel(new String[]{"项目名称", "需求单位", "岗位描述", "开始时间", "结束时间", "报酬"}, 0);
        studentProjectTable = new JTable(studentProjectTableModel);
        JScrollPane scrollPane = new JScrollPane(studentProjectTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton confirmButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        JButton backButton = new JButton("返回登录界面");

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "login");
            }
        });
        panel.add(backButton, BorderLayout.EAST);
        loadButton.addActionListener(e -> loadStudentAssignedProjects(studentIDInputField.getText()));
        confirmButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "项目确认"));
        cancelButton.addActionListener(e -> cancelAssignedProject(studentIDInputField.getText()));

        return panel;
    }

    private void loadStudentAssignedProjects(String studentID) {
        studentProjectTableModel.setRowCount(0); // 清除现有数据

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM show2students WHERE StudentID = ?")) {
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String projectName = rs.getString("ProjectName");
                String demandUnit = rs.getString("demandunit");
                String jobDescription = rs.getString("jobdescription");
                String startTime = rs.getString("starttime");
                String overTime = rs.getString("overtime");
                String pay = rs.getString("pay");
                studentProjectTableModel.addRow(new Object[]{projectName, demandUnit, jobDescription, startTime, overTime, pay});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void cancelAssignedProject(String studentID) {
        int selectedRow = studentProjectTable.getSelectedRow();
        if (selectedRow != -1) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM 参与 WHERE studentid = ?")) {
                stmt.setString(1, studentID);

                // 调试信息
                System.out.println("Executing DELETE with studentid = " + studentID);

                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "项目分配记录已删除");
                    loadStudentAssignedProjects(studentID); // 重新加载项目列表
                } else {
                    JOptionPane.showMessageDialog(this, "未找到对应的项目分配记录");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(); // 打印堆栈追踪以进行调试
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要取消的项目");
        }
    }



    private JPanel createCreditManagementPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("学生ID:"));
        creditStudentIDField = new JTextField();
        panel.add(creditStudentIDField);
        panel.add(new JLabel("服务质量:"));
        serviceQualityField = new JTextField();
        panel.add(serviceQualityField);
        panel.add(new JLabel("工作态度:"));
        workAttitudeField = new JTextField();
        panel.add(workAttitudeField);
        panel.add(new JLabel("信誉分数:"));
        creditScoreField = new JTextField();
        panel.add(creditScoreField);
        JButton addCreditButton = new JButton("添加信誉记录");
        addCreditButton.addActionListener(e -> addCreditRecord());
        panel.add(addCreditButton);
        return panel;
    }

    private JPanel createViewCreditRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        creditRecordTableModel = new DefaultTableModel(new String[]{"反馈ID", "服务质量", "工作态度", "信誉分数", "学生ID"}, 0);
        creditRecordTable = new JTable(creditRecordTableModel);
        JScrollPane scrollPane = new JScrollPane(creditRecordTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadCreditRecordData();
        JButton refreshButton = new JButton("刷新信誉记录");
        refreshButton.addActionListener(e -> loadCreditRecordData());
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private void addCreditRecord() {
        String studentID = creditStudentIDField.getText();
        int serviceQuality = Integer.parseInt(serviceQualityField.getText());
        String workAttitude = workAttitudeField.getText();
        int creditScore = Integer.parseInt(creditScoreField.getText());

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO credit (ServiceQuality, WorkAttitude, CreditScore, StudentID) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, serviceQuality);
            stmt.setString(2, workAttitude);
            stmt.setInt(3, creditScore);
            stmt.setString(4, studentID);
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "信誉记录添加成功！");
                creditStudentIDField.setText("");
                serviceQualityField.setText("");
                workAttitudeField.setText("");
                creditScoreField.setText("");
                loadCreditRecordData(); // 更新信誉记录显示
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCreditRecordData() {
        creditRecordTableModel.setRowCount(0); // 清除现有数据
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM credit")) {
            while (rs.next()) {
                int feedbackID = rs.getInt("FeedbackID");
                int serviceQuality = rs.getInt("ServiceQuality");
                String workAttitude = rs.getString("WorkAttitude");
                int creditScore = rs.getInt("CreditScore");
                String studentID = rs.getString("StudentID");
                creditRecordTableModel.addRow(new Object[]{feedbackID, serviceQuality, workAttitude, creditScore, studentID});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createLoginPanel() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600));

        BackgroundPanel backgroundPanel = new BackgroundPanel("D:/桌面/数据库课设/62d9eb3f70b4a61889b571bf8fa0c1a.jpg");
        backgroundPanel.setBounds(0, 0, 800, 600);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        JPanel roleSelectionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        roleSelectionPanel.setOpaque(false); // 使面板透明以显示背景图片
        roleSelectionPanel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200)); // 添加边距

        JButton adminButton = new JButton("管理员");
        JButton studentButton = new JButton("学生");
        JButton customerButton = new JButton("客户");

        adminButton.addActionListener(e -> showAdminLoginPanel());
        studentButton.addActionListener(e -> selectRole("student"));
        customerButton.addActionListener(e -> selectRole("customer"));

        roleSelectionPanel.add(adminButton);
        roleSelectionPanel.add(studentButton);
        roleSelectionPanel.add(customerButton);

        JLabel titleLabel = new JLabel("请选择登录角色:", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20)); // 设置标题字体

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false); // 使面板透明以显示背景图片
        container.setBounds(0, 0, 800, 600);
        container.add(titleLabel, BorderLayout.NORTH);
        container.add(roleSelectionPanel, BorderLayout.CENTER);

        layeredPane.add(container, JLayeredPane.PALETTE_LAYER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(layeredPane, BorderLayout.CENTER);
        return panel;
    }

    private void showAdminLoginPanel() {
        JPanel adminLoginPanel = new JPanel(new GridLayout(6, 5));
        adminLoginPanel.add(new JLabel("用户名:"));
        loginUsernameField = new JTextField();
        adminLoginPanel.add(loginUsernameField);
        adminLoginPanel.add(new JLabel("密码:"));
        loginPasswordField = new JPasswordField();
        adminLoginPanel.add(loginPasswordField);

        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(e -> authenticateAdmin());
        adminLoginPanel.add(loginButton);

        JButton backButton = new JButton("返回");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        adminLoginPanel.add(backButton);

        mainPanel.add(adminLoginPanel, "adminLogin");
        cardLayout.show(mainPanel, "adminLogin");
    }
    private void authenticateAdmin() {
        String username = loginUsernameField.getText();
        String password = new String(loginPasswordField.getPassword());

        // 数据库验证管理员账号和密码
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // 如果验证成功，显示管理员界面
                cardLayout.show(mainPanel, "admin");
            } else {
                // 如果验证失败，显示错误消息
                JOptionPane.showMessageDialog(this, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void selectRole(String role) {
        selectedRole = role;
        cardLayout.show(mainPanel, role);
    }

    private void addStudent() {
        String studentID = studentIDField.getText();
        String name = nameField.getText();
        String department = departmentField.getText();
        String serviceAbility = serviceAbilityField.getText();
        String serviceCategory = serviceCategoryField.getText();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Students (StudentID, Name, Department, ServiceAbility, ServiceCategory) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, studentID);
            stmt.setString(2, name);
            stmt.setString(3, department);
            stmt.setString(4, serviceAbility);
            stmt.setString(5, serviceCategory);
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "学生添加成功！");
                studentIDField.setText("");
                nameField.setText("");
                departmentField.setText("");
                serviceAbilityField.setText("");
                serviceCategoryField.setText("");
                loadStudentData(); // 更新学生信息显示
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStudentManagementPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("学生ID:"));
        studentIDField = new JTextField();
        panel.add(studentIDField);
        panel.add(new JLabel("姓名:"));
        nameField = new JTextField();
        panel.add(nameField);
        panel.add(new JLabel("系别:"));
        departmentField = new JTextField();
        panel.add(departmentField);
        panel.add(new JLabel("服务能力:"));
        serviceAbilityField = new JTextField();
        panel.add(serviceAbilityField);
        panel.add(new JLabel("服务类别:"));
        serviceCategoryField = new JTextField();
        panel.add(serviceCategoryField);
        JButton addButton = new JButton("添加学生");
        addButton.addActionListener(e -> addStudent());
        panel.add(addButton);
        return panel;
    }

    private JPanel addprojectPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("项目名称:"));
        projectNameField = new JTextField();
        panel.add(projectNameField);
        panel.add(new JLabel("需求单位:"));
        demandUnitField = new JTextField();
        panel.add(demandUnitField);
        panel.add(new JLabel("岗位描述:"));
        jobDescriptionField = new JTextField();
        panel.add(jobDescriptionField);
        panel.add(new JLabel("工作时间:"));
        workTimeField = new JTextField();
        panel.add(workTimeField);
        panel.add(new JLabel("报酬:"));
        payField = new JTextField();
        panel.add(payField);
        JButton addButton = new JButton("添加项目");
        addButton.addActionListener(e -> addProject());
        panel.add(addButton);
        return panel;
    }

    private void addProject() {
        String projectName = projectNameField.getText();
        String demandUnit = demandUnitField.getText();
        String jobDescription = jobDescriptionField.getText();
        String workTime = workTimeField.getText();
        String pay = payField.getText();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Projects (ProjectName, DemandUnit, JobDescription, WorkTime, Pay) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, projectName);
            stmt.setString(2, demandUnit);
            stmt.setString(3, jobDescription);
            stmt.setString(4, workTime);
            stmt.setString(5, pay);
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "项目添加成功！");
                projectNameField.setText("");
                demandUnitField.setText("");
                jobDescriptionField.setText("");
                workTimeField.setText("");
                payField.setText("");
                loadProjectData(); // 更新项目信息显示
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignProject() {
        String studentID = assignmentStudentIDField.getText();
        String projectID = assignmentProjectIDField.getText();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Assignments (StudentID, ProjectID) VALUES (?, ?)")) {
            stmt.setString(1, studentID);
            stmt.setString(2, projectID);
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "项目分配成功！");
                assignmentStudentIDField.setText("");
                assignmentProjectIDField.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createAssignmentManagementPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("学生ID:"));
        assignmentStudentIDField = new JTextField();
        panel.add(assignmentStudentIDField);
        panel.add(new JLabel("项目ID:"));
        assignmentProjectIDField = new JTextField();
        panel.add(assignmentProjectIDField);
        JButton assignButton = new JButton("分配项目");
        assignButton.addActionListener(e -> assignProject());
        panel.add(assignButton);
        return panel;
    }

    private JPanel createViewStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        studentTableModel = new DefaultTableModel(new String[]{"学生ID", "姓名", "系别", "服务能力", "服务类别"}, 0);
        studentTable = new JTable(studentTableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadStudentData();
        JButton refreshButton = new JButton("刷新学生信息");
        refreshButton.addActionListener(e -> loadStudentData());
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createViewProjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        projectTableModel = new DefaultTableModel(new String[]{"项目ID", "项目名称", "需求单位", "岗位描述",  "报酬"}, 0);
        projectTable = new JTable(projectTableModel);
        JScrollPane scrollPane = new JScrollPane(projectTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadProjectData();
        JButton refreshButton = new JButton("刷新项目信息");
        refreshButton.addActionListener(e -> loadProjectData());
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private void loadStudentData() {
        studentTableModel.setRowCount(0); // 清除现有数据
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Students")) {
            while (rs.next()) {
                String studentID = rs.getString("StudentID");
                String name = rs.getString("Name");
                String department = rs.getString("Department");
                String serviceAbility = rs.getString("ServiceAbility");
                String serviceCategory = rs.getString("ServiceCategory");
                studentTableModel.addRow(new Object[]{studentID, name, department, serviceAbility, serviceCategory});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProjectData() {
        projectTableModel.setRowCount(0); // 清除现有数据
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Projects")) {
            while (rs.next()) {
                String projectID = rs.getString("ProjectID");
                String projectName = rs.getString("ProjectName");
                String demandUnit = rs.getString("DemandUnit");
                String jobDescription = rs.getString("JobDescription");
                String pay = rs.getString("Pay");
                projectTableModel.addRow(new Object[]{projectID, projectName, demandUnit, jobDescription, pay});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchoolSystemGUI().setVisible(true));
    }
}
