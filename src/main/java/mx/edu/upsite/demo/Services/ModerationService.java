package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModerationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    // Método principal: evalúa tanto texto como imagen al mismo tiempo
    public boolean esContenidoApropiado(String texto, String imageUrl) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey;         String promptDefinitivo = "Actúa como moderador estricto de una red social universitaria. " +
                    "Revisa si el siguiente texto y/o imagen contienen contenido explícito, violencia gráfica o insultos severos. " +
                    "Responde ÚNICAMENTE con la palabra 'true' si es seguro para publicar, o 'false' si debe ser bloqueado. " +
                    "Texto de la publicación: " + (texto != null && !texto.isEmpty() ? texto : "[Sin texto]");

            List<Object> parts = new java.util.ArrayList<>(List.of(
                    Map.of("text", promptDefinitivo)
            ));

            // 2. Si hay imagen, la descargamos y la inyectamos en la petición
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String base64Image = descargarImagenBase64(imageUrl);
                if (!base64Image.isEmpty()) {
                    parts.add(Map.of("inline_data", Map.of(
                            "mime_type", "image/jpeg", // Asumimos jpeg/png, Gemini lo maneja bien
                            "data", base64Image
                    )));
                }
            }

            // 3. Estructura final del JSON para Gemini
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", parts))
            );

            // 4. Hacemos la petición
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestBody, JsonNode.class);

            // 5. Leemos la respuesta (Gemini responde dentro de 'candidates')
            JsonNode candidates = response.getBody().path("candidates");

            if (candidates.isMissingNode() || candidates.isEmpty()) {
                // Si Gemini bloqueó la respuesta por seguridad, es porque el contenido es MALO
                return false;
            }

            String respuesta = candidates.get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim().toLowerCase();

            return respuesta.contains("true");

        } catch (Exception e) {
            System.err.println("Error en moderación con Gemini: " + e.getMessage());
            // Fail-open: si se cae el internet o la API, dejamos pasar para no bloquear la app
            return true;
        }
    }

    // Método auxiliar para que Gemini pueda "ver" la imagen desde una URL
    private String descargarImagenBase64(String url) {
        try {
            byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            System.err.println("No se pudo descargar la imagen para moderarla: " + url);
            return "";
        }
    }
}
