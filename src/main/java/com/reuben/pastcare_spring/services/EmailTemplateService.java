package com.reuben.pastcare_spring.services;

import org.springframework.stereotype.Service;

/**
 * Service for generating email templates
 */
@Service
public class EmailTemplateService {

    /**
     * Generate HTML email template for new user credentials
     *
     * @param userName The name of the new user
     * @param userEmail The email of the new user
     * @param temporaryPassword The temporary password generated for the user
     * @param churchName The name of the church
     * @param loginUrl The URL where users can log in
     * @return HTML string for the email body
     */
    public String generateNewUserCredentialsEmail(
            String userName,
            String userEmail,
            String temporaryPassword,
            String churchName,
            String loginUrl) {

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to PastCare</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 20px auto;
                            background: white;
                            border-radius: 8px;
                            overflow: hidden;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 30px 20px;
                        }
                        .greeting {
                            font-size: 18px;
                            margin-bottom: 20px;
                        }
                        .credentials-box {
                            background: #f8f9fa;
                            border-left: 4px solid #667eea;
                            padding: 20px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .credentials-box h3 {
                            margin-top: 0;
                            color: #667eea;
                            font-size: 16px;
                        }
                        .credential-item {
                            margin: 15px 0;
                        }
                        .credential-label {
                            font-weight: 600;
                            color: #666;
                            display: block;
                            margin-bottom: 5px;
                            font-size: 12px;
                            text-transform: uppercase;
                        }
                        .credential-value {
                            font-family: 'Courier New', monospace;
                            font-size: 16px;
                            color: #333;
                            background: white;
                            padding: 10px;
                            border-radius: 4px;
                            border: 1px solid #ddd;
                            word-break: break-all;
                        }
                        .password-value {
                            color: #d63384;
                            font-weight: 600;
                        }
                        .warning-box {
                            background: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .warning-box strong {
                            color: #856404;
                        }
                        .btn {
                            display: inline-block;
                            padding: 12px 30px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white !important;
                            text-decoration: none;
                            border-radius: 6px;
                            margin: 20px 0;
                            font-weight: 600;
                            text-align: center;
                        }
                        .btn:hover {
                            background: linear-gradient(135deg, #5568d3 0%, #653a8b 100%);
                        }
                        .steps {
                            margin: 20px 0;
                        }
                        .step {
                            margin: 15px 0;
                            padding-left: 30px;
                            position: relative;
                        }
                        .step::before {
                            content: attr(data-step);
                            position: absolute;
                            left: 0;
                            top: 0;
                            background: #667eea;
                            color: white;
                            width: 24px;
                            height: 24px;
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 12px;
                            font-weight: 600;
                        }
                        .footer {
                            background: #f8f9fa;
                            padding: 20px;
                            text-align: center;
                            color: #666;
                            font-size: 14px;
                            border-top: 1px solid #dee2e6;
                        }
                        .footer a {
                            color: #667eea;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to PastCare</h1>
                        </div>

                        <div class="content">
                            <p class="greeting">Hello %s,</p>

                            <p>Your account has been created for <strong>%s</strong> on PastCare - the comprehensive church management system.</p>

                            <div class="credentials-box">
                                <h3>🔐 Your Login Credentials</h3>

                                <div class="credential-item">
                                    <span class="credential-label">Email Address</span>
                                    <div class="credential-value">%s</div>
                                </div>

                                <div class="credential-item">
                                    <span class="credential-label">Temporary Password</span>
                                    <div class="credential-value password-value">%s</div>
                                </div>
                            </div>

                            <div class="warning-box">
                                <strong>⚠️ Security Notice:</strong> This is a temporary password. For your security, you will be required to change it upon your first login.
                            </div>

                            <div class="steps">
                                <h3>Getting Started:</h3>
                                <div class="step" data-step="1">
                                    Click the button below or visit the login page
                                </div>
                                <div class="step" data-step="2">
                                    Enter your email and temporary password
                                </div>
                                <div class="step" data-step="3">
                                    Create a new secure password
                                </div>
                                <div class="step" data-step="4">
                                    Start managing your church effectively!
                                </div>
                            </div>

                            <center>
                                <a href="%s" class="btn">Login to PastCare</a>
                            </center>

                            <p style="margin-top: 30px; font-size: 14px; color: #666;">
                                If you have any questions or need assistance, please contact your church administrator.
                            </p>
                        </div>

                        <div class="footer">
                            <p>
                                <strong>PastCare</strong> - Church Management Made Simple<br>
                                This is an automated email, please do not reply.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, churchName, userEmail, temporaryPassword, loginUrl);
    }

    /**
     * Generate plain text version of new user credentials email (fallback)
     */
    public String generateNewUserCredentialsTextEmail(
            String userName,
            String userEmail,
            String temporaryPassword,
            String churchName,
            String loginUrl) {

        return """
                Welcome to PastCare
                ===================

                Hello %s,

                Your account has been created for %s on PastCare - the comprehensive church management system.

                YOUR LOGIN CREDENTIALS:
                ----------------------
                Email Address: %s
                Temporary Password: %s

                SECURITY NOTICE:
                ⚠️ This is a temporary password. For your security, you will be required to change it upon your first login.

                GETTING STARTED:
                1. Visit: %s
                2. Enter your email and temporary password
                3. Create a new secure password
                4. Start managing your church effectively!

                If you have any questions or need assistance, please contact your church administrator.

                ---
                PastCare - Church Management Made Simple
                This is an automated email, please do not reply.
                """.formatted(userName, churchName, userEmail, temporaryPassword, loginUrl);
    }

    /**
     * Generate HTML email template for password reset by administrator
     */
    public String generatePasswordResetByAdminEmail(
            String userName,
            String userEmail,
            String temporaryPassword,
            String churchName,
            String loginUrl) {

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset - PastCare</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 20px auto;
                            background: white;
                            border-radius: 8px;
                            overflow: hidden;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
                            color: white;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 30px 20px;
                        }
                        .credentials-box {
                            background: #f8f9fa;
                            border-left: 4px solid #dc3545;
                            padding: 20px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .credentials-box h3 {
                            margin-top: 0;
                            color: #dc3545;
                            font-size: 16px;
                        }
                        .credential-item {
                            margin: 15px 0;
                        }
                        .credential-label {
                            font-weight: 600;
                            color: #666;
                            display: block;
                            margin-bottom: 5px;
                            font-size: 12px;
                            text-transform: uppercase;
                        }
                        .credential-value {
                            font-family: 'Courier New', monospace;
                            font-size: 16px;
                            color: #333;
                            background: white;
                            padding: 10px;
                            border-radius: 4px;
                            border: 1px solid #ddd;
                            word-break: break-all;
                        }
                        .password-value {
                            color: #d63384;
                            font-weight: 600;
                        }
                        .warning-box {
                            background: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .btn {
                            display: inline-block;
                            padding: 12px 30px;
                            background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
                            color: white !important;
                            text-decoration: none;
                            border-radius: 6px;
                            margin: 20px 0;
                            font-weight: 600;
                            text-align: center;
                        }
                        .footer {
                            background: #f8f9fa;
                            padding: 20px;
                            text-align: center;
                            color: #666;
                            font-size: 14px;
                            border-top: 1px solid #dee2e6;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Password Reset</h1>
                        </div>

                        <div class="content">
                            <p>Hello %s,</p>

                            <p>Your password for <strong>%s</strong> on PastCare has been reset by an administrator.</p>

                            <div class="credentials-box">
                                <h3>🔐 Your New Temporary Password</h3>

                                <div class="credential-item">
                                    <span class="credential-label">Email Address</span>
                                    <div class="credential-value">%s</div>
                                </div>

                                <div class="credential-item">
                                    <span class="credential-label">Temporary Password</span>
                                    <div class="credential-value password-value">%s</div>
                                </div>
                            </div>

                            <div class="warning-box">
                                <strong>⚠️ Security Notice:</strong> This is a temporary password. For your security, you will be required to change it when you log in.
                            </div>

                            <center>
                                <a href="%s" class="btn">Login to PastCare</a>
                            </center>

                            <p style="margin-top: 30px; font-size: 14px; color: #666;">
                                If you did not request this password reset, please contact your church administrator immediately.
                            </p>
                        </div>

                        <div class="footer">
                            <p>
                                <strong>PastCare</strong> - Church Management Made Simple<br>
                                This is an automated email, please do not reply.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, churchName, userEmail, temporaryPassword, loginUrl);
    }

    /**
     * Generate plain text version of password reset by admin email
     */
    public String generatePasswordResetByAdminTextEmail(
            String userName,
            String userEmail,
            String temporaryPassword,
            String churchName,
            String loginUrl) {

        return """
                Password Reset - PastCare
                ========================

                Hello %s,

                Your password for %s on PastCare has been reset by an administrator.

                YOUR NEW TEMPORARY PASSWORD:
                ---------------------------
                Email Address: %s
                Temporary Password: %s

                SECURITY NOTICE:
                ⚠️ This is a temporary password. For your security, you will be required to change it when you log in.

                Login here: %s

                If you did not request this password reset, please contact your church administrator immediately.

                ---
                PastCare - Church Management Made Simple
                This is an automated email, please do not reply.
                """.formatted(userName, churchName, userEmail, temporaryPassword, loginUrl);
    }

    /**
     * Generate HTML email template for forgot password request
     */
    public String generateForgotPasswordEmail(
            String userName,
            String resetLink,
            int expirationMinutes) {

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reset Your Password - PastCare</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 20px auto;
                            background: white;
                            border-radius: 8px;
                            overflow: hidden;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 30px 20px;
                        }
                        .btn {
                            display: inline-block;
                            padding: 12px 30px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white !important;
                            text-decoration: none;
                            border-radius: 6px;
                            margin: 20px 0;
                            font-weight: 600;
                            text-align: center;
                        }
                        .warning-box {
                            background: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                        }
                        .footer {
                            background: #f8f9fa;
                            padding: 20px;
                            text-align: center;
                            color: #666;
                            font-size: 14px;
                            border-top: 1px solid #dee2e6;
                        }
                        .link-box {
                            background: #f8f9fa;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                            word-break: break-all;
                            font-size: 14px;
                            color: #666;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Reset Your Password</h1>
                        </div>

                        <div class="content">
                            <p>Hello %s,</p>

                            <p>We received a request to reset your password for your PastCare account. Click the button below to create a new password:</p>

                            <center>
                                <a href="%s" class="btn">Reset Password</a>
                            </center>

                            <p style="font-size: 14px; color: #666;">Or copy and paste this link into your browser:</p>
                            <div class="link-box">%s</div>

                            <div class="warning-box">
                                <strong>⏰ This link will expire in %d minutes.</strong><br>
                                If you didn't request this password reset, you can safely ignore this email. Your password will remain unchanged.
                            </div>

                            <p style="font-size: 14px; color: #666;">
                                For security reasons, this password reset link can only be used once.
                            </p>
                        </div>

                        <div class="footer">
                            <p>
                                <strong>PastCare</strong> - Church Management Made Simple<br>
                                This is an automated email, please do not reply.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, resetLink, resetLink, expirationMinutes);
    }

    /**
     * Generate plain text version of forgot password email
     */
    public String generateForgotPasswordTextEmail(
            String userName,
            String resetLink,
            int expirationMinutes) {

        return """
                Reset Your Password - PastCare
                ==============================

                Hello %s,

                We received a request to reset your password for your PastCare account.

                Click this link to create a new password:
                %s

                ⏰ This link will expire in %d minutes.

                If you didn't request this password reset, you can safely ignore this email. Your password will remain unchanged.

                For security reasons, this password reset link can only be used once.

                ---
                PastCare - Church Management Made Simple
                This is an automated email, please do not reply.
                """.formatted(userName, resetLink, expirationMinutes);
    }
}
