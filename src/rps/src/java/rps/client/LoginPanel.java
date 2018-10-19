/*
 * LoginPanel.java
 *
 */

package rps.client;

import rps.common.*;
import java.awt.*;

/**
 *
 * @author  dhilder
 */
public class LoginPanel extends javax.swing.JPanel {
    
    RPSClient rpsClient;
    
    /** Creates new form LoginPanel */
    public LoginPanel(RPSClient rpsClient) {
        this.rpsClient = rpsClient;
        initComponents();
        errorMessageLabel.setVisible(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        userIdTF = new javax.swing.JTextField();
        passwordTF = new javax.swing.JPasswordField();
        loginButton = new javax.swing.JButton();
        registerButton = new javax.swing.JButton();
        leaderBoardButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        errorMessageLabel = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(450, 500));
        setPreferredSize(new java.awt.Dimension(450, 500));
        jLabel1.setText("user id:");

        jLabel2.setText("password:");

        userIdTF.setColumns(10);

        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        registerButton.setText("Register");
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerButtonActionPerformed(evt);
            }
        });

        leaderBoardButton.setText("Leader Board");
        leaderBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaderBoardButtonActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Palatino", 3, 36));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Rock, Paper, Scissors");

        errorMessageLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        errorMessageLabel.setForeground(new java.awt.Color(255, 51, 51));
        errorMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorMessageLabel.setText("error message");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(104, 104, 104)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(passwordTF)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, userIdTF))
                .addContainerGap(144, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(errorMessageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(131, 131, 131)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(leaderBoardButton))
                    .add(layout.createSequentialGroup()
                        .add(loginButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(registerButton)))
                .add(159, 159, 159))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .add(72, 72, 72)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userIdTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(passwordTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(26, 26, 26)
                .add(errorMessageLabel)
                .add(25, 25, 25)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(loginButton)
                    .add(registerButton))
                .add(15, 15, 15)
                .add(leaderBoardButton)
                .add(183, 183, 183))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void leaderBoardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaderBoardButtonActionPerformed
        errorMessageLabel.setVisible(false);
        rpsClient.pushPanel("LoginPanel");
        ((CardLayout)getParent().getLayout()).show(getParent(), "LeaderBoardPanel");
    }//GEN-LAST:event_leaderBoardButtonActionPerformed

    private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerButtonActionPerformed
        errorMessageLabel.setVisible(false);
        ((CardLayout)getParent().getLayout()).show(getParent(), "RegistrationPanel");
    }//GEN-LAST:event_registerButtonActionPerformed

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed

        String userId = (String)userIdTF.getText();
        String password = new String(passwordTF.getPassword());
        
        try {
            LoginResponse loginResponse = (LoginResponse)rpsClient.sendMessage(new LoginRequest(userId, password));
            if (loginResponse.isAuthenticated()) {
                errorMessageLabel.setVisible(false);
                rpsClient.setUserId(userId);
                ((CardLayout)getParent().getLayout()).show(getParent(), "JoinPanel");
            }
            else {
                errorMessageLabel.setText("Login failed. Invalid userid or password.");
                errorMessageLabel.setVisible(true);
            }
        }
        catch (Exception e) {
            // TO DO: display error (JOptionPane? in an applet?)
            e.printStackTrace();
        }
    }//GEN-LAST:event_loginButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorMessageLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton leaderBoardButton;
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordTF;
    private javax.swing.JButton registerButton;
    private javax.swing.JTextField userIdTF;
    // End of variables declaration//GEN-END:variables
    
}
