package com.dustray.tools;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Dustray on 2017/4/4 0004.
 */

public class SendEmail {


    // 邮件发送协议
    private String PROTOCOL = "smtp";
    // SMTP邮件服务器
    private String HOST = "smtp.163.com";
    // SMTP邮件服务器默认端口
    private String PORT = "25";
    // 是否要求身份认证
    private String IS_AUTH = "true";
    // 是否启用调试模式（启用调试模式可打印客户端与服务器交互过程时一问一答的响应消息）
    private String IS_ENABLED_DEBUG_MOD = "true";
    // 发件人
    private String from = "getuwp@163.com";
    // 收件人
    private String to = "1360741742@qq.com";
    // 初始化连接邮件服务器的会话信息
    private Properties props = null;

    public SendEmail(String toEmail ){
        to = toEmail;
        props = new Properties();
        props.setProperty("mail.transport.protocol", PROTOCOL);
        props.setProperty("mail.smtp.host", HOST);
        props.setProperty("mail.smtp.port", PORT);
        props.setProperty("mail.smtp.auth", IS_AUTH);
        props.setProperty("mail.debug", IS_ENABLED_DEBUG_MOD);
    }

    /**
     * 发送简单的文本验证码邮件，Num为验证码
     * @param Num
     * @throws Exception
     */
    public void sendTextEmail(long Num) throws Exception {
        // 创建Session实例对象
        Session session = Session.getDefaultInstance(props);

        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置发件人
        message.setFrom(new InternetAddress(from));
        // 设置邮件主题
        message.setSubject("表弃");
        // 设置收件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        // 设置发送时间
        message.setSentDate(new Date());
        // 设置纯文本内容为邮件正文
        message.setText("使用撒范德萨发给!");
        // 保存并生成最终的邮件内容
        message.saveChanges();

        // 获得Transport实例对象
        Transport transport = session.getTransport();
        // 打开连接
        transport.connect("getuwp", "yxy199558yxy");
        // 将message对象传递给transport对象，将邮件发送出去
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接
        transport.close();
    }

    /**
     * 发送简单的html验证码邮件，Num为验证码
     * @param Num
     * @throws Exception
     */
    public void sendHtmlEmail(long Num) throws Exception {
        // 创建Session实例对象
        Session session = Session.getInstance(props, new MyAuthenticator());

        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置邮件主题
        message.setSubject("绿色守护浏览器的注册验证码");
        // 设置发送人
        message.setFrom(new InternetAddress(from));
        // 设置发送时间
        message.setSentDate(new Date());
        // 设置收件人
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        // 设置html内容为邮件正文，指定MIME类型为text/html类型，并指定字符编码为gbk
        message.setContent("<span style='color:blue;'>您本次的验证码为：</span>"+
                        "<span style='color:red;'>"+Num+"</span>"+
                        "<br><span style='color:blue;'>请勿向任何人透漏验证码</span>",
                "text/html;charset=gbk");
        // 保存并生成最终的邮件内容
        message.saveChanges();
        // 发送邮件
        Transport.send(message);
    }


    /**
     * 向邮件服务器提交认证信息
     */
    static class MyAuthenticator extends Authenticator {

        private String username = "getuwp";

        private String password = "yxy199558yxy";

        public MyAuthenticator() {
            super();
        }

        public MyAuthenticator(String username, String password) {
            super();
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {

            return new PasswordAuthentication(username, password);
        }
    }

}
