package com.example.curriruralizacao.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
