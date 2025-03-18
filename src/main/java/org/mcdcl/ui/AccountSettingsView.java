package org.mcdcl.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.mcdcl.config.UserPreferences;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;

import java.util.List;

public class AccountSettingsView extends VBox {
    private final Label titleLabel;
    private final Label userStatusLabel;
    private final Button loginButton;
    private Stage loginStage;
    
    // 离线模式组件
    private VBox offlineLoginBox;
    private TextField offlineUsernameField;
    private Button offlineLoginButton;
    private String currentUsername;
    
    // 账号列表组件
    private ListView<String> accountListView;
    private ObservableList<String> accountList;
    private Button selectAccountButton;
    private Button deleteAccountButton;

    public AccountSettingsView() {
        // 设置基本布局
        setSpacing(15);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);

        // 创建标题
        titleLabel = new Label("账户设置");
        titleLabel.getStyleClass().add("section-title");

        // 创建用户状态标签
        userStatusLabel = new Label("未登录");
        userStatusLabel.getStyleClass().add("user-info-label");
        
        // 检查是否有保存的用户名
        currentUsername = UserPreferences.getCurrentAccount();
        if (!currentUsername.isEmpty()) {
            userStatusLabel.setText("当前用户: " + currentUsername);
        }

        // 创建登录按钮
        loginButton = new Button("在线登录");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(200);
        
        // 创建离线模式登录区域
        createOfflineLoginArea();
        
        // 创建账号列表区域
        createAccountListArea();

        // 添加组件到布局
        getChildren().addAll(titleLabel, userStatusLabel, loginButton, offlineLoginBox, createAccountListArea());

        // 设置登录按钮点击事件
        loginButton.setOnAction(event -> showLoginDialog());
    }

    private void showLoginDialog() {
        if (loginStage == null) {
            loginStage = new Stage();
            loginStage.setTitle("登录");

            LoginView loginView = new LoginView();
            javafx.scene.Scene scene = new javafx.scene.Scene(loginView);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            // 设置离线模式按钮点击事件
            loginView.getOfflineButton().setOnAction(event -> {
                String username = loginView.getUsernameField().getText();
                if (username != null && !username.isEmpty()) {
                    loginWithOfflineMode(username);
                    loginStage.close();
                } else {
                    showAlert("错误", "请输入用户名");
                }
            });

            loginStage.setScene(scene);
            loginStage.show();
        } else {
            loginStage.show();
        }
    }
    
    /**
     * 创建离线模式登录区域
     */
    private void createOfflineLoginArea() {
        offlineLoginBox = new VBox(10);
        offlineLoginBox.setPadding(new Insets(15, 0, 0, 0));
        offlineLoginBox.setAlignment(Pos.CENTER);
        
        Label offlineTitle = new Label("离线模式登录");
        offlineTitle.getStyleClass().add("section-subtitle");
        
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        
        offlineUsernameField = new TextField();
        offlineUsernameField.setPromptText("输入离线用户名");
        offlineUsernameField.setPrefWidth(200);
        
        offlineLoginButton = new Button("登录");
        offlineLoginButton.getStyleClass().add("login-button");
        
        inputBox.getChildren().addAll(offlineUsernameField, offlineLoginButton);
        
        offlineLoginBox.getChildren().addAll(offlineTitle, inputBox);
        
        // 设置离线登录按钮点击事件
        offlineLoginButton.setOnAction(event -> {
            String username = offlineUsernameField.getText();
            if (username != null && !username.isEmpty()) {
                loginWithOfflineMode(username);
            } else {
                showAlert("错误", "请输入用户名");
            }
        });
    }
    
    /**
     * 使用离线模式登录
     * 
     * @param username 用户名
     */
    private void loginWithOfflineMode(String username) {
        try {
            // 保存用户名到偏好设置
            UserPreferences.saveCredentials(username, "");
            
            // 更新UI
            userStatusLabel.setText("当前用户: " + username);
            currentUsername = username;
            
            // 刷新账号列表
            refreshAccountList();
            
            showAlert("成功", "已使用离线模式登录: " + username);
        } catch (Exception e) {
            showAlert("错误", "登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建账号列表区域
     * 
     * @return 账号列表区域的VBox
     */
    private VBox createAccountListArea() {
        VBox accountListBox = new VBox(10);
        accountListBox.setPadding(new Insets(15, 0, 0, 0));
        accountListBox.setAlignment(Pos.CENTER);
        
        Label accountListTitle = new Label("已保存的账号");
        accountListTitle.getStyleClass().add("section-subtitle");
        
        // 创建账号列表视图
        accountList = FXCollections.observableArrayList();
        accountListView = new ListView<>(accountList);
        accountListView.setPrefHeight(150);
        accountListView.setPrefWidth(300);
        accountListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // 加载保存的账号
        refreshAccountList();
        
        // 创建按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        selectAccountButton = new Button("选择账号");
        selectAccountButton.getStyleClass().add("login-button");
        selectAccountButton.setOnAction(event -> selectAccount());
        
        deleteAccountButton = new Button("删除账号");
        deleteAccountButton.getStyleClass().add("login-button");
        deleteAccountButton.setOnAction(event -> deleteAccount());
        
        buttonBox.getChildren().addAll(selectAccountButton, deleteAccountButton);
        
        accountListBox.getChildren().addAll(accountListTitle, accountListView, buttonBox);
        return accountListBox;
    }
    
    /**
     * 刷新账号列表
     */
    private void refreshAccountList() {
        List<String> accounts = UserPreferences.getSavedAccounts();
        accountList.clear();
        accountList.addAll(accounts);
        
        // 选中当前账号
        String currentAccount = UserPreferences.getCurrentAccount();
        if (!currentAccount.isEmpty() && accountList.contains(currentAccount)) {
            accountListView.getSelectionModel().select(currentAccount);
        }
    }
    
    /**
     * 选择账号
     */
    private void selectAccount() {
        String selectedAccount = accountListView.getSelectionModel().getSelectedItem();
        if (selectedAccount != null && !selectedAccount.isEmpty()) {
            UserPreferences.setCurrentAccount(selectedAccount);
            currentUsername = selectedAccount;
            userStatusLabel.setText("当前用户: " + selectedAccount);
            showAlert("成功", "已选择账号: " + selectedAccount);
        } else {
            showAlert("错误", "请先选择一个账号");
        }
    }
    
    /**
     * 删除账号
     */
    private void deleteAccount() {
        String selectedAccount = accountListView.getSelectionModel().getSelectedItem();
        if (selectedAccount != null && !selectedAccount.isEmpty()) {
            UserPreferences.removeAccount(selectedAccount);
            refreshAccountList();
            
            // 如果删除的是当前账号，更新状态标签
            if (selectedAccount.equals(currentUsername)) {
                currentUsername = UserPreferences.getCurrentAccount();
                if (currentUsername.isEmpty()) {
                    userStatusLabel.setText("未登录");
                } else {
                    userStatusLabel.setText("当前用户: " + currentUsername);
                }
            }
            
            showAlert("成功", "已删除账号: " + selectedAccount);
        } else {
            showAlert("错误", "请先选择一个账号");
        }
    }
    
    /**
     * 显示提示对话框
     * 
     * @param title 标题
     * @param message 消息内容
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}