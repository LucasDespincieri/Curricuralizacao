import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

/** Dados devolvidos pelo backend em POST /api/uploads/assinatura. */
interface AssinaturaUpload {
  cloudName: string;
  apiKey: string;
  timestamp: number;
  folder: string;
  signature: string;
}

export const TIPOS_IMAGEM_ACEITOS = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
export const TAMANHO_MAXIMO_IMAGEM = 5 * 1024 * 1024; // 5 MB

/**
 * Envia imagens direto do navegador para o Cloudinary.
 *
 * O arquivo não passa pelo backend: o backend só gera uma assinatura (o api_secret fica lá),
 * e o navegador manda o arquivo para o Cloudinary, que devolve a URL pública da imagem.
 */
@Injectable({ providedIn: 'root' })
export class UploadService {
  private readonly http = inject(HttpClient);

  /** Retorna uma mensagem de erro se o arquivo não puder ser enviado, ou null se estiver ok. */
  validarImagem(arquivo: File): string | null {
    if (!TIPOS_IMAGEM_ACEITOS.includes(arquivo.type)) {
      return 'Formato não suportado. Use JPG, PNG, WEBP ou GIF.';
    }
    if (arquivo.size > TAMANHO_MAXIMO_IMAGEM) {
      return 'A imagem deve ter no máximo 5 MB.';
    }
    return null;
  }

  /** Envia a imagem e devolve a URL pública (https) gerada pelo Cloudinary. */
  async enviarImagemPost(arquivo: File): Promise<string> {
    const assinatura = await firstValueFrom(
      this.http.post<AssinaturaUpload>('/api/uploads/assinatura', {}, { withCredentials: true }),
    );

    const form = new FormData();
    form.append('file', arquivo);
    form.append('api_key', assinatura.apiKey);
    form.append('timestamp', String(assinatura.timestamp));
    form.append('folder', assinatura.folder);
    form.append('signature', assinatura.signature);

    const resposta = await fetch(
      `https://api.cloudinary.com/v1_1/${assinatura.cloudName}/image/upload`,
      { method: 'POST', body: form },
    );
    const dados = await resposta.json();
    if (!resposta.ok || typeof dados?.secure_url !== 'string') {
      throw new Error(dados?.error?.message ?? 'Falha ao enviar a imagem.');
    }
    return dados.secure_url as string;
  }
}

/**
 * Pede ao Cloudinary uma versão otimizada da imagem (formato e qualidade automáticos,
 * largura máxima de 1000px). Economiza banda e créditos do plano gratuito.
 */
export function urlImagemOtimizada(url: string | null | undefined): string | null {
  if (!url) return null;
  return url.replace('/image/upload/', '/image/upload/f_auto,q_auto,c_limit,w_1000/');
}
