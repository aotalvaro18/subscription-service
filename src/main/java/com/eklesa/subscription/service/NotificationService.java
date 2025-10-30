package com.eklesa.subscription.service;

import com.eklesa.subscription.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Servicio para envío de notificaciones.
 * 
 * RESPONSABILIDADES:
 * - Enviar emails via AWS SES
 * - Templates de emails
 * - Notificaciones de trial
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final SesClient sesClient;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    /**
     * Envía notificación de trial iniciado.
     */
    public void sendTrialStartedEmail(String toEmail, Subscription subscription) {
        log.info("Sending trial started email to: {}", toEmail);
        
        String subject = "¡Bienvenido a Eklesa! Tu prueba ha comenzado";
        String body = buildTrialStartedEmail(subscription);
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Envía recordatorio de trial próximo a expirar.
     */
    public void sendTrialExpiringEmail(String toEmail, Subscription subscription, long daysLeft) {
        log.info("Sending trial expiring email to: {}, days left: {}", toEmail, daysLeft);
        
        String subject = String.format("Tu prueba expira en %d días", daysLeft);
        String body = buildTrialExpiringEmail(subscription, daysLeft);
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Envía notificación de trial expirado.
     */
    public void sendTrialExpiredEmail(String toEmail, Subscription subscription) {
        log.info("Sending trial expired email to: {}", toEmail);
        
        String subject = "Tu período de prueba ha finalizado";
        String body = buildTrialExpiredEmail(subscription);
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Envía confirmación de suscripción activada.
     */
    public void sendSubscriptionActivatedEmail(String toEmail, Subscription subscription) {
        log.info("Sending subscription activated email to: {}", toEmail);
        
        String subject = "¡Tu suscripción está activa!";
        String body = buildSubscriptionActivatedEmail(subscription);
        
        sendEmail(toEmail, subject, body);
    }
    
    /**
     * Envía notificación de pago fallido.
     */
    public void sendPaymentFailedEmail(String toEmail, Subscription subscription) {
        log.error("Sending payment failed email to: {}", toEmail);
        
        String subject = "Problema con tu pago";
        String body = buildPaymentFailedEmail(subscription);
        
        sendEmail(toEmail, subject, body);
    }
    
    // ============================================
    // EMAIL SENDING
    // ============================================
    
    private void sendEmail(String toEmail, String subject, String htmlBody) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                    .toAddresses(toEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                        .html(Content.builder().data(htmlBody).build())
                        .build())
                    .build())
                .build();
            
            sesClient.sendEmail(request);
            
            log.info("Email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
        }
    }
    
    // ============================================
    // EMAIL TEMPLATES
    // ============================================
    
    private String buildTrialStartedEmail(Subscription subscription) {
        return String.format("""
            <html>
            <body>
                <h2>¡Bienvenido a Eklesa!</h2>
                <p>Tu período de prueba de 21 días ha comenzado.</p>
                <p>Durante este tiempo, tendrás acceso completo a todas las funciones del plan Starter.</p>
                <p><strong>Tu prueba expira el:</strong> %s</p>
                <p><a href="%s/pricing" style="background: #0066cc; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Ver Planes</a></p>
                <p>¡Esperamos que disfrutes usando Eklesa!</p>
            </body>
            </html>
            """,
            subscription.getTrialEndDate().toString(),
            frontendUrl
        );
    }
    
    private String buildTrialExpiringEmail(Subscription subscription, long daysLeft) {
        return String.format("""
            <html>
            <body>
                <h2>Tu prueba expira pronto</h2>
                <p>Te quedan <strong>%d días</strong> de tu período de prueba.</p>
                <p>No pierdas acceso a tu cuenta. Selecciona un plan ahora:</p>
                <p><a href="%s/pricing" style="background: #ff6600; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Actualizar Ahora</a></p>
            </body>
            </html>
            """,
            daysLeft,
            frontendUrl
        );
    }
    
    private String buildTrialExpiredEmail(Subscription subscription) {
        return String.format("""
            <html>
            <body>
                <h2>Tu período de prueba ha finalizado</h2>
                <p>Tu prueba de 21 días ha expirado. Tienes 7 días de acceso de solo lectura.</p>
                <p>Para continuar usando Eklesa, selecciona un plan:</p>
                <p><a href="%s/pricing" style="background: #cc0000; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Suscribirse Ahora</a></p>
            </body>
            </html>
            """,
            frontendUrl
        );
    }
    
    private String buildSubscriptionActivatedEmail(Subscription subscription) {
        return String.format("""
            <html>
            <body>
                <h2>¡Tu suscripción está activa!</h2>
                <p>Gracias por suscribirte al plan <strong>%s</strong>.</p>
                <p>Tu pago ha sido procesado exitosamente.</p>
                <p><a href="%s/dashboard" style="background: #00cc66; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Ir al Dashboard</a></p>
            </body>
            </html>
            """,
            subscription.getPlan().getName(),
            frontendUrl
        );
    }
    
    private String buildPaymentFailedEmail(Subscription subscription) {
        return String.format("""
            <html>
            <body>
                <h2>Problema con tu pago</h2>
                <p>No pudimos procesar tu pago automático.</p>
                <p>Por favor, actualiza tu método de pago para evitar la suspensión de tu cuenta.</p>
                <p><a href="%s/account/billing" style="background: #cc0000; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Actualizar Método de Pago</a></p>
            </body>
            </html>
            """,
            frontendUrl
        );
    }
}

