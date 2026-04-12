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

    @Async // Crucial para que el usuario no espere a que se envíe el mail
    public void enviarNotificacion(NotificacionRequestDTO dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(dto.destinatarioEmail());
            helper.setSubject(dto.asunto());

            // Construcción del HTML para que se vea profesional en Mazatlán
            String htmlContent = String.format("""
                <div style='font-family: Arial, sans-serif; border: 1px solid #ddd; padding: 20px;'>
                    <h2 style='color: #2c3e50;'>%s</h2>
                    <p>%s</p>
                    <div style='background: #f9f9f9; padding: 15px; border-radius: 5px;'>
                        <strong>Tipo:</strong> %s
                    </div>
                    <br>
                    <a href='%s' style='background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>
                        Ver en UPSITE
                    </a>
                </div>
                """,
                    dto.tituloCuerpo(),
                    dto.mensajeCuerpo(),
                    dto.tipoNotificacion(),
                    dto.urlAccion()
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo institucional: " + e.getMessage());
        }
    }

}
