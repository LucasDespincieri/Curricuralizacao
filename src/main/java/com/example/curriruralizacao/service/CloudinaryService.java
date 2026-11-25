package com.example.curriruralizacao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

// @Service: Centraliza a integração com o Cloudinary (serviço externo onde as imagens ficam guardadas).
// O arquivo da imagem NÃO passa pelo backend: o navegador envia direto para o Cloudinary usando uma
// assinatura gerada aqui. Assim o api_secret nunca sai do servidor e o Render (512 MB) não processa uploads.
@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    // Pasta dentro da conta do Cloudinary onde as imagens dos posts ficam organizadas.
    public static final String PASTA_POSTS = "blog-cristao/posts";

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    // As credenciais vêm das variáveis de ambiente (CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET),
    // mapeadas no application.properties. Nunca coloque os valores reais no código ou no repositório.
    public CloudinaryService(@Value("${cloudinary.cloud-name:}") String cloudName,
                             @Value("${cloudinary.api-key:}") String apiKey,
                             @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudName = cloudName.trim();
        this.apiKey = apiKey.trim();
        this.apiSecret = apiSecret.trim();
    }

    // Indica se as três credenciais foram configuradas no ambiente.
    public boolean estaConfigurado() {
        return !cloudName.isEmpty() && !apiKey.isEmpty() && !apiSecret.isEmpty();
    }

    // Gera os dados que o frontend precisa para enviar uma imagem direto ao Cloudinary.
    // Regra do Cloudinary: ordena os parâmetros assinados (folder, timestamp) em ordem alfabética,
    // junta como "chave=valor&chave=valor", concatena o api_secret no final e aplica SHA-1.
    // A assinatura vale por 1 hora e só para esta pasta.
    public Map<String, Object> gerarAssinaturaUpload() {
        long timestamp = Instant.now().getEpochSecond();
        String paraAssinar = "folder=" + PASTA_POSTS + "&timestamp=" + timestamp + apiSecret;

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("cloudName", cloudName);
        dados.put("apiKey", apiKey);
        dados.put("timestamp", timestamp);
        dados.put("folder", PASTA_POSTS);
        dados.put("signature", sha1Hex(paraAssinar));
        return dados;
    }

    // Aceita apenas URLs de imagens da NOSSA conta e da pasta de posts, para ninguém
    // conseguir salvar no banco um link qualquer da internet.
    public boolean urlValida(String url) {
        if (url == null || url.length() > 500 || cloudName.isEmpty()) {
            return false;
        }
        return url.startsWith("https://res.cloudinary.com/" + cloudName + "/image/upload/")
                && url.contains("/" + PASTA_POSTS + "/");
    }

    /**
     * Apaga do Cloudinary a imagem de um post excluído, para não ocupar espaço/créditos do plano.
     * Falhas são só registradas no log: o post já foi excluído do banco e isso não deve dar erro ao usuário.
     */
    public void excluirImagem(String url) {
        if (!estaConfigurado() || !urlValida(url)) return;
        String publicId = extrairPublicId(url);
        if (publicId == null) return;

        long timestamp = Instant.now().getEpochSecond();
        // Mesma regra da assinatura de upload: parâmetros em ordem alfabética + api_secret, com SHA-1.
        String assinatura = sha1Hex("public_id=" + publicId + "&timestamp=" + timestamp + apiSecret);
        String corpo = "public_id=" + URLEncoder.encode(publicId, StandardCharsets.UTF_8)
                + "&timestamp=" + timestamp
                + "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&signature=" + assinatura;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200 || !resp.body().contains("\"ok\"")) {
                log.warn("Cloudinary não apagou a imagem {}: HTTP {} {}", publicId, resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Falha ao apagar a imagem {} no Cloudinary: {}", publicId, e.getMessage());
        }
    }

    // De ".../image/upload/v1795030512/blog-cristao/posts/abc123.png" extrai "blog-cristao/posts/abc123".
    static String extrairPublicId(String url) {
        int i = url.indexOf("/image/upload/");
        if (i < 0) return null;
        String caminho = url.substring(i + "/image/upload/".length());
        caminho = caminho.replaceFirst("^v\\d+/", "");
        int ponto = caminho.lastIndexOf('.');
        if (ponto > caminho.lastIndexOf('/')) caminho = caminho.substring(0, ponto);
        return caminho.isEmpty() ? null : caminho;
    }

    private static String sha1Hex(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 faz parte de toda JVM padrão; se faltar, é um problema do ambiente.
            throw new IllegalStateException("Algoritmo SHA-1 indisponível na JVM", e);
        }
    }
}
