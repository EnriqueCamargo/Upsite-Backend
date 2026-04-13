package mx.edu.upsite.demo.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.NotificacionRequestDTO;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void enviarNotificacion(NotificacionRequestDTO dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(dto.destinatarioEmail());
            helper.setSubject(dto.asunto());

            // Eliminamos el botón <a> y el último %s
            String htmlContent = String.format("""
                            <div style='font-family: Arial, sans-serif; border: 1px solid #ddd; padding: 20px;'>
                                <h2 style='color: #2c3e50;'>%s</h2>
                                <p>%s</p>
                                <div style='background: #f9f9f9; padding: 15px; border-radius: 5px;'>
                                    <strong>Tipo:</strong> %s
                                </div>
                                <br>
                                <p style='color: #7f8c8d; font-size: 12px;'>
                                    Ingresa a la plataforma para revisar los detalles de esta notificación.
                                </p>
                            </div>
                            """,
                    dto.tituloCuerpo(),
                    dto.mensajeCuerpo(),
                    dto.tipoNotificacion()
                    // Ya no pasamos dto.urlAccion() porque quitamos el %s
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo institucional: " + e.getMessage());
        }
    }
}