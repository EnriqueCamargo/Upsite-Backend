package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModerationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    // Método principal: evalúa tanto texto como imagen al mismo tiempo
    public boolean esContenidoApropiado(String texto, String imageUrl) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey;

                String promptDefinitivo = "Actúa como moderador estricto de una red social universitaria. " +
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
                                "mime_type", "image/jpeg",
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

                // 5. Leemos la respuesta
                JsonNode candidates = response.getBody().path("candidates");

                if (candidates.isMissingNode() || candidates.isEmpty()) {
                    // Si Gemini bloqueó la respuesta por seguridad, es porque el contenido es sospechoso
                    return false;
                }

                String respuesta = candidates.get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText().trim().toLowerCase();

                return respuesta.contains("true");

            } catch (HttpServerErrorException e) {
                if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                    attempt++;
                    System.err.println("Gemini 503 (Servicio no disponible). Intento " + attempt + " de " + MAX_RETRIES);
                    if (attempt >= MAX_RETRIES) {
                        System.err.println("Se agotaron los intentos para moderar con Gemini.");
                        return true; // Fail-open
                    }
                    try { Thread.sleep(RETRY_DELAY_MS * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    System.err.println("Error de servidor en moderación con Gemini (" + e.getStatusCode() + "): " + e.getMessage());
                    return true; // Fail-open
                }
            } catch (Exception e) {
                System.err.println("Error inesperado en moderación con Gemini: " + e.getMessage());
                return true; // Fail-open
            }
        }
        return true;
    }

    // Método auxiliar para descargar la imagen
    private String descargarImagenBase64(String url) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                URI uri = URI.create(url);
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "Upsite-Backend/1.0 (ModerationService)");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
                byte[] imageBytes = response.getBody();

                if (imageBytes != null) {
                    return Base64.getEncoder().encodeToString(imageBytes);
                }
                return "";
            } catch (Exception e) {
                attempt++;
                System.err.println("Error al descargar imagen para moderación (" + attempt + "): " + e.getMessage());
                if (attempt >= MAX_RETRIES) break;
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        System.err.println("No se pudo descargar la imagen tras varios intentos: " + url);
        return "";
    }
}
